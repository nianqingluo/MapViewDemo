package com.jiadu.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jiadu.bean.IMUDataBean;
import com.jiadu.iinterface.IPresent;
import com.jiadu.iinterface.IView;
import com.jiadu.impl.MapFragmentPresentImpl;
import com.jiadu.mapdemo.R;
import com.jiadu.mapdemo.util.ToastUtils;
import com.jiadu.mapdemo.view.MapView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/2/23.
 */
public class MapFragment extends Fragment implements View.OnClickListener, IView {

    private final int UPDATETV = 1;
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
    private TextView mTv_linearacc_x;
    private TextView mTv_linearacc_y;
    private TextView mTv_linearacc_z;
    private TextView mTv_anglevelocit_y;
    private TextView mTv_anglevelocit_z;
    private TextView mTv_anglevelocit_x;
    private TextView mTv_magnet_x;
    private TextView mTv_magnet_y;
    private TextView mTv_magnet_z;
    private TextView mTv_pitch;
    private TextView mTv_roll;
    private TextView mTv_yaw;
    private TextView mTv_pressure;
    private IPresent mIPresent ;
    private Timer mTimer;
    private View mRootView;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATETV:

                    if (msg.obj == null){

                        updata13TV(null);
                    }else {

                        IMUDataBean bean = (IMUDataBean) msg.obj;

                        updata13TV(bean);

                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mRootView!=null){

            return mRootView;
        }

        mRootView = inflater.inflate(R.layout.fragment_map, null);

        initView();

        initData();

        return mRootView;
    }

    private void initData() {

        mIPresent = new MapFragmentPresentImpl(this);

        //设置pathSpinner的数据
        String[] pathItem = getResources().getStringArray(R.array.pathspinner);
        ArrayAdapter<String> pathAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, pathItem);
        pathAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSp_path .setAdapter(pathAdapter);
        mSp_path.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                path = pos+1;
                mMapview.setPathNum(path);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        //设置scaleSpinner的数据
        String[] scaleItem = getResources().getStringArray(R.array.scalespinner);
        ArrayAdapter<String> scaleAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, scaleItem);
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

        mTv_linearacc_x = (TextView) findViewById(R.id.tv_linearacc_x);
        mTv_linearacc_y = (TextView) findViewById(R.id.tv_linearacc_y);
        mTv_linearacc_z = (TextView) findViewById(R.id.tv_linearacc_z);

        mTv_anglevelocit_x = (TextView) findViewById(R.id.tv_anglevelocity_x);
        mTv_anglevelocit_y = (TextView) findViewById(R.id.tv_anglevelocity_y);
        mTv_anglevelocit_z = (TextView) findViewById(R.id.tv_anglevelocity_z);

        mTv_magnet_x = (TextView) findViewById(R.id.tv_magnet_x);
        mTv_magnet_y = (TextView) findViewById(R.id.tv_magnet_y);
        mTv_magnet_z = (TextView) findViewById(R.id.tv_magnet_z);

        mTv_pitch = (TextView) findViewById(R.id.tv_pitch);
        mTv_roll = (TextView) findViewById(R.id.tv_roll);
        mTv_yaw = (TextView) findViewById(R.id.tv_yaw);

        mTv_pressure = (TextView) findViewById(R.id.tv_pressure);

        mBt_magnify.setOnClickListener(this);
        mBt_reduce.setOnClickListener(this);
        mBt_centerpoint.setOnClickListener(this);
        mBt_path.setOnClickListener(this);
        mBt_deletePath.setOnClickListener(this);

        mMapview = (MapView) findViewById(R.id.mv_mapview);
    }

    private View findViewById(int viewId) {

        View viewById = mRootView.findViewById(viewId);

        return viewById;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapview.getCenterPoint()==null){

            mLl_path.setVisibility(View.GONE);

        }

        try {
            mIPresent.openIMUSerialPort();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        mTimer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                IMUDataBean imuDataBean = mIPresent.getIMUDataBean();

                Message msg = Message.obtain(mHandler,UPDATETV);

                msg.obj = imuDataBean;

                msg.sendToTarget();
            }
        };

        mTimer.schedule(task,0,200);
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
                    ToastUtils.makeToast(getActivity(),"已经最大");
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

                    ToastUtils.makeToast(getActivity(),"已经最小");
                }
            }
            break;

            case R.id.bt_centerpoint:

                mMapview.setCanSetCenterPoint(true);
                mMapview.setCanSetPath(false);
                ToastUtils.makeToast(getActivity(),"请在地图上设置原点位置");
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
                    ToastUtils.makeToast(getActivity(),"请先点击设置路径");
                    return;
                }
                mMapview.deletePathPoint(path);
                break;
            default:
                break;
        }

    }

    @Override
    public void onPause() {
        mIPresent.closeIMUSerialPort();
        mTimer.cancel();
        mTimer=null;
        super.onPause();
    }

    @Override
    public void updata13TV(IMUDataBean bean) {

        if (bean == null){

            mTv_linearacc_x.setText("---");
            mTv_linearacc_y.setText("---");
            mTv_linearacc_z.setText("---");

            mTv_anglevelocit_x.setText("---");
            mTv_anglevelocit_y.setText("---");
            mTv_anglevelocit_z.setText("---");

            mTv_magnet_x.setText("---");
            mTv_magnet_y.setText("---");
            mTv_magnet_z.setText("---");

            mTv_pitch.setText("---");
            mTv_roll.setText("---");
            mTv_yaw.setText("---");

            mTv_pressure.setText("---");


        }else {

            mTv_linearacc_x.setText(bean.linearAcc[0] + "");
            mTv_linearacc_y.setText(bean.linearAcc[1] + "");
            mTv_linearacc_z.setText(bean.linearAcc[2] + "");

            mTv_anglevelocit_x.setText(bean.angleVelocity[0] + "");
            mTv_anglevelocit_y.setText(bean.angleVelocity[1] + "");
            mTv_anglevelocit_z.setText(bean.angleVelocity[2] + "");

            mTv_magnet_x.setText(bean.magnet[0] + "");
            mTv_magnet_y.setText(bean.magnet[1] + "");
            mTv_magnet_z.setText(bean.magnet[2] + "");

            mTv_pitch.setText(bean.pose[0] + "°");
            mTv_roll.setText(bean.pose[1] + "°");
            mTv_yaw.setText(bean.pose[2] + "°");

            mTv_pressure.setText(bean.pressure + "Pa");
        }

    }
}
