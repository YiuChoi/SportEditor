package name.caiyao.tencentsport;

import android.hardware.Sensor;
import android.util.SparseArray;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by 蔡小木 on 2016/2/16 0016.
 */
public class MainHook implements IXposedHookLoadPackage {
    static int wechat = 1000;
    static int stepCount = 1;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm"))
            return;
        final Class<?> sensorEL = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", loadPackageParam.classLoader);

        XposedBridge.hookAllMethods(sensorEL, "dispatchSensorEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] + wechat * stepCount;
                stepCount += 1;

                Field field = param.thisObject.getClass().getEnclosingClass().getDeclaredField("sHandleToSensor");
                field.setAccessible(true);
                XposedBridge.log("   Field: " + field.toString());
                int handle = (Integer) param.args[0];
                Sensor ss = ((SparseArray<Sensor>) field.get(0)).get(handle);
                XposedBridge.log("   SensorEvent: sensor=" + ss);
            }
        });
    }
}
