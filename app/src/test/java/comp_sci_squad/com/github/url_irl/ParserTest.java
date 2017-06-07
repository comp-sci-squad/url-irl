package comp_sci_squad.com.github.url_irl;

import org.junit.Test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Brent Frederisy and Brennan Tracy
 */
public class ParserTest {
    @Test
    public void parseURLs() throws Exception {
        ArrayList<String> expected = new ArrayList<>(Arrays.asList("www.google.com", "www.reddit.com"));
        assertEquals(expected, Parser.parseURLs("adfalsdhttp://www.google.com cornbread asdfasd www.reddit.com alsdkj"));
    }

}