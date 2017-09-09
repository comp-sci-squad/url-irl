package comp_sci_squad.com.github.url_irl;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comp_sci_squad.com.github.url_irl.data.URLDbHelper;

public class ListURLsActivity extends AppCompatActivity implements UriAdapter.ListItemClickListener {

    private static final String TAG = "ListURLSActivity";
    private UriAdapter mAdapter;
    private RecyclerView recyclerView;
    private String[] mStringBlocks;
    private ProgressBar mLoadingIndicator;
    private ImageView mImageView;
    private byte[] urlScanImage;
    private Toolbar mToolBar;
    public URLDbHelper dbs;
    public ArrayList<String> URLHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_urls);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);

        dbs = new URLDbHelper(this.getBaseContext());

        Log.d(TAG, "On create " + TAG);

        Intent sourceIntent = getIntent();
        if (sourceIntent != null && sourceIntent.hasExtra(getString(R.string.URI_ARRAY_LIST))) {
            mStringBlocks = sourceIntent.getStringArrayExtra(getString(R.string.URI_ARRAY_LIST));

            urlScanImage = sourceIntent.getByteArrayExtra("bitmapBytes");
        }

        Log.d(TAG, "Creating Recycler view, progress bar, layout manager, and URIADapter");
        recyclerView = (RecyclerView) findViewById(R.id.rv_id);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        mImageView = (ImageView) findViewById(R.id.image_thumbnail);
        mImageView.setImageBitmap(BitmapFactory.decodeByteArray(urlScanImage, 0, urlScanImage.length));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        mAdapter = new UriAdapter(this);
        recyclerView.setAdapter(mAdapter);

        //getURLHistory();
        //insertURLsIntoDbs();

        //Converts arraylist<string> to var args and runs the async task
        Log.d(TAG, "Starting UrlParseTask");
       new UrlParseTask().execute(mStringBlocks);
    }

    /**
     * Functions to access the URLDbsHelper
     * -created by Kevin
     */

    public void getURLHistory()
    {
        Cursor temp = dbs.getURLFromDbs();

        while(temp.moveToNext())
        {
            URLHistory.add(temp.getString(0));
        }
    }

    /**
     * Needs the mTimeStamp
     */
    public void insertURLsIntoDbs()
    {
     //   boolean value = dbs.insertURLs(mStringBlocks, mTimestamp.toString());
    }

    public static Intent newIntent(Context packageContext, String[] stringList) {
        Log.d(TAG, "Getting Intent");

        Intent i = new Intent(packageContext, ListURLsActivity.class);
        i.putExtra(packageContext.getString(R.string.URI_ARRAY_LIST), stringList);

        return i;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Log.d(TAG, "onListItemClick");
        Intent i = new Intent(Intent.ACTION_VIEW, mAdapter.getUri(clickedItemIndex));
        startActivity(i);
    }

    @Override
    public void onShareButtonClick(int clickedItemIndex) {
        Log.d(TAG, "onShareButtonClick");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        //shareIntent.putExtra(Intent.EXTRA_SUBJECT, "R.string.sharing_url_subject");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mAdapter.getUri(clickedItemIndex).toString());
        startActivity(shareIntent);
    }

    @Override
    public void onSearchButtonClick(int clickedItemIndex) {
        Log.d(TAG, "onSearchButtonClick");
        String googleSearchUrl = "https://www.google.com/search?q=";
        Uri searchUri = Uri.parse("https://www.google.com/search?q=" + mAdapter.getUri(clickedItemIndex).getHost());
        Intent searchIntent = new Intent(Intent.ACTION_VIEW, searchUri);
        startActivity(searchIntent);
    }

    /**
     * @author Brent Frederisy & Brennan Tracy
     */
    public class UrlParseTask extends AsyncTask<String, Void, ArrayList<Uri>> {

        @Override
        protected void onPreExecute() {
            if (!isCancelled()) {
                mLoadingIndicator.setVisibility(View.VISIBLE);
            }
        }

        /**
         *   Returns list of urls from a body of text.
         *
         *   As a precondition: a url in the text MUST have spaces before and after it.
         *   as URLs rules allow very diverse strings.
         *
         *   @param textBlocks the blocks of text to parse for urls.
         *   @return ArrayList<String> List of urls.
         */
        @Override   
        protected ArrayList<Uri> doInBackground(String... textBlocks) {
            ArrayList<Uri> urls = new ArrayList<>();

            String url_regex = getString(R.string.url_regex);
            Pattern urlPattern = Pattern.compile(url_regex);

            Matcher urlMatcher;
            String temp;

            for (int i = 0; i < textBlocks.length && !isCancelled(); i++)
            {
                urlMatcher = urlPattern.matcher(textBlocks[i]);
                while (urlMatcher.find() && !isCancelled())
                {
                    temp = urlMatcher.group();
                    if (!temp.startsWith("http://") && !temp.startsWith("https://"))
                        temp = "http://" + temp;
                    urls.add(Uri.parse(temp));
                }
            }

            return urls;
        }

        @Override
        protected void onPostExecute(ArrayList<Uri> urls) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Loaded " + urls.size() + " URLS", Toast.LENGTH_SHORT).show();
            mAdapter.setArray(urls);
        }
    }

}
