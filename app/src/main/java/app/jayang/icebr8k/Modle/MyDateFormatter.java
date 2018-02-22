package app.jayang.icebr8k.Modle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yjj781265 on 2/20/2018.
 */

public class MyDateFormatter {
    private final static String  bullet = " \u2022 ";

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isSameYear(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameYear(cal1, cal2);
    }

    public static boolean isSameYear(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }

    public static boolean isToday(Calendar calendar) {
        return isSameDay(calendar, Calendar.getInstance());
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    public static boolean isYesterday(Calendar calendar) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return isSameDay(calendar, yesterday);
    }

    public static boolean isYesterday(Date date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return isSameDay(date, yesterday.getTime());
    }

    public static boolean isCurrentYear(Date date) {
        return isSameYear(date, Calendar.getInstance().getTime());
    }

    public static boolean isCurrentYear(Calendar calendar) {
        return isSameYear(calendar, Calendar.getInstance());
    }

    public static String timeStampToDateConverter(long timeStamp, boolean dateHeader) {
        Date date = new Date(timeStamp);
        if (dateHeader) {
            if (isToday(date)) {
                return "Today";
            } else if (isYesterday(date)) {
                return "Yesterday";
            } else {
                String pattern = "MM/dd/yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                return simpleDateFormat.format(date);
            }
        } else {
            String pattern = "h:mm a";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String time =simpleDateFormat.format(date);

            String pattern2 = "MM/dd/yyyy";
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(pattern2);
            String dateStr = simpleDateFormat2.format(date);

            String pattern3 = "MM/dd";
            SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat(pattern3);
            String dateStrSameYear = simpleDateFormat3.format(date);

            if(isYesterday(date)) {
                return "Yesterday" + bullet + time;
            }else if(isToday(date)) {
                return  time;
            }else if(isSameYear(date,new Date())){
                return dateStrSameYear + bullet + time;
            }else{
                return dateStr+bullet+time;
            }

        }
    }


}
