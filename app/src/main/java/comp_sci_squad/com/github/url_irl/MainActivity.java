package comp_sci_squad.com.github.url_irl;

import android.Manifest;
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

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.cameraview.CameraView;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
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
                 * Loads the image into a byte array with Glide.
                 * Shrinks the bitmap based on available memory.
                 * It is parsed for text with an AsyncTask that handles starting the ListURLS activity.
                 * @param cameraView - A reference to the cameraView the picture was taken with
                 * @param data - The taken picture as a byte array
                 */
                @Override
                public void onPictureTaken(CameraView cameraView, byte[] data) {
                    super.onPictureTaken(cameraView, data);
                    Log.d(TAG, "Picture taken");
                    final long timeImageTaken = System.currentTimeMillis();

                    Log.d(TAG, "Getting picture Dimensions");
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(data, 0, data.length, bitmapOptions);
                    int originalImageWidth = bitmapOptions.outWidth;
                    int originalImageHeight = bitmapOptions.outHeight;

                    Log.d(TAG, "Byte Array Size: " + data.length);
                    Log.d(TAG, "ImageWidth: " + originalImageWidth);
                    Log.d(TAG, "ImageHeight: " + originalImageHeight);

                    int inSampleSize = getInSampleSize(originalImageWidth, originalImageHeight);

                    final int newImageWidth = originalImageWidth / inSampleSize;
                    final int newImageHeight = originalImageHeight / inSampleSize;

                    //Rotate Options
                    final RequestOptions options = new RequestOptions();
                    if (mLastOrientation != 0)
                        options.transform(new RotateTransformation(90.0f * mLastOrientation));

                    //Load parsing bitmap
                    GlideApp.with(MainActivity.this).asBitmap().load(data).override(newImageWidth, newImageHeight).apply(options).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap image, Transition<? super Bitmap> transition) {
                            TextRecognitionTask parsingTask = new TextRecognitionTask(MainActivity.this, timeImageTaken);
                            parsingTask.execute(image);
                        }
                    });
                }
            };

    /**
     * This class parses images into text and starts ListUrlsActivity when done.
     */
    protected class TextRecognitionTask extends AsyncTask<Bitmap, Integer, ArrayList<String>> {
        private Context mContext;
        private long mTimeImageTaken;
        private byte[] mThumbnail;

        /**
         * Constructor to set extra variables
         * @param context - The context for creating the new intent.
         * @param timeImageTaken - The time the image was taken at as a long.
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
         * @return ArrayList<String> - Returns the parsed text from the image.
         */
        @Override
        protected ArrayList<String> doInBackground(Bitmap... params) {
            Log.d(TAG, "URL Parsing Task Started");
            ArrayList<String> result = new ArrayList<>();

            result.addAll(ImageToString.getTextFromPage(mContext, params[0]));
            Log.d(TAG, "Converted image to text: ");

            mThumbnail = compressBitmap(params[0]);
            params[0].recycle();
            params[0] = null;

            return result;
        }

        /**
         * Resets UI elements before starting ListUrlsActivity.
         * Called automatically by the AsyncTask.
         * @param mParsedText - The parsed text from the image.
         */
        @Override
        protected void onPostExecute(ArrayList<String> mParsedText) {
            Log.d(TAG, "URL Parsing Task Ended.");
            mProgressBar.setVisibility(View.INVISIBLE);
            mShutterButton.setEnabled(true);
            Intent intent = ListURLsActivity.newIntent(mContext, mParsedText, mThumbnail, mTimeImageTaken);
            startActivity(intent);

            Log.d(TAG, "Resetting Views.");
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
                            GlideApp.with(MainActivity.this).load(R.raw.tester_pic_remove_on_release).into(mCapturedImagePreview);
                        else
                            mCamera.takePicture();

                        mShutterSound.play(MediaActionSound.SHUTTER_CLICK);

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
        mToolBar.setTitle(getString(R.string.title_activity_camera_viewer));
        setSupportActionBar(mToolBar);

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
            mEmulatorPreview = (ImageView) findViewById(R.id.emulator_image);

            Log.d(TAG, "Loading Image into mEmulatorPreview");
            GlideApp.with(this).load(R.raw.tester_pic_remove_on_release).into(mEmulatorPreview);

            Log.d(TAG, "Loading Image into Bitmap");
            GlideApp.with(this).asBitmap().load(R.raw.tester_pic_remove_on_release).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    mEmulatorImage = resource;
                }
            });
        }

        mShutterButton = (ImageButton) findViewById(R.id.shutter_button);
        mShutterButton.setOnClickListener(mOnClickListener);

        mProgressBar = (ProgressBar) findViewById(R.id.loading_indicator);

        mShutterSound = new MediaActionSound();
        mShutterSound.load(MediaActionSound.FOCUS_COMPLETE);

        mCapturedImagePreview = (ImageView) findViewById(R.id.image_preview);
    }


    /**
     * Create an options menu. It will create
     *  - Torchlight button
     * @param menu - The Options Menu being created.
     * @return - True because it handled the menu creation.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d(TAG, "Creating Options Menu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    /**
     * Handles menu items being selected.
     * If the item was share_all, start the share chooser with all the urls.
     * @param item - The item selected.
     * @return - Returns true if successfully handled or calls super method.
     */
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
     * Class for GlideApp to Transform bitmaps.
     * The bitmap pool isn't correctly used but most likely isn't needed because of the size of the bitmaps involved.
     */
    private class RotateTransformation extends BitmapTransformation {
        private float mRotationAngle = 0.0f;
        private static final String BASE_ID = "comp_sci_squad.com.github.url_irl.MainActivity.RotateTransformation";

        /**
         * Constructor for the rotation transformation.
         * The class is designed to be reused with multiple transformations.
         * @param rotationAngle - The angle, generally 90, 180, 270
         */
        private RotateTransformation(float rotationAngle) {
            this.mRotationAngle = rotationAngle;
        }

        @Override
        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            Log.d(TAG, "Transforming Bitmap by degrees: " + mRotationAngle);
            if (Float.compare(mRotationAngle, 0.0f) == 0)
                return toTransform;

            Matrix rotationMatrix = new Matrix();
            rotationMatrix.postRotate(mRotationAngle);
            Bitmap rotatedBitmap = Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), rotationMatrix, true);
            return rotatedBitmap;
         }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update((BASE_ID + mRotationAngle).getBytes());
        }
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
     * Calculates the power of 2 the bitmap size must be reduced by to fit within the curent JVM Heap.
     * Assumes there will be other allocations and that 2 bitmaps of the size will be allocated.
     * @param width - current width of the image.
     * @param height - current height of the image.
     * @return The power of 2 the image is to be shrunken by.
     */
    private int getInSampleSize(int width, int height) {
        Runtime rt = Runtime.getRuntime();
        long availiableMem = rt.maxMemory() - rt.totalMemory() - rt.freeMemory();

        //Width * Height * bytes/pixel * 2 bitmaps because rotation
        long memCost = (width * height * 4 * 2);
        int inSampleSize = 1;

        //.9 for random overhead from other allocations
        while (memCost > availiableMem * .9) {
            inSampleSize *= 2;
            memCost /= 2;
        }

        Log.d(TAG, "In Sample Size: " + inSampleSize);
        return inSampleSize;
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
}