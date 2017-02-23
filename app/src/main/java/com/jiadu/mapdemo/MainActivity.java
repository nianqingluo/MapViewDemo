package com.jiadu.mapdemo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.jiadu.fragment.CleanFragment;
import com.jiadu.fragment.MapFragment;
import com.jiadu.fragment.PowerFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private Button mBt_map;
    private Button mBt_clean;
    private Button mBt_power;
    private Map<String,Fragment> mFragmentMap;
    private FragmentManager mFM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

//        if (Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }

        initView();

        initData();
    }

    private void initData() {

        mFragmentMap = new HashMap<>();

        mFragmentMap.put("power",new PowerFragment());
        mFragmentMap.put("map",new MapFragment());
        mFragmentMap.put("clean",new CleanFragment());

        mFM = getFragmentManager();
    }

    private void initView() {

        mBt_power = (Button) findViewById(R.id.bt_power);
        mBt_clean = (Button) findViewById(R.id.bt_clean);
        mBt_map = (Button) findViewById(R.id.bt_map);

        mBt_power.setOnClickListener(this);
        mBt_clean.setOnClickListener(this);
        mBt_map.setOnClickListener(this);

    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.bt_power:{
                FragmentTransaction transaction = mFM.beginTransaction();
                transaction.replace(R.id.fl_fragment,mFragmentMap.get("power"));
                transaction.commit();
            }
            break;
            case R.id.bt_clean:{
                FragmentTransaction transaction = mFM.beginTransaction();
                transaction.replace(R.id.fl_fragment,mFragmentMap.get("clean"));
                transaction.commit();
            }
            break;
            case R.id.bt_map:{
                FragmentTransaction transaction = mFM.beginTransaction();
                transaction.replace(R.id.fl_fragment,mFragmentMap.get("map"));
                transaction.commit();
            }
                break;
            default:
            break;
        }
    }

    @Override
    public void finish() {
        super.finish();

        System.exit(2);
    }
}
