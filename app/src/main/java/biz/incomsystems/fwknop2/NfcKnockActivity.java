package biz.incomsystems.fwknop2;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class NfcKnockActivity extends Activity {
    boolean IsNotNFC;
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
        Log.i("fwknop2", "calling onNewIntent");
            if( NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msg;
            if (rawMsgs != null && rawMsgs.length > 0) {
                msg = (NdefMessage) rawMsgs[0];

                NdefRecord[] contentRecs = msg.getRecords();
                for (NdefRecord rec : contentRecs) {
                    try {
                        String id = new String(rec.getId(), "UTF-8");
                        if(id.equals("fwknop2")){
                            String nick = new String(rec.getPayload(), "UTF-8");
                            Toast.makeText(this, "Sending SPA to " + nick, Toast.LENGTH_LONG).show();
                            OurSender.send(nick, this);
                            break;
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "NFC Tag parsing error", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("fwknop2", "calling onActivityResult");
        IsNotNFC = true;
        OurSender.onActivityResult(requestCode, resultCode, data); // have to call this manually as it isn't an activity class
        finish();
    }
}
