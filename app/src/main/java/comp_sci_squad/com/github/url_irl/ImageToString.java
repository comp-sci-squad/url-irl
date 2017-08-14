

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

    public static String[] getTextFromPage(Context context, Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // Create a frame from the bitmap and run text detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> textBlockSparseArray = textRecognizer.detect(frame);

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

        // Although detector may be used multiple times for different images, it should be released
        // when it is no longer needed in order to free native resources.
        textRecognizer.release();
        return getStrings(textBlockSparseArray);
    }

    private static String[] getStrings(SparseArray<TextBlock> tester) {
        ArrayList<String> inputString = new ArrayList<>();

        Log.d(TAG, "Strings found:");
        for (int i = 0; i < tester.size(); i++) {
            Log.d(TAG, tester.valueAt(i).getValue());
            inputString.add(tester.valueAt(i).getValue());
        }
        String[] arrayStrings = new String[inputString.size()];
        return inputString.toArray(arrayStrings);
    }
}