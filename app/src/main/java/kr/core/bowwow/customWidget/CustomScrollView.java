package kr.core.bowwow.customWidget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

import kr.core.bowwow.utils.MyUtil;

public class CustomScrollView extends ScrollView {

    OnBottomReachedListener mListener;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        View view = (View) getChildAt(getChildCount()-1);
        int diff = (view.getBottom()-(getHeight()+getScrollY()));

        Log.d(MyUtil.TAG, "onScrollChanged: "+getHeight()+getScrollY());
        Log.d(MyUtil.TAG, "getBottom: "+view.getBottom());

        if ((view.getBottom()/2 - 100 < diff) && (diff < view.getBottom()/2 + 100) && mListener != null) {
            mListener.onBottomReached();
        }
    }

    public OnBottomReachedListener getOnBottomReachedListener() {
        return mListener;
    }

    public void setOnBottomReachedListener(
            OnBottomReachedListener onBottomReachedListener) {
        mListener = onBottomReachedListener;
    }

    public interface OnBottomReachedListener{
        public void onBottomReached();
    }
}
