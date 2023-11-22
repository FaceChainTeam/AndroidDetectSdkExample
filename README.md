# OVision Android SDK Android

## Installation

1. settings.gradle
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url "https://reposilite.o.vision/releases" // <-- Add this
        }
    }
}
```

2. app/build.gradle file
```gradle
android {
    // ...
    buildTypes.each {
        it.resValue 'string', 'OvisionApiKey', '<OVISION_API_KEY>' // <-- Add this and replace OVISION_API_KEY by your api key.
    }
    // ...
}

dependencies {
    // ...

    implementation "o.vision:facedetector:1.0.0" // <-- Add this
}
```

## Usage Example

```java
import o.vision.facedetector.DetectResponse;
import o.vision.facedetector.OnDetect;
import o.vision.facedetector.OvisionFaceDetector;

public class MainActivity extends AppCompatActivity {
    private OvisionFaceDetector faceDetector;
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        startCamera();
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

    public void stop() {
        if (faceDetector != null) {
            faceDetector.stop();
        }
    }
}
```

Layout example
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <o.vision.facedetector.OvisionFaceDetector
        android:id="@+id/face_detector"
        android:layout_height="match_parent"
        android:layout_width="match_parent" />

    <TextView
        android:background="@drawable/bg_rounded_sending"
        android:id="@+id/status"
        android:visibility="gone"
        android:textColor="#ff0000"
        android:paddingVertical="25dp"
        android:textAlignment="center"
        android:layout_gravity="bottom"
        android:layout_marginBottom="20dp"
        android:layout_marginHorizontal="20dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

</FrameLayout>
```
