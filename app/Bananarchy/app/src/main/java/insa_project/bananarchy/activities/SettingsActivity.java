package insa_project.bananarchy.activities;


import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
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
    private OutputStream bluetoothOutputStream;
    private InputStream bluetoothInStream;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket = null;
    Handler bluetoothIn;
    private static ConnectBluetoothThread mConnectedThread;

    final int handlerState = 0;


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


            editor.commit();
            return true;
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    private void init() throws IOException {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(context, "Bluetooth non supporté sur cet appareil", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                BluetoothDevice myDevice = null;
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    if (device.getName().equals("Xperia Z2"))
                        myDevice = device;
                    Log.d("NAME", deviceName);
                    Log.d("ADDRESS", deviceHardwareAddress);
                }
                if (myDevice != null) {
                    try {
                        btSocket = createBluetoothSocket(myDevice);
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        btSocket.connect();
                        Toast.makeText(getBaseContext(), "Connection done", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        try {
                            btSocket.close();
                        } catch (IOException e2) {
                            //insert code to deal with this
                        }
                    }
                    mConnectedThread = new ConnectBluetoothThread(btSocket);
                    mConnectedThread.start();

                    //I send a character when resuming.beginning transmission to check device is connected
                    //If it is not an exception will be thrown in the write method and finish() will be called
                    mConnectedThread.write("x");
                }
            }
        }
    }

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
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Toast.makeText(getApplicationContext(),msg.toString(),Toast.LENGTH_LONG).show();
            }
        };
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
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
                    SettingsActivity.mConnectedThread.write("TEST");
                    return false;
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
                                Log.d("MES RESSOURCES",listAgenda);
                                /*
                                JSONObject j = new JSONObject();
                                j.accumulate("agenda",)

                                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.PUT, APIConnexion.URL_SETTINGS_AGENDA,j,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                // Display the first 500 characters of the response string.

                                                Toast.makeText(getActivity(),"Modifications enregistrées",Toast.LENGTH_LONG);
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("That didn't work!","ERROR");
                                    }
                                });
                                RequestQueue queue = Volley.newRequestQueue(this);
                                queue.add(stringRequest);
                                */
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                    category.addPreference(pref);
                }
                setPreferenceScreen(screen);

                //Intent intent = new Intent(SyncActivity.this, MainActivity.class);
                //startActivity(intent);
            }
        }

    }

    private class ConnectBluetoothThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectBluetoothThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                Log.d("WRITEBLUE",input);
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}
