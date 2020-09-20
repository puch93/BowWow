package kr.core.bowwow.customWidget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import kr.core.bowwow.R;
import kr.core.bowwow.utils.MyUtil;


public class CustomSpinnerAdapter extends BaseAdapter {

    Context context;
    List<String> data;
    LayoutInflater inflater;
    boolean isSelected = false;

    public CustomSpinnerAdapter(Context context, List<String> data){
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (data != null){
            return data.size();
        }else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.spinner_normal,parent,false);
        }

        ((ImageView) convertView.findViewById(R.id.iv_spinarrow)).setSelected(isSelected);

        if (data != null){
            String text = data.get(position);
            ((TextView)convertView.findViewById(R.id.tv_spintext)).setText(text);
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.spinner_dropdown,parent,false);
        }

        String text = data.get(position);

        int top = 0;
        int bottom = 0;
        if (position == 0){
            top = (int)MyUtil.convertDpToPixel((float) 8,context);
        }else{
            top = (int)MyUtil.convertDpToPixel((float) 4,context);
        }

        if (position < data.size()-1){
            bottom = (int)MyUtil.convertDpToPixel((float) 4,context);
        }else{
            bottom = (int)MyUtil.convertDpToPixel((float) 8,context);
        }

        ((LinearLayout) convertView.findViewById(R.id.ll_dropdown_area)).setPadding((int)MyUtil.convertDpToPixel((float) 16,context),top,(int)MyUtil.convertDpToPixel((float) 16,context),bottom);

//        if(position == 0) {
//            ((LinearLayout) convertView.findViewById(R.id.ll_dropdown_area)).setBackgroundResource(R.drawable.bg_set_boxback01);
//        }else{
//            ((LinearLayout) convertView.findViewById(R.id.ll_dropdown_area)).setBackgroundResource(R.drawable.bg_set_boxback02);
//        }
        ((TextView)convertView.findViewById(R.id.tv_dropdown_text)).setText(text);

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void isSelected(boolean isSelected){
        this.isSelected = isSelected;
    }

    public int getSelectedIdx(String obj){
        int idx = 0;
        for(int i = 0; i < data.size(); i++){
            if(data.get(i).equalsIgnoreCase(obj)){
                idx = i;
                break;
            }
        }
        return idx;
    }

}
