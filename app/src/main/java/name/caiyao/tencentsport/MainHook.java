package name.caiyao.tencentsport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
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
    public final String SETTING_CHANGED = "name.caiyao.tencentsport.SETTING_CHANGED";
    private static final String WEXIN = "com.tencent.mm";
    private static final String QQ = "com.tencent.mobileqq";
    static int stepCount = 0;
    static boolean isWeixin, isQQ, isAuto;
    XSharedPreferences sharedPreferences;
    static int m, max;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(WEXIN) || loadPackageParam.packageName.equals(QQ)) {
            getKey();
            final Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
            final Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SETTING_CHANGED);
            systemContext.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    getKey();
                }
            }, intentFilter);

            final Class<?> sensorEL = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", loadPackageParam.classLoader);
            XposedBridge.hookAllMethods(sensorEL, "dispatchSensorEvent", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int handle = (Integer) param.args[0];
                    Field field = param.thisObject.getClass().getDeclaredField("mSensorsEvents");
                    field.setAccessible(true);
                    Sensor ss = ((SparseArray<SensorEvent>) field.get(param.thisObject)).get(handle).sensor;
                    if (ss == null) {
                        XposedBridge.log("传感器为NULL");
                        return;
                    }
                    if (ss.getType() == Sensor.TYPE_STEP_COUNTER || ss.getType() == Sensor.TYPE_STEP_DETECTOR) {
                        XposedBridge.log("传感器类型: " + ss.getType() + " 名称:" + ss.getName());
                        XposedBridge.log("当前设置weixin: " + isWeixin + ",qq:" + isQQ + ",m=" + m + ",auto:" + isAuto + ",max:" + max);
                        if ((isWeixin && loadPackageParam.packageName.equals(WEXIN)) || (isQQ && loadPackageParam.packageName.equals(QQ))) {
                            if (isAuto) {
                                if (m * stepCount < max) {
                                    ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] + m * stepCount;
                                    stepCount += 1;
                                } else {
                                    stepCount = 0;
                                }
                            } else {
                                ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * m;
                            }
                            XposedBridge.log(loadPackageParam.packageName + "计步器步数修改后   SensorEvent: x=" + ((float[]) param.args[1])[0]);
                        }
                    }
                }
            });
        }
    }

    private void getKey() {
        sharedPreferences.reload();
        XposedBridge.log("设置路径：" + sharedPreferences.getFile().getAbsolutePath() + ",是否可读：" + sharedPreferences.getFile().canRead());
        isWeixin = sharedPreferences.getBoolean("weixin", true);
        isQQ = sharedPreferences.getBoolean("qq", true);
        m = Integer.valueOf(sharedPreferences.getString("magnification", "100"));
        isAuto = sharedPreferences.getBoolean("autoincrement", false);
        max = Integer.valueOf(sharedPreferences.getString("max", "100000"));
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        sharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
    }
}
