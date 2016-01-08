package biz.incomsystems.fwknop2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class NfcKnockActivity extends Activity {
    boolean IsNotNFC;
    boolean nfcEnabled;
    SendSPA OurSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OurSender = new SendSPA();
        onNewIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SharedPreferences prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        nfcEnabled = prefs.getBoolean("EnableNfc", false);
            if( NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msg;
            if (rawMsgs != null && rawMsgs.length > 0) {
                msg = (NdefMessage) rawMsgs[0];

                NdefRecord[] contentRecs = msg.getRecords();
                if (!nfcEnabled){
                    Toast.makeText(this, R.string.nfcDisabled, Toast.LENGTH_LONG).show();
                    finish();
                }
                for (NdefRecord rec : contentRecs) {
                    try {
                        String id = new String(rec.getId(), "UTF-8");
                        if(id.equals("fwknop2")){
                            String nick = new String(rec.getPayload(), "UTF-8");
                            Toast.makeText(this, getString(R.string.SendingSPATo) + nick, Toast.LENGTH_LONG).show();
                            OurSender.send(nick, this);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.nfcError, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        return;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IsNotNFC = true;
        OurSender.onActivityResult(requestCode, resultCode, data); // have to call this manually as it isn't an activity class
        finish();
    }
}
