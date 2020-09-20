package kr.core.bowwow.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.MainActivity;
import kr.core.bowwow.audioRecord.AudioClipRecorder;
import kr.core.bowwow.detectApi.DetectorThread;
import kr.core.bowwow.detectApi.RecorderThread;
import kr.core.bowwow.dto.pref.UserPref;
import kr.core.bowwow.utils.MyUtil;

public class ForegroundService extends Service {
//    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String CHANNEL_ID = "BowWow";
    boolean heard = false;
    AudioClipRecorder recorder;

    public static final int DETECT_NONE = 0;
    public static final int DETECT_WHISTLE = 1;
    public static int selectedDetection = DETECT_NONE;

    // detection parameters
    private DetectorThread detectorThread;
    private RecorderThread recorderThread;

    @Override
    public void onCreate() {
        super.onCreate();
        recorder = new AudioClipRecorder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("바우와우")
//                .setContentText("소리 감지중...")
                .setContentText("소리 듣기 활성화")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.baseline_pets_black_24))
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.baseline_hearing_24))

//                .setSmallIcon(android.R.drawable.sym_def_app_icon)
//                .setSmallIcon(R.drawable.baseline_pets_black_18)
                .setSmallIcon(R.drawable.baseline_hearing_black_18)

//                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        selectedDetection = DETECT_WHISTLE;
        recorderThread = new RecorderThread();
        recorderThread.start();
        detectorThread = new DetectorThread(recorderThread);
        detectorThread.setOnSignalsDetectedListener(MainActivity.mainApp);
        detectorThread.start();

//        UserPref.setDetected(this,true);      서비스 시작시 상태적용(앱 실행시 다시 감지 / 상태에 따라 서비스 실행)

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (!heard){
//                    try
//                    {
//                        heard =
//                                recorder.startRecordingForTime(1000,
//                                        AudioClipRecorder.RECORDER_SAMPLERATE_8000,
//                                        AudioFormat.ENCODING_PCM_16BIT);
//                        break;
//                    } catch (IllegalStateException ie)
//                    {
//                        // failed to setup, sleep and try again
//                        // if still can't set it up, just fail
//                        try
//                        {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e)
//                        {
//                        }
//                    }
//                }
//            }
//        }).start();

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(MyUtil.TAG, "service onDestroy");
        if (recorderThread != null) {
            recorderThread.stopRecording();
            recorderThread = null;
        }
        if (detectorThread != null) {
            detectorThread.stopDetection();
            detectorThread = null;
        }
        selectedDetection = DETECT_NONE;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
