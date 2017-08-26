package comp_sci_squad.com.github.url_irl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.util.Log;
import android.widget.Toast;


import com.google.android.cameraview.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private final float[] OFFSET = {90.0f, 0.0f, -90.0f, -180.0f};
    private final int INDEX_OFFSET_AT_0 = 0;
    private final int INDEX_OFFSET_AT_90 = 1;
    private final int INDEX_OFFSET_AT_180 = 2;
    private final int INDEX_OFFSET_AT_270 = 3;

    private String TAG = "CAMERA_ACTIVITY";
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private CameraView mCamera;
    private ImageButton mShutterButton;

    private CameraView.Callback mCameraCallback =
            new CameraView.Callback() {
                @Override
                public void onCameraOpened(CameraView cameraView) {
                    super.onCameraOpened(cameraView);
                    Log.d(TAG, "Camera Opened");
                }

                @Override
                public void onCameraClosed(CameraView cameraView) {
                    super.onCameraClosed(cameraView);
                    Log.d(TAG, "Camera Closed");
                }

                @Override
                public void onPictureTaken(CameraView cameraView, byte[] data) {
                    super.onPictureTaken(cameraView, data);
                    Log.d(TAG, "Picture taken");

                    mShutterButton.removeCallbacks(null);

                    Display display = getWindowManager().getDefaultDisplay();
                    Bitmap image = rotatePictureByOrientation(data, display.getRotation());

                    TextRecognitionTask parsingTask = new TextRecognitionTask(MainActivity.this);
                    parsingTask.execute(image);
                }
            };

    protected class TextRecognitionTask extends AsyncTask<Bitmap, Integer, Intent> {
        private Context mContext;

        public TextRecognitionTask(Context context) {
            mContext = context;
        }

        @Override
        protected Intent doInBackground(Bitmap... params) {
            Log.d(TAG, "URL Parsing Task Started");
            ArrayList<String> result = new ArrayList<>();
            byte[] compressedImage = null;

            for (Bitmap image : params) {
                Log.d(TAG, "Converted image to text: ");

                result.addAll(ImageToString.getTextFromPage(mContext, image));
                compressedImage = compressBitmap(image);
            }

            Intent intent = ListURLsActivity.newIntent(mContext, result);
            intent.putExtra("bitmapBytes", compressedImage);

            return intent;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            Log.d(TAG, "URL Parsing Task Ended.");
            startActivity(intent);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shutter_button:
                    if (mCamera != null) {
                        Log.d(TAG, "Shutter Button Pressed");
                        mCamera.takePicture();
                    }
                    break;
            }
        }
    };

    public static boolean isEmulator() {
        return false;
        /*
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);*/
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isEmulator()) {
            InputStream stream = getResources().openRawResource(R.raw.tester_pic_four_facebook);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            ArrayList<String> allText = ImageToString.getTextFromPage(this, bitmap);

            // Start the intent to get a List of Strings from the image
            Intent i = ListURLsActivity.newIntent(MainActivity.this, allText);
            startActivity(i);
        } // if program was ran on an emulator
        else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else
                inflateViews();
        }
    }

    private void inflateViews() {
        mCamera = (CameraView) findViewById(R.id.camera);
        mCamera.addCallback(mCameraCallback);

        mShutterButton = (ImageButton) findViewById(R.id.shutter_button);
        mShutterButton.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Camera Resuming");

        if (mCamera != null)
            mCamera.start();

        Toast.makeText(this, R.string.camera_prompt, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()");

        if (mCamera != null)
            mCamera.stop();
        Log.v(TAG, "Camera Paused");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                Log.d(TAG, "Permissions Result for Camera");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Request Successful");
                    inflateViews();
                } else {
                    Log.d(TAG, "Permission for camera denied");
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
                break;
        }
    }

    private Bitmap rotatePictureByOrientation(byte[] imageData, int rotation) {
        float rotationAmount = 0.0f; // Picture is sideways + camera rotation
        switch (rotation) {
            case Surface.ROTATION_0:
                Log.d(TAG, "Rotation: 0");
                rotationAmount = OFFSET[INDEX_OFFSET_AT_0];
                break;
            case Surface.ROTATION_90:
                Log.d(TAG, "Rotation: 90");
                rotationAmount = OFFSET[INDEX_OFFSET_AT_90];
                break;
            case Surface.ROTATION_180:
                Log.d(TAG, "Rotation: 180");
                rotationAmount = OFFSET[INDEX_OFFSET_AT_180];
                break;
            case Surface.ROTATION_270:
                Log.d(TAG, "Rotation: 270");
                rotationAmount = OFFSET[INDEX_OFFSET_AT_270];
                break;
        }

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate(rotationAmount);

        Bitmap img = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

        img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(),
                rotationMatrix, true);

        return  img;
    }

    private byte[] compressBitmap(Bitmap image) {
        Log.d(TAG, "Compressing thumbnail Bitmap");
        int thumbnailHeight = image.getHeight()/8;
        int thumbnailWidth = image.getWidth()/8;
        
        Bitmap scaled = Bitmap.createScaledBitmap(image, thumbnailWidth, thumbnailHeight, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] compressedByteArray = stream.toByteArray();
        Log.d(TAG, "Passing byte array thumbnail image of size: " + compressedByteArray.length);
        return stream.toByteArray();
    }
}