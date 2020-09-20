package kr.core.bowwow.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.core.bowwow.R;

public class NotificationHelper {
    Context ctx;
    NotificationManager manager;

    String _CHANNEL_ID = "0";
    String _CHANNEL_NAME = "bowwowad";

    int _NOTIFICATION_ID_NORMAL = 1;
    int _NOTIFICATION_ID_PICTURE = 2;

    Bitmap img = null;
    Bitmap background = null;

    public NotificationHelper(Context ctx){
        this.ctx = ctx;
        manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(_CHANNEL_ID, _CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("PowerLotto Notification Description");
            channel.enableVibration(true);
            channel.enableLights(true);
            //중복된 channel ID로 생성하면 아무 일도 일어나지 않음.
            manager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, String message, String imgUrl, String link) {
        if(MyUtil.isNull(title)){
            title = ctx.getResources().getString(R.string.app_name);
        }

        if(MyUtil.isNull(message)){
            message = "내용없음";
        }

        //PendingIntent
        final PendingIntent pIntent;
        if(MyUtil.isNull(link)){

            pIntent = null;
        }else{

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            pIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }

//        if(CommonPref.getNotiIdPref(ctx,"noti_id") == 100){
//            CommonPref.setNotiIdPref(ctx,"noti_id",0);
//        }
//        CommonPref.setNotiIdPref(ctx,"noti_id",CommonPref.getNotiIdPref(ctx,"noti_id")+1);
        if(MyUtil.isNull(imgUrl)) {
            showNormalNotification(title, message, pIntent, _NOTIFICATION_ID_NORMAL);
        }else{
            showPictureNotification(title, message, imgUrl, pIntent, _NOTIFICATION_ID_PICTURE);
        }
    }

    private void showNormalNotification(String title, String message, PendingIntent pIntent, int pushID) {
        final Notification.Builder builder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder = new Notification.Builder(ctx, _CHANNEL_ID);
        }else{
            builder = new Notification.Builder(ctx);
            builder.setDefaults(Notification.DEFAULT_ALL);
            builder.setPriority(Notification.PRIORITY_MAX);
        }
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setTicker(title);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_launcher));


        if(pIntent != null) {
            builder.setContentIntent(pIntent);
        }
        Notification notification = builder.build();
        manager.notify(pushID, builder.build());
    }

    private void showPictureNotification(String title, String message, final String imgUrl, PendingIntent pIntent, final int pushID){
//        final RemoteViews expendView = new RemoteViews(ctx.getPackageName(), R.layout.top_adview);

//        expendView.setImageViewBitmap(R.id.iv_noti_img_background,getBitmap(imgUrl));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, _CHANNEL_ID);
        builder.setTicker(title);
        builder.setAutoCancel(true);
        builder.setContentTitle(title);
        builder.setContentText(message);
//        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_launcher));
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), AudioManager.STREAM_NOTIFICATION);
        if(pIntent != null) {
            builder.setContentIntent(pIntent);
        }

//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
//            builder.setCustomBigContentView(expendView);
//        }else{
//            builder.setCustomContentView(expendView);
//        }

        final Notification notification = builder.build();

        manager.notify(pushID, notification);

    }

    private Bitmap getBitmap(String url) {
        URL imgUrl = null;
        HttpURLConnection connection = null;
        InputStream is = null;

        Bitmap retBitmap = null;

        try {
            imgUrl = new URL(url);
            connection = (HttpURLConnection) imgUrl.openConnection();
            connection.setDoInput(true);
            connection.connect();
            is = connection.getInputStream();
            Bitmap tmp = BitmapFactory.decodeStream(is);
            retBitmap = Bitmap.createScaledBitmap(tmp, tmp.getWidth(), tmp.getHeight(),true);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            return retBitmap;
        }
    }

    public Bitmap blurBitmap(Context context, Bitmap bitmap, float blurRadius) {
        if (bitmap.getConfig() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                bitmap.setConfig(Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            }

        }

        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);

        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        blurScript.setRadius(blurRadius);

        blurScript.setInput(allIn);

        blurScript.forEach(allOut);

        allOut.copyTo(outBitmap);

        bitmap.recycle();

        rs.destroy();

        return outBitmap;
    }
}

