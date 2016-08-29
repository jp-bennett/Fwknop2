package biz.incomsystems.fwknop2;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.util.SparseBooleanArray;


public class knockCountdownTimer extends IntentService {


    public String digest_type;
    public String hmac_type;
    public boolean reknock;
    private SendSPA OurSender;
    private int notificationId;

    private NotificationManager nm;
    MyCounter timer;
    private static SparseBooleanArray timerDict = new SparseBooleanArray();

    public long counter = 60;
    public int init_counter;
    public long init_mscounter;
    public String nickname;
    public knockCountdownTimer() {
        super("knockCountdownTimer");
    }

    @Override
    public void onCreate() {
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    @Override
    public int onStartCommand(Intent intent, int blah, int blah2) {
        reknock = intent.getBooleanExtra("keep_open", false);
        nickname = intent.getStringExtra("nickname");
        if (nickname == null) {
            notificationId = intent.getIntExtra("notificationId", 0);
            if (notificationId != 0) {
                timerDict.put(notificationId, false);
                nm.cancel(notificationId);
            }
            stopSelf();
            return 0;
        }
        if (reknock) {
            OurSender = new SendSPA();
            OurSender.reKnock = true;
            OurSender.access_str = intent.getStringExtra("access_str");
            OurSender.allowip_str = intent.getStringExtra("allowip_str");
            OurSender.passwd_str = intent.getStringExtra("passwd_str");
            OurSender.passwd_b64 = intent.getStringExtra("passwd_b64");
            OurSender.hmac_str = intent.getStringExtra("hmac_str");
            OurSender.hmac_b64 = intent.getStringExtra("hmac_b64");
            OurSender.fw_timeout_str = intent.getStringExtra("fw_timeout_str");
            OurSender.nat_ip_str = intent.getStringExtra("nat_ip_str");
            OurSender.nat_port_str = intent.getStringExtra("nat_port_str");
            OurSender.nat_access_str = intent.getStringExtra("nat_access_str");
            OurSender.nat_local = intent.getStringExtra("nat_local");
            OurSender.server_cmd_str = intent.getStringExtra("server_cmd_str");
            OurSender.legacy = intent.getStringExtra("legacy");
            OurSender.digest_type = intent.getStringExtra("digest_type");
            OurSender.hmac_type = intent.getStringExtra("hmac_type");
        }
        init_counter = Integer.valueOf(intent.getStringExtra("timeout"));

        long time = System.currentTimeMillis() / 1000L;
        String tmpStr = Long.toString(time);
        String last4Str = tmpStr.substring(tmpStr.length() -5);
        notificationId = Integer.valueOf(last4Str);

        counter = init_counter;
        init_mscounter = init_counter * 1000;
        timer = new MyCounter(this, init_mscounter, 1000, notificationId);
        timer.start();
        timerDict.put(notificationId, true);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onHandleIntent(Intent intent) {
    }

    public class MyCounter extends CountDownTimer
    {
        int counterID;
        NotificationCompat.Builder builder;
        public MyCounter(Context MyCon, long millisInFuture, long countDownInterval, int cID)
        {

            super(millisInFuture, countDownInterval);
            counterID = cID;
            Intent intent = new Intent(MyCon, knockCountdownTimer.class);
            intent.putExtra("notificationId", notificationId);
            PendingIntent pIntent = PendingIntent.getService(MyCon, notificationId, intent, 0);
            builder = new NotificationCompat.Builder(MyCon)
                    .setSmallIcon(R.drawable.fwknop2_outline)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.fwknop2_small))
                    .setContentTitle("Fwknop2: "  + nickname)
                    .setContentText(String.valueOf(counter));
            builder.setDeleteIntent(pIntent);
            nm.notify(notificationId, builder.build());
        }

        @Override
        public void onFinish()
        {
            nm.cancel(counterID);
            stopSelf();
            timerDict.put(counterID, false);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            counter = millisUntilFinished / 1000;
            if (!timerDict.get(counterID)) {
                stopSelf();
                this.cancel();
                nm.cancel(counterID);

            } else {
                builder.setContentText(String.valueOf(counter));
                nm.notify(counterID, builder.build());
                if (counter < 11 && reknock) {
                    if (OurSender.resend().equalsIgnoreCase("Success")) {
                        //if counter is < 10, send knock and restart the timer
                        this.start();
                    }
                }
            }
        }
    }
}
