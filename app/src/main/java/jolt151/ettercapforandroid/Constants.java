package jolt151.ettercapforandroid;

import android.os.Environment;

/**
 * Created by michael on 7/27/2018.
 */

public class Constants {
    public static String FILES_DIR = "/data/data/jolt151.ettercapforandroid/files/";
    public static String CHMOD = "chmod 777 " + FILES_DIR + "EttercapForAndroid-master/bin/ettercap";
    public static String OUTPUT_DIR = Environment.getExternalStorageDirectory() + "/EttercapForAndroid/";
}
