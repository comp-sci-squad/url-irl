package comp_sci_squad.com.github.url_irl;

/**
 * @author Brent Frederisy & Brennan Tracy
 */

import android.content.res.Resources;
import android.net.Uri;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final String url_regex = "\\b(?<![@.,%&#-])(\\w{2,10}:\\/\\/)?((?:\\w|\\&\\#\\d{3,5};)[.-]?)+\\.([a-z]{2,15})\\b(?![@])(\\/)?(?:([\\w\\d\\?\\-=#:%@&.;])+(?:\\/(?:([\\w\\d\\?\\-=#:%@&;.])+))*)?(?<![.,?!-])";

    /**
     *   Returns list of urls from a body of text.
     *
     *   As a precondition: a url in the text MUST have spaces before and after it.
     *   as URLs rules allow very diverse.
     *
     *   @param textBlocks the body of text to parse for urls.
     *   @return ArrayList<String> List of urls.
     */
    public static ArrayList<Uri> parseURLs(ArrayList<String> textBlocks)
    {
        ArrayList<Uri> urls = new ArrayList<>();

        //String url_regex = Resources.getSystem().getString(R.string.url_regex);
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
}
