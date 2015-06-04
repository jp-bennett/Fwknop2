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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

/**
 * A fragment representing a single Config detail screen.
 * This fragment is either contained in a {@link ConfigListActivity}
 * in two-pane mode (on tablets) or a {@link ConfigDetailActivity}
 * on handsets.
 */
public class ConfigDetailFragment extends Fragment {
    DBHelper mydb;
    Config config;
    TextView txt_NickName ;  // objects representing the config options
    Spinner spn_allowip ;
    TextView txt_allowIP ;
    LinearLayout lay_allowIP;
    CheckBox chknataccess;
    CheckBox chkservercmd;
    LinearLayout lay_natIP;
    LinearLayout lay_natport;
    LinearLayout lay_serverCMD;

    TextView txt_tcp_ports ;
    TextView txt_udp_ports ;
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

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.chknataccess:
                if (checked) {
                    lay_natIP.setVisibility(View.VISIBLE);
                    lay_natport.setVisibility(View.VISIBLE);
                } else {
                    lay_natIP.setVisibility(View.GONE);
                    lay_natport.setVisibility(View.GONE);
                }
                break;
            case R.id.chkservercmd:
                if (checked) {
                    lay_serverCMD.setVisibility(View.VISIBLE);
                } else {
                    lay_serverCMD.setVisibility(View.GONE);
                }
                break;

        }
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
                //These are placeholders for future input validation
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
                if (chknataccess.isChecked()) {
                    config.NAT_IP = txt_nat_ip.getText().toString();
                    config.NAT_PORT = txt_nat_port.getText().toString();
                } else {
                    config.NAT_IP = "";
                    config.NAT_PORT = "";
                }
                if (chkservercmd.isChecked()) {
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
                config.TCP_PORTS = txt_tcp_ports.getText().toString();
                config.UDP_PORTS = txt_udp_ports.getText().toString();
                config.SERVER_IP = txt_server_ip.getText().toString();
                config.SERVER_PORT = txt_server_port.getText().toString();
                config.SERVER_TIMEOUT = txt_server_time.getText().toString();
                config.KEY = txt_KEY.getText().toString();       //key
                config.KEY_BASE64 = chkb64key.isChecked();                      //is key b64
                config.HMAC = txt_HMAC.getText().toString(); // hmac key
                config.HMAC_BASE64 = chkb64hmac.isChecked();                     //is hmac base64
                mydb.updateConfig(context, config);
                //Need to somehow call
                Activity activity = getActivity();
                //if(activity instanceof ConfigDetailActivity){
                //    ConfigDetailActivity myactivity = (ConfigDetailActivity) activity;
                //    myactivity.onItemSaved();
                //} else
                if(activity instanceof ConfigListActivity) {
                    ConfigListActivity myactivity = (ConfigListActivity) activity;
                    myactivity.onItemSaved();
                }

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

        //fields from the config detail
        txt_NickName = (TextView) rootView.findViewById(R.id.NickName);
        //spinner below
        txt_allowIP = (TextView) rootView.findViewById(R.id.iptoallow);
        lay_allowIP = (LinearLayout) rootView.findViewById(R.id.iptoallowsl);
        lay_natIP = (LinearLayout) rootView.findViewById(R.id.natipsl);
        lay_natport = (LinearLayout) rootView.findViewById(R.id.natportsl);
        lay_serverCMD = (LinearLayout) rootView.findViewById(R.id.servercmdsl);
        txt_tcp_ports = (TextView) rootView.findViewById(R.id.tcpAccessPorts);
        txt_udp_ports = (TextView) rootView.findViewById(R.id.udpAccessPorts);
        txt_server_ip = (TextView) rootView.findViewById(R.id.destIP);
        txt_server_port = (TextView) rootView.findViewById(R.id.destPort);
        txt_server_time = (TextView) rootView.findViewById(R.id.fwTimeout);
        txt_server_cmd = (TextView) rootView.findViewById(R.id.servercmd);
        txt_nat_ip = (TextView) rootView.findViewById(R.id.natip);
        txt_nat_port = (TextView) rootView.findViewById(R.id.natport);


        txt_KEY = (TextView) rootView.findViewById(R.id.passwd);
        txt_HMAC = (TextView) rootView.findViewById(R.id.hmac);

        chkb64hmac = (CheckBox) rootView.findViewById(R.id.chkb64hmac);
        chkb64key = (CheckBox) rootView.findViewById(R.id.chkb64key);
        chknataccess = (CheckBox) rootView.findViewById(R.id.chknataccess);
        chkservercmd = (CheckBox) rootView.findViewById(R.id.chkservercmd);
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

            txt_tcp_ports.setText(config.TCP_PORTS);
            txt_udp_ports.setText(config.UDP_PORTS);
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
                chkservercmd.setChecked(true);
                txt_server_cmd.setText(config.SERVER_CMD);
                lay_serverCMD.setVisibility(View.VISIBLE);
            }
            if (!config.NAT_IP.equalsIgnoreCase("")) {
                chknataccess.setChecked(true);
                txt_nat_ip.setText(config.NAT_IP);
                txt_nat_port.setText(config.NAT_PORT);
                lay_natIP.setVisibility(View.VISIBLE);
                lay_natport.setVisibility(View.VISIBLE);
            }
        }
        return rootView;
    }
}

