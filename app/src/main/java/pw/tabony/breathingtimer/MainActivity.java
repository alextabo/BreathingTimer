package pw.tabony.breathingtimer;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String nextState = "init";
    int cycleNum = 0;
    long bTime = 0L;
    long rTime = 0L;
    long resetTime = 0L;
    long totalTime = 0L;
    Integer[] times = new Integer[3];
    ArrayList<Integer[]> sessions = new ArrayList<>();

    private TextView time;
    private TextView tTime;
    private TextView tip;
    private TextView cycle;
    private Button finished;

    private long startTime = 0L;
    private long tstartTime = 0L;

    private Handler cycleHandler = new Handler();
    private Handler timeHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    long ttimeInMilliseconds = 0L;
    long ttimeSwapBuff = 0L;
    long tupdatedTime = 0L;
    long longestRetTime = 0L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        time = (TextView) findViewById(R.id.time);
        cycle = (TextView) findViewById(R.id.cycle);
        tTime = (TextView) findViewById(R.id.totalTime);
        tip = (TextView) findViewById(R.id.tip);
        final TextView rResult = (TextView) findViewById(R.id.rResult);
        finished = (Button) findViewById(R.id.finished);

        time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(nextState.equals("init")){
                    nextState = "breathing";
                    time.setTextSize(40);
                    time.setText("Breath");
                    tip.setText("Tap when done");
                    startTime = SystemClock.uptimeMillis();
                    cycleHandler.postDelayed(updateTimerThread, 0);
                    if (resetTime > 0){
                        times[2] = (int) (long) (resetTime/1000);
                        if (rTime > longestRetTime) longestRetTime = rTime;
                    } else {
                        tstartTime = SystemClock.uptimeMillis();
                        timeHandler.postDelayed(updateTimerThread, 0);
                    }
                } else if (nextState.equals("breathing")){
                    nextState = "reset";
                    time.setTextSize(80);
                    tip.setText("(retention)");
                    times[0] = (int) (long) (updatedTime/1000);
                    startTime = SystemClock.uptimeMillis();
                    cycleHandler.postDelayed(updateTimerThread, 0);
                } else if (nextState.equals("reset")){
                    rTime = updatedTime;
                    startTime = SystemClock.uptimeMillis();
                    cycleHandler.postDelayed(updateTimerThread, 0);
                    time.setTextSize(40);
                    time.setText("Restart");
                    tip.setText("Tap to restart");
                    resetTime = updatedTime;
                    times[1] = (int) (long) (updatedTime/1000);
                    rResult.setText("" + times[1] + "s");
                    cycleNum++;
                    cycle.setText("" + cycleNum);
                    nextState = "init";
                } else {
                    //huh?
                    tip.setText("Something wrong...");
                }
            }
        });
        finished.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setContentView(R.layout.summary_main);

                TextView sumTotalTime = (TextView) findViewById(R.id.sumTotalTime);
                TextView sumCycles = (TextView) findViewById(R.id.sumCycles);
                TextView sumLongestRetention = (TextView) findViewById(R.id.sumLongestRetention);

                int tsecs = (int) (longestRetTime / 1000);
                int tmins = tsecs / 60;
                tsecs = tsecs % 60;
                sumTotalTime.setText("" + tmins + ":" + String.format("%02d", tsecs));

                sumCycles.setText("" + cycleNum);
                int longest = 12;
                sumLongestRetention.setText("" + longest);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            //int mins = secs / 60;
            //secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            if(nextState.equals("reset")) {
                time.setText("" + secs);
            }


            ttimeInMilliseconds = SystemClock.uptimeMillis() - tstartTime;
            tupdatedTime = ttimeSwapBuff + ttimeInMilliseconds;

            int tsecs = (int) (tupdatedTime / 1000);
            int tmins = tsecs / 60;
            tsecs = tsecs % 60;
            tTime.setText("" + tmins + ":" + String.format("%02d", tsecs));

            cycleHandler.postDelayed(this, 0);
        }

    };

}