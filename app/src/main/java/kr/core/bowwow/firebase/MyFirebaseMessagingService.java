package kr.core.bowwow.firebase;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import kr.core.bowwow.activity.FrontAd;
import kr.core.bowwow.dto.pref.SettingPref;
import kr.core.bowwow.network.NetUrls;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.NotificationHelper;

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

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
//        {filename=, idx=1, msg=안녕하세요~ POWER LOTTO 입니다, url=http://naver.com, type=top}

//        {filename=/UPLOAD/push_msg/30726684_ODKZm4xG_8e029ecb-84b9-4e5a-81c1-dacfc39a3c74.jpg, idx=1, msg=위1, url=http://naver.com, type=top, regdate=0000-00-00 00:00:00, senddate=0000-00-00 00:00:00}
//        {filename=, idx=2, url=http://naver.com, type=front, regdate=0000-00-00 00:00:00, senddate=0000-00-00 00:00:00}
        Log.i(MyUtil.TAG, "push data: " + remoteMessage.getData());

        Map<String, String> dataList = remoteMessage.getData();

        if (SettingPref.isPushReceive(this)){

            // 전면, 상단 광고푸시 처리
            if (dataList != null) {
                //푸시 띄우기
                String type = dataList.get(_P_TYPE);

//            if (type != null && type.equals(PushType.NORMAL.name())) {
                if (type != null && type.equals(PushType.top.name())) {
//                String title = dataList.get(_P_TITLE);
                    String title = "바우와우";
                    String message = dataList.get(_P_MESSAGE);
                    String imgUrl = NetUrls.MEDIADOMAIN + dataList.get(_P_IMG);
                    String link = dataList.get(_P_LINK);

                    NotificationHelper helper = new NotificationHelper(getBaseContext());
                    helper.showNotification(title, message, imgUrl, link);
//            } else if (type != null && type.equals(PushType.FRONT.name())) {
                } else if (type != null && type.equals(PushType.front.name())) {

                    Intent intent = new Intent(this, FrontAd.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("type", _P_FRONT);
                    intent.putExtra("targeturl", dataList.get(_P_LINK));     // 광고 연결주소
                    intent.putExtra("imgurl", NetUrls.MEDIADOMAIN + dataList.get(_P_IMG));        // 광고 이미지

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
