package com.jiadu.mapdemo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jiadu.broadcast.MessageBroadcast;
import com.jiadu.fragment.CleanFragment;
import com.jiadu.fragment.MapFragment;
import com.jiadu.fragment.PowerFragment;
import com.jiadu.iinterface.IViewMainActivity;
import com.jiadu.mapdemo.util.LogUtil;
import com.jiadu.mapdemo.util.SharePreferenceUtils;
import com.jiadu.service.ServerSocketUtil;
import com.jiaud.Manager.ReceiveBrain;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements IViewMainActivity{


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
    private RadioGroup mRg;
    private TextView mTv_menu;
    private TextView mTv_position;
    private MessageBroadcast mMbReceive;
    private Intent mIntentService;

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
        
        startSocketService();
        
        registerSocketReceiver();
    }

    private void registerSocketReceiver() {

        mMbReceive = new MessageBroadcast();
        ReceiveBrain listener = ReceiveBrain.getInstance();
//        listener.setContext(this);
        mMbReceive.setListener(listener);

        IntentFilter filter =  new IntentFilter("com.jdrd.fragment.Map");

        registerReceiver(mMbReceive,filter);
    }

    private void startSocketService() {

        mIntentService = new Intent(this, ServerSocketUtil.class);

        startService(mIntentService);
    }

    private void initData() {

        mFragmentMap = new HashMap<>();

        mFragmentMap.put("power",new PowerFragment());
        mFragmentMap.put("map",new MapFragment());
        mFragmentMap.put("clean",new CleanFragment());

        mFM = getFragmentManager();

        mMapScale=SharePreferenceUtils.getInt(this, "scale");

        OpenPower();
    }

    private void initView() {

        mRg = (RadioGroup) findViewById(R.id.rg_mainactivity);
        mTv_menu = (TextView) findViewById(R.id.tv_mainactivity_menu);

        mTv_position = (TextView) findViewById(R.id.tv_mainactivity_position);

        mRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.gb_power:
                        OpenPower();
                        mTv_menu.setText("电源");
                    break;
                    case R.id.gb_clean:
                        OpenClean();
                        mTv_menu.setText("清洁");
                    break;
                    case R.id.gb_map:
                        OpenMap();
                        mTv_menu.setText("地图");
                    break;
                    case R.id.gb_setting:
                        OpenSetting();
                        mTv_menu.setText("设置");
                    break;
                    default:
                    break;
                }
            }
        });
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
        mMf.setRobotPointInfo();
    }

    public int getMapScale() {
        return mMapScale;
    }

    public int getMapScaleFactor(){

        int temp = 0;
        switch (mMapScale){
            case 0:
                temp= 10;
            break;
            case 1:
                temp= 100;

            break;
            case 2:
                temp= 1000;

            break;
            case 3:
                temp= 10000;

            break;
            default:
            break;
        }
        return temp;
    }

    @Override
    public void finish() {
        super.finish();

        unregisterReceiver(mMbReceive);

        stopService(mIntentService);

        System.exit(2);
    }

    @Override
    public void OpenPower() {
        FragmentTransaction transaction = mFM.beginTransaction();
        transaction.replace(R.id.fl_fragment,mFragmentMap.get("power"), TAG_FRAGMENT_POWER);
        transaction.commit();
    }

    @Override
    public void OpenClean() {
        FragmentTransaction transaction = mFM.beginTransaction();
        transaction.replace(R.id.fl_fragment,mFragmentMap.get("clean"), TAG_FRAGMENT_CLEAN);
        transaction.commit();
    }

    @Override
    public void OpenMap() {
        FragmentTransaction transaction = mFM.beginTransaction();
        transaction.replace(R.id.fl_fragment,mFragmentMap.get("map"), TAG_FRAGMENT_MAP);
        transaction.commit();
        if (mMf == null){
            mMf = (MapFragment) mFragmentMap.get("map");
        }
    }

    @Override
    public void OpenSetting() {

    }

    @Override
    public void setPosition(String position) {

        mTv_position.setText(position);
    }
}
