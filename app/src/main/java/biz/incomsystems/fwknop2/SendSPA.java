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

/**
 * Created by jbennett on 5/31/15.
 */
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendSPA extends Application {
    private String output;
    DBHelper mydb;
    Context ourCtx;
    public native String sendSPAPacket();

    //These are the configs to pass to the native code
    public String access_str;
    public String allowip_str;
    public String tcpAccessPorts_str;
    public String passwd_str;
    public String passwd_b64;
    public String hmac_str;
    public String hmac_b64;
    public String destip_str;
    public String destport_str;
    public String fw_timeout_str;

    public void sendSPA() {
        startSPASend();
    }

    //    Start calling the JNI interface
    public synchronized void startSPASend() {
        output = sendSPAPacket();
    }

    public int send(String nick, Context ctx) {
        loadNativeLib("libfwknop.so", "/data/data/biz.incomsystems.fwknop2/lib");
        mydb = new DBHelper(ctx);
        Cursor CurrentIndex = mydb.getData(nick);
        CurrentIndex.moveToFirst();

        //These variables are the ones that the jni pulls settings from.
        tcpAccessPorts_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_TCP_PORTS));
        access_str = "tcp/" + CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_TCP_PORTS));
        passwd_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_KEY));
        hmac_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_HMAC));
        destip_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_IP));
        destport_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_PORT));
        fw_timeout_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_TIMEOUT));
        allowip_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_ACCESS_IP));
        if (CurrentIndex.getInt(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_KEY_BASE64)) == 1) {
            passwd_b64 = "true";
        } else {
            passwd_b64 = "false";
        }
        if (CurrentIndex.getInt(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_HMAC_BASE64)) == 1) {
            hmac_b64 = "true";
        } else {
            hmac_b64 = "false";
        }
        if (allowip_str.equalsIgnoreCase("Source IP")) {
            allowip_str = "0.0.0.0";
            this.sendSPA();
        } else if (allowip_str.equalsIgnoreCase("Resolve IP")) {
            getExternalIP task = new getExternalIP();
            task.execute();
        }
        return 0;
    }

    private void loadNativeLib(String lib, String destDir) {
        if (true) {
            String libLocation = destDir + "/" + lib;
            try {
                System.load(libLocation);
            } catch (Exception ex) {
                Log.e("fwknop2", "failed to load native library: " + ex);
            }
        }
    }

    public Handler handler = new Handler() {

        @Override
        public synchronized void handleMessage(Message msg) {
            Bundle b = msg.getData();
            Integer messageType = (Integer) b.get("message_type");
        }
    };

    private class getExternalIP extends AsyncTask<Void, Void, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(ourCtx);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("\tSending...");
            pdLoading.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            Log.v("fwknop2", "Your external IP address is " + allowip_str);
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet("http://whatismyip.akamai.com");
                HttpResponse response;
                response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    long len = entity.getContentLength();
                    if (len != -1 && len < 1024) {
                        allowip_str = EntityUtils.toString(entity);
                        Log.v("fwknop2", "Your external IP address is " + allowip_str);
                    }
                }
            } catch (Exception ex) {
                Log.e("fwknop2", "error " + ex);
            }
            return allowip_str;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            allowip_str = result;
            sendSPA();
            pdLoading.dismiss();
        }
    }
}