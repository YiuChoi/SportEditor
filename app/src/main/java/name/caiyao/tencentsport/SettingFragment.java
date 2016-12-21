package name.caiyao.tencentsport;


import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private EditTextPreference mEditTextPreference;

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
        mEditTextPreference = (EditTextPreference) findPreference("magnification");
        findPreference("version").setSummary(BuildConfig.VERSION_NAME);
        changeSummary();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        changeSummary();
        getKey();
        return true;
    }

    private void getKey() {
        String SETTING_CHANGED = "name.caiyao.tencentsport.SETTING_CHANGED";
        Intent intent = new Intent(SETTING_CHANGED);
        intent.putExtra("magnification", getPreferenceManager().getSharedPreferences().getString("magnification", "100"));
        intent.putExtra("on", getPreferenceManager().getSharedPreferences().getBoolean("on", true));
        if (getActivity() != null) {
            getActivity().sendBroadcast(intent);
        }
        boolean enabled = getPreferenceManager().getSharedPreferences().getBoolean("icon", true);
        int state;
        if (enabled) {
            state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        } else {
            state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        }
        getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getActivity(), "name.caiyao.sporteditor.SettingsActivity-Alias"), state, 1);
    }

    private void changeSummary() {
        if (mEditTextPreference != null)
            mEditTextPreference.setSummary(getPreferenceManager().getSharedPreferences().getString("magnification", "1000"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        changeSummary();
        getKey();
    }
}
