package ru.nettrash.sibliteandroid;

import org.jetbrains.annotations.Contract;

import java.util.Date;

/**
 * Created by nettrash on 09.02.2018.
 */

final class Variables {

    public static String lastStoppedActivityClassName = "";
    public static Date lastStopDate = null;
    public static int MAX_INACTIVE_SECONDS = 15;
    public static boolean runFirstLogin = true;
    public static Double commissionDefault = 0.001;

    @Contract(pure = true)
    public static boolean needCheckPIN() {
        if (runFirstLogin) return true;
        if (lastStopDate == null) return false;
        Date d = new Date();
        return (d.getTime() - lastStopDate.getTime()) / 1000 > MAX_INACTIVE_SECONDS;
    }
}
