package app.jayang.icebr8k.Modle;

/**
 * Created by yjj781265 on 3/18/2018.
 */

public class Birthdate {
    private int year = 2018 , month,day;

    public Birthdate() {
    }

    public Birthdate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Birthdate{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}

