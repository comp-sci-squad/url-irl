package comp_sci_squad.com.github.url_irl;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageButton;
import android.util.Log;
import android.widget.Toast;


import com.google.android.cameraview.CameraView;

public class MainActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
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

                    Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                    String[] text = ImageToString.getTextFromPage(getApplicationContext(), image);
                    Log.d(TAG, "Converted image to text: ");

                    for (int i = 0; i < text.length; ++i)
                        Log.v(TAG, text[i]);

                    Intent i = ListURLsActivity.newIntent(MainActivity.this, text);
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

        mCamera = (CameraView)findViewById(R.id.camera);
        mShutterButton = (ImageButton)findViewById(R.id.shutter_button);

        if (mCamera != null)
            mCamera.addCallback(mCameraCallback);

        if (mShutterButton != null)
            mShutterButton.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.start();
        Toast.makeText(this, R.string.camera_prompt, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
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
        Log.d(TAG, "Permissions Requested");
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    Log.e(TAG, "Error With Camera Request");
                    throw new RuntimeException("Error on requesting camera");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission Not Granted");
                    Toast.makeText(this, R.string.need_permission, Toast.LENGTH_LONG);
                }
                Log.d(TAG, "Permission Granted");
                break;
        }
    }
}