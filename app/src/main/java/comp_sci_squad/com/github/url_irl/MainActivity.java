package comp_sci_squad.com.github.url_irl;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.AsyncTask;

import com.google.android.cameraview.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
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
     * Logging Tag
     */
    private String TAG = "CAMERA_ACTIVITY";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    /**
     * Duration of the picture taken animation. Set to system's default short animation time.
     */
    private int mShortAnimationDuration;

    /**
     * UI Elements
     */
    CameraView mCamera;
    ImageButton mShutterButton;
    ProgressBar mProgressBar;
    MediaActionSound mShutterSound;
    ImageView mCapturedImagePreview;
    Toolbar mToolBar;

    /**
     * Orientation Private Variables
     *
     * MyOrientationEventListener extends OrientationEventListener and calls onOrientationChanged()
     * if phone is rotated 55 degrees away from orientation
     */
    private MyOrientationEventListener mOrientationEventListener;
    private int mLastOrientation = 0;
    private float mLastRotation = 0.0f;

    /**
     * Emulator Variables. Remove before release.
     */
    ImageView mEmulatorPreview;
    private Bitmap mEmulatorImage;
    private boolean mEmulated;

    /**
     * Callback for Camera View
     */
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

                /**
                 * Converts the byte array to a bitmap and rotates it.
                 * The parsing library only takes bitmaps and frames.
                 * It is parsed for text with an AsyncTask that handles the next steps
                 * @param cameraView - A reference to the cameraView the picture was taken with
                 * @param data - The taken picture as a byte array
                 */
                @Override
                public void onPictureTaken(CameraView cameraView, byte[] data) {
                    super.onPictureTaken(cameraView, data);
                    Log.d(TAG, "Picture taken");

                    Bitmap image = rotatePictureByOrientation(data, mLastOrientation);
                    Log.d(TAG, "Converted image to text: ");

                    // Displays the image to the user
                    mCapturedImagePreview.setImageBitmap(image);
                    mCapturedImagePreview.setVisibility(View.VISIBLE);

                    TextRecognitionTask parsingTask = new TextRecognitionTask(MainActivity.this,
                            System.currentTimeMillis());

                    parsingTask.execute(image);
                }
            };

    /**
     * This class parses images into text and starts ListUrlsActivity when done.
     */
    private class TextRecognitionTask extends AsyncTask<Bitmap, Integer, Intent> {
        private Context mContext;
        private long mTimeImageTaken;

        /**
         * Constructor sets context for creating intent.
         * @param context - Main Activity's context.
         * @param timeImageTaken - The time the picture was taken at as a long. System.currentTimeMillis().
         */
        public TextRecognitionTask(Context context, long timeImageTaken) {
            mContext = context;
            mTimeImageTaken = timeImageTaken;
        }

        /**
         * Enables progress bar on UI thread before task starts.
         * Called automatically by the AsyncTask.
         */
        @Override
        protected void onPreExecute() {
            if (!isCancelled())
                mProgressBar.setVisibility(View.VISIBLE);
        }

        /**
         * Called automatically by the AsyncTask.
         * @param params Bitmap varargs array that is going to be parsed for text.
         * @return Intent - Returns an intent to start ListUrlsActivity passing in the the string arraylist,
         * the thumbnail, and the time.
         */
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

            Intent intent = ListURLsActivity.newIntent(mContext, result, compressedImage, mTimeImageTaken);

            return intent;
        }

        /**
         * Resets UI elements before starting ListUrlsActivity.
         * Called automatically by the AsyncTask.
         * @param intent - The intent doInBackground() returns.
         */
        @Override
        protected void onPostExecute(Intent intent) {
            Log.d(TAG, "URL Parsing Task Ended.");
            mProgressBar.setVisibility(View.INVISIBLE);

            startActivity(intent);

            Log.d(TAG, "Resetting Views.");

            undoPictureAnimationChanges();

            mShutterButton.setEnabled(true);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Shutter Button Pressed");

            switch (v.getId()) {
                case R.id.shutter_button:
                    if (mCamera != null || mEmulatorPreview != null) {
                        mShutterButton.setEnabled(false);

                        if (mEmulated)
                            mCapturedImagePreview.setImageBitmap(mEmulatorImage);
                        else
                            mCamera.takePicture();

                        mShutterSound.play(MediaActionSound.SHUTTER_CLICK);

                        takePictureAnimation();

                        if (mEmulated) {
                            TextRecognitionTask parsingTask = new TextRecognitionTask(
                                    MainActivity.this, System.currentTimeMillis());
                            parsingTask.execute(mEmulatorImage);
                        }

                    } else {
                        Log.e(TAG, "Camera not instantiated.");
                    }
                    break;
            }
        }
    };

    /**
     * Calculates the devices current rotation and compares to current orientation and updates UI accordingly.
     * 
     * If the difference is greater than 55 degrees it calculates new orientation and
     * and creates an animation for the rotation of the UI elements.
     * It then rotates the UI elements with that animation.
     * Finally, it saves the new orientation and UI element rotation to member variables.
     *
     * @param orientation - The orientation of the device as an angle. Vertical is 0 clockwise to 359
     */
    public void onOrientationChanged(int orientation) {
        //Calculate Difference from last Orientation
        int diff = Math.abs(mLastOrientation * 90 - orientation);
        diff = Math.min(diff, 360 - diff);

        if (diff > 55) {
            int newOrientation = Math.round(orientation/90.0f) % 4;
            float newRotation = 0;

            //Rotate animation forward, backward, or 180 depending on change in Orientation
            if (newOrientation == (mLastOrientation + 1) % 4) {
                Log.d(TAG, "Phone rotated clockwise");
                newRotation = mLastRotation - 90.0f;
            } else if (newOrientation == ((mLastOrientation + 3) % 4)) {
                Log.d(TAG, "Phone rotated counterclockwise");
                newRotation = mLastRotation + 90.0f;
            } else {
                Log.d(TAG, "Phone flipped");
                newRotation = mLastRotation + 180.0f;
            }
            Log.d(TAG, "Rotate Previous: " + mLastRotation + "   Rotate New: " + newRotation);

            //Create Rotation Animation
            RotateAnimation rotateAnimation = new RotateAnimation(
                    mLastRotation,
                    newRotation,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            rotateAnimation.setDuration(250);
            rotateAnimation.setFillAfter(true);

            //Rotate the views
            mShutterButton.startAnimation(rotateAnimation);

            //Set variables for next iteration
            mLastOrientation = newOrientation;
            mLastRotation = newRotation;
            Log.d(TAG, "Orientation Changed to: " + newOrientation);
        }
    }

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
        Log.d(TAG, "onCreate()");
        mEmulated = isEmulator();
        setContentView(mEmulated ? R.layout.emulator_main_activity: R.layout.activity_main);

        mOrientationEventListener = new MyOrientationEventListener(this);

        mToolBar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            setUpCameraViews();
        }
    }

    /**
     * Associates view variables with their layout counterparts.
     */
    private void setUpCameraViews() {
        if (!mEmulated) {
            mCamera = (CameraView) findViewById(R.id.camera);
            mCamera.addCallback(mCameraCallback);
        } else {
            Log.w(TAG, "Emulator display. Remove before release.");

            InputStream stream = getResources().openRawResource(R.raw.tester_pic_four_facebook);
            mEmulatorImage = BitmapFactory.decodeStream(stream);

            mEmulatorPreview = (ImageView) findViewById(R.id.emulator_image);
            mEmulatorPreview.setImageBitmap(mEmulatorImage);
        }

        mShutterButton = (ImageButton) findViewById(R.id.shutter_button);
        mShutterButton.setOnClickListener(mOnClickListener);

        mProgressBar = (ProgressBar) findViewById(R.id.loading_indicator);

        mShutterSound = new MediaActionSound();
        mShutterSound.load(MediaActionSound.FOCUS_COMPLETE);

        mCapturedImagePreview = (ImageView) findViewById(R.id.image_preview);

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Creating Options Menu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Options Item Selected");
        if (item.getItemId() == R.id.torch_menu_item) {
            if (mCamera.getFlash() == CameraView.FLASH_TORCH) {
                Log.d(TAG, "Disabling Torch");
                mCamera.setFlash(CameraView.FLASH_OFF);
            } else {
                Log.d(TAG, "Enabling Torch");
                mCamera.setFlash(CameraView.FLASH_TORCH);
            }
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (mOrientationEventListener.canDetectOrientation()) {
            Log.d(TAG, "Orientation Event Listener can detect orientation");
            mOrientationEventListener.enable();
        } else {
            Log.d(TAG, "Orientation Event Listener cannot detect orientation");
        }
        if (mCamera != null)
            mCamera.start();
        Log.d(TAG, "Camera Resumed");

        Toast.makeText(this, R.string.camera_prompt, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        mOrientationEventListener.disable();
        if (mCamera != null) {
            mCamera.setFlash(CameraView.FLASH_OFF);
            mCamera.stop();
        }
        Log.d(TAG, "Camera Paused");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mShutterSound != null)
            mShutterSound.release();
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    /**
     * Keeps requesting camera permission til accepted.
     * Once accepted, initialize the Camera View
     * @param requestCode - The request code for permission. Only acts if its REQUEST_CAMERA_PERMISSION
     * @param permissions - The requested permissions. Never null.
     * @param grantResults - The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                Log.d(TAG, "Permissions Result for Camera");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Request Successful");
                    setUpCameraViews();
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
        float rotationAmount = mLastOrientation * 90.0f + 90.0f;

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate(rotationAmount);

        Bitmap img = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

        img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(),
                rotationMatrix, true);

        return  img;
    }

    /**
     * Compresses a bitmap so its length and width are 1/8 the size
     * Results in a 1/64th size image
     *
     * @param image - a bitmap
     * @return byte[] - the compressed picture as a byte array
     */
    private byte[] compressBitmap(Bitmap image) {
        Log.d(TAG, "Compressing thumbnail Bitmap");
        int thumbnailHeight = image.getHeight()/getResources().getInteger(R.integer.THUMBNAIL_SHRINK_RATIO);
        int thumbnailWidth = image.getWidth()/getResources().getInteger(R.integer.THUMBNAIL_SHRINK_RATIO);
        
        Bitmap scaled = Bitmap.createScaledBitmap(image, thumbnailWidth, thumbnailHeight, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] compressedByteArray = stream.toByteArray();
        Log.d(TAG, "Passing byte array thumbnail image of size: " + compressedByteArray.length);
        return stream.toByteArray();
    }

    /**
     * OrientationEventListener is abstract so this subclass extends it
     */
    private class MyOrientationEventListener
            extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        /**
         * The orientationListener uses Android's Sensor Manager in the background
         * It calls MainActivity's onOrientationChanged if the orientation isn't flat (ORIENTATION_UNKOWN)
         *
         * @param orientation - the orientation of the device as an angle. Vertical is 0 clockwise to 359
         */
        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != MyOrientationEventListener.ORIENTATION_UNKNOWN) {
                Log.v("Orientation", Integer.toString(orientation));
                MainActivity.this.onOrientationChanged(orientation);
            }
        }
    }

    /**
     * Animation for when a user takes a picture. The changes must be undone
     * with the {@link #undoPictureAnimationChanges()} before starting a new activity.
     */
    private void takePictureAnimation() {
        final View cameraLikeView = mEmulated ? mEmulatorPreview : mCamera;

        mCapturedImagePreview.setAlpha(0f);
        mCapturedImagePreview.setVisibility(View.VISIBLE);
        mCapturedImagePreview.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        mShutterButton.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mShutterButton.setVisibility(View.GONE);
                        mShutterButton.setAlpha(1f);
                    }
                });

        cameraLikeView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        cameraLikeView.setVisibility(View.GONE);
                        cameraLikeView.setAlpha(1f);
                    }
                });
    }

    /**
     * Undoes the changes made by the {@link #takePictureAnimation()}.
     */
    private void undoPictureAnimationChanges() {
        View cameraLikeView = mEmulated ? mEmulatorPreview : mCamera;

        cameraLikeView.setVisibility(View.VISIBLE);
        mShutterButton.setVisibility(View.VISIBLE);
        mCapturedImagePreview.setVisibility(View.GONE);
    }
}