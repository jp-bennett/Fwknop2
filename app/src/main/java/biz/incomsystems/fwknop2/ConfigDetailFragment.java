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
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
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

import com.sonelli.juicessh.pluginlibrary.PluginContract;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.DomainValidator;

import java.util.UUID;

/**
 * A fragment representing a single Config detail screen.
 * This fragment is either contained in a {@link ConfigListActivity}
 * in two-pane mode (on tablets) or a {@link ConfigDetailActivity}
 * on handsets.
 */
public class ConfigDetailFragment extends Fragment {
    private ConnectionListLoader connectionListLoader;

    DBHelper mydb;
    PluginContract myJuice;
    Config config;
    TextView txt_NickName ;  // objects representing the config options
    Spinner spn_allowip ;
    Spinner spn_configtype ;
    Spinner spn_ssh ;
    Spinner spn_juice;
    ConnectionSpinnerAdapter juice_adapt;
    TextView txt_allowIP ;
    LinearLayout lay_allowIP;
    LinearLayout lay_natIP;
    LinearLayout lay_natport;
    LinearLayout lay_serverCMD;
    LinearLayout lay_AccessPort;
    LinearLayout lay_fwTimeout;
    LinearLayout lay_sshcmd;
    TextView txt_ssh_cmd;
    String configtype = "Open Port";

    TextView txt_ports ;
//    TextView txt_udp_ports ;
    TextView txt_server_ip ;
    TextView txt_server_port ;
    TextView txt_server_time ;
    TextView txt_nat_ip;
    TextView txt_nat_port;
    TextView txt_server_cmd;
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
        config = new Config();
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
                toast.setText("You Must choose a unique Nickname."); // choosing a used nick will just overwrite it. So really
                toast.show();
            } else if (spn_allowip.getSelectedItem().toString().equalsIgnoreCase("Allow IP") && (!ipValidate.isValid(txt_allowIP.getText().toString()))){ //Have to have a valid ip to allow, if using allow ip
                toast.setText("You Must supply a valid IP address to 'Allow IP'.");
                toast.show();
            }  else if (!ipValidate.isValid(txt_server_ip.getText().toString()) && !DomainValidator.getInstance().isValid(txt_server_ip.getText().toString())){ // check server entry. Must be a valid url or ip.
                toast.setText("You Must supply a valid server address.");
                toast.show();

//            //end input validation, actual saving below
            } else {
                toast.show();
                if (configtype.equalsIgnoreCase("Open Port")) {
                    config.PORTS = txt_ports.getText().toString();
                    config.SERVER_TIMEOUT = txt_server_time.getText().toString();
                } else {
                    config.PORTS = "";
                    config.SERVER_TIMEOUT = "";
                }
                if (configtype.equalsIgnoreCase("Nat Access")) {
                    config.NAT_IP = txt_nat_ip.getText().toString();
                    config.NAT_PORT = txt_nat_port.getText().toString();
                    config.PORTS = txt_ports.getText().toString();
                    config.SERVER_TIMEOUT = txt_server_time.getText().toString();
                } else {
                    config.NAT_IP = "";
                    config.NAT_PORT = "";
                }
                if (configtype.equalsIgnoreCase("Server Command")) {
                    config.SERVER_CMD = txt_server_cmd.getText().toString();
                } else {
                    config.SERVER_CMD = "";
                }
                if (spn_allowip.getSelectedItem().toString().equalsIgnoreCase("Resolve IP")) {
                    config.ACCESS_IP = spn_allowip.getSelectedItem().toString();
                } else if (spn_allowip.getSelectedItem().toString().equalsIgnoreCase("Source IP")) {
                    config.ACCESS_IP = "0.0.0.0";
                } else {
                    config.ACCESS_IP = txt_allowIP.getText().toString();
                }
                config.NICK_NAME = txt_NickName.getText().toString();  //nickname
                //config.TCP_PORTS = txt_tcp_ports.getText().toString();

                config.SERVER_IP = txt_server_ip.getText().toString();
                config.SERVER_PORT = txt_server_port.getText().toString();
                config.SSH_CMD = "";
                if (spn_ssh.getSelectedItem().toString().equalsIgnoreCase("SSH Uri")) {
                    config.SSH_CMD = txt_ssh_cmd.getText().toString();
                    config.juice_uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
                } else if (spn_ssh.getSelectedItem().toString().equalsIgnoreCase("Juicessh")) {
                    config.SSH_CMD = "juice:" + juice_adapt.getConnectionName(spn_juice.getSelectedItemPosition());
                    config.juice_uuid = juice_adapt.getConnectionId(spn_juice.getSelectedItemPosition());
                    Log.v("fwknop2", config.juice_uuid.toString());
                } else {
                    config.juice_uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
                    config.SSH_CMD = "";
                }

                config.KEY = txt_KEY.getText().toString();       //key
                config.KEY_BASE64 = chkb64key.isChecked();                      //is key b64
                config.HMAC = txt_HMAC.getText().toString(); // hmac key
                config.HMAC_BASE64 = chkb64hmac.isChecked();                     //is hmac base64
                mydb.updateConfig(context, config);
                Activity activity = getActivity();
                if(activity instanceof ConfigListActivity) {
                    ConfigListActivity myactivity = (ConfigListActivity) activity;
                    myactivity.onItemSaved();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { // This handles the qrcode results
        Log.v("fwknop2", "Detail fragment activity result");
        if (requestCode == 0) {

            if (resultCode == Activity.RESULT_OK) {

                String contents = data.getStringExtra("SCAN_RESULT");
                for (String stanzas: contents.split(" ")){
                    String[] tmp = stanzas.split(":");
                    if (tmp[0].equalsIgnoreCase("KEY_BASE64")) {
                        txt_KEY.setText(tmp[1]);
                        chkb64key.setChecked(true);
                    } else  if (tmp[0].equalsIgnoreCase("KEY")) {
                        txt_KEY.setText(tmp[1]);
                        chkb64key.setChecked(false);
                    } else if (tmp[0].equalsIgnoreCase("HMAC_KEY_BASE64")) {
                        txt_HMAC.setText(tmp[1]);
                        chkb64hmac.setChecked(true);
                    } else if (tmp[0].equalsIgnoreCase( "HMAC_KEY")) {
                            txt_HMAC.setText(tmp[1]);
                            chkb64hmac.setChecked(false);
                    }
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
        myJuice = new PluginContract();
        // do something here to get the list of nicks


        //Handlers for the input fields
        txt_NickName = (TextView) rootView.findViewById(R.id.NickName);
        txt_allowIP = (TextView) rootView.findViewById(R.id.iptoallow);
        lay_allowIP = (LinearLayout) rootView.findViewById(R.id.iptoallowsl);
        lay_natIP = (LinearLayout) rootView.findViewById(R.id.natipsl);
        lay_natport = (LinearLayout) rootView.findViewById(R.id.natportsl);
        lay_serverCMD = (LinearLayout) rootView.findViewById(R.id.servercmdsl);
        txt_ports = (TextView) rootView.findViewById(R.id.AccessPorts);
        txt_server_ip = (TextView) rootView.findViewById(R.id.destIP);
        txt_server_port = (TextView) rootView.findViewById(R.id.destPort);
        txt_server_time = (TextView) rootView.findViewById(R.id.fwTimeout);
        txt_server_cmd = (TextView) rootView.findViewById(R.id.servercmd);
        txt_nat_ip = (TextView) rootView.findViewById(R.id.natip);
        txt_nat_port = (TextView) rootView.findViewById(R.id.natport);
        lay_AccessPort = (LinearLayout) rootView.findViewById(R.id.AccessPortsl);
        lay_fwTimeout = (LinearLayout) rootView.findViewById(R.id.fwTimeoutl);
        lay_sshcmd = (LinearLayout) rootView.findViewById(R.id.sshcmdsl);
        txt_ssh_cmd = (TextView) rootView.findViewById(R.id.sshcmd);
        txt_KEY = (TextView) rootView.findViewById(R.id.passwd);
        txt_HMAC = (TextView) rootView.findViewById(R.id.hmac);
        chkb64hmac = (CheckBox) rootView.findViewById(R.id.chkb64hmac);
        chkb64key = (CheckBox) rootView.findViewById(R.id.chkb64key);

        spn_juice = (Spinner) rootView.findViewById(R.id.juice_spn);
        juice_adapt = new ConnectionSpinnerAdapter(getActivity());
        spn_juice.setAdapter(juice_adapt);


            //connectionListLoader.setOnLoadedListener(connectionListLoader.OnLoadedListener());
        spn_allowip = (Spinner) rootView.findViewById(R.id.allowip);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_allowip.setAdapter(adapter);
        spn_allowip.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (parent.getItemAtPosition(pos).toString().equalsIgnoreCase("Allow IP")) {
                    lay_allowIP.setVisibility(View.VISIBLE);
                } else {
                    lay_allowIP.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spn_ssh = (Spinner) rootView.findViewById(R.id.ssh);
        ArrayAdapter<CharSequence> adapter_ssh = ArrayAdapter.createFromResource(getActivity(),
                R.array.ssh_options, android.R.layout.simple_spinner_item);
        adapter_ssh.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_ssh.setAdapter(adapter_ssh);
        spn_ssh.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)

                if (parent.getItemAtPosition(pos).toString().equalsIgnoreCase("None")) {
                    lay_sshcmd.setVisibility(View.GONE);
                    spn_juice.setVisibility(View.GONE);
                    // blank the other options here
                } else if (parent.getItemAtPosition(pos).toString().equalsIgnoreCase("SSH Uri")) {
                    // show the txt for the uri
                    lay_sshcmd.setVisibility(View.VISIBLE);
                    spn_juice.setVisibility(View.GONE);
                } else if (parent.getItemAtPosition(pos).toString().equalsIgnoreCase("Juicessh")) {
                    lay_sshcmd.setVisibility(View.GONE);

                    if (connectionListLoader == null) {
                        connectionListLoader = new ConnectionListLoader(getActivity(), juice_adapt);
                        connectionListLoader.setOnLoadedListener(new ConnectionListLoader.OnLoadedListener() {
                            @Override
                            public void onLoaded() {  // This is so ugly...
                                spn_juice.setVisibility(View.VISIBLE);
                                if (config.SSH_CMD.contains("juice:") && spn_juice.getCount() > 0 ) {
                                    for (int n = 0; n < spn_juice.getCount(); n++) {
                                        if (config.SSH_CMD.contains(juice_adapt.getConnectionName(n))) {
                                            spn_juice.setSelection(n);
                                        }
                                    }
                                }
                            }
                        });
                        getActivity().getSupportLoaderManager().initLoader(0, null, connectionListLoader);
                    } else {
                        getActivity().getSupportLoaderManager().restartLoader(0, null, connectionListLoader);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spn_configtype = (Spinner) rootView.findViewById(R.id.configtype);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.configtype_options, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_configtype.setAdapter(adapter2);
        spn_configtype.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (parent.getItemAtPosition(pos).toString().equalsIgnoreCase("Open Port")) {
                    configtype = "Open Port";
                    lay_AccessPort.setVisibility(View.VISIBLE);
                    lay_fwTimeout.setVisibility(View.VISIBLE);
                    txt_nat_ip.setText("");
                    txt_nat_port.setText("");
                    txt_server_cmd.setText("");
                } else {
                    lay_AccessPort.setVisibility(View.GONE);
                    lay_fwTimeout.setVisibility(View.GONE);
                }
                if (parent.getItemAtPosition(pos).toString().equalsIgnoreCase("Nat Access")) {
                    configtype = "Nat Access";
                    txt_server_cmd.setText("");
                    lay_AccessPort.setVisibility(View.VISIBLE);
                    lay_fwTimeout.setVisibility(View.VISIBLE);
                    lay_natIP.setVisibility(View.VISIBLE);
                    lay_natport.setVisibility(View.VISIBLE);
                } else {
                    lay_natIP.setVisibility(View.GONE);
                    lay_natport.setVisibility(View.GONE);
                }
                if (parent.getItemAtPosition(pos).toString().equalsIgnoreCase("Server Command")) {
                    configtype = "Server Command";
                    lay_serverCMD.setVisibility(View.VISIBLE);
                    txt_ports.setText("");
                    txt_nat_ip.setText("");
                    txt_nat_port.setText("");
                    txt_server_time.setText("");
                } else {
                    lay_serverCMD.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //Below is the loading of a saved config
        if (active_Nick.equalsIgnoreCase("New Config")) {
            txt_NickName.setText("");
            config.SSH_CMD = "";
            txt_HMAC.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            txt_KEY.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            config = mydb.getConfig(active_Nick);
            txt_NickName.setText(active_Nick);
            if (config.ACCESS_IP.equalsIgnoreCase( "Resolve IP")) {
                spn_allowip.setSelection(0);
            } else if (config.ACCESS_IP.equalsIgnoreCase("0.0.0.0")) {
                spn_allowip.setSelection(1);
            } else {
                spn_allowip.setSelection(2);
                txt_allowIP.setText(config.ACCESS_IP);
            }
            txt_ports.setText(config.PORTS);
            txt_server_ip.setText(config.SERVER_IP);
            txt_server_port.setText(config.SERVER_PORT);
            txt_server_time.setText(config.SERVER_TIMEOUT);
            txt_KEY.setText(config.KEY);
            if (config.KEY_BASE64) {
                chkb64key.setChecked(true);
            } else { chkb64key.setChecked(false);}
            txt_HMAC.setText(config.HMAC);
            if (config.HMAC_BASE64) {
                chkb64hmac.setChecked(true);
            } else { chkb64hmac.setChecked(false);}
            if (!config.SERVER_CMD.equalsIgnoreCase("")) {
                spn_configtype.setSelection(2);
                txt_server_cmd.setText(config.SERVER_CMD);
            } else if (!config.NAT_IP.equalsIgnoreCase("")) {
                spn_configtype.setSelection(1);
                txt_ports.setText(config.PORTS);
                txt_nat_ip.setText(config.NAT_IP);
                txt_nat_port.setText(config.NAT_PORT);
                txt_server_time.setText(config.SERVER_TIMEOUT);
            } else {
                spn_configtype.setSelection(0);
            }
            if (config.SSH_CMD.equalsIgnoreCase("")) {
                spn_ssh.setSelection(0);
            } else if (config.SSH_CMD.contains("juice:")) {
                spn_ssh.setSelection(2);
            } else {
                spn_ssh.setSelection(1);
                txt_ssh_cmd.setText(config.SSH_CMD);
            }
        }
        return rootView;
    }
}

