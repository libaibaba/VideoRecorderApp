package com.videorecorderapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.videorecorderapp.Adapters.PreviewFileAdapter;
import com.videorecorderapp.R;
import com.videorecorderapp.Util.AutoFitTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity {

    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;

    @BindView(R.id.capture_image)
    ImageView mCaptureImage;
    @BindView(R.id.record_video)
    ImageView mRecordVideo;
    @BindView(R.id.textureView)
    AutoFitTextureView mTextureView;
    @BindView(R.id.timer_text)
    TextView timerText;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    Context context;

    PreviewFileAdapter adapter;
    ArrayList<HashMap<String, Object>> photoVideoList;

    boolean isRecording = false, isPhotoCapturing = false;


    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    @BindView(R.id.car_image)
    ImageView carImage;
    private int mCaptureState = STATE_PREVIEW;


    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            connectCamera();
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
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            if (isRecording) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            } else {
                startPreview();
            }
            //Toast.makeText(getApplicationContext(),"Camera connection made!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };
    private String mCameraId;
    private Size mPreviewSize;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;
    private int mTotalRotation;

    private Size mImageSize;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new
            ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
                }
            };

    private class ImageSaver implements Runnable {

        private final Image mImage;

        public ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFileName);
                fileOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {

                private void process(CaptureResult captureResult) {
                    //Toast.makeText(getApplicationContext(), "ock", Toast.LENGTH_SHORT).show();
                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            // Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                //Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                                //startStillCaptureRequest();
                            }
                            startStillCaptureRequest();
                            break;
                    }
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    process(result);
                }
            };


    private CameraCaptureSession mRecordCaptureSession;
    private CameraCaptureSession.CaptureCallback mRecordCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {

                private void process(CaptureResult captureResult) {
                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            // Do nothing
                            break;
                        case STATE_WAIT_LOCK:

                            //Toast.makeText(getApplicationContext(), "DDDDD", Toast.LENGTH_SHORT).show();
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                //Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                                //startStillCaptureRequest();
                            }
                            startStillCaptureRequest();
                            break;
                    }
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    process(result);
                }
            };


    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private File mVideoFolder;
    private String mVideoFileName;
    private File mImageFolder;
    private String mImageFileName;

    private static final int MAX_VIDEO_DURATION = 10000;
    private static final int VIDEO_TYPE = 1;
    private static final int PHOTO_TYPE = 2;

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        context = this;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.REPORTING_MODE_SPECIAL_TRIGGER);

        if (sensor == null) {
            Toast.makeText(getApplicationContext(), "Sensor Error!", Toast.LENGTH_SHORT).show();
            finish();
        }

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(final SensorEvent event) {
                //Toast.makeText(getApplicationContext(), ""+event.values[0], Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        updateCar((float) Math.round(event.values[0]));
                    }
                }, 250);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        ;


        photoVideoList = new ArrayList<>();
        adapter = new PreviewFileAdapter(this, photoVideoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        createVideoFolder();
        createImageFolder();

        mMediaRecorder = new MediaRecorder();

        mRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPhotoCapturing = false;
                if (isRecording) {
                    stopVideoRecording();
                } else {
                    isRecording = true;
                    mRecordVideo.setImageResource(R.drawable.ic_videocam_on);
                    checkWriteStoragePermission();
                }
            }
        });

        mCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPhotoCapturing = true;
                checkWriteStoragePermission();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocas) {
        super.onWindowFocusChanged(hasFocas);
        View decorView = getWindow().getDecorView();
        if (hasFocas) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.AXIS_X);

        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();

        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isRecording) {
                    mMediaRecorder = new MediaRecorder();
                    //isRecording = true;
                    mRecordVideo.setImageResource(R.drawable.ic_videocam_on);
                    try {
                        createVideoFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startTimer();
                }
                Toast.makeText(this,
                        "Permission successfully granted!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this,
                        "App needs to save video to run", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        closeCamera();

        sensorManager.unregisterListener(sensorEventListener);
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if (swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mCameraId = cameraId;

                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this,
                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
                }

            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {

        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mRecordCaptureSession = session;
                            try {
                                mRecordCaptureSession.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(), null, null
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    }, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mPreviewCaptureSession = session;
                            try {
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(),
                                    "Unable to setup camera preview", Toast.LENGTH_SHORT).show();

                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest() {
        try {
            if (isRecording) {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            } else {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            }
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new
                    CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);

                            try {
                                createImageFileName();
                                //Toast.makeText(getApplicationContext(), "FFFFD", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(CameraActivity.this, mImageFileName, Toast.LENGTH_SHORT).show();
                                HashMap<String, Object> tempMap = new HashMap<>();
                                tempMap.put("type", PHOTO_TYPE);
                                tempMap.put("path", mImageFileName);
                                photoVideoList.add(0, tempMap);

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Do something after 100ms
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }, 800);


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };

            if (isRecording) {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
            } else {
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Video Recorder");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrienatation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrienatation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "VideoRecorderApp");
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void createImageFolder() {
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile, "VideoRecorderApp");
        if (!mImageFolder.exists()) {
            mImageFolder.mkdirs();
        }
    }

    private File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                if (isRecording && !isPhotoCapturing) {
                    isRecording = true;
                    mRecordVideo.setImageResource(R.drawable.ic_videocam_on);
                    try {
                        createVideoFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startRecord();
                    mMediaRecorder.start();

                    startTimer();
                } else {
                    lockFocus();
                }

            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {

            if (isRecording && !isPhotoCapturing) {
                isRecording = true;
                mRecordVideo.setImageResource(R.drawable.ic_videocam_on);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
                startTimer();
            } else {
                lockFocus();
            }
        }
    }

    CountDownTimer mCountDownTimer;

    private void startTimer() {

        timerText.setVisibility(View.VISIBLE);
        mCountDownTimer = new CountDownTimer((MAX_VIDEO_DURATION + 1000), 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timerText.setVisibility(View.GONE);
            }
        }.start();


    }

    private void setupMediaRecorder() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.setMaxDuration(MAX_VIDEO_DURATION);

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopVideoRecording();
                }
            }
        });
        mMediaRecorder.prepare();
    }

    private void stopVideoRecording() {

        timerText.setVisibility(View.GONE);
        mCountDownTimer.cancel();

        isRecording = false;
        mRecordVideo.setImageResource(R.drawable.ic_videocam_off);

        mMediaRecorder.stop();
        mMediaRecorder.reset();

        startPreview();

        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put("type", VIDEO_TYPE);
        tempMap.put("path", mVideoFileName);
        photoVideoList.add(0, tempMap);

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void lockFocus() {
        mCaptureState = STATE_WAIT_LOCK;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            if (isRecording) {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), mRecordCaptureCallback, mBackgroundHandler);
            } else {
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private boolean isFirstCar = true;
    private float initialReading = 0.0f;

    private void updateCar(float f) {
        //this.f1134a.setText("Heading: " + Float.toString(f) + " degrees");
        if (this.isFirstCar) {
            Log.e("CompassActivity", " degree value inside: " + f);
            this.initialReading = f;
            this.isFirstCar = false;
        }
        float f2 = f - this.initialReading;
        Log.e("ComapassActivity", "Current degree value: " + String.valueOf(this.initialReading) + " DIFF value: " + String.valueOf(f2));
        if (f2 <= 3.0f && f2 > -3.0f) {
            this.carImage.setImageResource(R.drawable.img44);
        } else if (f2 <= -3.0f && f2 > -9.0f) {
            this.carImage.setImageResource(R.drawable.img43);
        } else if (f2 <= -9.0f && f2 > -15.0f) {
            this.carImage.setImageResource(R.drawable.img42);
        } else if (f2 <= -15.0f && f2 > -21.0f) {
            this.carImage.setImageResource(R.drawable.img41);
        } else if (f2 <= -21.0f && f2 > -28.0f) {
            this.carImage.setImageResource(R.drawable.img40);
        } else if (f2 <= -28.0f && f2 > -35.0f) {
            this.carImage.setImageResource(R.drawable.img39);
        } else if (f2 <= -35.0f && f2 > -42.0f) {
            this.carImage.setImageResource(R.drawable.img38);
        } else if (f2 <= -42.0f && f2 > -49.0f) {
            this.carImage.setImageResource(R.drawable.img37);
        } else if (f2 <= -49.0f && f2 > -56.0f) {
            this.carImage.setImageResource(R.drawable.img36);
        } else if (f2 <= -56.0f && f2 > -63.0f) {
            this.carImage.setImageResource(R.drawable.img35);
        } else if (f2 <= -63.0f && f2 > -70.0f) {
            this.carImage.setImageResource(R.drawable.img34);
        } else if (f2 <= -70.0f && f2 > -77.0f) {
            this.carImage.setImageResource(R.drawable.img33);
        } else if (f2 <= -77.0f && f2 > -84.0f) {
            this.carImage.setImageResource(R.drawable.img32);
        } else if (f2 <= -84.0f && f2 > -91.0f) {
            this.carImage.setImageResource(R.drawable.img31);
        } else if (f2 <= -91.0f && f2 > -98.0f) {
            this.carImage.setImageResource(R.drawable.img30);
        } else if (f2 <= -98.0f && f2 > -105.0f) {
            this.carImage.setImageResource(R.drawable.img29);
        } else if (f2 <= -105.0f && f2 > -112.0f) {
            this.carImage.setImageResource(R.drawable.img28);
        } else if (f2 <= -112.0f && f2 > -119.0f) {
            this.carImage.setImageResource(R.drawable.img27);
        } else if (f2 <= -119.0f && f2 > -126.0f) {
            this.carImage.setImageResource(R.drawable.img26);
        } else if (f2 <= -126.0f && f2 > -133.0f) {
            this.carImage.setImageResource(R.drawable.img25);
        } else if (f2 <= -133.0f && f2 > -140.0f) {
            this.carImage.setImageResource(R.drawable.img24);
        } else if (f2 <= -140.0f && f2 > -147.0f) {
            this.carImage.setImageResource(R.drawable.img23);
        } else if (f2 <= -147.0f && f2 > -154.0f) {
            this.carImage.setImageResource(R.drawable.img22);
        } else if (f2 <= -154.0f && f2 > -161.0f) {
            this.carImage.setImageResource(R.drawable.img21);
        } else if (f2 <= -161.0f && f2 > -168.0f) {
            this.carImage.setImageResource(R.drawable.img20);
        } else if (f2 <= -168.0f && f2 > -175.0f) {
            this.carImage.setImageResource(R.drawable.img19);
        } else if (f2 <= -175.0f && f2 > -182.0f) {
            this.carImage.setImageResource(R.drawable.img18);
        } else if (f2 <= -182.0f && f2 > -189.0f) {
            this.carImage.setImageResource(R.drawable.img17);
        } else if (f2 <= -189.0f && f2 > -196.0f) {
            this.carImage.setImageResource(R.drawable.img16);
        } else if (f2 <= -196.0f && f2 > -203.0f) {
            this.carImage.setImageResource(R.drawable.img15);
        } else if (f2 <= -203.0f && f2 > -210.0f) {
            this.carImage.setImageResource(R.drawable.img14);
        } else if (f2 <= -210.0f && f2 > -217.0f) {
            this.carImage.setImageResource(R.drawable.img13);
        } else if (f2 <= -217.0f && f2 > -224.0f) {
            this.carImage.setImageResource(R.drawable.img12);
        } else if (f2 <= -224.0f && f2 > -231.0f) {
            this.carImage.setImageResource(R.drawable.img11);
        } else if (f2 <= -231.0f && f2 > -238.0f) {
            this.carImage.setImageResource(R.drawable.img10);
        } else if (f2 <= -238.0f && f2 > -245.0f) {
            this.carImage.setImageResource(R.drawable.img9);
        } else if (f2 <= -245.0f && f2 > -252.0f) {
            this.carImage.setImageResource(R.drawable.img8);
        } else if (f2 <= -252.0f && f2 > -259.0f) {
            this.carImage.setImageResource(R.drawable.img7);
        } else if (f2 <= -259.0f && f2 > -266.0f) {
            this.carImage.setImageResource(R.drawable.img6);
        } else if (f2 <= -266.0f && f2 > -273.0f) {
            this.carImage.setImageResource(R.drawable.img5);
        } else if (f2 <= -273.0f && f2 > -280.0f) {
            this.carImage.setImageResource(R.drawable.img4);
        } else if (f2 <= -280.0f && f2 > -287.0f) {
            this.carImage.setImageResource(R.drawable.img3);
        } else if (f2 <= -287.0f && f2 > -294.0f) {
            this.carImage.setImageResource(R.drawable.img2);
        } else if (f2 <= -294.0f && f2 > -301.0f) {
            this.carImage.setImageResource(R.drawable.img1);
        } else if (f2 <= -301.0f && f2 > -308.0f) {
            this.carImage.setImageResource(R.drawable.img52);
        } else if (f2 <= -308.0f && f2 > -315.0f) {
            this.carImage.setImageResource(R.drawable.img51);
        } else if (f2 <= -315.0f && f2 > -322.0f) {
            this.carImage.setImageResource(R.drawable.img50);
        } else if (f2 <= -322.0f && f2 > -329.0f) {
            this.carImage.setImageResource(R.drawable.img49);
        } else if (f2 <= -329.0f && f2 > -336.0f) {
            this.carImage.setImageResource(R.drawable.img48);
        } else if (f2 <= -336.0f && f2 > -344.0f) {
            this.carImage.setImageResource(R.drawable.img47);
        } else if (f2 <= -344.0f && f2 > -352.0f) {
            this.carImage.setImageResource(R.drawable.img46);
        } else if (f2 <= -352.0f && f2 >= -360.0f) {
            this.carImage.setImageResource(R.drawable.img45);
        } else if (f2 > 3.0f && f2 <= 9.0f) {
            this.carImage.setImageResource(R.drawable.img45);
        } else if (f2 > 9.0f && f2 <= 15.0f) {
            this.carImage.setImageResource(R.drawable.img46);
        } else if (f2 > 15.0f && f2 <= 21.0f) {
            this.carImage.setImageResource(R.drawable.img47);
        } else if (f2 > 21.0f && f2 <= 28.0f) {
            this.carImage.setImageResource(R.drawable.img48);
        } else if (f2 > 28.0f && f2 <= 35.0f) {
            this.carImage.setImageResource(R.drawable.img49);
        } else if (f2 > 35.0f && f2 < 42.0f) {
            this.carImage.setImageResource(R.drawable.img50);
        } else if (f2 >= 42.0f && f2 < 49.0f) {
            this.carImage.setImageResource(R.drawable.img51);
        } else if (f2 >= 49.0f && f2 < 56.0f) {
            this.carImage.setImageResource(R.drawable.img52);
        } else if (f2 >= 56.0f && f2 < 63.0f) {
            this.carImage.setImageResource(R.drawable.img1);
        } else if (f2 >= 63.0f && f2 < 70.0f) {
            this.carImage.setImageResource(R.drawable.img2);
        } else if (f2 >= 70.0f && f2 < 77.0f) {
            this.carImage.setImageResource(R.drawable.img3);
        } else if (f2 >= 77.0f && f2 < 84.0f) {
            this.carImage.setImageResource(R.drawable.img4);
        } else if (f2 >= 84.0f && f2 < 91.0f) {
            this.carImage.setImageResource(R.drawable.img5);
        } else if (f2 >= 91.0f && f2 < 98.0f) {
            this.carImage.setImageResource(R.drawable.img6);
        } else if (f2 >= 98.0f && f2 < 105.0f) {
            this.carImage.setImageResource(R.drawable.img7);
        } else if (f2 >= 105.0f && f2 < 112.0f) {
            this.carImage.setImageResource(R.drawable.img8);
        } else if (f2 >= 112.0f && f2 < 119.0f) {
            this.carImage.setImageResource(R.drawable.img9);
        } else if (f2 >= 119.0f && f2 < 126.0f) {
            this.carImage.setImageResource(R.drawable.img10);
        } else if (f2 >= 126.0f && f2 < 133.0f) {
            this.carImage.setImageResource(R.drawable.img11);
        } else if (f2 >= 133.0f && f2 < 140.0f) {
            this.carImage.setImageResource(R.drawable.img12);
        } else if (f2 >= 140.0f && f2 < 147.0f) {
            this.carImage.setImageResource(R.drawable.img13);
        } else if (f2 > 147.0f && f2 < 154.0f) {
            this.carImage.setImageResource(R.drawable.img14);
        } else if (f2 >= 154.0f && f2 < 161.0f) {
            this.carImage.setImageResource(R.drawable.img15);
        } else if (f2 >= 161.0f && f2 < 168.0f) {
            this.carImage.setImageResource(R.drawable.img16);
        } else if (f2 >= 168.0f && f2 < 175.0f) {
            this.carImage.setImageResource(R.drawable.img17);
        } else if (f2 >= 175.0f && f2 < 182.0f) {
            this.carImage.setImageResource(R.drawable.img18);
        } else if (f2 >= 182.0f && f2 < 189.0f) {
            this.carImage.setImageResource(R.drawable.img19);
        } else if (f2 >= 189.0f && f2 < 196.0f) {
            this.carImage.setImageResource(R.drawable.img20);
        } else if (f2 >= 196.0f && f2 < 203.0f) {
            this.carImage.setImageResource(R.drawable.img21);
        } else if (f2 >= 203.0f && f2 < 210.0f) {
            this.carImage.setImageResource(R.drawable.img22);
        } else if (f2 >= 210.0f && f2 < 217.0f) {
            this.carImage.setImageResource(R.drawable.img23);
        } else if (f2 >= 217.0f && f2 < 224.0f) {
            this.carImage.setImageResource(R.drawable.img24);
        } else if (f2 >= 224.0f && f2 < 231.0f) {
            this.carImage.setImageResource(R.drawable.img25);
        } else if (f2 >= 231.0f && f2 < 238.0f) {
            this.carImage.setImageResource(R.drawable.img26);
        } else if (f2 >= 238.0f && f2 < 245.0f) {
            this.carImage.setImageResource(R.drawable.img27);
        } else if (f2 >= 245.0f && f2 < 252.0f) {
            this.carImage.setImageResource(R.drawable.img28);
        } else if (f2 >= 252.0f && f2 < 259.0f) {
            this.carImage.setImageResource(R.drawable.img29);
        } else if (f2 >= 259.0f && f2 < 266.0f) {
            this.carImage.setImageResource(R.drawable.img30);
        } else if (f2 >= 266.0f && f2 < 273.0f) {
            this.carImage.setImageResource(R.drawable.img31);
        } else if (f2 >= 273.0f && f2 < 280.0f) {
            this.carImage.setImageResource(R.drawable.img32);
        } else if (f2 >= 280.0f && f2 < 287.0f) {
            this.carImage.setImageResource(R.drawable.img33);
        } else if (f2 >= 287.0f && f2 < 294.0f) {
            this.carImage.setImageResource(R.drawable.img34);
        } else if (f2 >= 294.0f && f2 < 301.0f) {
            this.carImage.setImageResource(R.drawable.img35);
        } else if (f2 >= 301.0f && f2 < 308.0f) {
            this.carImage.setImageResource(R.drawable.img36);
        } else if (f2 >= 308.0f && f2 < 315.0f) {
            this.carImage.setImageResource(R.drawable.img37);
        } else if (f2 >= 315.0f && f2 < 322.0f) {
            this.carImage.setImageResource(R.drawable.img38);
        } else if (f2 >= 322.0f && f2 < 329.0f) {
            this.carImage.setImageResource(R.drawable.img39);
        } else if (f2 >= 329.0f && f2 < 336.0f) {
            this.carImage.setImageResource(R.drawable.img40);
        } else if (f2 >= 336.0f && f2 < 344.0f) {
            this.carImage.setImageResource(R.drawable.img41);
        } else if (f2 >= 344.0f && f2 < 352.0f) {
            this.carImage.setImageResource(R.drawable.img42);
        } else if (f2 >= 352.0f && f2 <= 360.0f) {
            this.carImage.setImageResource(R.drawable.img43);
        }
    }
}
