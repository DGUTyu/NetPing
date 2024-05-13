package com.example.net.util;

import android.text.TextUtils;
import android.util.Log;


import com.example.net.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : huangdh
 * @version :13-7-12
 * @Copyright : copyrights reserved by personal 20012-2013
 * @see : 日志管理
 */
public class Logger {

    private static final String defaultTag = "Logger";
    private static Boolean MYLOG_SWITCH = false; // 发布的时候设置false ,日志文件总开关
    private static Boolean MYLOG_WRITE_TO_FILE = true;// 日志写入文件开关
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd卡中日志文件的最多保存天数
    private static String MYLOGFILEName = "logger.txt";// 本类输出的日志文件名称
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式

    /**
     * 记录一条警告异常
     */
    public static void w(String tag, Object msg) { // 警告信息
        log(tag, msg.toString(), LogType.WARNING);
    }

    public static void w(String msg, Throwable throwable) {
        log(null, msg, throwable, LogType.WARNING);
    }

    public static void w(Throwable throwable) {
        log(null, null, throwable, LogType.WARNING);
    }

    public static void w(String tag, String text) {
        log(tag, text, LogType.WARNING);
    }

    ////////////////记录一条错误///////////////////
    public static void e(String tag, String text) {
        log(tag, text, LogType.ERROR);
    }

    public static void e(String tag, Object msg) { // 错误信息
        log(tag, msg.toString(), LogType.ERROR);
    }

    public static void e(String msg, Throwable throwable) {
        log(null, msg, throwable, LogType.ERROR);
    }

    public static void e(Throwable throwable) {
        log(null, null, throwable, LogType.ERROR);
    }

    //////////////////////////////////////////
    public static void d(String tag, Object msg) {// 调试信息
        log(tag, msg.toString(), LogType.DEBUG);
    }

    public static void d(Object msg) {// 调试信息
        log(null, msg.toString(), LogType.DEBUG);
    }

    public static void d(String tag, String text) {
        log(tag, text, LogType.DEBUG);
    }

    public static void i(Object msg) {//
        log(null, msg.toString(), LogType.INFO);
    }

    public static void i(String tag, Object msg) {//
        log(tag, msg.toString(), LogType.INFO);
    }

    public static void i(String tag, String text) {
        log(tag, text, LogType.INFO);
    }

    public static void v(String tag, Object msg) {
        log(tag, msg.toString(), LogType.VERBOSE);
    }

    public static void v(String tag, String text) {
        log(tag, text, LogType.VERBOSE);
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param msg
     * @param throwable
     * @param level
     */
    private static void log(String tag, String msg, Throwable throwable, LogType level) {
        if (!TextUtils.isEmpty(msg)) {
            log(tag, msg, level);
        }
        if (throwable != null) {
            //调试时候直接print
            if (BuildConfig.DEBUG) {
                throwable.printStackTrace();
                return;
            }
            StackTraceElement[] stes = throwable.getStackTrace();
            if (stes != null) {
                for (StackTraceElement ste : stes) {
                    /* StringBuilder sb = new StringBuilder();
                     String fileName = ste.getFileName();
                     String lineNumber = ste.getLineNumber()+"";*/
                    log(tag, ste.toString(), level);
                }
            }
        }
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     */
    private static void log(String tag, String msg, LogType level) {
        if (tag == null) {
            tag = defaultTag;
        }
        if (BuildConfig.DEBUG) {
            if (LogType.ERROR == level) { // 输出错误信息
                Log.e(tag, msg);
            } else if (LogType.WARNING == level) {
                Log.w(tag, msg);
            } else if (LogType.DEBUG == level) {
                Log.d(tag, msg);
            } else if (LogType.INFO == level) {
                Log.i(tag, msg);
                return;
            } else {
                Log.v(tag, msg);
            }
        }

       /* if (GlobalConstant.isDebug) {
            writeLogtoFile(String.valueOf(level), tag, msg);
        }*/
    }

    /**
     * 日志类别
     */
    public static enum LogType {
        // Log.v
        VERBOSE,
        // Log.d
        DEBUG,
        // Log.i
        INFO,
        // Log.w
        WARNING,
        // Log.e
        ERROR;
    }

}
