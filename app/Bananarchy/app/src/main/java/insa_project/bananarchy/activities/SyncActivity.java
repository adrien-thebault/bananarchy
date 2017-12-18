package insa_project.bananarchy.activities;

/**
 * Created by pierre on 18/10/17.
 */

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import insa_project.bananarchy.R;
import insa_project.bananarchy.bdd.GroupDAO;
import insa_project.bananarchy.bdd.LevelDAO;
import insa_project.bananarchy.bdd.RessourcesDAO;
import insa_project.bananarchy.model.Group;
import insa_project.bananarchy.model.Level;
import insa_project.bananarchy.utils.APIConnexion;
import insa_project.bananarchy.utils.CustomJobService;
import insa_project.bananarchy.utils.UTF8StringRequest;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SyncActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.

        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                //actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sync);

        mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);

        ProgressBar pgBar = (ProgressBar)findViewById(R.id.progress_bar);
        pgBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        StringRequest stringRequest = new UTF8StringRequest(Request.Method.GET, APIConnexion.URL_GRP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            HashMap<String,ArrayList<Group>>listGr = APIConnexion.getGroups(response);
                            long id;
                            GroupDAO gdao = new GroupDAO(getApplicationContext());
                            RessourcesDAO rdao = new RessourcesDAO(getApplicationContext());
                            LevelDAO ndao = new LevelDAO(getApplicationContext());
                            if(listGr != null && !listGr.isEmpty())
                            {
                                for(Map.Entry<String,ArrayList<Group>> entry : listGr.entrySet())
                                {

                                    long levelID = ndao.ajouter(new Level(entry.getKey()));
                                    ArrayList<Group> listGrByLevel = entry.getValue();
                                    for(Group g : listGrByLevel)
                                    {
                                        id = gdao.ajouter(g,levelID);
                                        for (String ressource : g.getRessource()) {
                                            rdao.ajouter(ressource, id);
                                        }
                                    }
                                }
                                Toast.makeText(SyncActivity.this,"Les cours ont été mis à jour ;)",Toast.LENGTH_LONG);


                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(SyncActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("That didn't work!","ERROR");
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
        scheduleJob();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public Context getContext()
    {
        return this;
    }

    private void scheduleJob() {
        final JobScheduler jobScheduler = (JobScheduler) getSystemService(
                Context.JOB_SCHEDULER_SERVICE);

        final ComponentName name = new ComponentName(this, CustomJobService.class);

        final int result = jobScheduler.schedule(getJobInfo(123, 1, name));

        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("BANANARCHY", "Scheduled job successfully!");
        }

    }

    private JobInfo getJobInfo(final int id, final long minutes,
                               final ComponentName name) {
        final JobInfo jobInfo;
        final long interval = TimeUnit.MINUTES.toMillis(minutes);
        final boolean isPersistent = true;
        final int networkType = JobInfo.NETWORK_TYPE_ANY;

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(id, name)
                    .setMinimumLatency(interval)
                    .setRequiredNetworkType(networkType)
                    .setPersisted(isPersistent)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(id, name)
                    .setPeriodic(interval)
                    .setRequiredNetworkType(networkType)
                    .setPersisted(isPersistent)
                    .build();
        }

        return jobInfo;
    }


    private class AsyncTaskSyncDB extends AsyncTask<String,Void,Boolean> {

        @Override
        protected Boolean doInBackground(String ... params) {
            APIConnexion api = new APIConnexion();
            HashMap<String,ArrayList<Group>> listGr;
            long id;
            try {
                listGr = api.getGroups("");
                GroupDAO gdao = new GroupDAO(getApplicationContext());
                RessourcesDAO rdao = new RessourcesDAO(getApplicationContext());
                LevelDAO ndao = new LevelDAO(getApplicationContext());
                if(listGr != null && !listGr.isEmpty())
                {
                    for(Map.Entry<String,ArrayList<Group>> entry : listGr.entrySet())
                    {

                        long levelID = ndao.ajouter(new Level(entry.getKey()));
                        ArrayList<Group> listGrByLevel = entry.getValue();
                        for(Group g : listGrByLevel)
                        {
                            id = gdao.ajouter(g,levelID);
                            for (String ressource : g.getRessource()) {
                                rdao.ajouter(ressource, id);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }


        // Méthode exécutée à la fin de l'execution de la tâche asynchrone
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result==false)
            {
                Toast.makeText(SyncActivity.this,"Impossible de mettre à jour la base de données",Toast.LENGTH_LONG);

            }
            Toast.makeText(SyncActivity.this,"Les cours ont été mis à jour ;)",Toast.LENGTH_LONG);

            Intent intent = new Intent(SyncActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
    }
}