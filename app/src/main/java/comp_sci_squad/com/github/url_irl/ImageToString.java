package comp_sci_squad.com.github.url_irl;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;

/**
 * Created by brentfred on 6/4/17.
 */

public class ImageToString {
    public static ArrayList<String> convertImageToText(Context context, Bitmap picture) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        Frame image = new Frame.Builder().setBitmap(picture).build();

        SparseArray<TextBlock> text = textRecognizer.detect(image);
        ArrayList<String> result = new ArrayList<>();

        int key;
        for (int i = 0; i < text.size(); ++i) {
            key = text.keyAt(i);
            result.add(text.get(key).getValue());
        }

        return result;
    }
}
