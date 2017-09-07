package comp_sci_squad.com.github.url_irl.utilities;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by HBTechnoDude on 8/29/2017.
 */

public class FormattingUtils {
    public static String  formatTimeStamp(long dateInMillis, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(new Date(dateInMillis));
    }
}
