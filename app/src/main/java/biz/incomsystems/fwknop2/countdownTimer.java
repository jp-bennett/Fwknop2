package biz.incomsystems.fwknop2;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.util.Log;


public class countdownTimer extends IntentService {


    public String digest_type;
    public String hmac_type;
    public boolean reknock;
    private SendSPA OurSender;
    public Notification.Builder builder;

    public NotificationManager nm;
    public MyCounter timer;

    public long counter = 60;
    public int init_counter;
    public long init_mscounter;
    public String nickname;
    public countdownTimer() {
        super("countdownTimer");

    }

    @Override
    public void onCreate() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    @Override
    public int onStartCommand(Intent intent, int blah, int blah2) {
        //Log.d("fwknop2", "in onHandleIntent");
        reknock = intent.getBooleanExtra("keep_open", false);
        nickname = intent.getStringExtra("nickname");
        if (nickname == null) {
            if (timer != null)
                timer.cancel();
            if (nm != null)
                nm.cancel(0);
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







        counter = init_counter;
        init_mscounter = init_counter * 1000;
        //Log.d("fwknop2", nickname);
        declareNotification();
        timer = new MyCounter(init_mscounter, 1000);
        timer.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
//        timer.cancel();
    }




    @Override
    public void onHandleIntent(Intent intent) {

    }

    public void declareNotification()
    {
        //Declare a new notification
        Intent intent = new Intent(this, countdownTimer.class);
        PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);
        builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.fwknop2_small)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.fwknop2_small))
                .setContentTitle("Fwknop2: " + nickname)
                .setContentText(String.valueOf(counter));
                builder.setDeleteIntent(pIntent);
        if (reknock) {
            builder.addAction(R.drawable.fwknop2, "Close connection", pIntent);
        }

        nm.notify(0, builder.build());
    }

    /**
     * Show a notification while this service is running.
     */


    public class MyCounter extends CountDownTimer
    {

        public MyCounter(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish()
        {
            counter = 0;


            //Kill the game
            nm.cancel(0);
            stopSelf();
        }

        @Override
        public void onTick(long millisUntilFinished)
        {
            counter = millisUntilFinished / 1000;
            //declareNotification();
            builder.setContentText(String.valueOf(counter));
            nm.notify(0, builder.build());
            if (counter < 11 && reknock) {
               if (OurSender.resend().equalsIgnoreCase("Success")) {
                   //restart the timer
                   timer.start();
               }


                //if counter is < 10, send knock and restart the timer
            }
        }

    }

}
