package ru.ovision.detectsdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

import o.vision.facedetector.DetectResponse;
import o.vision.facedetector.OnDetect;
import o.vision.facedetector.OvisionFaceDetector;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private OvisionFaceDetector faceDetector;
    private long time;

    @Override
    protected void onSaveInstanceState(Bundle oldInstanceState) {
        super.onSaveInstanceState(oldInstanceState);
        oldInstanceState.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showCameraPreview();
    }

    public void startCamera() {
        faceDetector = findViewById(R.id.face_detector);
        TextView statusTextView = findViewById(R.id.status);
        time = System.currentTimeMillis();
        faceDetector.setApiToken(getString(R.string.OvisionApiKey));
        faceDetector.setDeviceName("DemoSDKExample");
        faceDetector.start(new OnDetect() {
            @Override
            public void detected(DetectResponse response) {
                String status = response.code;
                statusTextView.setTextColor(Color.WHITE);
                int flowTime = (int) (System.currentTimeMillis() - time);
                statusTextView.setVisibility(TextView.VISIBLE);
                switch (status) {
                    case "TAKE_SNAPSHOT":
                        statusTextView.setText("Делаем ваше фото");
                        break;
                    case "SEND_TO_API":
                        statusTextView.setText("Отправляем на сервер");
                        break;
                    case "NO_FACE":
                        statusTextView.setText("В кадре нет лица");
                        break;
                    case "FACE_NOT_VALID_FACE_IS_TOO_SMALL_ERROR":
                        statusTextView.setText("Вы слишком далеко");
                        break;
                    case "FACE_NOT_VALID_FACE_IS_TOO_BIG_ERROR":
                        statusTextView.setText("Вы слишком близко");
                        break;
                    case "FACE_NOT_VALID_FACE_IS_NOT_CENTERED_ERROR":
                        statusTextView.setText("Постарайтесь быть в центре кадра");
                        break;
                    case "FACE_NOT_VALID_LEFT_ROTATION":
                        statusTextView.setText("Поверните голову налево");
                        break;
                    case "FACE_NOT_VALID_RIGHT_ROTATION":
                        statusTextView.setText("Поверните голову направо.");
                        break;
                    case "FACE_NOT_VALID_MASK_ON_ERROR":
                    case "FACE_NOT_VALID_SEVERAL_FACES_ON_IMAGE_ERROR":
                    case "FACE_NOT_VALID_LANDMARKS_NOT_CORRECT_ERROR":
                    case "FACE_NOT_VALID_LANDMARKS_NOT_FOUND_ERROR":
                    case "FACE_IS_TOO_CLOSE_TO_ANOTHER_FACE_ERROR":
                    case "FACE_NOT_VALID_ANOTHER_PERSON_ON_IMAGE_ERROR":
                        statusTextView.setTextColor(Color.RED);
                        statusTextView.setText(status);
                        break;
                    case "FACE_NOT_VALID_UNKNOWN_ERROR":
                    case "FACE_NOT_VALID_NO_FACE_ON_IMAGE_ERROR":
                    case "FACE_NOT_VALID_SPOOF_ERROR":
                    case "INTERNAL_SERVER_ERROR":
                    case "IMAGE_CAPTURE_EXCEPTION":
                    case "REQUEST_TIMEOUT":
                    case "FAILED_TO_CONNECT_SERVER":
                    case "QUALITY_IS_NOT_GOOD":
                    case "UNCERTAINTY_IS_NOT_GOOD":
                    case "UNKNOWN_ERROR":
                    case "ACCESS_DENIED":
                        statusTextView.setTextColor(Color.RED);
                        statusTextView.setText(status + "\nМы вас не узнали" + "\nFlow Time: " + flowTime);
                        restart();
                        break;
                    case "SUCCESS":
                        statusTextView.setTextColor(Color.GREEN);

                        // Session detect id
                        String sid = response.data.getOrDefault("sid", "");

                        // User id
                        String userId = response.data.getOrDefault("userId", "");

                        // Detect image
                        byte[] imageBytes = response.imageBytes;

                        statusTextView.setText("Мы вас узнали"
                                + "\nFlow Time: " + flowTime
                                + "\nSid: " + sid);
                        restart();
                        break;
                    default:
                        statusTextView.setText(status);
                }
            }
        });
    };

    public void restart() {
        if (faceDetector != null) {
            Timer retryTimer = new Timer();
            retryTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    faceDetector.restart();
                    time = System.currentTimeMillis();
                }
            }, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }

    public void stop() {
        if (faceDetector != null) {
            faceDetector.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    private void showCameraPreview() {
        PackageManager pm = getPackageManager();

        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "Device not have camera hardware", Toast.LENGTH_LONG);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
}
