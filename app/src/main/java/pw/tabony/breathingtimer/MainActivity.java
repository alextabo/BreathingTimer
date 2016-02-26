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
    /* The state of this app are
        1) init = app just started, start timer etc
        2) breathing = breathing phase
        3) retention = retention phase
        4) reset = time before starting breathing again or finishing to summary
        5) summary = summary page - back button returns to timer but it broken and resets times */

    private boolean init = true;
    private String nextState;           //Indicates what the next phase is

    private int cycleNum = 0;           //Number of breathing cycles done so far
    private int phaseTimeInSecs = 0;    //Last retention time
    private int sessionTimeInSecs = 0;  //Length of total session
    private long phaseStartTime = 0L;   //Starting time of each phase
    private long sessionStartTime = 0L; //Starting time for entire session
    private int longestRetTime = 0;     //Kinda obvious, no?

    //private long ttimeSwapBuff = 0L;
    //private long tupdatedTime = 0L;

    private Integer[] times = new Integer[3]; //Stored arrays of times
    private ArrayList<Integer[]> sessions = new ArrayList<>(); //breathing time, retention time, reset time

    private TextView time;
    private TextView totalTime;
    private TextView tip;
    private TextView cycle;
    
    private Handler cycleHandler = new Handler(); // Timer for main screen
    private Handler timeHandler = new Handler(); // Timer for total session time

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        time = (TextView) findViewById(R.id.time);
        cycle = (TextView) findViewById(R.id.cycle);
        totalTime = (TextView) findViewById(R.id.totalTime);
        tip = (TextView) findViewById(R.id.tip);
        final TextView rResult = (TextView) findViewById(R.id.rResult);
        final Button finished = (Button) findViewById(R.id.finished);

        time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(init){
                    sessionStartTime = SystemClock.uptimeMillis(); //Start session
                    timeHandler.postDelayed(updateTimerThread, 0);
                    nextState = "breathing";
                    init = false;
                } else {
                    //Not init? Must be a reset then - grab reset time
                    times[2] = phaseTimeInSecs;
                    // Save times array into arraylist... someday
                }
                if (nextState.equals("breathing")){
                    //We are breathing now
                    phaseStartTime = SystemClock.uptimeMillis(); //start breathing phase timer
                    cycleHandler.postDelayed(updateTimerThread, 0);
                    time.setText("");
                    time.setTextSize(60);
                    time.setText("breathe");
                    tip.setText("Tap when done");
                    nextState = "retention";
                } else if (nextState.equals("retention")){
                    //We are starting retention
                    times[0] = phaseTimeInSecs; //Get breathing phase time

                    phaseStartTime = SystemClock.uptimeMillis(); //Restart phase timer
                    cycleHandler.postDelayed(updateTimerThread, 0);

                    time.setText("");
                    time.setTextSize(120); //Big timer!
                    tip.setText("(retention)");
                    nextState = "reset";
                } else if (nextState.equals("reset")){
                    //We are in reset mode - new cycle or finish
                    times[1] = phaseTimeInSecs; //Get retention phase time
                    if (times[1] > longestRetTime) longestRetTime = times[1];

                    phaseStartTime = SystemClock.uptimeMillis();
                    cycleHandler.postDelayed(updateTimerThread, 0);
                    time.setText("");
                    time.setTextSize(60);
                    time.setText("restart");
                    tip.setText("Tap to restart");
                    rResult.setText(String.format("%ds", times[1]));

                    cycle.setText(String.format("%d", ++cycleNum));
                    nextState = "breathing";
                } else {
                    //Transition from reset to breathing

                    nextState = "breathing";
                    phaseStartTime = SystemClock.uptimeMillis();
                    cycleHandler.postDelayed(updateTimerThread, 0);
                }
            }
        });
        finished.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (nextState.equals("breathing")) {
                    setContentView(R.layout.summary_main);
                    nextState = "summary";
                    TextView sumTotalTime = (TextView) findViewById(R.id.sumTotalTime);
                    TextView sumCycles = (TextView) findViewById(R.id.sumCycles);
                    TextView sumLongestRetention = (TextView) findViewById(R.id.sumLongestRetention);

                    //Display session time
                    int mins = sessionTimeInSecs / 60;
                    int secs = sessionTimeInSecs % 60;
                    sumTotalTime.setText(String.format("%d:%02d", mins, secs));

                    //Display number of cycles - add longest cycle later
                    sumCycles.setText(String.format("%d", cycleNum));

                    //Display longest retention time
                    mins = longestRetTime / 60;
                    secs = longestRetTime % 60;
                    sumLongestRetention.setText(String.format("%d:%02d / %ds", mins, secs, longestRetTime));
                }
            }
        });

    }
    @Override
    public void onBackPressed()
    {
        if(nextState.equals("summary")) {
            setContentView(R.layout.activity_main);
            //nextState = "breathing";
        } else {
            super.onBackPressed();
        }


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
            int secs, mins;
            long ms;

            //Update timer on main screen if retention phase
            long phaseTimeInMs = SystemClock.uptimeMillis() - phaseStartTime;
            // Why do we have these lines?
            //long timeSwapBuff = 0L;
            //updatedTime = timeSwapBuff + phaseTimeinMS;
            phaseTimeInSecs = (int) (phaseTimeInMs / 1000);
            if(nextState.equals("reset")) time.setText(String.format("%d", phaseTimeInSecs));

            //Update total session time
            ms = SystemClock.uptimeMillis() - sessionStartTime;
            //As above, what purpose did/does this serve?
            //tupdatedTime = ttimeSwapBuff + ms;
            sessionTimeInSecs = (int) (ms / 1000);
            mins = sessionTimeInSecs / 60;
            secs = sessionTimeInSecs % 60;
            totalTime.setText(String.format("%d:%02d", mins, secs));

            cycleHandler.postDelayed(this, 0);
        }

    };

}
