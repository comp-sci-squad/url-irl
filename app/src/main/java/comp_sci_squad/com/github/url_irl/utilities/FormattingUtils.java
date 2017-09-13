package comp_sci_squad.com.github.url_irl.utilities;

import android.content.Context;

import java.text.SimpleDateFormat;
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
     * @param dateFormat - The date format as a SimpleDateFormat string.
     * @return - Returns the formatted timestamp string.
     */
    public static String formatTimeStamp(long timeInMillis, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(new Date(timeInMillis));
    }
}
