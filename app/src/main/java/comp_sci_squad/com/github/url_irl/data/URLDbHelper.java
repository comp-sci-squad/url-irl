package comp_sci_squad.com.github.url_irl.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    /**
     *
     * @param  - a list of URL's from the picture
     * @param  - the current time that urls' were taken
     * @return - returns true if data was successfully stored
     */
    public boolean insertURLs(ArrayList<String> urlList, String timeStamp){
        SQLiteDatabase dbs = this.getWritableDatabase();

        for(int i = 0; i < urlList.size(); i++)
        {
            ContentValues values = new ContentValues();
            values.put("url", urlList.get(i));
            values.put("timestamp", timeStamp);

            long result = dbs.insert( "urlList", null, values);

            if(result == -1)return false;
        }
        return true;
    }

    /**
     *
     * @return a Cursor that can be extracted to get data from the database
     */
    public Cursor getURLFromDbs()
    {
        SQLiteDatabase dbs = this.getWritableDatabase();
        Cursor reg = dbs.rawQuery("SELECT * FROM " + URLEntry.TABLE_NAME, null);
        return reg;
    }

    /**
     *
     * @param URLName - the time that you want to be deleted
     * @return - returns true if the item was deleted and no errors occurred
     */
    public boolean deleteByURL(String URLName)
    {
        SQLiteDatabase dbs = this.getWritableDatabase();
        dbs.delete(URLEntry.TABLE_NAME,  URLEntry.COLUMN_URL + " = ?" , new String[]{URLName});
        return true;
    }


    /**
     * @param dateToDelete to be deleted
     * @return true if delete was successful
     * edit for future --> delete every url that is before a certain time stampt
     */

   private boolean deleteByTimeStamp(Date dateToDelete)
    {
        SQLiteDatabase dbs = this.getWritableDatabase();
        dbs.delete(URLEntry.TABLE_NAME,  URLEntry.COLUMN_TIMESTAMP + " = ?" , new String[]{dateToDelete.toString()});
        return true;
    }


    /**
     *
     * @param time that is used to be deleted... format must be in yyyy-MM-dd
     */
    public void deleteAtTimeStamp(String time)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dt;
        try {
             dt = dateFormat.parse(time);
             deleteByTimeStamp(dt);
        }catch(ParseException ex) {
            ex.printStackTrace();
        }
    }


}

