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
public class MainHook implements IXposedHookLoadPackage {
    public final String SETTING_CHANGED = "name.caiyao.tencentsport.SETTING_CHANGED";
    private static final String WEXIN = "com.tencent.mm";
    private static final String QQ = "com.tencent.mobileqq";
    static int QQStepCount = 0;
    static boolean isWeixin, isQQ;
    XSharedPreferences sharedPreferences;
    static int m;
    static int WechatStepCount = 0;

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
                    isWeixin = intent.getExtras().getBoolean("weixin", true);
                    isQQ = intent.getExtras().getBoolean("qq", true);
                    m = intent.getExtras().getInt("magnification", 1000);
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
                    XposedBridge.log("传感器类型: " + ss.getType() + " 名称:" + ss.getName());
                    if (ss.getType() == Sensor.TYPE_STEP_COUNTER||ss.getType() == Sensor.TYPE_STEP_DETECTOR) {
                        XposedBridge.log("当前设置weixin: " + isWeixin + ",qq:" + isQQ + ",m=" + m + ",packagename:" + loadPackageParam.packageName);
                        if (isWeixin && loadPackageParam.packageName.equals(WEXIN)) {
                            ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] + m * WechatStepCount;
                            WechatStepCount += 1;
                            XposedBridge.log("微信计步器步数修改后   SensorEvent: x=" + ((float[]) param.args[1])[0]);
                        }
                        if (isQQ && loadPackageParam.packageName.equals(QQ)) {
                            ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] + m * QQStepCount;
                            QQStepCount += 1;
                            XposedBridge.log("QQ计步器步数修改后   SensorEvent: x=" + ((float[]) param.args[1])[0]);
                        }
                    }
                }
            });
        }
    }

    private void getKey() {
        sharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        sharedPreferences.makeWorldReadable();
        sharedPreferences.reload();
        isWeixin = sharedPreferences.getBoolean("weixin", true);
        isQQ = sharedPreferences.getBoolean("qq", true);
        m = Integer.valueOf(sharedPreferences.getString("magnification", "1000"));
    }
}
