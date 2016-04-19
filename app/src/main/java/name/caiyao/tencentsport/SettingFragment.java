package name.caiyao.tencentsport;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    EditTextPreference editTextPreference;
    public final String SETTING_CHANGED = "name.caiyao.tencentsport.SETTING_CHANGED";

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(1);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.preference);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //findPreference("weixin").setOnPreferenceChangeListener(this);
        //findPreference("qq").setOnPreferenceChangeListener(this);
        editTextPreference = (EditTextPreference) findPreference("magnification");
        //editTextPreference.setOnPreferenceChangeListener(this);
        changeSummary();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        changeSummary();
        getKey();
        return true;
    }

    public void getKey() {
        Intent intent = new Intent(SETTING_CHANGED);
        getActivity().sendBroadcast(intent);
    }

    private void changeSummary() {
        editTextPreference.setSummary(getPreferenceManager().getSharedPreferences().getString("magnification", "1000"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        changeSummary();
        getKey();
    }
}
