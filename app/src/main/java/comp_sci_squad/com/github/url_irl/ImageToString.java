
package comp_sci_squad.com.github.url_irl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;

/**
 * Created by brentfred on 6/4/17.
 * Edited by Kevin on 6/25/17
 */

public class ImageToString {
    private static final String TAG = "ImageToString";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static ArrayList<String> inputString;

    private static String[] arrayStrings;

    public static String[] getTextFromPage(Context context, TextBlockView overlay, Bitmap bitmap)
    {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // Create a frame from the bitmap and run text detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock>  textBlockSparseArray = textRecognizer.detect(frame);

        if (!textRecognizer.isOperational()) {
            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = context.registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(context, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, context.getString(R.string.low_storage_error));
            }
        }

        //TextBlockView overlay = (TextBlockView) findViewById(R.id.faceView);
        overlay.setContent(bitmap, textBlockSparseArray);

        // Although detector may be used multiple times for different images, it should be released
        // when it is no longer needed in order to free native resources.
        textRecognizer.release();
        getStrings(textBlockSparseArray);

        return arrayStrings;
    }

    private static void getStrings(SparseArray<TextBlock> tester)
    {
        inputString = new ArrayList<String>();

        for(int i = 0; i < tester.size(); i++)
            inputString.add(tester.valueAt(i).getValue());

        arrayStrings = new String[inputString.size()];
        inputString.toArray(arrayStrings);
    }
}
