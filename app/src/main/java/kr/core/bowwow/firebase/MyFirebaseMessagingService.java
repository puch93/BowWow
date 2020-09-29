package kr.core.bowwow.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.FrontAd;
import kr.core.bowwow.activity.PushAct;
import kr.core.bowwow.dto.pref.SettingPref;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.utils.LayoutWebView;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.NotificationHelper;
import kr.core.bowwow.utils.StringUtil;

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    Context ctx;

    private final String _P_TYPE = "type";
    private final String _P_TITLE = "title";
    private final String _P_MESSAGE = "msg";
    private final String _P_IMG = "filename";
    private final String _P_LINK = "url";
    private final String _P_FRONT = "p_front";

    enum PushType {
        top,
        front,
        NORMAL,
        FRONT
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        ctx = getApplicationContext();
        Log.i(MyUtil.TAG, "push data: " + remoteMessage.getData());

        JSONObject dataList = new JSONObject(remoteMessage.getData());

        String idx = StringUtil.getStr(dataList, "idx");
        String msg = StringUtil.getStr(dataList, "msg");
        String url = StringUtil.getStr(dataList, "url");
        String send = StringUtil.getStr(dataList, "send");
        String type = StringUtil.getStr(dataList, "type");
        String title = StringUtil.getStr(dataList, "title");
        String regdate = StringUtil.getStr(dataList, "regdate");
        String senddate = StringUtil.getStr(dataList, "senddate");
        String img_url = StringUtil.getStr(dataList, "img_url");

        if (!StringUtil.isNull(type)) {
            if (type.equalsIgnoreCase("coupang_partners")) {
                Intent intent;
                intent = new Intent(getApplicationContext(), LayoutWebView.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", "push");
                startActivity(intent);
            } else {
                if (SettingPref.isPushReceive(this)) {
                    if (type.equals(PushType.top.name())) {
                        sendDefaultNotification(title, msg, url, img_url);
                    } else if (type.equals(PushType.front.name())) {
                        Intent intent = new Intent(this, FrontAd.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("type", _P_FRONT);
                        intent.putExtra("targeturl", url);     // 광고 연결주소
                        intent.putExtra("imgurl", NetUrls.MEDIADOMAIN + img_url);        // 광고 이미지

                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    private void sendDefaultNotification(String title, String message, String url, String imageUrl) {
        //매니저 설정
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        //채널설정
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "바우와우", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("바우와우 알림설정");

            notificationManager.createNotificationChannel(channel);
        }

        //인텐트 설정
        Intent intent = null;
        if (StringUtil.isNull(url)) {
            intent = new Intent(ctx, PushAct.class);
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        //노티 설정
        if (StringUtil.isNull(imageUrl)) {
            Notification notification = new NotificationCompat.Builder(ctx, "default")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();

            //푸시 날리기
            notificationManager.notify(0, notification);
        } else {
            Bitmap bitmap = getBitmapFromURL(NetUrls.MEDIADOMAIN + imageUrl);
            Notification notification = new NotificationCompat.Builder(ctx, "default")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null))
                    .setContentIntent(pendingIntent)
                    .build();
            //푸시 날리기
            notificationManager.notify(0, notification);
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        Log.i(StringUtil.TAG, "getBitmapFromURL: " + src);
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
            return null;
        }
    }

}
