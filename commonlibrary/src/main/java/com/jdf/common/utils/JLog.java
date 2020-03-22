package com.jdf.common.utils;

import android.util.Log;

public class JLog  extends BaseLog{
    public static final String MAIN_TAG = "Camera_";

    public static final boolean ENABLE_LOG = true;

    private static int LOG_LEVEL = Log.DEBUG;


    public static void d(String tag, String msg) {
        logReally(Log.DEBUG, tag, msg);
    }

    public static void d(String tag, String msg, Object... args) {
        logReally(Log.DEBUG, tag, msg, args);
    }


    public static void i(String tag, String msg) {
        logReally(Log.INFO, tag, msg);
    }

    public static void i(String tag, String msg, Object... args) {
        logReally(Log.INFO, tag, msg,args);
    }


    public static void w(String tag, String msg) {
        logReally(Log.WARN, tag, msg);
    }

    public static void w(String tag, Throwable msg) {
        logReally(Log.WARN, tag, msg);
    }


    public static void w(String tag, String msg, Object... args) {
        logReally(Log.WARN, tag, msg, args);
    }


    public static void e(String tag, String msg) {
        logReally(Log.ERROR, tag, msg);

    }

    public static void e(String tag, String msg, Object... args) {
        logReally(Log.ERROR, tag, msg, args);
    }


    public static void v(String tag, String msg) {
        logReally(Log.VERBOSE, tag, msg);
    }

    public static void V(String tag, String msg) {
        logReally(Log.VERBOSE, tag, msg);
    }


    public static void v(String tag, String msg, Object... args) {
        logReally(Log.VERBOSE, tag, msg, args);
    }


    private static String format(String msg, Object... args) {
        return String.format(msg, args);
    }


    public static void logReally(final int level, String tag, Throwable msg) {
        try {
            if (ENABLE_LOG || LOG_LEVEL <= level) {
                switch (level) {
                    case Log.WARN:
                        Log.w(MAIN_TAG + tag, msg);
                        break;
                    case Log.ERROR:
                        Log.e(MAIN_TAG + tag, "", msg);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void logReally(final int level, String tag, String msg, Object... msgargs) {
        try {
            if (ENABLE_LOG || LOG_LEVEL <= level) {
                String showInfo = msgargs != null ? String.format(msg, msgargs) : msg;
                switch (level) {
                    case Log.VERBOSE:
                        Log.v(MAIN_TAG + tag, showInfo);
                        break;
                    case Log.DEBUG:
                        Log.d(MAIN_TAG + tag, showInfo);
                        break;
                    case Log.INFO:
                        Log.i(MAIN_TAG + tag, showInfo);
                        break;
                    case Log.WARN:
                        Log.w(MAIN_TAG + tag, showInfo);
                        break;
                    case Log.ERROR:
                        Log.e(MAIN_TAG + tag, showInfo);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
