package kr.core.bowwow.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devzone.fillprogresslayout.FillProgressLayout;

import java.util.ArrayList;

import kr.core.bowwow.R;
import kr.core.bowwow.app;
import kr.core.bowwow.dto.DogstatsItem;
import kr.core.bowwow.utils.MyUtil;

public class DogstatsAdapter extends RecyclerView.Adapter<DogstatsAdapter.ViewHolder>{

    private static final int NORMAL = 1;
    private static final int UNNORMAL = 2;

    Activity act;
    ArrayList<DogstatsItem> list;

    FillProgressLayout[] statsbar;

    public DogstatsAdapter(Activity act, ArrayList<DogstatsItem> list) {
        this.act = act;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = null;

        switch (viewType){
            case NORMAL:
                view = inflater.inflate(R.layout.item_dogcont01, parent, false);
                return new Cont01Holder(view);
            case UNNORMAL:
                view = inflater.inflate(R.layout.item_dogcont02, parent, false);
                return new Cont02Holder(view);
                default:
                    return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == NORMAL){
            Cont01Holder c1Holder = (Cont01Holder)holder;

            switch (list.get(position).getItemtype()){
                case "m1":
                    c1Holder.tv_breed.setVisibility(View.VISIBLE);
                    c1Holder.tv_breed.setText(list.get(position).getBreed());
                    c1Holder.tv_tabtitle.setBackgroundResource(R.drawable.pettranslate_bg_main_category01_200117);
                    break;
                case "m3":
                    c1Holder.tv_breed.setVisibility(View.GONE);
                    c1Holder.tv_tabtitle.setBackgroundResource(R.drawable.pettranslate_bg_main_category04_200117);
                    break;
                case "m4":
                    c1Holder.tv_breed.setVisibility(View.GONE);
                    c1Holder.tv_tabtitle.setBackgroundResource(R.drawable.pettranslate_bg_main_category03_200117);
                    break;
            }

            c1Holder.tv_tabtitle.setText(list.get(position).getMTitle());
            c1Holder.tv_content.setText(list.get(position).getExplanation());

        }else if (viewType == UNNORMAL){
            Cont02Holder cont02Holder= (Cont02Holder)holder;

            cont02Holder.tv_content.setText(list.get(position).getExplanation());
            cont02Holder.tv_s01num.setText(list.get(position).getStats01());
            cont02Holder.tv_s02num.setText(list.get(position).getStats02());
            cont02Holder.tv_s03num.setText(list.get(position).getStats03());
            cont02Holder.tv_s04num.setText(list.get(position).getStats04());

//            cont02Holder.stats01.setProgress(Integer.parseInt(list.get(position).getStats01()),true);
//            cont02Holder.stats01.setDuration(2500);
//            cont02Holder.stats02.setProgress(Integer.parseInt(list.get(position).getStats02()),true);
//            cont02Holder.stats02.setDuration(2500);
//            cont02Holder.stats03.setProgress(Integer.parseInt(list.get(position).getStats03()),true);
//            cont02Holder.stats03.setDuration(2500);
//            cont02Holder.stats04.setProgress(Integer.parseInt(list.get(position).getStats04()),true);
//            cont02Holder.stats04.setDuration(2500);

        }
    }

    public void progAni(DogstatsItem item){
        Log.d(MyUtil.TAG, "progAni: "+item);
        if (statsbar != null) {
            statsbar[0].setProgress(Integer.parseInt(item.getStats01()),true);
            statsbar[0].setDuration(2500);
            statsbar[1].setProgress(Integer.parseInt(item.getStats02()),true);
            statsbar[1].setDuration(2500);
            statsbar[2].setProgress(Integer.parseInt(item.getStats03()),true);
            statsbar[2].setDuration(2500);
            statsbar[3].setProgress(Integer.parseInt(item.getStats04()),true);
            statsbar[3].setDuration(2500);
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        DogstatsItem item = list.get(position);

        if (item.getItemtype().equalsIgnoreCase("m2")){
            return UNNORMAL;
        }else{
            return NORMAL;
        }

    }

    public class ViewHolder extends BaseViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public class Cont01Holder extends ViewHolder{
        TextView tv_breed,tv_tabtitle,tv_content;
        public Cont01Holder(View v){
            super(v);
            tv_breed = v.findViewById(R.id.tv_breed);
            tv_tabtitle = v.findViewById(R.id.tv_tabtitle);
            tv_content = v.findViewById(R.id.tv_content);

            tv_tabtitle.setTypeface(app.tf_bmjua);
        }
    }

    public class Cont02Holder extends ViewHolder{
        TextView tv_tabtitle,tv_content,tv_s01num,tv_s02num,tv_s03num,tv_s04num,tv_s01title,tv_s02title,tv_s03title,tv_s04title,
                tv_ptext1,tv_ptext2,tv_ptext3,tv_ptext4;
        FillProgressLayout stats01,stats02,stats03,stats04;

        public Cont02Holder(View v){
            super(v);
            tv_tabtitle = v.findViewById(R.id.tv_tabtitle);
            tv_content = v.findViewById(R.id.tv_content);
            tv_s01num = v.findViewById(R.id.tv_s01num);
            tv_s02num = v.findViewById(R.id.tv_s02num);
            tv_s03num = v.findViewById(R.id.tv_s03num);
            tv_s04num = v.findViewById(R.id.tv_s04num);

            tv_s01title = v.findViewById(R.id.tv_s01title);
            tv_s02title = v.findViewById(R.id.tv_s02title);
            tv_s03title = v.findViewById(R.id.tv_s03title);
            tv_s04title = v.findViewById(R.id.tv_s04title);

            tv_ptext1 = v.findViewById(R.id.tv_ptext1);
            tv_ptext2 = v.findViewById(R.id.tv_ptext2);
            tv_ptext3 = v.findViewById(R.id.tv_ptext3);
            tv_ptext4 = v.findViewById(R.id.tv_ptext4);

            stats01 = v.findViewById(R.id.stats01);
            stats02 = v.findViewById(R.id.stats02);
            stats03 = v.findViewById(R.id.stats03);
            stats04 = v.findViewById(R.id.stats04);

            statsbar = new FillProgressLayout[4];
            statsbar[0] = stats01;
            statsbar[1] = stats02;
            statsbar[2] = stats03;
            statsbar[3] = stats04;

            tv_tabtitle.setTypeface(app.tf_bmjua);
            tv_s01title.setTypeface(app.tf_bmjua);
            tv_s02title.setTypeface(app.tf_bmjua);
            tv_s03title.setTypeface(app.tf_bmjua);
            tv_s04title.setTypeface(app.tf_bmjua);
            tv_s01num.setTypeface(app.tf_bmjua);
            tv_s02num.setTypeface(app.tf_bmjua);
            tv_s03num.setTypeface(app.tf_bmjua);
            tv_s04num.setTypeface(app.tf_bmjua);
            tv_ptext1.setTypeface(app.tf_bmjua);
            tv_ptext2.setTypeface(app.tf_bmjua);
            tv_ptext3.setTypeface(app.tf_bmjua);
            tv_ptext4.setTypeface(app.tf_bmjua);

        }
    }

}
