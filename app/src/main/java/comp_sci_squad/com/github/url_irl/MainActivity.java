package comp_sci_squad.com.github.url_irl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.AsyncTask;

import com.google.android.cameraview.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    /**
     * Extra Name Constants for the ListURLsActivity Intent
     *
     * PICTURE_EXTRA labels a byte array
     * TIME_EXTRA labels a long
     */
    public static final String PICTURE_EXTRA = "bitmapBytes";
    public static final String TIME_EXTRA = "time";

    /**
     * Rotation Offset Constants
     *
     * Defines the transformations that align camera orientation with
     * device orientation per each angle.
     */
    private final float[] OFFSET = {90.0f, 0.0f, -90.0f, -180.0f};
    final int INDEX_OFFSET_AT_0 = 0;
    final int INDEX_OFFSET_AT_90 = 1;
    final int INDEX_OFFSET_AT_180 = 2;
    final int INDEX_OFFSET_AT_270 = 3;

    /**
     * Logging Tag
     */
    private String TAG = "CAMERA_ACTIVITY";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private CameraView mCamera;

    ImageButton mShutterButton;
    ProgressBar mProgressBar;
    MediaActionSound mShutterSound;

    /**
     * Emulator Variables. Remove before release.
     */
    ImageView mEmulatorPicture;
    Bitmap mEmulatorImage;


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

                    Display display = getWindowManager().getDefaultDisplay();
                    Bitmap image = rotatePictureByOrientation(data, display.getRotation());

                    TextRecognitionTask parsingTask = new TextRecognitionTask(MainActivity.this,
                            System.currentTimeMillis());

                    parsingTask.execute(image);
                }
            };

    protected class TextRecognitionTask extends AsyncTask<Bitmap, Integer, Intent> {
        private Context mContext;
        private long mTimeImageTaken;

        public TextRecognitionTask(Context context, long timeImageTaken) {
            mContext = context;
            mTimeImageTaken = timeImageTaken;
        }

        @Override
        protected void onPreExecute() {
            if (!isCancelled())
                mProgressBar.setVisibility(View.VISIBLE);
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
            intent.putExtra(PICTURE_EXTRA, compressedImage);
            intent.putExtra(TIME_EXTRA, mTimeImageTaken);

            return intent;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            Log.d(TAG, "URL Parsing Task Ended.");
            mProgressBar.setVisibility(View.INVISIBLE);

            if (isEmulator())
                mShutterButton.setOnClickListener(mEmulatorOnClickListener);
            else
                mShutterButton.setOnClickListener(mOnClickListener);

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
                        mShutterSound.play(MediaActionSound.SHUTTER_CLICK);
                        mCamera.takePicture();

                        mShutterButton.setOnClickListener(null);
                    }
                    break;
            }
        }
    };

    private View.OnClickListener mEmulatorOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shutter_button:
                    Log.d(TAG, "Shutter Button Pressed");
                    mShutterSound.play(MediaActionSound.SHUTTER_CLICK);

                    TextRecognitionTask parsingTask = new TextRecognitionTask(MainActivity.this,
                            System.currentTimeMillis());

                    parsingTask.execute(mEmulatorImage);

                    mShutterButton.setOnClickListener(null);
                    break;
            }
        }
    };

    /**
     * Checks if the app is run on an emulator.
     *
     * Warning: this and other emulator code should be removed from release
     * versions of the app, as it is rumored to yield some false positives.
     *
     * @return boolean - True if the app is being run on an emulator.
     */
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);

    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");;

        if(isEmulator()) {
            Log.w(TAG, "Emulator display. Remove before release.");
            setContentView(R.layout.emulator_main_activity);

            InputStream stream = getResources().openRawResource(R.raw.tester_pic_four_facebook);
            mEmulatorImage = BitmapFactory.decodeStream(stream);

            mEmulatorPicture = (ImageView) findViewById(R.id.emulator_image);
            mEmulatorPicture.setImageBitmap(mEmulatorImage);

            mShutterButton = (ImageButton) findViewById(R.id.shutter_button);
            mShutterButton.setOnClickListener(mEmulatorOnClickListener);

            mProgressBar = (ProgressBar) findViewById(R.id.loading_indicator);
        } else {
            setContentView(R.layout.activity_main);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else
                inflateViews();
        }

        mShutterSound = new MediaActionSound();
        mShutterSound.load(MediaActionSound.FOCUS_COMPLETE);
    }

    /**
     * Associates view variables with their layout counterparts.
     */
    private void inflateViews() {
        mCamera = (CameraView) findViewById(R.id.camera);
        mCamera.addCallback(mCameraCallback);

        mShutterButton = (ImageButton) findViewById(R.id.shutter_button);
        mShutterButton.setOnClickListener(mOnClickListener);

        mProgressBar = (ProgressBar) findViewById(R.id.loading_indicator);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");

        if (mCamera != null)
            mCamera.start();
        Log.v(TAG, "Camera Resumed");

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
        mShutterSound.release();
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

    /**
     * Rotates an image so that it's orientation matches the phone's, as camera orientation
     * and phone orientation differ.
     *
     * @param imageData - a byte array of image data
     * @param rotation - a integer corresponding to the rotation of the phone.
     * @return Bitmap - a picture that is rotated properly according to the phone orientation.
     */
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