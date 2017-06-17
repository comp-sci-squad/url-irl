package comp_sci_squad.com.github.url_irl;

/**
 * @author Brent Frederisy & Brennan Tracy
 */

import android.content.res.Resources;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlParseTask extends AsyncTask<String, Void, ArrayList<Uri>> {

    @Override
    protected void onPreExecute() {
        //Start ShowURLs activity. Note should use ListAdapter
    }

    /**
     *   Returns list of urls from a body of text.
     *
     *   As a precondition: a url in the text MUST have spaces before and after it.
     *   as URLs rules allow very diverse.
     *
     *   @param textBlocks the body of text to parse for urls.
     *   @return ArrayList<String> List of urls.
     */
    @Override
    protected ArrayList<Uri> doInBackground(String... textBlocks) {
        ArrayList<Uri> urls = new ArrayList<>();
        String url_regex = context.getString(R.string.url_regex);
        Pattern urlPattern = Pattern.compile(url_regex);

        Matcher urlMatcher;
        String temp;

        for (String text : textBlocks)
        {
            urlMatcher = urlPattern.matcher(text);
            while (urlMatcher.find())
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
    protected void onPostExecute(ArrayList<Uri> uris) {
        //Add uris to ListURLS  activity
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        ///Add a URI Button to the ListURLS activity for the given URL
    }
}
