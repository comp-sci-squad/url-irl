package comp_sci_squad.com.github.url_irl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Size;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageButton;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private enum STATE {PREVIEW, WAIT_LOCK};
    private STATE mState = STATE.PREVIEW;

    private TextureView mCameraDisplay;
    private ImageButton mShutterButton;

    private String mCameraID;
    private Size mCameraDimensions;
    private Size mMaxCameraDimensions;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage()));
                }
            };

    private static class ImageSaver implements Runnable {
        private final Image mImage;

        private ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            Bitmap imgBitMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);


        }
    }

    private CameraDevice mCamera;

    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult result) {
                    switch (mState) {
                        case PREVIEW:
                            // Pass
                            break;
                        case WAIT_LOCK:
                            if (result.get(CaptureResult.CONTROL_AF_STATE) ==
                                    CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED) {
                                unlockFocus();
                                Toast.makeText(getApplicationContext(), "Focus Lock Successful", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }

                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    process(result);
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);

                    Toast.makeText(getApplication(), "Focus Lock Unsuccessful", Toast.LENGTH_SHORT).show();
                }


            };

    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCamera = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCamera.close();
            mCamera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCamera.close();
            mCamera = null;
        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    mCameraID = findCamera();
                    mCameraDimensions = findFittingDimensions(mCameraID , height, width);
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraDisplay = (TextureView)findViewById(R.id.camera_display);
        mShutterButton = (ImageButton)findViewById(R.id.shutter_button);

    }

    @Override
    protected void onResume() {
        super.onResume();
        openBackgroundThread();

        if (mCameraDisplay.isAvailable()) {
            mCameraID = findCamera();
            mCameraDimensions = findFittingDimensions(mCameraID,
                    mCameraDisplay.getHeight(), mCameraDisplay.getWidth());
            openCamera();
        } else {
            mCameraDisplay.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();

        closeBackgroundThread();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private String findCamera() {
        CameraManager cameraManager = (CameraManager)getSystemService(CAMERA_SERVICE);

        try {
            for (String cameraID : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraID;
                }
            }

            return cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Size findFittingDimensions(String cameraId, int height, int width) {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            StreamConfigurationMap map = cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            List<Size> validDimensions = new ArrayList<>();

            for (Size dimension : map.getOutputSizes(SurfaceTexture.class)) {
                if (height > width) {
                    if (dimension.getHeight() > height && dimension.getWidth() > width)
                        validDimensions.add(dimension);
                } else {
                    // Checks for horizontal mode, so has to compare the opposite vals.
                    if (dimension.getHeight() > width && dimension.getWidth() > height)
                        validDimensions.add(dimension);
                }
            }

            if (validDimensions.size() > 0)
                return Collections.min(validDimensions, new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight();
                    }
                });
            else
                return map.getOutputSizes(SurfaceTexture.class)[0];

        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO: Give this its own thread
    private void openCamera() {
        CameraManager cameraManager = (CameraManager)getSystemService(CAMERA_SERVICE);

        try {
            cameraManager.openCamera(mCameraID, mCameraStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            Toast.makeText(this, "This app requires Camera Permission, please enable it!", Toast.LENGTH_LONG);
        }
    }

    private void closeCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
    }

    private void takePhoto() {
        lockFocus();
    }

    private void lockFocus() {
        try {
            mState = STATE.WAIT_LOCK;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_START);

            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(),
                    mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            mState = STATE.PREVIEW;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);

            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(),
                    mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture previewSurfaceTexture = mCameraDisplay.getSurfaceTexture();
            previewSurfaceTexture.setDefaultBufferSize(mCameraDimensions.getWidth(), mCameraDimensions.getHeight());

            Surface previewSurface = new Surface(previewSurfaceTexture);
            mPreviewCaptureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);

            mCamera.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (mCamera != null) {
                                try {
                                    mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                                    mCameraCaptureSession = session;
                                    mCameraCaptureSession.setRepeatingRequest(mPreviewCaptureRequest,
                                            mCaptureCallback, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "Couldn't Configure Camera", Toast.LENGTH_LONG).show();
                        }
                }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Display Background Thread");
        mBackgroundThread.start();

        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void closeBackgroundThread() {
        mBackgroundThread.quitSafely();
        mBackgroundThread = null;
        mBackgroundHandler = null;

        try {
            mBackgroundThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}