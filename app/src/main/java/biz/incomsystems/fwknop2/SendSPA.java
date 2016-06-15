/*
This file is part of Fwknop2.

    Fwknop2 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package biz.incomsystems.fwknop2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sonelli.juicessh.pluginlibrary.PluginClient;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnClientStartedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionFinishedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionStartedListener;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendSPA implements OnSessionStartedListener, OnSessionFinishedListener {
    DBHelper mydb;
    public static Config config;
    Activity ourAct;
    ProgressDialog pdLoading;
    Boolean ready;
    public PluginClient client;
    public boolean isConnected = false;
    public boolean reKnock;
    private SharedPreferences prefs;

    public native String sendSPAPacket();

    //These are the configs to pass to the native code
    public String access_str;
    public String allowip_str;
    public String passwd_str;
    public String passwd_b64;
    public String hmac_str;
    public String hmac_b64;
    public String fw_timeout_str;
    public String nat_ip_str;
    public String nat_port_str;
    public String nat_access_str;
    public String nat_local;
    public String server_cmd_str;
    public String legacy;
    public String digest_type;
    public String hmac_type;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2585){ //This request code is specifically for juicessh
            client.gotActivityResult(requestCode, resultCode, data);
        } else { // This will match a qr code captured IP
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if ((scanResult != null) && (scanResult.getContents() != null)) {
                String contents = scanResult.getContents();
                Log.v("fwknop2", contents);
                for (String stanzas: contents.split(" ")){
                    String[] tmp = stanzas.split(":");
                    if (tmp[0].equalsIgnoreCase("CLIENT_IP")) {
                        allowip_str = (tmp[1]);
                    }
                }// end for loop
                final getExternalIP task = new getExternalIP(ourAct);
                task.execute();
            }
        }
    }

    @Override
    public void onSessionStarted(int i, String s) {
        SendSPA.this.isConnected = true;
        try {
            client.attach(i,s);
        } catch (ServiceNotConnectedException ex){
            Log.e("fwknop2", "Error attaching");
        }
    }

    @Override
    public void onSessionCancelled() {
    }

    @Override
    public void onSessionFinished() {
    }
    public String resend() {
        final getExternalIP task = new getExternalIP(ourAct);
        task.execute();
        return "Success";
    }
    public int send(String nick, final Activity newAct) {
        ourAct = newAct;
        prefs = ourAct.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        final InetAddressValidator ipValidate = new InetAddressValidator();
        final IntentIntegrator getQR = new IntentIntegrator(ourAct);
        mydb = new DBHelper(ourAct);
        config = new Config();
        config = mydb.getConfig(nick);
        mydb.close();

        //These variables are the ones that the jni pulls settings from.
        access_str = config.PORTS;
        passwd_str = config.KEY;
        hmac_str = config.HMAC;
        fw_timeout_str = config.SERVER_TIMEOUT;
        allowip_str = config.ACCESS_IP;
        nat_ip_str = config.NAT_IP;
        nat_port_str = config.NAT_PORT;
        server_cmd_str = "";
        nat_access_str = "";
        if (!nat_ip_str.equalsIgnoreCase("")) {
            nat_access_str = nat_ip_str + "," + nat_port_str;
        }
        digest_type = config.DIGEST_TYPE;
        hmac_type = config.HMAC_TYPE;
        if (config.KEY_BASE64) {
            passwd_b64 = "true";
        } else {
            passwd_b64 = "false";
        }
        if (config.HMAC_BASE64) {
            hmac_b64 = "true";
        } else {
            hmac_b64 = "false";
        }
        if (config.LEGACY) {
            legacy = "true";
        } else {
            legacy = "false";
        }
        if (config.SERVER_PORT.equalsIgnoreCase("random")) {
            Random r = new Random();
            int random_port = r.nextInt(65535 - 10000) + 10000;
            config.SERVER_PORT = String.valueOf(random_port);
        }
        final AlertDialog.Builder IPPrompt = new AlertDialog.Builder(ourAct);
        IPPrompt.setTitle("Source IP");
        final EditText input = new EditText(ourAct);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        IPPrompt.setView(input);
        IPPrompt.setPositiveButton(ourAct.getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                allowip_str = input.getText().toString();
                if (ipValidate.isValid(allowip_str)) {
                    final getExternalIP task = new getExternalIP(ourAct);
                    task.execute();
                } else {
                    Toast.makeText(ourAct, "invalid ip", Toast.LENGTH_LONG).show();
                }
            }
        });
        IPPrompt.setNegativeButton(ourAct.getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                ourAct.finish();
            }
        });
        IPPrompt.setNeutralButton("Scan QR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Scan a qr code.
                try {
                    getQR.initiateScan();
                    //startActivityForResult(intent, 0);
                } catch (Exception e) {
                    Log.e("fwknop2", "Could not capture QR: " + e);
                }
            }
        });


        if (passwd_str.equalsIgnoreCase("")) { //here is where we prompt for a key
            //sadly, Android is retarded, and there is no way to wait for the user to finish with the dialog.
            AlertDialog.Builder PassPrompt = new AlertDialog.Builder(ourAct);
            PassPrompt.setTitle(ourAct.getResources().getText(R.string.Rijndael_Key));
            final EditText inputPass = new EditText(ourAct);
            inputPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            PassPrompt.setView(inputPass);
            PassPrompt.setPositiveButton(ourAct.getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    passwd_str = inputPass.getText().toString();
                    if (!(passwd_str.equalsIgnoreCase(""))) {
                        if (allowip_str.equalsIgnoreCase("Prompt IP")) {
                            IPPrompt.show();
                        } else {
                            final getExternalIP task = new getExternalIP(ourAct);
                            task.execute();
                        }
                    } else {
                        Toast.makeText(ourAct, ourAct.getResources().getText(R.string.blank_key), Toast.LENGTH_LONG).show();
                    }
                }
            });
            PassPrompt.setNegativeButton(ourAct.getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            PassPrompt.show();
        } else if (allowip_str.equalsIgnoreCase("Prompt IP")) {
            IPPrompt.show();
        } else {
            final getExternalIP task = new getExternalIP(ourAct);
            task.execute();
        }
        return 0;
    }


    private class getExternalIP extends AsyncTask<Void, Void, String>
    {
        private Activity mActivity;
        private String spaPacket;
        public getExternalIP (Activity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!reKnock) {
                if (!mActivity.getLocalClassName().equals("biz.incomsystems.fwknop2.NfcKnockActivity")) {
                    pdLoading = new ProgressDialog(mActivity);
                    pdLoading.setMessage("\t" + mActivity.getResources().getText(R.string.sending));
                    pdLoading.show();
                }
            }
        }

        @Override
        public String doInBackground(Void... params) {
            InetAddressValidator ipValidate = new InetAddressValidator();

            try {
                System.loadLibrary("fwknop");
            } catch (Exception ex) {
                Log.e("fwknop2", "Could not load libfko: " + ex);
                return mActivity.getString(R.string.libLoadError);
            }
            if (allowip_str.equalsIgnoreCase("Source IP")) {
                allowip_str = "0.0.0.0";
            } else if (allowip_str.equalsIgnoreCase("Resolve IP")) {



                //SharedPreferences prefs = mActivity.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                if (prefs.getBoolean("EnableDns", true)) {
                    try {
                        Resolver resolver = new SimpleResolver("208.67.222.222");
                        Lookup lookup = new Lookup("myip.opendns.com", Type.A);
                        lookup.setResolver(resolver);
                        Record[] records = lookup.run();
                        allowip_str = ((ARecord) records[0]).getAddress().toString();
                        Log.v("fwknop2", "Your external IP address is " + allowip_str);
                        if (allowip_str.contains("/")) {
                            allowip_str = allowip_str.split("/")[1];
                        }
                    } catch (Exception ex) {
                        Log.e("fwknop2", "dns error " + ex);
                    }
                }

                try {
                    if (!(ipValidate.isValid(allowip_str))) {
                        Pattern p = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
                        URL url = new URL(prefs.getString("ipSource", "https://api.ipify.org"));
                        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(url.openStream()));
                        String result;
                        while ((result = bufferReader.readLine()) != null) {
                            Log.v("fwknop2", result);
                            Matcher m = p.matcher(result);
                            if (m.find()) {
                                allowip_str = m.group();
                                Log.v("fwknop2", "Your external IP address is " + allowip_str);
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.e("fwknop2", "error " + ex);
                }

            }
            if (!config.SERVER_CMD.equalsIgnoreCase("")) {
                server_cmd_str = allowip_str + "," + config.SERVER_CMD;
            }
            if (!(ipValidate.isValid(allowip_str))) {
                Log.e("fwknop2", "Invalid Source IP");
                return mActivity.getString(R.string.error_resolve);
            }
            InetAddress resolved_IP;  //we need to resolve the server's IP, particularly for the nat-local case.
            try {
                resolved_IP = InetAddress.getByName(config.SERVER_IP);
            } catch (Exception ex) {
                return ex.toString();
            }
            if (config.NAT_IP.equalsIgnoreCase("127.0.0.1")) { //if Nat-local
                nat_access_str = resolved_IP.getHostAddress() + "," +config.NAT_PORT; //The nat-local address is the public ip
                nat_local = "true"; // let the jni function know that we are doing nat-local
            } else if (!config.NAT_IP.equalsIgnoreCase("")) {
                nat_local = "false";
                nat_access_str = config.NAT_IP + "," + config.NAT_PORT;
                Log.d("fwknop2", nat_access_str);
            } else {
                nat_local = "false";
            }

            spaPacket = sendSPAPacket();
            if (spaPacket != null) {
                //InetAddress resolved_IP;
                try {
                    //resolved_IP = InetAddress.getByName(config.SERVER_IP);
                    if (config.PROTOCOL.equalsIgnoreCase("udp")) {
                        byte[] spaBytes = spaPacket.getBytes();
                        DatagramPacket p = new DatagramPacket(spaBytes, spaBytes.length, resolved_IP, Integer.parseInt(config.SERVER_PORT));
                        DatagramSocket s = new DatagramSocket();
                        s.send(p);
                        s.close();
                    } else if (config.PROTOCOL.equalsIgnoreCase("tcp")) {
                        Socket s = new Socket(resolved_IP, Integer.parseInt(config.SERVER_PORT));
                        OutputStream out = s.getOutputStream();
                        PrintWriter output = new PrintWriter(out);
                        output.print(spaPacket);
                        output.flush();
                        output.close();
                        out.flush();
                        out.close();
                        s.close();
                    } else if (config.PROTOCOL.equalsIgnoreCase("http")) {
                        spaPacket = spaPacket.replace("+", "-");
                        spaPacket = spaPacket.replace("/", "_");
                        URL packet = new URL("http://" + config.SERVER_IP + "/" + spaPacket);
                        HttpURLConnection conn = (HttpURLConnection)packet.openConnection();
                        conn.setRequestMethod("GET");
                        conn.connect();
                        conn.getResponseCode();
                        conn.disconnect();
                    }
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    return ex.toString();
                }

                return "Success";
            } else return "Failure generating SPA data"; //mActivity.getResources().getText(R.string.spa_failure).toString();//
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (reKnock)
                return;
            if(!mActivity.getLocalClassName().equals("biz.incomsystems.fwknop2.NfcKnockActivity"))
                pdLoading.dismiss();

            Toast.makeText(mActivity, result, Toast.LENGTH_LONG).show();
            Log.v("fwknop2", result);
            if (result.contains("Success")) {
                if (config.SSH_CMD.contains("juice:")) {
                    Log.i("Fwknop2", "Attempting to launch Juicessh");
                    ready = false; //probably not needed
                    client = new PluginClient();
                    client.start(mActivity, new OnClientStartedListener() {
                        @Override
                        public void onClientStarted() {
                            SendSPA.this.isConnected = true;
                            try {
                                client.connect(mActivity, config.juice_uuid, SendSPA.this, 2585);
                            } catch (ServiceNotConnectedException ex) {
                                Log.e("fwknop2", "not connected error");
                            }
                        }
                        @Override
                        public void onClientStopped() {
                            Log.v("fwknop2", "client stopped");
                        }
                    });

                } else if (config.SSH_CMD.contains("ovpn:")) {
                    final String EXTRA_NAME = "de.blinkt.openvpn.shortcutProfileName";
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setClassName("de.blinkt.openvpn", "de.blinkt.openvpn.LaunchVPN");
                    i.putExtra(EXTRA_NAME, config.SSH_CMD.substring(5));


                    try {
                        mActivity.startActivity(i);
                    } catch (Exception ex) {
                        AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
                        alertDialog.setTitle("Error");
                        if (ex.toString().contains("ActivityNotFoundException")) {
                            alertDialog.setMessage("No OVPN app found.  OpenVPN for Android is recommended.");
                        } else {
                            alertDialog.setMessage(ex.toString());
                        }
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                } else if (!config.SSH_CMD.equalsIgnoreCase("")) {
                    String ssh_uri = "ssh://" + config.SSH_CMD + "/#" + config.NICK_NAME;
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(ssh_uri));
                    try {
                        mActivity.startActivity(i);
                    } catch (Exception ex) {
                        AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
                        alertDialog.setTitle("Error");
                        if (ex.toString().contains("ActivityNotFoundException")) {
                            alertDialog.setMessage("No SSH app found.  Juicessh or Connectbot are both recommended.");
                        } else {
                            alertDialog.setMessage(ex.toString());
                        }
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                }
                    if ((prefs.getBoolean("EnableNotification", true)) && config.SERVER_CMD.equalsIgnoreCase("") && Build.VERSION.SDK_INT > 15) {
                        Intent newtimer = new Intent(mActivity, countdownTimer.class); //countdownTimer ourtimer = new countdownTimer();
                        newtimer.putExtra("timeout", config.SERVER_TIMEOUT);
                        newtimer.putExtra("nickname", config.NICK_NAME);
                        if (config.KEEP_OPEN) {
                            newtimer.putExtra("keep_open", true);

                            newtimer.putExtra("access_str", access_str);
                            newtimer.putExtra("allowip_str", allowip_str);
                            newtimer.putExtra("passwd_str", passwd_str);
                            newtimer.putExtra("passwd_b64", passwd_b64);
                            newtimer.putExtra("hmac_str", hmac_str);
                            newtimer.putExtra("hmac_b64", hmac_b64);
                            newtimer.putExtra("fw_timeout_str", fw_timeout_str);
                            newtimer.putExtra("nat_ip_str", nat_ip_str);
                            newtimer.putExtra("nat_port_str", nat_port_str);
                            newtimer.putExtra("nat_access_str", nat_access_str);
                            newtimer.putExtra("nat_local", nat_local);
                            newtimer.putExtra("server_cmd_str", server_cmd_str);
                            newtimer.putExtra("legacy", legacy);
                            newtimer.putExtra("digest_type", digest_type);
                            newtimer.putExtra("hmac_type", hmac_type);
                        }


                        mActivity.startService(newtimer);
                    }

            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(result);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            if (config.SSH_CMD.equalsIgnoreCase("") && (ourAct.getLocalClassName().contains("NfcKnockActivity"))) {
                ourAct.finish();
            }
        }
    }
}
