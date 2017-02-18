package com.jiadu.mapdemo;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.jiadu.mapdemo.util.ToastUtils;
import com.jiadu.mapdemo.view.MapView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBt_magnify;
    private Button mBt_reduce;
    private MapView mMapview;
    private Button mBt_centerpoint;
    private Button mBt_path;
    private boolean isSetPath;
    public int path = 1;
    public LinearLayout mLl_path;
    private Spinner mSp_path;
    private Button mBt_deletePath;
    private Spinner mSp_scale;
    private View mLl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        initView();

        initData();
    }

    private void initData() {


        //设置pathSpinner的数据
        String[] pathItem = getResources().getStringArray(R.array.pathspinner);
        ArrayAdapter<String> pathAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, pathItem);
        pathAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSp_path .setAdapter(pathAdapter);
        mSp_path.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                path = pos+1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        //设置scaleSpinner的数据
        String[] scaleItem = getResources().getStringArray(R.array.scalespinner);
        ArrayAdapter<String> scaleAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, scaleItem);
        pathAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSp_scale .setAdapter(scaleAdapter);
        mSp_scale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    private void initView() {

        mBt_magnify = (Button) findViewById(R.id.bt_magnify);

        mBt_reduce = (Button) findViewById(R.id.bt_reduce);

        mBt_centerpoint = (Button) findViewById(R.id.bt_centerpoint);

        mBt_path = (Button) findViewById(R.id.bt_path);

        mLl_path = (LinearLayout) findViewById(R.id.ll_path);

        mSp_path = (Spinner) findViewById(R.id.sp_path);

        mSp_scale = (Spinner) findViewById(R.id.sp_scale);

        mBt_deletePath = (Button) findViewById(R.id.bt_deletepath);

        mBt_magnify.setOnClickListener(this);
        mBt_reduce.setOnClickListener(this);
        mBt_centerpoint.setOnClickListener(this);
        mBt_path.setOnClickListener(this);
        mBt_deletePath.setOnClickListener(this);

        mMapview = (MapView) findViewById(R.id.mv_mapview);


        mLl = findViewById(R.id.ll);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus){
            if (mMapview.getCenterPoint()==null){

                mLl_path.setVisibility(View.GONE);
               }
           }

        int width = mLl.getWidth();
        int height = mLl.getHeight();
        
        System.out.println(width+":"+height);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.bt_magnify: {
              float scale = mMapview.getScale();

              if (scale == 1) {
                  mMapview.setScale(1.5f);
              } else if (scale == 1.5) {

                  mMapview.setScale(2.0f);
              } else {
                  ToastUtils.makeToast(this,"已经最大");
              }
            }
              break;
            case R.id.bt_reduce: {
                float scale = mMapview.getScale();

                if (scale == 2.0) {
                    mMapview.setScale(1.5f);
                } else if (scale == 1.5) {

                    mMapview.setScale(1.0f);
                } else {

                    ToastUtils.makeToast(this,"已经最小");
                }
            }
              break;

            case R.id.bt_centerpoint:

                mMapview.setCanSetCenterPoint(true);
                mMapview.setCanSetPath(false);
                ToastUtils.makeToast(this,"请在地图上设置原点位置");
                break;

            case R.id.bt_path:

                isSetPath = !isSetPath;
                mBt_path.setText(isSetPath?"完成设置":"设置路径");
                if (isSetPath){
                    mMapview.setCanSetPath(true);
                    mMapview.setCanSetCenterPoint(false);
                }
                else {
                    mMapview.setCanSetPath(false);
                }
                break;

            case R.id.bt_deletepath:
                if (!isSetPath){
                     ToastUtils.makeToast(this,"请先点击设置路径");
                    return;
                }

                mMapview.deletePathPoint(path);
                break;
              default:
              break;
        }
    }
}
