package name.caiyao.tencentsport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.util.SparseArray;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by 蔡小木 on 2016/2/16 0016.
 */
public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String WEXIN = "com.tencent.mm";
    private static final String QQ = "com.tencent.mobileqq";
    private static final String YUEDONG = "com.yuedong.sport";
    private static final String LEDONG = "cn.ledongli.ldl";
    private static final String PINGAN = "com.pingan.papd";
    private static final String CODOON = "com.codoon.gps";
    private static final String WEIBO = "com.sina.weibo";
    Context systemContext;

    private static int ledongCount = 0;
    private XSharedPreferences sharedPreferences;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
        IntentFilter intentFilter = new IntentFilter();
        String SETTING_CHANGED = "name.caiyao.tencentsport.SETTING_CHANGED";
        intentFilter.addAction(SETTING_CHANGED);
        systemContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        }, intentFilter);

        final Class<?> sensorEL = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", loadPackageParam.classLoader);
        XposedBridge.hookAllMethods(sensorEL, "dispatchSensorEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                int handle = (Integer) param.args[0];
                Field field = param.thisObject.getClass().getDeclaredField("mSensorsEvents");
                field.setAccessible(true);
                Sensor ss = ((SparseArray<SensorEvent>) field.get(param.thisObject)).get(handle).sensor;
                if (ss == null) {
                    XposedBridge.log("传感器为NULL");
                    return;
                }
                if (ss.getType() == Sensor.TYPE_ACCELEROMETER) {
                    ledongCount += 1;
                    //完美
                    if (ledongCount % 3 == 0) {
                        ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 100;
                        ((float[]) param.args[1])[1] += (float) -10;
                    } else if (ledongCount % 2 == 0) {
                        ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 1000;
                        ((float[]) param.args[1])[2] += (float) -20;
                        ((float[]) param.args[1])[1] += (float) -5;
                    } else {
                        ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 10;
                        ((float[]) param.args[1])[2] += (float) 20;
                        ((float[]) param.args[1])[1] += (float) -15;
                    }

                }
                if (ss.getType() == Sensor.TYPE_STEP_COUNTER) {
                    ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 1000;
                }
            }
        });
    }

    private void getKey() {
        sharedPreferences.reload();
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        sharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
    }
}
