package comp_sci_squad.com.github.url_irl;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Brongan on 6/16/2017.
 */

public class ParserTest {

    @Test
    public void testParseUrls() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        //Generate test data
        ArrayList<String> test = new ArrayList<String>(Arrays.asList(
                "a d google.com d ", "a d www.hbbrennan.github.io", "https://reddit.org", "corn bread", "google.com/search?q=hi&ie=utf-8&oe=utf-8"));
        ArrayList<String> correctOutput =  new ArrayList<>(Arrays.asList(
                "http://google.com", "http://www.hbbrennan.github.io", "https://reddit.org", "http://google.com/search?q=hi&ie=utf-8&oe=utf-8"));
       // ArrayList<Uri> output = Parser.parseURLs(test, appContext);

        //First assert size then test each element like usual testing.
        assertEquals(output.size(), correctOutput.size());
        int outputSize = output.size();

        //Get might be bad for list type. Probably use iterators
        for (int i = 0; i < outputSize; i++)
        {
            assertEquals(output.get(i).toString(), correctOutput.get(i));
        }
    }

}
