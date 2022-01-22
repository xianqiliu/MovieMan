package fr.isep.ii3510.movieman.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTime {

    public static String getDateTime(String date, Integer runtime) {
        String res = "";

        if (date != null && !date.trim().isEmpty()) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            SimpleDateFormat sdf2 = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            try {
                Date releaseDate = sdf1.parse(date);
                if (releaseDate != null) res += "Release Date: " + sdf2.format(releaseDate) + "\n";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            res = "-\n";
        }

        if (runtime != null && runtime != 0) res += "Runtime: " + runtime + " min";
        else res += "-";

        return res;
    }

}
