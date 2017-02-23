package com.jiadu.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/2/23.
 */
public class CleanFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        TextView tv = new TextView(getActivity());

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1,-1);

        tv.setLayoutParams(layoutParams);

        tv.setText("清洁的Fragment");
        tv.setTextSize(40);

        return tv;
    }
}
