package name.caiyao.tencentsport;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

/**
 * Created by 蔡小木 on 2016/8/8 0008.
 */

public class Utils {
    public static boolean hasStepSensor(Context context) {
        if (context == null) {
            return false;
        }
        Context appContext = context.getApplicationContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return false;
        } else {
            boolean hasSensor = false;
            Sensor sensor = null;
            try {
                hasSensor = appContext.getPackageManager().hasSystemFeature("android.hardware.sensor.stepcounter");
                SensorManager sm = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
                sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return hasSensor && sensor != null;
        }
    }
}
