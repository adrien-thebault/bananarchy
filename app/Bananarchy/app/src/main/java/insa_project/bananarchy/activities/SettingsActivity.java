package insa_project.bananarchy.activities;


import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import insa_project.bananarchy.R;
import insa_project.bananarchy.bdd.GroupDAO;
import insa_project.bananarchy.bdd.LevelDAO;
import insa_project.bananarchy.bdd.RessourcesDAO;
import insa_project.bananarchy.model.Group;
import insa_project.bananarchy.model.Level;
import insa_project.bananarchy.utils.APIConnexion;
import insa_project.bananarchy.utils.ConnectBluetoothThread;
import insa_project.bananarchy.utils.CustomJobService;
import insa_project.bananarchy.utils.UTF8StringRequest;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static SettingsActivity context;
    private static GeneralPreferenceFragment contextFrag;





    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).edit();

            SwitchPreference switchPreferenceCar = null;
            SwitchPreference switchPreferenceWalker = null;
            SwitchPreference switchPreferenceBus = null;
            String keyJSON = null;

            if(contextFrag != null) {
                switchPreferenceCar = (SwitchPreference) contextFrag.findPreference("car_preference");
                switchPreferenceWalker = (SwitchPreference) contextFrag.findPreference("walker_preference");
                switchPreferenceBus = (SwitchPreference) contextFrag.findPreference("bus_preference");
            }

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            }else if(preference instanceof SwitchPreference && preference.getKey().equals("bus_preference")){
                Boolean val = (Boolean)value;
                keyJSON = "bus";
                if(val) {
                    switchPreferenceCar.setChecked(false);
                    switchPreferenceWalker.setChecked(false);
                }
                if(!val && !switchPreferenceCar.isChecked() && !switchPreferenceWalker.isChecked()){
                    switchPreferenceWalker.setChecked(true);
                }
                editor.putBoolean(preference.getKey(),val);
            }else if(preference instanceof SwitchPreference && preference.getKey().equals("car_preference")){
                Boolean val = (Boolean)value;
                keyJSON = "car";
                if(val) {
                    switchPreferenceBus.setChecked(false);
                    switchPreferenceWalker.setChecked(false);
                }
                if(!val && !switchPreferenceBus.isChecked() && !switchPreferenceWalker.isChecked()){
                    switchPreferenceWalker.setChecked(true);
                }
                editor.putBoolean(preference.getKey(),val);
            }else if(preference instanceof SwitchPreference && preference.getKey().equals("walker_preference")){
                Boolean val = (Boolean)value;
                keyJSON = "pedestrian";
                if(!val && !switchPreferenceBus.isChecked() && !switchPreferenceCar.isChecked()) {
                    return false;
                }
                if(val) {
                    switchPreferenceCar.setChecked(false);
                    switchPreferenceBus.setChecked(false);
                }
                editor.putBoolean(preference.getKey(),val);
            }else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            if(keyJSON != null){

                JSONObject j = new JSONObject();
                try {
                    j.accumulate("name","transportation");
                    j.accumulate("value",keyJSON);
                    Log.d("JSON",j.toString());
                    JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.PUT, APIConnexion.URL_SETTINGS_TRANSPORTATION,j,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // Display the first 500 characters of the response string.
                                    Log.d("RESULT","ON EST TOUT BON");
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("That didn't work!","ERROR");
                        }
                    });
                    RequestQueue queue = Volley.newRequestQueue(context);
                    queue.add(stringRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            editor.commit();
            return true;
        }
    };





    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.

        if(preference instanceof SwitchPreference){
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));


        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        context = this;


        // Acquire a reference to the system Location Manager
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.d("LOCATION",location.getLatitude()+" - "+location.getLongitude());
                JSONObject j = new JSONObject();
                try {
                    j.accumulate("name","location");
                    j.accumulate("value",location.getLatitude()+","+location.getLongitude());
                    JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.PUT, APIConnexion.URL_SETTINGS_LOCATION,j,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // Display the first 500 characters of the response string.
                                    Log.d("RESULT","ON EST TOUT BON");
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("That didn't work!","ERROR");
                        }
                    });
                    RequestQueue queue = Volley.newRequestQueue(context);
                    queue.add(stringRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                locationManager.removeUpdates(this);

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

// Register the listener with the Location Manager to receive location updates

        if (ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }



    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || EdtPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_transport);
            setHasOptionsMenu(true);

            contextFrag = this;
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("bus_preference"));
            bindPreferenceSummaryToValue(findPreference("walker_preference"));
            bindPreferenceSummaryToValue(findPreference("car_preference"));

            /*final SwitchPreference switchPreferenceBus = (SwitchPreference) findPreference("bus_preference");
            switchPreferenceBus.setChecked(false);

            final SwitchPreference switchPreferenceWalker = (SwitchPreference) findPreference("walker_preference");
            switchPreferenceWalker.setChecked(true);

            final SwitchPreference switchPreferenceCar = (SwitchPreference) findPreference("car_preference");
            switchPreferenceCar.setChecked(false);
*/
            /*switchPreferenceBus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean value = (Boolean) newValue;
                    if(value) {
                        switchPreferenceCar.setChecked(false);
                        switchPreferenceWalker.setChecked(false);
                    }
                    else {
                        switchPreferenceWalker.setChecked(true);
                    }
                    return true;
                }
            });

            switchPreferenceWalker.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean value = (Boolean) newValue;
                    if(value) {
                        switchPreferenceCar.setChecked(false);
                        switchPreferenceBus.setChecked(false);
                    }
                    else {
                        switchPreferenceWalker.setChecked(true);
                    }
                    return true;
                }
            });

            switchPreferenceCar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean value = (Boolean) newValue;
                    if(value) {
                        switchPreferenceBus.setChecked(false);
                        switchPreferenceWalker.setChecked(false);
                    }
                    else {
                        switchPreferenceWalker.setChecked(true);
                    }
                    return true;
                }
            });*/

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            Preference sendButton = findPreference("sendButton");
            sendButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AsyncTaskSendBluetooth taskSendBluetooth = new AsyncTaskSendBluetooth();
                    taskSendBluetooth.execute();
                    return false;
                }
            });


            final EditTextPreference editTextPreference = (EditTextPreference) findPreference("alarm_delay_preferences");
            editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    EditTextPreference editTextPreference1 = (EditTextPreference)preference;
                    Log.d("BANANARCHY","UPDATE DELAY : "+editTextPreference1.getEditText().getText());
                    try {
                        JSONObject j = new JSONObject();
                        j.accumulate("name","preparation_time");
                        j.accumulate("value",editTextPreference1.getEditText().getText());

                        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.PUT, APIConnexion.URL_SETTINGS_PREPARATION_TIME,j,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // Display the first 500 characters of the response string.
                                        Toast.makeText(getActivity(),"Modifications enregistrées",Toast.LENGTH_LONG);
                                        Log.d("RESULT","ON EST TOUT BON");
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("That didn't work!","ERROR");
                            }
                        });
                        RequestQueue queue = Volley.newRequestQueue(getActivity());
                        queue.add(stringRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private class AsyncTaskSendBluetooth extends AsyncTask<String,Void,Boolean> {

            @Override
            protected Boolean doInBackground(String... strings) {

                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, APIConnexion.URL_DATA_AGENDA, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject agendaJSON) {
                                if(ConnectBluetoothThread.isInit()){
                                    try {
                                        String agendaRequest = "AGENDA " + agendaJSON.getInt("beginning") + ";" + agendaJSON.getString("summary") + ";" + agendaJSON.getString("location");
                                        ConnectBluetoothThread.write(agendaRequest);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                Log.d("BANANARCHY","WRITE AGENDA");
                RequestQueue queue1 = Volley.newRequestQueue(context);
                queue1.add(jsonRequest);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result){
                    Toast.makeText(getContext(),"SYNCHRONISATION EFFECTUÉE",Toast.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class EdtPreferenceFragment extends PreferenceFragment {

        protected HashMap<String, HashSet<String>> savePref = new HashMap<>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            /*super.onCreate(savedInstanceState);
            //addPreferencesFromResource(R.xml.pref_edt);
            Preference pref = new Preference(SettingsActivity.context);
            pref.setTitle("3INFO");
            Intent t = new Intent(SettingsActivity.context, NotificationPreferenceFragment.class);
            addPreferencesFromIntent(t);
            setHasOptionsMenu(true);*/
            super.onCreate(savedInstanceState);
            AsynkTaskGetLevels asynkTaskGetLevels = new AsynkTaskGetLevels();
            asynkTaskGetLevels.execute();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            Log.d("Test",item.getItemId()+"");
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private class AsynkTaskGetLevels extends AsyncTask<String,Void,ArrayList<Level>> {

            @Override
            protected ArrayList<Level> doInBackground(String ... params) {
                /*LevelDAO ndao = new LevelDAO(EdtPreferenceFragment.this.getActivity());
                GroupDAO groupDAO = new GroupDAO(getActivity());
                ArrayList<Level> listLevels;
                listLevels = ndao.getAllLevels();
                ArrayList<Group> listGroup;
                for(Level level : listLevels){
                    listGroup = groupDAO.listGroupeFromLevel(level.getId());
                    level.addAllGroupsToLevel(listGroup);
                }*/

                LevelDAO levelDAO = new LevelDAO(getActivity());
                GroupDAO groupDAO = new GroupDAO(getActivity());
                RessourcesDAO ressourcesDAO = new RessourcesDAO(getActivity());
                ArrayList<Level> listLevels;
                listLevels = levelDAO.getAllLevels();
                ArrayList<Level> result = new ArrayList<>();

                ArrayList<Group> listGroup;
                for(Level level : listLevels){
                    listGroup = groupDAO.listGroupeFromLevel(level.getId());
                    ArrayList<Group> result1 = new ArrayList<>();
                    for(Group group : listGroup){
                        group.setRessource(ressourcesDAO.getRessources(group.getId()));
                        result1.add(group);
                    }
                    level.addAllGroupsToLevel(result1);
                    result.add(level);
                }

                return result;
            }


            // Méthode exécutée à la fin de l'execution de la tâche asynchrone
            @Override
            protected void onPostExecute(ArrayList<Level> levels) {
                super.onPostExecute(levels);

                PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());

                PreferenceCategory category = new PreferenceCategory(getActivity());
                category.setTitle("Sélectionnez des matières");

                screen.addPreference(category);
                Set<String> listVal = new HashSet<>();
                try{
                    File file = new File(getActivity().getDir("data", MODE_PRIVATE), "map_preferences");
                    ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
                    savePref = (HashMap<String, HashSet<String>>) inputStream.readObject();
                    inputStream.close();
                } catch(Exception e){

                }
                for(Level level : levels){
                    MultiSelectListPreference pref = new MultiSelectListPreference(getActivity());
                    pref.setTitle(level.getLibelle());
                    String[] listKey = new String[level.getGroups().size()];
                    String[] listValues = new String[level.getGroups().size()];
                    pref.setPersistent(true);
                    int i = 0;
                    for(Group group : level.getGroups()){
                        listKey[i] = group.getLibelle();
                        Log.d("LIBELLE",group.getLibelle());
                        int j = 0;
                        for(String res : group.getRessource()){
                            if(j>0)
                                listValues[i] = listValues[i]+","+res;
                            else
                                listValues[i] = res;
                            j++;
                        }
                        listVal.add(listValues[i]);
                        i++;
                    }
                    pref.setEntries(listKey);
                    pref.setEntryValues(listValues);
                    if(savePref.containsKey(level.getLibelle()))
                        pref.setDefaultValue(savePref.get(level.getLibelle()));
                    pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            Log.d("Changement de pref",newValue.toString());
                            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();

                            savePref.put(preference.toString(),(HashSet<String>)newValue);
                            File file = new File(getActivity().getDir("data", MODE_PRIVATE), "map_preferences");
                            ObjectOutputStream outputStream = null;
                            try {
                                outputStream = new ObjectOutputStream(new FileOutputStream(file));
                                outputStream.writeObject(savePref);
                                outputStream.flush();
                                outputStream.close();
                                String listAgenda = "";
                                for(String s : (HashSet<String>)newValue){
                                    listAgenda+=s+",";
                                }
                                listAgenda = listAgenda.substring(0, listAgenda.length() - 1);
                                Log.d("MES RESSOURCES",listAgenda);

                                JSONObject j = new JSONObject();
                                j.accumulate("name","agenda");
                                j.accumulate("value",listAgenda);
                                Log.d("JSON",j.toString());
                                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.PUT, APIConnexion.URL_SETTINGS_AGENDA,j,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                // Display the first 500 characters of the response string.
                                                Toast.makeText(getActivity(),"Modifications enregistrées",Toast.LENGTH_LONG);
                                                Log.d("RESULT","ON EST TOUT BON");
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("That didn't work!","ERROR");
                                    }
                                });
                                RequestQueue queue = Volley.newRequestQueue(getActivity());
                                queue.add(stringRequest);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                    category.addPreference(pref);
                }
                setPreferenceScreen(screen);

            }
        }

    }


}
