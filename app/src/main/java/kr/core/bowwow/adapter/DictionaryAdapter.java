package kr.core.bowwow.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.core.bowwow.R;
import kr.core.bowwow.dto.DictionaryData;

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
        DictionaryAdapter.ViewHolder viewHolder = new DictionaryAdapter.ViewHolder(parent);

        int height = (int) ((parent.getMeasuredWidth() - act.getResources().getDimensionPixelSize(R.dimen.dimen_40)) / 1.36);

        if (height <= 0) {
            height = act.getResources().getDimensionPixelSize(R.dimen.dimen_117);
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.image.getLayoutParams();
        params.height = height;

        return new DictionaryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryAdapter.ViewHolder holder, final int position) {
        DictionaryData data = list.get(position);

        Glide.with(act)
                .load(R.drawable.test)
                .into(holder.image);

        holder.name.setText(data.getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
        }
    }
}
