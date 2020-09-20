package kr.core.bowwow.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import kr.core.bowwow.R;
public class AllOfDecoration extends RecyclerView.ItemDecoration {
    private Activity act;
    private String type;

    public AllOfDecoration(Activity act, String type) {
        this.act = act;
        this.type = type;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);

        switch (type) {
            case "reg":
                if (position == 0) {
                    outRect.left = act.getResources().getDimensionPixelSize(R.dimen.dimen_16);
                }
                break;
        }
    }
}