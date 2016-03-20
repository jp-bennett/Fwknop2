package biz.incomsystems.fwknop2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.validator.routines.DomainValidator;

public class GeneralConfigActivity extends FragmentActivity {
    CheckBox chkDns;
    CheckBox chkNfc;
    CheckBox chkNotification;
    TextView txt_url;
    boolean dnsEnabled;
    boolean nfcEnabled;
    boolean notificationEnabled;
    String ipUrl;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        dnsEnabled = prefs.getBoolean("EnableDns", true);
        nfcEnabled = prefs.getBoolean("EnableNfc", false);
        notificationEnabled = prefs.getBoolean("EnableNotification", true);
        ipUrl = prefs.getString("ipSource", "http://whatismyip.akamai.com");
        chkDns = (CheckBox) findViewById(R.id.chkb_dns);
        chkNfc = (CheckBox) findViewById(R.id.chkb_nfc);
        chkNotification = (CheckBox) findViewById(R.id.chkb_notification);
        txt_url = (TextView) findViewById(R.id.myip_url);
        chkDns.setChecked(dnsEnabled);
        chkNfc.setChecked(nfcEnabled);
        chkNotification.setChecked(notificationEnabled);
        txt_url.setText(ipUrl);
    }

    public void defaultSettings(View view) {
        txt_url.setText("http://whatismyip.akamai.com");
        chkDns.setChecked(true);
        chkNfc.setChecked(false);
        chkNotification.setChecked(true);
    }

    public void saveSettings(View view) {
        if (txt_url.getText().toString().matches("(.+)://(.+)")) {
            String[] tmp = txt_url.getText().toString().split("://");
            if (tmp[0].equalsIgnoreCase("http") || tmp[0].equalsIgnoreCase("https")){
                if (DomainValidator.getInstance().isValid(tmp[1])) {
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putBoolean("EnableDns", chkDns.isChecked());
                    ed.putBoolean("EnableNfc", chkNfc.isChecked());
                    ed.putBoolean("EnableNotification", chkNotification.isChecked());
                    ed.putString("ipSource", txt_url.getText().toString());
                    ed.apply();
                    finish();
                    return;
                }
            }
        }
        Toast.makeText(getApplicationContext(), getString(R.string.my_ip_source_error),Toast.LENGTH_SHORT).show();
    }
}
