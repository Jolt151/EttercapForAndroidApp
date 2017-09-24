package jolt151.ettercapforandroid;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();*/
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        onSharedPreferenceChanged(sharedPreferences,getString(R.string.pref_default_args));
        onSharedPreferenceChanged(sharedPreferences,getString(R.string.pref_default_interface));
        onSharedPreferenceChanged(sharedPreferences,getString(R.string.pref_default_targets));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    
/*    SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals(prefs.getString("default_args",null))) {
                        Preference default_args = findPreference(key);
                        // Set summary to be the user-description for the selected value
                        default_args.setSummary(prefs.getString(key, ""));
                    }
                    else if (key.equals(prefs.getString("default_interface",null))) {
                        Preference default_interface = findPreference(key);
                        default_interface.setSummary(prefs.getString(key,""));
                    }
                    else if (key.equals(prefs.getString("default_targets",null))) {
                        Preference default_targets = findPreference(key);
                        // Set summary to be the user-description for the selected value
                        default_targets.setSummary(prefs.getString(key, ""));
                    }
                }
            };*/

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(sharedPreferences.getString(key,null));
        }
    }

}