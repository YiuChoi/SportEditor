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
    private static final String YUEDONG = "com.yuedong.sport";
    private static final String LEDONG = "cn.ledongli.ldl";
    static int weixinCount = 0, qqCount = 0, ledongCount = 0, yuedongCount = 0;
    static boolean isWeixin, isQQ, isAuto, isLedong, isYuedong;
    XSharedPreferences sharedPreferences;
    static int m, max = Integer.MAX_VALUE;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        final Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SETTING_CHANGED);
        systemContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isWeixin = intent.getExtras().getBoolean("weixin", true);
                isQQ = intent.getExtras().getBoolean("qq", true);
                m = Integer.valueOf(intent.getExtras().getString("magnification", "100"));
                isAuto = intent.getExtras().getBoolean("autoincrement", false);
                isLedong = intent.getExtras().getBoolean("ledong", true);
                isYuedong = intent.getExtras().getBoolean("yuedong", true);

            }
        }, intentFilter);

        if (loadPackageParam.packageName.equals(WEXIN) || loadPackageParam.packageName.equals(QQ) || loadPackageParam.packageName.equals(YUEDONG) || loadPackageParam.packageName.equals(LEDONG)) {
            getKey();
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
                    if (ss.getType() == Sensor.TYPE_ACCELEROMETER) {
                        if (isLedong && loadPackageParam.packageName.equals(LEDONG)) {
                            ledongCount += 1;
                            if (ledongCount % 2 == 0) {
                                ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 100;
                                ((float[]) param.args[1])[2] += (float) -20;
                                ((float[]) param.args[1])[1] += (float) -5;
                            } else {
                                ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 10;
                                ((float[]) param.args[1])[2] += (float) 20;
                                ((float[]) param.args[1])[1] += (float) -15;
                            }
                        }
                        if (isYuedong && loadPackageParam.packageName.equals(YUEDONG)) {
                            yuedongCount += 1;
                            ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * 1000;
                            if (yuedongCount % 2 == 0) {
                                ((float[]) param.args[1])[2] += (float) -20;
                                ((float[]) param.args[1])[1] += (float) -5;
                            } else {
                                ((float[]) param.args[1])[2] += (float) 20;
                                ((float[]) param.args[1])[1] += (float) -15;
                            }
                        }
                    }
                    if (ss.getType() == Sensor.TYPE_STEP_COUNTER || ss.getType() == Sensor.TYPE_STEP_DETECTOR) {
                        if ((isWeixin && loadPackageParam.packageName.equals(WEXIN))) {
                            if (isAuto) {
                                if (m * weixinCount < max) {
                                    ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] + m * weixinCount;
                                    weixinCount += 1;
                                } else {
                                    weixinCount = 0;
                                }
                            } else {
                                ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * m;
                            }
                        }
                        if ((isQQ && loadPackageParam.packageName.equals(QQ))) {
                            if (isAuto) {
                                if (m * qqCount < max) {
                                    ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] + m * qqCount;
                                    qqCount += 1;
                                } else {
                                    qqCount = 0;
                                }
                            } else {
                                ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * m;
                            }
                        }
                        if ((isYuedong && loadPackageParam.packageName.equals(YUEDONG)) || (isLedong && loadPackageParam.packageName.equals(LEDONG))) {
                            ((float[]) param.args[1])[0] = ((float[]) param.args[1])[0] * m;
                        }
                        XposedBridge.log(loadPackageParam.packageName + "修改后：" + ((float[]) param.args[1])[0]);
                    }
                }
            });
        }
    }

    private void getKey() {
        sharedPreferences.reload();
        isWeixin = sharedPreferences.getBoolean("weixin", true);
        isQQ = sharedPreferences.getBoolean("qq", true);
        m = Integer.valueOf(sharedPreferences.getString("magnification", "100"));
        isAuto = sharedPreferences.getBoolean("autoincrement", false);
        isLedong = sharedPreferences.getBoolean("ledong", true);
        isYuedong = sharedPreferences.getBoolean("yuedong", true);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        sharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
    }
}
