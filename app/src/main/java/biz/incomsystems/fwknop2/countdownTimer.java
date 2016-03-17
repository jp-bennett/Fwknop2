package biz.incomsystems.fwknop2;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

public class countdownTimer extends IntentService {

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
        Log.d("fwknop2", "in onHandleIntent");
        init_counter = Integer.valueOf(intent.getStringExtra("timeout"));
        nickname = intent.getStringExtra("nickname");
        counter = init_counter;
        init_mscounter = init_counter * 1000;
        //Log.d("fwknop2", nickname);
        timer = new MyCounter(init_mscounter, 100);
        timer.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
    }


    public NotificationManager nm;
    public MyCounter timer;

    @Override
    public void onHandleIntent(Intent intent) {

    }

    public void declareNotification()
    {
        //Declare a new notification
        Notification builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Fwknop2: " + nickname)
                .setContentText(String.valueOf(counter)).build();

        nm.notify(0, builder);
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
            declareNotification();
            //if counter is < 10, send knock and restart the timer
        }

    }

}
