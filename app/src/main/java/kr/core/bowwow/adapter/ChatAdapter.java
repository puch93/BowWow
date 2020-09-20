package kr.core.bowwow.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.core.bowwow.R;
import kr.core.bowwow.app;
import kr.core.bowwow.customWidget.VisualizerView;
import kr.core.bowwow.dto.ChatItem;
import kr.core.bowwow.utils.DBHelper;
import kr.core.bowwow.utils.MyUtil;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final int MYMSG = 1;
    private static final int DOGMSG = 2;

    Activity act;
    ArrayList<ChatItem> list;

    int prePos = -1;
    int currPos = -1;

    MediaPlayer mediaPlayer;
    boolean mpStop = false;

    private static final float VISUALIZER_HEIGHT_DIP = 50f;

    private Visualizer mVisualizer;
    private VisualizerView mVisualizerView;

    public ChatAdapter(Activity act, ArrayList<ChatItem> list) {
        this.act = act;
        this.list = list;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mVisualizerView = new VisualizerView(act);
//        mpStop = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = null;

        switch (viewType) {
            case MYMSG:
                view = inflater.inflate(R.layout.item_mymsg, parent, false);
                return new MymsgHolder(view);
            case DOGMSG:
                view = inflater.inflate(R.layout.item_dogmsg, parent, false);
                return new DogmsgHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int pos) {
        int viewType = getItemViewType(pos);

        if (viewType == MYMSG) {
            final MymsgHolder myHolder = (MymsgHolder) holder;

            myHolder.tv_mymsg.setText(list.get(pos).getT_msg());


            SimpleDateFormat orgin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd a hh:mm");

            Date date = null;
            try {
                Date old = orgin.parse(list.get(pos).getT_regdate());
                myHolder.tv_regdate.setText(sdf.format(old).replaceFirst(" ", "\n"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            myHolder.tv_regdate.setText(list.get(pos).getT_regdate());
//            myHolder.tv_playtime.setText(MyUtil.getTime(list.get(pos).getDuration()));

            if (list.get(pos).isSelected()) {
                app.isTrans = true;
                myHolder.btn_playstop.setSelected(true);

                myHolder.tv_playtime.setText(list.get(pos).getCurrTime());

                myHolder.drawview.setVisibility(View.VISIBLE);
                if (mVisualizerView.getParent() != null) {
                    ((ViewGroup) mVisualizerView.getParent()).removeView(mVisualizerView);
                }
                if (myHolder.drawview.getChildCount() == 0) {
                    myHolder.drawview.addView(mVisualizerView);
                }
                mVisualizerView.updateVisualizer(list.get(pos).getBytes());

            } else {
                myHolder.btn_playstop.setSelected(false);
//                app.isTrans = false;
                myHolder.tv_playtime.setText(list.get(pos).getDuration());
                myHolder.drawview.setVisibility(View.INVISIBLE);
                myHolder.drawview.removeAllViews();
//                myHolder.drawview.addView();
//                mVisualizerView.
            }


        } else if (viewType == DOGMSG) {
            final DogmsgHolder dogHolder = (DogmsgHolder) holder;

            dogHolder.tv_dogname.setText(app.myDogKname);
            dogHolder.tv_dogmsg.setText(list.get(pos).getT_msg());

            SimpleDateFormat orgin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd a hh:mm");

            Date date = null;
            try {
                Date old = orgin.parse(list.get(pos).getT_regdate());
                dogHolder.tv_regdate.setText(sdf.format(old).replaceFirst(" ", "\n"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

//            dogHolder.tv_regdate.setText(list.get(pos).getT_regdate());
//            dogHolder.tv_playtime.setText(MyUtil.getTime(list.get(pos).getDuration()));

            Glide.with(act)
                    .load(app.myDogImg)
                    .into(dogHolder.iv_dogimg);

            if (list.get(pos).isSelected()) {
                dogHolder.btn_playstop.setSelected(true);
                dogHolder.tv_playtime.setText(list.get(pos).getCurrTime());
                dogHolder.drawview.setVisibility(View.VISIBLE);

                if (mVisualizerView.getParent() != null) {
                    ((ViewGroup) mVisualizerView.getParent()).removeView(mVisualizerView);
                }
                if (dogHolder.drawview.getChildCount() == 0) {
                    dogHolder.drawview.addView(mVisualizerView);
                }
//                dogHolder.drawview.addView(mVisualizerView);
                mVisualizerView.updateVisualizer(list.get(pos).getBytes());
            } else {
                dogHolder.btn_playstop.setSelected(false);
                dogHolder.tv_playtime.setText(list.get(pos).getDuration());
                dogHolder.drawview.setVisibility(View.INVISIBLE);
                dogHolder.drawview.removeAllViews();
            }

        }

    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        ChatItem data = list.get(position);

        if (data.getT_type().equals(MyUtil.DOG)) {
            return DOGMSG;
        } else {
            return MYMSG;
        }
    }

    public void stopMediaplayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Thread.interrupted();

            DBHelper db = new DBHelper();
            if (app.chatItems.size() > 0) {
                app.chatItems.clear();
            }
            app.chatItems.addAll(db.getChatList(act));
            app.isTrans = false;
        }

//        if (mediaPlayer != null) {
//            if (mediaPlayer.isPlaying()) {
//                mpStop = true;
//                app.isTrans = false;
//                mediaPlayer.stop();
//                mediaPlayer.release();
//                Thread.currentThread().interrupt();
//            }
//        }
    }

    public class ViewHolder extends BaseViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public class MymsgHolder extends ViewHolder implements View.OnClickListener {
//    public class MymsgHolder extends ViewHolder {

        TextView tv_mymsg, tv_regdate, tv_playtime;
        ImageView btn_playstop;
        LinearLayout drawview, all_area;

        public MymsgHolder(View v) {
            super(v);

            tv_mymsg = v.findViewById(R.id.tv_mymsg);
            tv_regdate = v.findViewById(R.id.tv_regdate);
            tv_playtime = v.findViewById(R.id.tv_playtime);
            btn_playstop = v.findViewById(R.id.btn_playstop);
            drawview = v.findViewById(R.id.drawview);
            all_area = v.findViewById(R.id.all_area);

            all_area.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.all_area:
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    }
                    currPos = getAdapterPosition();
                    if (prePos != -1) {
                        if (prePos != getAdapterPosition()) {
                            if (list.get(prePos).isSelected()) {
                                list.get(prePos).setSelected(false);
                                if (list.get(prePos).isPlay()) {
                                    mediaPlayer.stop();
                                    mediaPlayer.reset();
                                    mVisualizer.setEnabled(false);
                                    list.get(prePos).setPlay(false);
                                }
                                notifyItemChanged(prePos, list.get(prePos).isSelected());
                            }
                        }
                    }
                    list.get(getAdapterPosition()).setSelected(!list.get(getAdapterPosition()).isSelected());
                    notifyItemChanged(getAdapterPosition(), list.get(getAdapterPosition()).isSelected());
                    if (list.get(getAdapterPosition()).isSelected()) {
                        app.isTrans = true;

                        try {
                            mediaPlayer.setDataSource(list.get(getAdapterPosition()).getT_sound());
                            mediaPlayer.prepare();      // 오류
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        drawview.addView(mVisualizerView);

                        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
                        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

                        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                            @Override
                            public void onWaveFormDataCapture(Visualizer visualizer, final byte[] bytes, int i) {
                                if (mVisualizer.getEnabled()) {
//                                    mVisualizerView.updateVisualizer(bytes);
                                    list.get(currPos).setBytes(bytes);
                                    notifyItemChanged(currPos, list.get(currPos).getBytes());
                                }
                            }

                            @Override
                            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                            }
                        }, Visualizer.getMaxCaptureRate() / 2, true, false);
                        mVisualizer.setEnabled(true);

                        mediaPlayer.start();
                        list.get(getAdapterPosition()).setPlay(true);
                        list.get(currPos).setCurrTime(MyUtil.getTime(String.valueOf(0)));

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                list.get(currPos).setSelected(false);
                                mediaPlayer.reset();
                                if (mVisualizer != null) {
                                    mVisualizer.setEnabled(false);
                                }
                                app.isTrans = false;
                            }
                        });

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer.isPlaying()) {
                                    while (!Thread.currentThread().isInterrupted()) {
//                                    Log.d(MyUtil.TAG, "playtime: "+mediaPlayer.getDuration() + " / " + mediaPlayer.getCurrentPosition());

                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        if (mediaPlayer != null) {
                                            list.get(currPos).setCurrTime(MyUtil.getTime(String.valueOf(mediaPlayer.getCurrentPosition())));
                                            act.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    notifyItemChanged(currPos, list.get(currPos).getCurrTime());
                                                }
                                            });
                                        }

                                    }
                                }
                            }
                        }).start();

                    } else {
//                        app.isTrans = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mVisualizer.setEnabled(false);
                        Thread.currentThread().interrupt();
                        list.get(getAdapterPosition()).setPlay(false);
                        app.isTrans = false;
                    }

                    prePos = getAdapterPosition();
                    break;
            }

        }

    }

    public boolean checkPlay() {
        for (ChatItem item : list) {
            Log.d(MyUtil.TAG, "checkPlay: " + item.isPlay());
            if (item.isPlay()) {
                return true;
            }
        }
        return false;
    }

    public class DogmsgHolder extends ViewHolder implements View.OnClickListener {
//    public class DogmsgHolder extends ViewHolder {

        TextView tv_dogname, tv_dogmsg, tv_regdate, tv_playtime;
        ImageView iv_dogimg, btn_playstop;
        LinearLayout drawview, all_area;

        public DogmsgHolder(View v) {
            super(v);
            tv_dogname = v.findViewById(R.id.tv_dogname);
            tv_dogmsg = v.findViewById(R.id.tv_dogmsg);
            tv_regdate = v.findViewById(R.id.tv_regdate);
            tv_playtime = v.findViewById(R.id.tv_playtime);
            iv_dogimg = v.findViewById(R.id.iv_dogimg);
            btn_playstop = v.findViewById(R.id.btn_playstop);
            drawview = v.findViewById(R.id.drawview);
            all_area = v.findViewById(R.id.all_area);

            iv_dogimg.setClipToOutline(true);
            all_area.setOnClickListener(this);

//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.all_area:
                    Log.d(MyUtil.TAG, "getAdapterPosition: " + getAdapterPosition());
                    currPos = getAdapterPosition();

                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    }

                    if (prePos != -1) {
                        if (prePos != getAdapterPosition()) {
                            if (list.get(prePos).isSelected()) {
                                list.get(prePos).setSelected(false);
                                if (list.get(prePos).isPlay()) {
                                    mediaPlayer.stop();
                                    mediaPlayer.reset();
                                    mVisualizer.setEnabled(false);
                                    list.get(prePos).setPlay(false);
                                }
                                notifyItemChanged(prePos, list.get(prePos).isSelected());
                            }
                        }
                    }
                    list.get(getAdapterPosition()).setSelected(!list.get(getAdapterPosition()).isSelected());
                    notifyItemChanged(getAdapterPosition(), list.get(getAdapterPosition()).isSelected());
                    if (list.get(getAdapterPosition()).isSelected()) {
                        app.isTrans = true;

                        try {
                            mediaPlayer.setDataSource(list.get(getAdapterPosition()).getT_sound());
                            mediaPlayer.prepare();      // 오류
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

//                        drawview.addView(mVisualizerView);

                        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
                        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

                        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                            @Override
                            public void onWaveFormDataCapture(Visualizer visualizer, final byte[] bytes, int i) {
                                if (mVisualizer.getEnabled()) {
//                                    mVisualizerView.updateVisualizer(bytes);
                                    list.get(currPos).setBytes(bytes);
                                    notifyItemChanged(currPos, list.get(currPos).getBytes());
                                }
                            }

                            @Override
                            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                            }
                        }, Visualizer.getMaxCaptureRate() / 2, true, false);
                        mVisualizer.setEnabled(true);

                        mediaPlayer.start();
                        list.get(currPos).setCurrTime(MyUtil.getTime(String.valueOf(0)));
                        list.get(getAdapterPosition()).setPlay(true);

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                list.get(currPos).setSelected(false);
                                mediaPlayer.reset();
                                if (mVisualizer != null) {
                                    mVisualizer.setEnabled(false);
                                }
                                app.isTrans = false;
                            }
                        });

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer.isPlaying()) {
                                    while (!Thread.currentThread().isInterrupted()) {
//                                    Log.d(MyUtil.TAG, "playtime: "+mediaPlayer.getDuration() + " / " + mediaPlayer.getCurrentPosition());

                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        if (mediaPlayer != null) {
                                            list.get(currPos).setCurrTime(MyUtil.getTime(String.valueOf(mediaPlayer.getCurrentPosition())));
                                            act.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    notifyItemChanged(currPos, list.get(currPos).getCurrTime());
                                                }
                                            });
                                        }

                                    }
                                }
                            }
                        }).start();

                    } else {
//                        app.isTrans = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mVisualizer.setEnabled(false);
                        Thread.currentThread().interrupt();
                        list.get(getAdapterPosition()).setPlay(false);
                        app.isTrans = false;
                    }
                    prePos = getAdapterPosition();
                    break;
            }
        }
    }

}
