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
import android.view.WindowManager;
import android.widget.ImageButton;
import android.util.Log;
import android.widget.Toast;


import com.google.android.cameraview.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

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

                    Display display = getWindowManager().getDefaultDisplay();
                    Bitmap image = rotatePictureByOrientation(data, display.getRotation());
                    String[] text = ImageToString.getTextFromPage(getApplicationContext(), image);

                    Log.d(TAG, "Converted image to text: ");

                    for (int i = 0; i < text.length; ++i)
                        Log.v(TAG, text[i]);


                    byte[] compressedByteArray = compressBitmap(image);
                    Intent i = ListURLsActivity.newIntent(MainActivity.this, text);
                    i.putExtra("bitmapBytes", compressedByteArray);
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

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else
            inflateViews();
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
        Log.v(TAG, "Camera Paused");

        if (mCamera != null)
            mCamera.stop();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
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
        Bitmap scaled = Bitmap.createScaledBitmap(image, 640, 480, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] compressedByteArray = stream.toByteArray();
        Log.d(TAG, "Passing byte array thumbnail image of size: " + compressedByteArray.length);
        return stream.toByteArray();
    }
}