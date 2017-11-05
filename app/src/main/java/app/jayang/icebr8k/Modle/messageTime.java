package app.jayang.icebr8k.Modle;


import android.icu.util.Calendar;
import android.text.format.DateFormat;

/**
 * Created by LoLJay on 11/3/2017.
 */

public class messageTime {
    public String getTimeStamp(){
        String timeStamp,time;
        Calendar calendar = Calendar.getInstance();
        int dayInt = calendar.get(Calendar.DAY_OF_WEEK);
        String delegate = "hh:mm aaa";
        time= DateFormat.format(delegate,Calendar.getInstance().getTime()).toString();
        switch (dayInt) {
            case Calendar.SUNDAY:
                timeStamp="Sun" +time;
                break;

            case Calendar.MONDAY:
                timeStamp="Mon " +time;
                break;



            case Calendar.TUESDAY:
                timeStamp="Tue " +time;
                break;

            case Calendar.WEDNESDAY:
                timeStamp="Wed " +time;
                break;

            case Calendar.THURSDAY:
                timeStamp="Thur " +time;
                break;


            case Calendar.FRIDAY:
                timeStamp="Fri " +time;
                break;


            case Calendar.SATURDAY:
                timeStamp="Sat " +time;
                break;

            default:
                timeStamp = time;

        }

     return timeStamp;
    }

}
