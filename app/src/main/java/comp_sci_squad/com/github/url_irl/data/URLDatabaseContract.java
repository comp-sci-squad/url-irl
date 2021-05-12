package comp_sci_squad.com.github.url_irl.data;

import android.provider.BaseColumns;

public class URLDatabaseContract {

  public static final class URLEntry implements BaseColumns {

    public static final String TABLE_NAME = "urlList";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TIMESTAMP = "timestamp";
  }
}
