package kr.core.bowwow.adapter;


import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import kr.core.bowwow.fragments.ImageFrag;


public class ImagePagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<String> image_list = new ArrayList<>();

    public ImagePagerAdapter(FragmentManager fm, ArrayList<String> image_list) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.image_list = image_list;
    }

    public void setList(ArrayList<String> list) {
        this.image_list = list;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i) {
        ImageFrag frag = new ImageFrag();
        frag.setData(image_list.get(i));
        return frag;
    }

    @Override
    public int getCount() {
        return image_list.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }
}