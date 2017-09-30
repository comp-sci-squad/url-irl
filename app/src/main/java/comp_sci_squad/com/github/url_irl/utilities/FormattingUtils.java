package comp_sci_squad.com.github.url_irl.utilities;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by HBTechnoDude on 8/29/2017.
 */

public class FormattingUtils {
    /**
     * Creates a formatted time stamp.
     * Used in ListURLSActivity.
     *
     * @param timeInMillis - The time in milliseconds.
     * @return - Returns the formatted timestamp string with the user's localization.
     */
    public static String formatTimeStamp(long timeInMillis) {
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(new Date(timeInMillis));
    }
}
