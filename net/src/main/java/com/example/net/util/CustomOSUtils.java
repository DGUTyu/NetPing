package com.example.net.util;

import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * 存在的问题有：
 * <p>
 * VIVO手机会全部识别成Funtouch，识别不到OriginOS
 * realme手机会全部识别成ColorOS, 识别不到realme ui
 * 一加手机会全部识别成HydrogeOS， 没有遇到过OxygenOS
 * 三星手机未作识别，会返回原生安卓系统
 */
public class CustomOSUtils {
    /**
     * HarmonyOS 系统输出的
     * 格式：2.0.0
     */
    private static final String KEY_HARMONYOS_VERSION_NAME = "hw_sc.build.platform.version";
    /**
     * EMUI系统输出的
     * 格式：EmotionUI_8.0.0
     */
    private static final String KEY_EMUI_VERSION_NAME = "ro.build.version.emui";
    /**
     * MagicUI系统输出的
     * 格式：3.1.0
     */
    private static final String KEY_MAGICUI_VERSION = "ro.build.version.magic";
    /**
     * MIUI系统输出的
     * 格式：V12
     */
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    /**
     * OPPO手机ColorOS系统输出的
     * 格式：9
     */
    private static final String KEY_COLOROS_VERSION_NAME = "ro.build.version.opporom";
    /**
     * VIVO手机系统输出的
     * name格式：funtouch
     * version格式： 9
     */
    private static final String KEY_VIVO_VERSION_NAME = "ro.vivo.os.name";
    private static final String KEY_VIVO_VERSION = "ro.vivo.os.version";
    /**
     * OonPlus手机系统输出的
     * 格式：Hydrogen OS 11.0.7.10.KB05
     */
    private static final String KEY_ONEPLUS_VERSION_NAME = "ro.rom.version";
    /**
     * 魅族手机系统输出的
     */
    private static final String KEY_FLYME_VERSION_NAME = "ro.build.display.id";
    /**
     * nubia手机系统输出的
     */
    private static final String KEY_NUBIA_VERSION_NAME = "ro.build.nubia.rom.name";
    private static final String KEY_NUBIA_VERSION_CODE = "ro.build.nubia.rom.code";
    /**
     * customOS默认值为"",如果识别出的手机厂商是预知的，会被重新赋值,如果未识别到该机型则返回原生安卓信息
     */
    private static String customOS = "";
    /**
     * CustomOSVersion默认值为"",如果识别出的手机厂商是预知的，会被重新赋值成对应rom系统的版本号
     * 如果未识别到该机型则返回原生安卓信息
     */
    private static String customOSVersion = "";

    /**
     * 传入获取手机系统属性的key，可以得到rom系统版本信息
     *
     * @param key
     * @return
     */
    private static String getSystemPropertyValue(String key) {
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            String value = (String) getMethod.invoke(classType, new Object[]{key});
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断是否是华为鸿蒙系统，能否识别荣耀鸿蒙未知
     *
     * @return
     */
    private static boolean isHarmonyOS() {
        try {
            Class<?> classType = Class.forName("com.huawei.system.BuildEx");
            Method getMethod = classType.getMethod("getOsBrand");
            String value = (String) getMethod.invoke(classType);
            return !TextUtils.isEmpty(value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getHarmonySystemPropertyValue() {
        try {
            Class<?> classType = Class.forName("com.huawei.system.BuildEx");
            Method getMethod = classType.getMethod("getOsBrand");
            String value = (String) getMethod.invoke(classType);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过手机品牌信息获取手机rom系统+系统版本号
     *
     * @param phoneBrand
     * @return
     */
    public static String getPhoneSystem(String phoneBrand) {
        if (TextUtils.isEmpty(customOS)) {
            setCustomOSInfo(phoneBrand);
        }
        return customOS + customOSVersion;
    }

    private static boolean isMagicUI() {
        return false;
    }

    /**
     * 通过手机品牌信息获取手机rom系统
     *
     * @param phoneBrand
     * @return
     */
    public static String getCustomOS(String phoneBrand) {
        if (TextUtils.isEmpty(customOS)) {
            setCustomOSInfo(phoneBrand);
        }
        return customOS;
    }

    /**
     * 通过手机品牌信息获取手机rom系统版本号
     *
     * @param phoneBrand
     * @return
     */
    public static String getCustomOSVersion(String phoneBrand) {
        if (TextUtils.isEmpty(customOS)) {
            setCustomOSInfo(phoneBrand);
        }
        return customOSVersion;
    }

    /**
     * 通过手机品牌信息获取手机rom系统版本号，截取成大版本，比如2.0.0截成2
     *
     * @param phoneBrand
     * @return
     */
    public static String getCustomOSVersionSimple(String phoneBrand) {
        String customOSVersionSimple = customOSVersion;
        if (TextUtils.isEmpty(customOS)) {
            getCustomOSVersion(phoneBrand);
        }
        if (customOSVersion.contains(".")) {
            int index = customOSVersion.indexOf(".");
            customOSVersionSimple = customOSVersion.substring(0, index);
        }
        return customOSVersionSimple;
    }

    /**
     * 删除字符串中的空格并全部转成大写
     *
     * @param str
     * @return
     */
    public static String deleteSpaceAndToUpperCase(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return str.replaceAll(" ", "").toUpperCase();
    }

    private static void setCustomOSInfo(String phoneBrand) {
        try {
            switch (deleteSpaceAndToUpperCase(phoneBrand)) {
                case "HUAWEI":
                    if (isHarmonyOS()) {
                        customOSVersion = getSystemPropertyValue(KEY_HARMONYOS_VERSION_NAME);
                        customOS = "HarmonyOS";
                    } else {
                        customOS = "EMUI";
                        customOSVersion = getSystemPropertyValue(KEY_EMUI_VERSION_NAME);
                        ;
                    }
                    break;
                case "HONOR":
                    if (isHarmonyOS()) {
                        customOS = "HarmonyOS";
                        if (!TextUtils.isEmpty(getSystemPropertyValue(KEY_HARMONYOS_VERSION_NAME))) {
                            customOSVersion = getSystemPropertyValue(KEY_HARMONYOS_VERSION_NAME);
                            ;
                        } else {
                            customOSVersion = "";
                        }
                    } else if (!TextUtils.isEmpty(getSystemPropertyValue(KEY_MAGICUI_VERSION))) {
                        customOS = "MagicUI";
                        customOSVersion = getSystemPropertyValue(KEY_MAGICUI_VERSION);
                    } else {
                        //格式：EmotionUI_8.0.0
                        customOS = "EMUI";
                        customOSVersion = getSystemPropertyValue(KEY_EMUI_VERSION_NAME);
                    }
                    break;
                case "XIAOMI":
                case "REDMI":
                    //格式：MIUIV12
                    customOS = "MIUI";
                    customOSVersion = getSystemPropertyValue(KEY_MIUI_VERSION_NAME);
                    break;
                case "REALME":
                case "OPPO":
                    //格式：ColorOSV2.1
                    customOS = "ColorOS";
                    customOSVersion = getSystemPropertyValue(KEY_COLOROS_VERSION_NAME);
                    break;
                case "VIVO":
                    //格式：Funtouch9
                    customOS = "Funtouch";
                    customOSVersion = getSystemPropertyValue(KEY_VIVO_VERSION);
                    break;
                case "ONEPLUS":
                    //格式：Hydrogen OS 11.0.7.10.KB05
                    customOS = "HydrogenOS";
                    customOSVersion = getSystemPropertyValue(KEY_ONEPLUS_VERSION_NAME);
                    break;
                case "MEIZU":
                    //格式:Flyme 6.3.5.1G
                    customOS = "Flyme";
                    customOSVersion = getSystemPropertyValue(KEY_FLYME_VERSION_NAME);
                    break;
                case "NUBIA":
                    //格式:nubiaUIV3.0
                    customOS = getSystemPropertyValue(KEY_NUBIA_VERSION_NAME);
                    customOSVersion = getSystemPropertyValue(KEY_NUBIA_VERSION_CODE);
                    break;
                default:
                    customOS = "Android";
                    customOSVersion = Build.VERSION.RELEASE;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

