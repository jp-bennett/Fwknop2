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
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;


import java.io.IOException;

public class WriteFwknopNickToNfcDialog extends Activity {

    private static final short TNF_MIME_MEDIA = 2;
    private static final String NFC_TOKEN_MIME_TYPE = "application/nfckey";

    private static final String TAG = WriteFwknopNickToNfcDialog.class.getName();

    public static final String ARG_ITEM_TARGET = "item_id";
    private NfcAdapter mAdapter;
    IntentFilter[] mFilters;
    private PendingIntent mPendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private String mNick;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_fwknop_host_to_nfc);
       // mText = (TextView) findViewById(R.id.text);
        mNick = getIntent().getStringExtra(ARG_ITEM_TARGET);
        mAdapter = NfcAdapter.getDefaultAdapter(this);



        mPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e); //TODO: fail gracefully
        }
        mFilters = new IntentFilter[]{
                ndef,
        };
    }

    private void handleWriteNfcEvent(Tag tag) {
        NdefRecord[] records = null;
        Ndef ndef = Ndef.get(tag);

        if (ndef != null) {
            if (ndef.isWritable()) {

                NdefRecord record = new NdefRecord(
                        TNF_MIME_MEDIA,
                        NFC_TOKEN_MIME_TYPE.getBytes(),
                        "fwknop2".getBytes(),
                        mNick.getBytes());

                NdefMessage message = ndef.getCachedNdefMessage();
                if (message != null) {
                    records = message.getRecords();
                }

                try {

                    if (message != null && records != null) {
                        Log.i(TAG, "Adding new fwknop2 record to existing tag records");
                        NdefRecord[] newRecords = new NdefRecord[records.length + 1];
                        System.arraycopy(records, 0, newRecords, 0, records.length);
                        newRecords[records.length] = record;
                        records = newRecords;
                    } else {
                        Log.i(TAG, "Creating new set of records and adding fwknop2 to it");
                        records = new NdefRecord[]{record};
                    }

                    ndef.connect();
                    ndef.writeNdefMessage(new NdefMessage(records));
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                          //  mProgressBar.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception e) {

                    Log.e(TAG, "Unable to write Fwknop nick to NFC tag. " + e);
                    return;
                }
            } else {

                Log.e(TAG, "Tag is not writable");
            }
        } else {
            Log.e(TAG, "Tag does not support NDEF");
        }
        finish();
    }





    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, intentFiltersArray, techListsArray);
    }

    public void onNewIntent(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        handleWriteNfcEvent(tagFromIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }
}
