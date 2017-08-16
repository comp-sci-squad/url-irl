package comp_sci_squad.com.github.url_irl.data;

import android.provider.BaseColumns;

/**
 * Created by rraym on 8/16/2017.
 */

public class URLDatabaseContract {

    public static final class WaitlistEntry implements BaseColumns {
        public static final String TABLE_NAME = "urlList";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
