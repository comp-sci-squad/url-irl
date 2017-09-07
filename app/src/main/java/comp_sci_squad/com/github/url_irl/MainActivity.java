package comp_sci_squad.com.github.url_irl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.util.Log;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String PICTURE_EXTRA = "bitmapBytes";
    public static final String TIME_EXTRA = "time";

    private final float[] OFFSET = {90.0f, 180.0f, -90.0f, 0.0f};
    private final float[] UI_ROTATION_OFFSET = {0.0f, -90.0f, 180.0f, 90.0f};
    private final int INDEX_OFFSET_AT_0 = 0;
    private final int INDEX_OFFSET_AT_90 = 1;
    private final int INDEX_OFFSET_AT_180 = 2;
    private final int INDEX_OFFSET_AT_270 = 3;

    private String TAG = "CAMERA_ACTIVITY";
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private CameraView mCamera;
    private ImageButton mShutterButton;
    private MyOrientationEventListener mOrientationEventListener;
    private int mLastOrientation = 0;

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

                    Bitmap image = rotatePictureByOrientation(data, mLastOrientation);
                    long timePictureTaken = System.currentTimeMillis();
                    String[] text = ImageToString.getTextFromPage(getApplicationContext(), image);

                    Log.d(TAG, "Converted image to text: ");

                    for (int i = 0; i < text.length; ++i)
                        Log.v(TAG, text[i]);


                    byte[] compressedByteArray = compressBitmap(image);
                    Intent i = ListURLsActivity.newIntent(MainActivity.this, text);
                    i.putExtra(PICTURE_EXTRA, compressedByteArray);
                    i.putExtra(TIME_EXTRA, timePictureTaken);
                    startActivity(i);
                }
            };

    private View.OnClickListener mOnClickListener =
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.shutter_button:
                        if (mCamera != null)
                            Log.d(TAG, "Shutter Button Pressed");
                            mCamera.takePicture();
                        break;
                }
            }

        };

    public void onOrientationChanged(int orientation) {
        int diff = Math.abs(mLastOrientation * 90 - orientation);
        diff = Math.min(diff, 360 - diff);
        if (diff > 55) {
            int newOrientation = Math.round(orientation/90.0f) % 4;
            float previousRotation = mShutterButton.getRotation();
            float newRotation = UI_ROTATION_OFFSET[newOrientation];
            Log.d(TAG, "Rotate Previous: " + previousRotation + "   Rotate New: " + newRotation);
            RotateAnimation rotate = new RotateAnimation(previousRotation, newRotation,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            mShutterButton.startAnimation(rotate);
            mShutterButton.setRotation(newRotation);
            mLastOrientation = newOrientation;
            Log.d(TAG, "Orientation Changed to: " + mLastOrientation);

        }
    }

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
        Log.v(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        mOrientationEventListener = new MyOrientationEventListener(this);

        if(isEmulator()) {
            InputStream stream = getResources().openRawResource(R.raw.tester_pic_four_facebook);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            String[] allText = ImageToString.getTextFromPage(this, bitmap);

            // Start the intent to get a List of Strings from the image
            Intent i = ListURLsActivity.newIntent(MainActivity.this, allText);
            startActivity(i);
        }// if program was ran on an emulator
        else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else
                inflateViews();
        }// if program isn't ran on an emulator
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
        Log.v(TAG, "onResume()");
        if (mOrientationEventListener.canDetectOrientation()) {
            Log.v(TAG, "Orientation Event Listener can detect orientation");
            mOrientationEventListener.enable();
        } else {
            Log.d(TAG, "Orientation Event Listener cannot detect orientation");
        }
        if (mCamera != null)
            mCamera.start();
        Log.v(TAG, "Camera Resumed");

        Toast.makeText(this, R.string.camera_prompt, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()");
        mOrientationEventListener.disable();
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


    private class MyOrientationEventListener
            extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != MyOrientationEventListener.ORIENTATION_UNKNOWN) {
                //Log.v("Orientation", Integer.toString(orientation));
                MainActivity.this.onOrientationChanged(orientation);
            }
        }
    }
}