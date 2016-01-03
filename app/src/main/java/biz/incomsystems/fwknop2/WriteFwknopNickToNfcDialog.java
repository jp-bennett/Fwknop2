/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.incomsystems.fwknop2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

class WriteFwknopNickToNfcDialog extends AlertDialog {

    private static final short TNF_MIME_MEDIA = 2;
    private static final String NFC_TOKEN_MIME_TYPE = "application/nfckey";

    private static final String TAG = WriteFwknopNickToNfcDialog.class.getName();

    private final PowerManager.WakeLock mWakeLock;

    private View mView;
    private Button mCancelButton;
    private TextView mLabelView;
    private ProgressBar mProgressBar;
    private Context mContext;
    private Activity mActivity;
    private String mNick;

    WriteFwknopNickToNfcDialog(Context context, String nick) {
        super(context);

        mContext = context;
        mActivity = (Activity)mContext;
        mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WriteFwknopNickToNfcDialog:wakeLock");
        mNick = nick;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mWakeLock.acquire();

        mView = getLayoutInflater().inflate(R.layout.write_fwknop_host_to_nfc, null);

        setView(mView);
        setInverseBackgroundForced(true);
        setTitle(R.string.setup_nfc_tag);
        setCancelable(true);
        setButton(DialogInterface.BUTTON_NEGATIVE,
                mContext.getResources().getString(R.string.cancel),
                (OnClickListener) null);

        mLabelView = (TextView) mView.findViewById(R.id.nfc_status_label);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.progress_bar);

        super.onCreate(savedInstanceState);

        mCancelButton = getButton(DialogInterface.BUTTON_NEGATIVE);

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(mActivity);

        nfcAdapter.enableReaderMode(mActivity, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {
                handleWriteNfcEvent(tag);
            }
        }, NfcAdapter.FLAG_READER_NFC_A |
                NfcAdapter.FLAG_READER_NFC_B |
                NfcAdapter.FLAG_READER_NFC_BARCODE |
                NfcAdapter.FLAG_READER_NFC_F |
                NfcAdapter.FLAG_READER_NFC_V,
                null);

        mLabelView.setText(R.string.status_awaiting_tap);

        mView.findViewById(R.id.write_nfc_layout).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void handleWriteNfcEvent(Tag tag) {
        Ndef ndef = Ndef.get(tag);

        if (ndef != null) {
            if (ndef.isWritable()) {

                NdefRecord record = new NdefRecord(
                        TNF_MIME_MEDIA,
                        NFC_TOKEN_MIME_TYPE.getBytes(),
                        "fwknop2".getBytes(),
                        mNick.getBytes());

                NdefMessage message = ndef.getCachedNdefMessage();
                NdefRecord [] records = message.getRecords();
                boolean exists = false;

                try{
                    for (int i = 0; i < records.length; i++) {
                        String id = new String(records[i].getId(), "UTF-8");
                        if(id.equals("fwknop2")){
                            Log.i(TAG, "Updating existing NFC tag record");
                            records[i] = record;
                            exists = true;
                            break;
                        }
                    }

                    if(records != null && exists == false){
                        Log.i(TAG, "Adding new fwknop2 record to existing tag records");
                        NdefRecord[] newRecords = new NdefRecord[records.length + 1];
                        for (int i = 0; i < records.length; i++) {
                            newRecords[i] = records[i];
                        }
                        newRecords[records.length] = record;
                        records = newRecords;
                    }
                    else if(records == null) {
                        Log.i(TAG, "Creating new set of records and adding fwknop2 to it");
                        NdefRecord[] newRecords = {record};
                        records = newRecords;
                    }

                    ndef.connect();
                    ndef.writeNdefMessage(new NdefMessage(records));
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                    setViewText(mLabelView, R.string.status_write_success);
                    setViewText(mCancelButton, R.string.done_label);
                } catch (IOException e) {
                    setViewText(mLabelView, R.string.status_failed_to_write);
                    Log.e(TAG, "Unable to write Fwknop nick to NFC tag.", e);
                    return;
                } catch (FormatException e) {
                    setViewText(mLabelView, R.string.status_failed_to_write);
                    Log.e(TAG, "Unable to write Fwknop nick to NFC tag.", e);
                    return;
                }
            } else {
                setViewText(mLabelView, R.string.status_tag_not_writable);
                Log.e(TAG, "Tag is not writable");
            }
        } else {
            setViewText(mLabelView, R.string.status_tag_not_writable);
            Log.e(TAG, "Tag does not support NDEF");
        }
    }

    @Override
    public void dismiss() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        super.dismiss();
    }

    private void setViewText(final TextView view, final int resid) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(resid);
            }
        });
    }
}
