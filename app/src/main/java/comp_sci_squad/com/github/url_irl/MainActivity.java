package comp_sci_squad.com.github.url_irl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.InputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This will take a picture from the resources raw in the APK and convert it to a bitmap
        // which the TextRecognizer can reads to create a SparseArray of TextBlock
        InputStream stream = getResources().openRawResource(R.raw.android_test_large);
        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        String[] allText = ImageToString.getTextFromPage(this,  bitmap);

        // Start the intent to get a List of Strings from the image
        Intent i = new ListURLsActivity().newIntent(MainActivity.this, allText);
        startActivity(i);
    }
}
