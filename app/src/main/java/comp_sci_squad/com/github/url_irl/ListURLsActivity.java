package comp_sci_squad.com.github.url_irl;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comp_sci_squad.com.github.url_irl.utilities.FormattingUtils;

import static comp_sci_squad.com.github.url_irl.MainActivity.PICTURE_EXTRA;
import static comp_sci_squad.com.github.url_irl.MainActivity.TIME_EXTRA;

public class ListURLsActivity extends AppCompatActivity implements UriAdapter.ListItemClickListener {

    /**
     * Logging Tag
     */
    private static final String TAG = "ListURLSActivity";

    /**
     * UI Elements
     */
    private ProgressBar mLoadingIndicator;
    private ImageView mImageView;
    private Toolbar mToolBar;
    private TextView mTimestamp;

    /**
     * Intent Extras
     */
    private ArrayList<String> mStringBlocks;
    private byte[] mURLScanThumbnail;
    private long mTimePictureTaken;

    /**
     * Data Storage Elements
     */
    private UriAdapter mAdapter;
    private RecyclerView recyclerView;

    /**
     * Sharing
     */
    private ShareActionProvider mShareActionProvider;
    private Intent mShareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_urls);
        Log.d(TAG, "onCreate()");

        Runtime.getRuntime().gc();

        //Get member variables from intent
        Intent sourceIntent = getIntent();
        if (sourceIntent != null && sourceIntent.hasExtra(getString(R.string.URI_ARRAY_LIST))) {
            mStringBlocks = sourceIntent.getStringArrayListExtra(getString(R.string.URI_ARRAY_LIST));

            mURLScanThumbnail = sourceIntent.getByteArrayExtra(PICTURE_EXTRA);
            mTimePictureTaken = sourceIntent.getLongExtra(TIME_EXTRA, 0);
        }

        //Set UI member variables and data storage elements
        Log.d(TAG, "Assigning Member variables");
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        recyclerView = (RecyclerView) findViewById(R.id.rv_id);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        mTimestamp = (TextView) findViewById(R.id.timestamp);
        mTimestamp.setText(FormattingUtils.formatTimeStamp(mTimePictureTaken, getString(R.string.timestamp_format_pattern)));
        mImageView = (ImageView) findViewById(R.id.image_thumbnail);
        GlideApp.with(this).
                load(mURLScanThumbnail).
                diskCacheStrategy(DiskCacheStrategy.NONE).
                skipMemoryCache(true).
                into(mImageView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        mAdapter = new UriAdapter(this);
        recyclerView.setAdapter(mAdapter);

        //Converts arraylist<string> to var args and runs the async task
        Log.d(TAG, "Starting UrlParseTask");

        new UrlParseTask().execute(mStringBlocks.toArray(new String[mStringBlocks.size()]));

    }

    /**
     * Create an options menu. It will create
     *  - Share All Button
     * @param menu - The Options Menu being created.
     * @return - True because it handled the menu creation.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Creating Options Menu");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_urls, menu);
        return true;
    }

    /**
     * Handles menu items being selected.
     * If the item was share_all, start the share chooser with all the urls.
     * The super class handles the back button.
     * @param item - The item selected.
     * @return - Returns true if successfully handled or calls super method.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Options Item Selected");
        switch (item.getItemId()) {
            case R.id.share_all:
                // Share the URLS
                if (mShareIntent != null)
                    startActivity(Intent.createChooser(mShareIntent, "Share using"));
                else
                    Log.e(TAG, "Share intent was null");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public static Intent newIntent(Context packageContext, ArrayList<String> stringListExtra, byte[] thumbnailExtra, long timePictureTakenExtra) {
        Log.d(TAG, "Creating intent");

        Intent intent = new Intent(packageContext, ListURLsActivity.class);
        intent.putExtra(packageContext.getString(R.string.URI_ARRAY_LIST), stringListExtra);
        intent.putExtra(PICTURE_EXTRA, thumbnailExtra);
        intent.putExtra(TIME_EXTRA, timePictureTakenExtra);

        return intent;
    }

    /**
     * Opens the link that is clicked on.
     * @param clickedItemIndex - The adapter position of the url that is clicked on.
     */
    @Override
    public void onListItemClick(int clickedItemIndex) {
        Log.d(TAG, "onListItemClick");
        Intent i = new Intent(Intent.ACTION_VIEW, mAdapter.getUri(clickedItemIndex));
        startActivity(i);
    }

    /**
     * Creates a sharing chooser for the link that of the button that was clicked on.
     * @param clickedItemIndex - The adapter position of the url that is clicked on.
     */
    @Override
    public void onShareButtonClick(int clickedItemIndex) {
        Log.d(TAG, "onShareButtonClick");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        //mShareIntent.putExtra(Intent.EXTRA_SUBJECT, "R.string.sharing_url_subject");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mAdapter.getUri(clickedItemIndex).toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_label)));
    }

    /**
     * Opens a google search with the query being the link of the button that was clicked on.
     * @param clickedItemIndex - The adapter position of the url that is clicked on.
     */
    @Override
    public void onSearchButtonClick(int clickedItemIndex) {
        Log.d(TAG, "onSearchButtonClick");
        String googleSearchUrl = "https://www.google.com/search?q=";
        Uri searchUri = Uri.parse(googleSearchUrl + mAdapter.getUri(clickedItemIndex).getHost());
        Intent searchIntent = new Intent(Intent.ACTION_VIEW, searchUri);
        startActivity(searchIntent);
    }

    /**
     * Copies the link that was long clicked on to the clipboard.
     * @param clickedItemIndex - The adapter position of the url that is clicked on.
     */
    @Override
    public void onListItemLongClick(int clickedItemIndex) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("URL", mAdapter.getUri(clickedItemIndex).toString());
        clipboardManager.setPrimaryClip(clip);
        Toast copyToast = Toast.makeText(this, R.string.copy_to_clipboard, Toast.LENGTH_SHORT);
        copyToast.show();
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
         *   Called automatically by the async task.
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

        /**
         * Sets the list of URLs scanned to the UI.
         *
         * Sets the intent for the share all button with the URLs.
         * Gives all the URLs to the recycle view's adapter.
         * Called automatically by the async task.
         *
         * @param urls - An arraylist of parsed urls prepended with http://
         */
        @Override
        protected void onPostExecute(ArrayList<Uri> urls) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            // Share intent
            String shareString = "";
            for (Uri uri : urls)
                shareString += uri.toString() + "\n";
            mShareIntent = new Intent();
            mShareIntent.setAction(Intent.ACTION_SEND);
            mShareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
            mShareIntent.setType("text/plain");

            Toast.makeText(getApplicationContext(), "Loaded " + urls.size() + " URLS", Toast.LENGTH_SHORT).show();
            mAdapter.setArray(urls);
        }
    }

}
