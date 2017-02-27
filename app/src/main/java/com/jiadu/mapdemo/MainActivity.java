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
import com.jiadu.mapdemo.util.LogUtil;
import com.jiadu.mapdemo.util.SharePreferenceUtils;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    public static final String TAG_FRAGMENT_POWER = "power";  //电源FRAGMENT的TAG
    public static final String TAG_FRAGMENT_CLEAN = "clean";  //清洁FRAGMENT的TAG
    public static final String TAG_FRAGMENT_MAP = "map";      //地图FRAGMENT的TAG
    /**
     * 0代表1:10
     * 1代表1:100
     * 2代表1:1000
     * 3代表1:10000
     * 地图中每个小格代表10cm。与实际中对应的长度 l = 格数/比例
     */
    private int mMapScale= 0;

    private Button mBt_map;
    private Button mBt_clean;
    private Button mBt_power;
    private Map<String,Fragment> mFragmentMap;
    private FragmentManager mFM;
    private MapFragment mMf;

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

        mMapScale=SharePreferenceUtils.getInt(this, "scale");
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

        LogUtil.debugLog("hasFocus:"+hasFocus);

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
    public void setMapScale(int mapScale) {
        mMapScale = mapScale;

        SharePreferenceUtils.putInt(this,"scale",mapScale);
        
        if (mMf == null){

            mMf = (MapFragment) mFM.findFragmentByTag(MainActivity.TAG_FRAGMENT_MAP);
        }


        switch (mapScale){
            case 0:
                mMf.mTv_scale.setText("比例尺 "+"1:10");
            break;
            case 1:
                mMf.mTv_scale.setText("比例尺 "+"1:100");
            break;
            case 2:
                mMf.mTv_scale.setText("比例尺 "+"1:1000");
            break;
            case 3:
                mMf.mTv_scale.setText("比例尺 "+"1:10000");
            break;
            default:
            break;
        }
    }

    public int getMapScale() {
        return mMapScale;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.bt_power:{
                FragmentTransaction transaction = mFM.beginTransaction();
                transaction.replace(R.id.fl_fragment,mFragmentMap.get("power"), TAG_FRAGMENT_POWER);
                transaction.commit();
            }
            break;
            case R.id.bt_clean:{
                FragmentTransaction transaction = mFM.beginTransaction();
                transaction.replace(R.id.fl_fragment,mFragmentMap.get("clean"), TAG_FRAGMENT_CLEAN);
                transaction.commit();
            }
            break;
            case R.id.bt_map:{
                FragmentTransaction transaction = mFM.beginTransaction();
                transaction.replace(R.id.fl_fragment,mFragmentMap.get("map"), TAG_FRAGMENT_MAP);
                transaction.commit();
                if (mMf == null){
                    mMf = (MapFragment) mFragmentMap.get("map");
                }
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
