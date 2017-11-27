package insa_project.bananarchy.activities;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import insa_project.bananarchy.R;
import insa_project.bananarchy.bdd.GroupDAO;
import insa_project.bananarchy.bdd.LevelDAO;
import insa_project.bananarchy.bdd.RessourcesDAO;
import insa_project.bananarchy.model.Group;
import insa_project.bananarchy.model.Level;
import insa_project.bananarchy.utils.MyPreference;

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

    private static Context context;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

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

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
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
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        context = this;
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
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            bindPreferenceSummaryToValue(findPreference("example_list"));
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

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
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
                for(Level level : levels){
                    MultiSelectListPreference pref = new MultiSelectListPreference(getActivity());
                    pref.setTitle(level.getLibelle());
                    String[] listKey = new String[level.getGroups().size()];
                    String[] listValues = new String[level.getGroups().size()];
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


                        i++;
                    }
                    pref.setEntries(listKey);
                    pref.setEntryValues(listValues);
                    category.addPreference(pref);
                }
                setPreferenceScreen(screen);

                //Intent intent = new Intent(SyncActivity.this, MainActivity.class);
                //startActivity(intent);
            }
        }

        private class AsynkTaskGroups extends AsyncTask<Long,Void,ArrayList<Group>> {

            @Override
            protected ArrayList<Group> doInBackground(Long ... params) {
                GroupDAO ndao = new GroupDAO(EdtPreferenceFragment.this.getActivity());
                ArrayList<Group> listGroups;
                listGroups = ndao.listGroupeFromLevel(params[0]);
                return listGroups;

            }

            // Méthode exécutée à la fin de l'execution de la tâche asynchrone
            @Override
            protected void onPostExecute(ArrayList<Group> groups) {
                super.onPostExecute(groups);

                PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());

                PreferenceCategory category = new PreferenceCategory(getActivity());
                category.setTitle("Choisir des ressources");

                screen.addPreference(category);

                MultiSelectListPreference pref = new MultiSelectListPreference(getActivity());
                String[] listKey = new String[groups.size()];
                String[] listValues = new String[groups.size()];
                int i = 0;
                for(Group group : groups){
                    listKey[i] = group.getLibelle();
                    listValues[i] = group.getId()+"";
                    i++;
                }
                pref.setEntries(listKey);
                pref.setEntryValues(listValues);
                category.addPreference(pref);

                setPreferenceScreen(screen);
            }
        }
    }
}
