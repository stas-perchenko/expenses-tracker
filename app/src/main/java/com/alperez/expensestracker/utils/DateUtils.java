package com.alperez.expensestracker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by stanislav.perchenko on 12-Sep-15.
 */
public class DateUtils {



    private final static SimpleDateFormat FORMAT_SQL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.US);

    public static Date parseSqlDate(String date) throws ParseException {
        Date result;
        synchronized (DateUtils.class) {
            result = FORMAT_SQL.parse(date);
        }
        return result;
    }

    public static String formatSqlDate(Date date) {
        String result;
        synchronized (DateUtils.class) {
            result = FORMAT_SQL.format(date);
        }
        return result;
    }

}
