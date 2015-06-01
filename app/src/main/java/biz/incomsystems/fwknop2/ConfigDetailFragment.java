package biz.incomsystems.fwknop2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Intent;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.DomainValidator;

import java.io.UnsupportedEncodingException;

/**
 * A fragment representing a single Config detail screen.
 * This fragment is either contained in a {@link ConfigListActivity}
 * in two-pane mode (on tablets) or a {@link ConfigDetailActivity}
 * on handsets.
 */
public class ConfigDetailFragment extends Fragment {
    DBHelper mydb ;
    private String output;
    public native String sendSPAPacket();
    TextView txt_NickName ;  // objects representing the config options
    Spinner spn_allowip ;
    TextView txt_allowIP ;
    LinearLayout lay_allowIP;
    TextView txt_tcp_ports ;
    TextView txt_udp_ports ;
    TextView txt_server_ip ;
    TextView txt_server_port ;
    TextView txt_server_time ;
    TextView txt_KEY ;
    CheckBox chkb64key ;
    TextView txt_HMAC ;
    CheckBox chkb64hmac ;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * active_Nick is our config index.
     */
    private String active_Nick;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConfigDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mydb = new DBHelper(getActivity()); // grabbing a database instance
        active_Nick = getArguments().getString(ARG_ITEM_ID); // This populates active_Nick with index of the selected config
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mainmenu, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.qr_code) {
            try {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
                startActivityForResult(intent, 0);
            } catch (Exception e) { // This is where the play store is called if the app is not installed
                Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                startActivity(marketIntent);
            }
        }
        if (id == R.id.save) {
            Boolean b64_key_error = false;
            Boolean b64_hmac_error = false;
            InetAddressValidator ipValidate = new InetAddressValidator();
            Context context = getActivity(); // We know we will use a toast, so set it up now
            int duration = Toast.LENGTH_LONG;
            CharSequence text = "Saving config";
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout toastLayout = (LinearLayout) toast.getView();
            TextView toastTV = (TextView) toastLayout.getChildAt(0);
            toastTV.setTextSize(30);

            //The following is all input validation

            if (txt_NickName.getText().toString().equalsIgnoreCase("")) { // Need to create a new Nick
                toast.setText("You Must choose a unique Nickname.");
                toast.show();
            } else if (spn_allowip.getSelectedItem().toString().equalsIgnoreCase("Allow IP") && (!ipValidate.isValid(txt_allowIP.getText().toString()))){ //Have to have a valid ip to allow, if using allow ip
                toast.setText("You Must supply a valid IP address to 'Allow IP'.");
                toast.show();
            } else if (txt_tcp_ports.getText().toString().equalsIgnoreCase("") && txt_udp_ports.getText().toString().equalsIgnoreCase("")){ // check for valid number? Can we open multiple ports at once?
                toast.setText("You Must supply either a TCP or UDP port to open.");
                toast.show();
            } else if (!ipValidate.isValid(txt_server_ip.getText().toString()) && !DomainValidator.getInstance().isValid(txt_server_ip.getText().toString())){ // check server entry. Must be a valid url or ip.
                toast.setText("You Must supply a valid server address.");
                toast.show();
            } else if (false){ // server udp port must make sense
            } else if (false){ // firewall timeout must make sense
            } else if (txt_KEY.getText().toString().equalsIgnoreCase("")){ //must have a key
                toast.setText("You Must supply a Rijndael Key.");
                toast.show();
            } else if (b64_key_error){ //if key is base64, check if is b64 compat
                toast.setText("Not a valid Base 64 key");
                toast.show();
            } else if (b64_hmac_error){ //if key is base64, check if is b64 compat
                toast.setText("Not a valid Base 64 hmac");
                toast.show();
            //end input validation, actual saving below
            } else {
                toast.show();
                String tmp_access;
                if (spn_allowip.getSelectedItem().toString().equalsIgnoreCase("Resolve IP") || spn_allowip.getSelectedItem().toString().equalsIgnoreCase("Source IP")) {
                    tmp_access = spn_allowip.getSelectedItem().toString();
                } else {
                    tmp_access = txt_allowIP.getText().toString();
                }
                mydb.updateConfig(
                        txt_NickName.getText().toString(),  //nickname
                        tmp_access,                        //overload this. possibilites are Resolve IP, Source IP, or the ip address
                        txt_tcp_ports.getText().toString(),
                        txt_udp_ports.getText().toString(),
                        txt_server_ip.getText().toString(),
                        txt_server_port.getText().toString(),
                        txt_server_time.getText().toString(),
                        txt_KEY.getText().toString(),       //key
                        chkb64key.isChecked(),                      //is key b64
                        txt_HMAC.getText().toString(), // hmac key
                        chkb64hmac.isChecked()                     //is hmac base64
                );
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       // super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == Activity.RESULT_OK) {

                String contents = data.getStringExtra("SCAN_RESULT");
                for (String stanzas: contents.split(" ")){
                    String[] tmp = stanzas.split(":");
                    if (tmp[0].equalsIgnoreCase("KEY_BASE64")) {

                        //case "KEY_BASE64":

                        txt_KEY.setText(tmp[1]);
                        chkb64key.setChecked(true);
                        //  break;

                    } else  if (tmp[0].equalsIgnoreCase("KEY")) {
                        txt_KEY.setText(tmp[1]);
                        chkb64key.setChecked(false);
                        //      break;

                    } else if (tmp[0].equalsIgnoreCase("HMAC_KEY_BASE64")) {
                        txt_HMAC.setText(tmp[1]);
                        chkb64hmac.setChecked(true);
                        //      break;

                    } else if (tmp[0].equalsIgnoreCase( "HMAC_KEY")) {
                            txt_HMAC.setText(tmp[1]);
                            chkb64hmac.setChecked(false);
                     //       break;

                    } // end switch
                }// end for loop
            }
            if(resultCode == Activity.RESULT_CANCELED){
                //handle cancel
                Context context = getActivity();
                CharSequence text = " QR Code Canceled";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(30);
                toast.show();
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_config_detail, container, false);
        active_Nick = getArguments().getString("item_id");

        //fields from the config detail
        txt_NickName = (TextView) rootView.findViewById(R.id.NickName);
        //spinner below
        txt_allowIP = (TextView) rootView.findViewById(R.id.iptoallow);
        lay_allowIP = (LinearLayout) rootView.findViewById(R.id.iptoallowsl);
        txt_tcp_ports = (TextView) rootView.findViewById(R.id.tcpAccessPorts);
        txt_udp_ports = (TextView) rootView.findViewById(R.id.udpAccessPorts);
        txt_server_ip = (TextView) rootView.findViewById(R.id.destIP);
        txt_server_port = (TextView) rootView.findViewById(R.id.destPort);
        txt_server_time = (TextView) rootView.findViewById(R.id.fwTimeout);

        txt_KEY = (TextView) rootView.findViewById(R.id.passwd);
        txt_HMAC = (TextView) rootView.findViewById(R.id.hmac);

        chkb64hmac = (CheckBox) rootView.findViewById(R.id.chkb64hmac);
        chkb64key = (CheckBox) rootView.findViewById(R.id.chkb64key);

        spn_allowip = (Spinner) rootView.findViewById(R.id.allowip);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_allowip.setAdapter(adapter);
        spn_allowip.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)

                if (parent.getItemAtPosition(pos).toString().equalsIgnoreCase("Allow IP")) {
                    lay_allowIP.setVisibility(View.VISIBLE);
                } else {
                    lay_allowIP.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        if (active_Nick.equalsIgnoreCase("New Config")) {
            txt_NickName.setText("");
        } else {
            Cursor CurrentIndex = mydb.getData(active_Nick);
            CurrentIndex.moveToFirst();

            txt_NickName.setText(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_NICK_NAME)));
            String tmp_access = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_ACCESS_IP));
            if (tmp_access.equalsIgnoreCase( "Resolve IP")) {
                spn_allowip.setSelection(0);
            } else if (tmp_access.equalsIgnoreCase("Source IP")) {
                spn_allowip.setSelection(1);
            } else {
                spn_allowip.setSelection(2);
                txt_allowIP.setText(tmp_access);
            }
            txt_tcp_ports.setText(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_TCP_PORTS)));
            txt_udp_ports.setText(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_UDP_PORTS)));
            txt_server_ip.setText(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_IP)));
            txt_server_port.setText(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_PORT)));
            txt_server_time.setText(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_TIMEOUT)));
            txt_KEY.setText(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_KEY)));
            if (CurrentIndex.getInt(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_KEY_BASE64)) == 1) {
                chkb64key.setChecked(true);
            } else { chkb64key.setChecked(false);}
            txt_HMAC.setText(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_HMAC)));
            if (CurrentIndex.getInt(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_HMAC_BASE64)) == 1) {
                chkb64hmac.setChecked(true);
            } else { chkb64hmac.setChecked(false);}
            CurrentIndex.close();
        }
        return rootView;
    }

    public void sendSPA() {
        startSPASend();
    }

    //    Start calling the JNI interface
    public synchronized void startSPASend() {
        output = sendSPAPacket();
        //sendHandlerMessage(handler, 1003);
      //  if (startApp) {
        //    startApp();
       // }
    }

    public int send(String nick){
        loadNativeLib("libfwknop.so", "/data/data/biz.incomsystems.fwknop2/lib");
        //mydb = new DBHelper(Fwknop2.getContext());
        Cursor CurrentIndex = mydb.getData(nick);
        CurrentIndex.moveToFirst();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("tcpAccessPorts_str", CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_TCP_PORTS)));
        edit.putString("access_str", ",tcp/" + CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_TCP_PORTS)));
        edit.putString("allowip_str", "0.0.0.0");
        edit.putString("passwd_str", CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_KEY)));
        edit.putString("hmac_str", "");
        edit.putString("destip_str", CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_IP)));
        edit.putString("destport_str", CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_PORT)));
        edit.putString("fw_timeout_str", CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_TIMEOUT)));
        edit.commit();
        this.sendSPA();
        return 0;
    }

    private void loadNativeLib(String lib, String destDir) {
        if (true) {
            String libLocation = destDir + "/" + lib;
            try {
                System.load(libLocation);
            } catch (Exception ex) {
                Log.e("JNIExample", "failed to load native library: " + ex);
            }
        }

    }

    public Handler handler = new Handler() {

        @Override
        public synchronized void handleMessage(Message msg) {
            Bundle b = msg.getData();
            Integer messageType = (Integer) b.get("message_type");

            Toast.makeText(getActivity(),messageType.toString(), Toast.LENGTH_LONG).show();
//            if (messageType != null && messageType == IPS_RESOLVED) {
//                progDialog.dismiss();
//            } else if (messageType != null && messageType == EXTIP_NOTRESOLVED) {
//                progDialog.dismiss();
//                UIAlert("Error", "Could not resolve your external IP. This means that "
//                        + "you're not connected to the internet or ifconfig.me "
//                        + "is not be accesible right now", activity);
//            } else if (messageType != null && messageType == LOCALIP_NOTRESOLVED) {
//                progDialog.dismiss();
//                UIAlert("Error", "Could not find any IP, makes sure you have an internet connection", activity);
//            } else if (messageType != null && messageType == SPA_SENT) {
//                Toast.makeText(activity, output, Toast.LENGTH_LONG).show();
//            }

        }
    };


}

