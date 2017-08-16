package comp_sci_squad.com.github.url_irl.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import comp_sci_squad.com.github.url_irl.data.URLDatabaseContract.*;

/**
 * Created by rraym on 8/16/2017.
 */

public class URLDbHelper extends SQLiteOpenHelper {
    //Name of local file that will store our data
    private static final String DATABASE_NAME = "urlList.db";

    //Update this when database schema is modified
    //Make sure the contract is changed as well
    private static final int DATABASE_VERSION = 1;

    public URLDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sq) {
        // Create a table to hold URL list data
        final String SQL_CREATE_URLLIST_TABLE = "CREATE TABLE " +
                URLEntry.TABLE_NAME + " (" +
                URLEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                URLEntry.COLUMN_URL + " TEXT NOT NULL, " +
                URLEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "); ";

        //call execSQL on sq and pass the string query SQL_CREATE_URLLIST_TABLE
        sq.execSQL(SQL_CREATE_URLLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + URLEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
