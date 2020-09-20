package kr.core.bowwow.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import kr.core.bowwow.R;
import kr.core.bowwow.activity.DictionaryDetailAct;
import kr.core.bowwow.dto.DictionaryData;
import kr.core.bowwow.utils.MyUtil;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.ViewHolder> {
    private Activity act;

    private ArrayList<DictionaryData> list;

    public DictionaryAdapter(Activity act, ArrayList<DictionaryData> list) {
        this.act = act;
        this.list = list;
    }

    public void setList(ArrayList<DictionaryData> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public ArrayList<DictionaryData> getList() {
        return list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dictionary, parent, false);
        DictionaryAdapter.ViewHolder viewHolder = new DictionaryAdapter.ViewHolder(view);

//        int height = (int) (((parent.getMeasuredWidth() - act.getResources().getDimensionPixelSize(R.dimen.dimen_40)) / 1.36) / 2);
//        int height = (parent.getMeasuredWidth() - act.getResources().getDimensionPixelSize(R.dimen.dimen_40)) / 2;
//
//        if (height <= 0) {
//            height = act.getResources().getDimensionPixelSize(R.dimen.dimen_117);
//        }
//
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.card_view.getLayoutParams();
//        params.height = height;

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryAdapter.ViewHolder holder, final int position) {
        final DictionaryData data = list.get(position);

        Glide.with(act)
                .load(data.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);

        holder.name.setText(data.getName());

        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.startActivity(new Intent(act, DictionaryDetailAct.class).putExtra("data", data));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        LinearLayout btn_detail;
        CardView card_view;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card_view = itemView.findViewById(R.id.card_view);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            btn_detail = itemView.findViewById(R.id.btn_detail);
        }
    }
}
