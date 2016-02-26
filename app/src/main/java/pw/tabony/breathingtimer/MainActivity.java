package pw.tabony.breathingtimer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    String state = "init";
    long bTime = 0L;
    long rTime = 0L;
    long resetTime = 0L;
    private TextView time;
    private TextView tip;

    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        time = (TextView) findViewById(R.id.time);
        tip = (TextView) findViewById(R.id.tip);

        final TextView resetResult = (TextView) findViewById(R.id.resetResult);
        final TextView rResult = (TextView) findViewById(R.id.rResult);
        final TextView bResult = (TextView) findViewById(R.id.bResult);

        time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(state.equals("init")){
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    state = "breathing";
                    tip.setText("Breathing");
                    if(rTime > 0) {
                        resetTime = updatedTime;
                        resetResult.setText("" + (resetTime / 1000) + "s");
                        rResult.setText("" + (rTime / 1000) + "s");
                        bResult.setText("" + (bTime / 1000) + "s");
                    }
                } else if (state.equals("breathing")){
                    bTime = updatedTime;
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    tip.setText("Retention");
                    state = "reset";
                } else if (state.equals("reset")){
                    rTime = updatedTime;
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    tip.setText("Tap to Restart");
                    state = "init";
                } else {
                    //huh?
                }


                //Pause
                //timeSwapBuff += timeInMilliseconds;
                //customHandler.removeCallbacks(updateTimerThread);


            }
        });
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            //int mins = secs / 60;
            //secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            time.setText("" + secs);
            customHandler.postDelayed(this, 0);
        }

    };

}