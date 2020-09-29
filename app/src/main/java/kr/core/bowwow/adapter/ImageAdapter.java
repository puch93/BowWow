package kr.core.bowwow.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.core.bowwow.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Activity act;

    private ArrayList<String> list;
    ButtonClickListener buttonClickListener;

    public interface ButtonClickListener {
        void deleteClicked();
        void addClicked();
    }

    public ImageAdapter(Activity act, ArrayList<String> list, ButtonClickListener buttonClickListener) {
        this.act = act;
        this.list = list;
        this.buttonClickListener = buttonClickListener;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public ArrayList<String> getList() {
        return list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reg_image, parent, false);
        return new ImageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, final int position) {
        String data = list.get(position);

        if (position == 0) {
            holder.area_add.setVisibility(View.VISIBLE);
            holder.area_added.setVisibility(View.GONE);
            holder.btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (list.size() < 6)
                        buttonClickListener.addClicked();
                    else
                        Toast.makeText(act, "이미지는 최대 5장까지 등록 가능합니다", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.area_add.setVisibility(View.GONE);
            holder.area_added.setVisibility(View.VISIBLE);
            holder.btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.remove(position);
                    notifyDataSetChanged();
                    buttonClickListener.deleteClicked();
                }
            });

            Glide.with(act).load(data).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout area_add, area_added, btn_close;
        ImageView image, btn_add;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            area_add = itemView.findViewById(R.id.area_add);
            area_added = itemView.findViewById(R.id.area_added);
            btn_close = itemView.findViewById(R.id.btn_close);
            image = itemView.findViewById(R.id.image);
            btn_add = itemView.findViewById(R.id.btn_add);
        }
    }
}
