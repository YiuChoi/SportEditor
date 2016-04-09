package name.caiyao.tencentsport;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    EditTextPreference editTextPreference;
    public final String SETTING_CHANGED = "name.caiyao.tencentsport.SETTING_CHANGED";

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(1);
        getPreferenceManager().getSharedPreferences().edit()
                .putBoolean("weixin",true)
                .putBoolean("qq",true)
                .putString("magnification", "1000").commit();
        addPreferencesFromResource(R.xml.preference);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findPreference("weixin").setOnPreferenceChangeListener(this);
        findPreference("qq").setOnPreferenceChangeListener(this);
        editTextPreference = (EditTextPreference) findPreference("magnification");
        editTextPreference.setOnPreferenceChangeListener(this);
        changeSummary();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        changeSummary();
        Toast.makeText(getActivity(), "重启手机将恢复默认值！", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        boolean isWeixin = sharedPreferences.getBoolean("weixin", false);
        boolean isQQ = sharedPreferences.getBoolean("qq", false);
        int m = Integer.valueOf(sharedPreferences.getString("magnification", "1000"));
        getKey(isWeixin, isQQ, m);
        return true;
    }

    public void getKey(boolean isWeixin, boolean isQQ, int m) {
        //为了设置实时生效
        Intent intent = new Intent(SETTING_CHANGED)
                .putExtra("weixin", isWeixin)
                .putExtra("qq",isQQ)
                .putExtra("magnification",m);
        getActivity().sendBroadcast(intent);
    }

    private void changeSummary() {
        editTextPreference.setSummary(getPreferenceManager().getSharedPreferences().getString("magnification", "1000"));
    }
}
