package app.jayang.icebr8k.Utility;

import java.util.Arrays;

import app.jayang.icebr8k.Model.BadWordException;

public class Icebr8kLanguageFilter {
    private static final String[] badWords = {"fuck","asshole","ass","bitch","cocksucker","cunt",
            "dick","dicksucker","faggot","motherfucker","nigger"};

    public static boolean containBadWord(String string) throws BadWordException {

        for(String word: badWords){
            if(string.toLowerCase().contains(word)){
                throw new BadWordException();
            }
        }
        return false;
    }
}
