package com.jiadu.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jiadu.bean.IMUDataBean;
import com.jiadu.dialog.ChoicePathDialog;
import com.jiadu.dialog.ChoiceScaleDialog;
import com.jiadu.iinterface.IMapFragmentPresent;
import com.jiadu.iinterface.IMapView;
import com.jiadu.impl.MapFragmentPresentImpl;
import com.jiadu.mapdemo.MainActivity;
import com.jiadu.mapdemo.R;
import com.jiadu.mapdemo.util.Constant;
import com.jiadu.mapdemo.util.LogUtil;
import com.jiadu.mapdemo.util.SharePreferenceUtils;
import com.jiadu.mapdemo.util.ToastUtils;
import com.jiadu.mapdemo.util.TurnAngleUtil;
import com.jiadu.mapdemo.view.MapView;
import com.jiaud.Manager.Brain;
import com.jiaud.Manager.ReceiveBrain;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/2/23.
 */
public class MapFragment extends Fragment implements View.OnClickListener, IMapView {

    public static final String WALKMODEL = "walkmodel";
    private final int UPDATETV = 1;//用于handler
    private Button mBt_magnify;
    private Button mBt_reduce;
    private MapView mMapview;
    private Button mBt_centerpoint;
    private Button mBt_setpath;
    private boolean isSetPath;
    private boolean hasShowScaleInfo = false;

    private Brain mBrain = Brain.getInstance();

    /**
     * 0代表当前线路循环
     * 1代表有序线路循环
     * 2代表随机线路循环
     */
    private int walkModel = 0;

    /**
     * 1:代表线路 1
     * 2:代表线路 2
     * 3:代表线路 3
     */
    public int mPath = 1;



    private Button mBt_choicepath;
    private Button mBt_deletePath;
    private Button mBt_scale;
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
    private IMapFragmentPresent mIMapFragmentPresent;
    private Timer mTimer;
    private View mRootView;

    public float currentAngle = 0;
    private Button mBt_setrobotpoint;
    public TextView mTv_scale;

    private DecimalFormat mFormat = new DecimalFormat("0.0");

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATETV:

                    if (msg.obj == null){

                        updata13TV(null);
                    }else {

                        IMUDataBean bean = (IMUDataBean) msg.obj;

                        currentAngle = bean.pose[2];

                        updata13TV(bean);

                        mMapview.setDirectionAngle(bean.pose[2]);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private EditText mEt_robot_x;
    private EditText mEt_robot_y;
    private Button mBt_confirmrobotpoint;
    private MainActivity mActivity;
    private TextView mTv_path;
    private Button mBt_operate;
    private Button mBt_path_layout;
    private Button mBt_walk;
    private Button mBt_map_backup;
    private boolean operateShowing =false;
    private boolean pathLayoutShowing = false;
    private boolean walkShowing = false;
    private LinearLayout mLl_operate;
    private LinearLayout mLl_path_layout;
    private LinearLayout mLl_walk;
    private RadioGroup mRg_walkmodel;
    public View mLl_path;
    private View mLl_setpathpoint;
    private EditText mEt_pathpoint_x;
    private EditText mEt_pathpoint_y;
    private Button mBt_confirmpathpoint;
    private Button mBt_startwalk;
    private Button mBt_stopwalk;

    public Point tempIntermediate = null;


    /**
     * 0:代表上
     * 1:代表下
     * 2:代表左
     * 3:代表右
     */
    private int direction = 0;


    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public float nextDirection = 0;//旋转之后的角度
    public void setNextDirection(float nextDirection) {
        this.nextDirection = nextDirection;
    }




    public void setPath(int path) {
        this.mPath = path;
        mMapview.setPathNum(path);

        switch (path){
            case 1:
                mTv_path.setTextColor(Constant.PATHCOLOR1);
                mTv_path.setText("线路: 1");

            break;
            case 2:
                mTv_path.setTextColor(Constant.PATHCOLOR2);
                mTv_path.setText("线路: 2");

            break;
            case 3:
                mTv_path.setTextColor(Constant.PATHCOLOR3);
                mTv_path.setText("线路: 3");
            break;
            default:
            break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mActivity==null){

            mActivity = (MainActivity) getActivity();
        }
        if (mRootView!=null){

            return mRootView;
        }

        mRootView = inflater.inflate(R.layout.fragment_map, null);

        initView();

        initData();

        return mRootView;
    }

    private void initData() {
        mIMapFragmentPresent = new MapFragmentPresentImpl(this);

        walkModel=SharePreferenceUtils.getInt(mActivity, WALKMODEL);

        switch (walkModel){
            case 0:
                mRg_walkmodel.check(R.id.rb_single_recycle);

            break;
            case 1:
                mRg_walkmodel.check(R.id.rb_order_recycle);

            break;
            case 2:
                mRg_walkmodel.check(R.id.rb_random_recycle);

            break;
            default:
            break;
        }

        ReceiveBrain.getInstance().setContext(mActivity);

        mBrain.setMapFragment(this);

    }

    private void initView() {

        mBt_magnify = (Button) findViewById(R.id.bt_magnify);
        mBt_reduce = (Button) findViewById(R.id.bt_reduce);
        mBt_centerpoint = (Button) findViewById(R.id.bt_centerpoint);

        mBt_choicepath = (Button) findViewById(R.id.bt_choicepath);

        mBt_scale = (Button) findViewById(R.id.bt_scale);
        mBt_setpath = (Button) findViewById(R.id.bt_path);

        mBt_deletePath = (Button) findViewById(R.id.bt_deletepath);
        mTv_scale = (TextView) findViewById(R.id.tv_scale);

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
        mBt_setrobotpoint = (Button) findViewById(R.id.bt_setrobotpoint);
        mBt_confirmrobotpoint = (Button) findViewById(R.id.bt_confirmrobotpoint);

        mEt_robot_x = (EditText) findViewById(R.id.et_robotpoint_x);
        mEt_robot_y = (EditText) findViewById(R.id.et_robotpoint_y);
        mTv_path = (TextView) findViewById(R.id.tv_path);

        mBt_operate = (Button) findViewById(R.id.bt_operate);
        mBt_walk = (Button) findViewById(R.id.bt_walk);
        mBt_path_layout = (Button) findViewById(R.id.bt_path_layout);
        mBt_map_backup = (Button) findViewById(R.id.bt_map_backup);

        mLl_operate = (LinearLayout) findViewById(R.id.ll_operate);
        mLl_path_layout = (LinearLayout) findViewById(R.id.ll_path_layout);
        mLl_walk = (LinearLayout) findViewById(R.id.ll_walk);

        mBt_startwalk = (Button) findViewById(R.id.bt_startwalk);
        mLl_path = findViewById(R.id.ll_pathinfo);
        mRg_walkmodel = (RadioGroup) findViewById(R.id.rg_walkmodel);

        mLl_setpathpoint = findViewById(R.id.ll_setpathpoint);
        mEt_pathpoint_x = (EditText) findViewById(R.id.et_pathpoint_x);
        mEt_pathpoint_y = (EditText) findViewById(R.id.et_pathpoint_y);
        mBt_confirmpathpoint = (Button) findViewById(R.id.bt_confirmpathpoint);

        mBt_stopwalk = (Button) findViewById(R.id.bt_stopwalk);

        mRg_walkmodel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId){
                    case R.id.rb_single_recycle:
                        SharePreferenceUtils.putInt(mActivity,WALKMODEL,0);
                    break;
                    case R.id.rb_order_recycle:
                        SharePreferenceUtils.putInt(mActivity,WALKMODEL,1);
                    break;
                    case R.id.rb_random_recycle:
                        SharePreferenceUtils.putInt(mActivity,WALKMODEL,2);
                    break;
                    default:
                    break;
                }
            }
        });

        mBt_stopwalk.setOnClickListener(this);
        mBt_startwalk.setOnClickListener(this);

        mBt_confirmpathpoint.setOnClickListener(this);

        mBt_operate.setOnClickListener(this);
        mBt_walk.setOnClickListener(this);
        mBt_path_layout.setOnClickListener(this);
        mBt_map_backup.setOnClickListener(this);

        mBt_confirmrobotpoint.setOnClickListener(this);
        mBt_setrobotpoint.setOnClickListener(this);
        mBt_magnify.setOnClickListener(this);
        mBt_reduce.setOnClickListener(this);
        mBt_centerpoint.setOnClickListener(this);
        mBt_setpath.setOnClickListener(this);
        mBt_deletePath.setOnClickListener(this);
        mBt_scale.setOnClickListener(this);
        mBt_choicepath.setOnClickListener(this);

        mMapview = (MapView) findViewById(R.id.mv_mapview);
    }

    private View findViewById(int viewId) {

        View viewById = mRootView.findViewById(viewId);

        return viewById;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (!operateShowing){
             mLl_operate.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }
        if (!operateShowing){
             mLl_path_layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }


        if (!hasShowScaleInfo){
            hasShowScaleInfo = true;
            switch (mActivity.getMapScale()){
                case 0:
                    mTv_scale.setText("比例尺 1:10");

                break;
                case 1:
                    mTv_scale.setText("比例尺 1:100");

                break;
                case 2:
                    mTv_scale.setText("比例尺 1:1000");

                break;
                case 3:
                    mTv_scale.setText("比例尺 1:10000");

                break;

                default:
                break;
            }
        }

        //设置当前位置的信息
        setRobotPointInfo();

        //如果没有原点，取消设置路径按钮
        if (mMapview.getCenterPoint()==null){
            mLl_path.setVisibility(View.GONE);
        }

        //获取陀螺仪的数据
        try {
            mIMapFragmentPresent.openIMUSerialPort();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        mTimer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                IMUDataBean imuDataBean = mIMapFragmentPresent.getIMUDataBean();

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
                ToastUtils.makeToast(getActivity(),"请在地图上设置原点位置");
                break;

            case R.id.bt_path:

                isSetPath = !isSetPath;
                mBt_setpath.setText(isSetPath?"完成设置":"设置路径");
                if (isSetPath){
                    mMapview.setCanSetPath(true);
                    mLl_setpathpoint.setVisibility(View.VISIBLE);
                    mMapview.setCanSetCenterPoint(false);
                }
                else {
                    mLl_setpathpoint.setVisibility(View.GONE);
                    mMapview.setCanSetPath(false);
                }
                break;

            case R.id.bt_deletepath:
                if (!isSetPath){
                    ToastUtils.makeToast(getActivity(),"请先点击设置路径");
                    return;
                }
                mMapview.deletePathPoint(mPath);
                break;
            case R.id.bt_choicepath:

                showChoicePathDialog();

                break;
            case R.id.bt_scale:

                showChoiceScaleDialog();
                break;

            case R.id.bt_setrobotpoint:

                mMapview.setCanSetRobotPoint(true);

                break;
            case R.id.bt_confirmrobotpoint:

                try {
                    float x = Float.parseFloat(mEt_robot_x.getText().toString());
                    float y = Float.parseFloat(mEt_robot_y.getText().toString());

                    int v1 = (int) (x * 100 * MapView.mGridWidth / Constant.DISTANCEPERGRID / mActivity.getMapScaleFactor() + mMapview.getCenterPoint().x+0.5f);
                    int v2 = (int) (mMapview.getCenterPoint().y - y * MapView.mGridWidth * 100 / Constant.DISTANCEPERGRID / mActivity.getMapScaleFactor()+0.5f);

                    mMapview.setRobotPointInMap(new Point(v1,v2),true);

                    mMapview.setCanSetRobotPoint(false);

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                break;

            case R.id.bt_operate:

                if (operateShowing){

                    mLl_operate.setLayoutParams(new LinearLayout.LayoutParams(-1,0));
                }else {
                    mLl_operate.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));

                }


                operateShowing=!operateShowing;

                break;
            case R.id.bt_path_layout:

                if (pathLayoutShowing){
                    mLl_path_layout.setLayoutParams(new LinearLayout.LayoutParams(-1,0));
                }else {

                    mLl_path_layout.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
                }

                pathLayoutShowing=!pathLayoutShowing;

                break;
            case R.id.bt_walk:

                if (walkShowing){
                    mLl_walk.setLayoutParams(new LinearLayout.LayoutParams(-1,0));
                }else {

                    mLl_walk.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
                }

                walkShowing=!walkShowing;

                break;
            case R.id.bt_startwalk://开始地图漫游

                Point currentPoint = mMapview.getRobotPointInMap();
                Point nextPoint = mMapview.getNextPoint(1);
                startManYou(currentPoint,nextPoint);
                
                break;
            case R.id.bt_stopwalk://停止地图漫游

                mBrain.setStateMapValue(mBrain.MANYOU,null);

                mBrain.sendCommand("{'type':'command','function':'stop','data':''}");

                break;

            case R.id.bt_confirmpathpoint://确认路径点

                try {
                    float x = Float.parseFloat(mEt_pathpoint_x.getText().toString());
                    float y = Float.parseFloat(mEt_pathpoint_y.getText().toString());

                    int v1 = (int) (x * 100 * MapView.mGridWidth / Constant.DISTANCEPERGRID / mActivity.getMapScaleFactor() + mMapview.getCenterPoint().x+0.5f);
                    int v2 = (int) (mMapview.getCenterPoint().y - y * MapView.mGridWidth * 100 / Constant.DISTANCEPERGRID / mActivity.getMapScaleFactor()+0.5f);

                    mMapview.addPathPoint(mPath,new Point(v1,v2),true,false);

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                break;


            default:
                break;
        }

    }

    public void startManYou(Point currentPoint, Point nextPoint) {

        System.out.println("currentPoint:"+currentPoint+" ;nextPoint:"+nextPoint);

        //说明不需要增加一个中间点
        if (Math.abs(currentPoint.x - nextPoint.x) < 0.2 ){//x坐标重合
            LogUtil.debugLog("x坐标重合");
            if (currentPoint.y>nextPoint.y){//往上走
                setNextDirection(0);
                setDirection(0);

                if (Math.abs(nextDirection-currentAngle) > Constant.TOLERANCEANGLE){//夹角大于允许范围，先转正再前进
                    mBrain.setStateMapValue(mBrain.MANYOU,mBrain.MANYOU_TARGET);
                    float transferAngle = TurnAngleUtil.getTransferAngle(currentAngle-nextDirection);
                    mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':"+transferAngle+"}}");
                }
                else {//夹角小于允许范围，直接前进
                    mBrain.setStateMapValue(mBrain.MANYOU,mBrain.MANYOU_TARGET);
                    mBrain.sendCommand("{'type':'command','function':'walk','data':{'distance':"+pxTrandferToDistance(Math.abs(currentPoint.y-nextPoint.y))+"}}");
                }
            }
            else if (currentPoint.y < nextPoint.y){//往下走
                setNextDirection(180);
                setDirection(1);

                if (Math.abs(nextDirection-currentAngle) > Constant.TOLERANCEANGLE ){//夹角大于允许范围，先转正再前进
                    mBrain.setStateMapValue(mBrain.MANYOU,mBrain.MANYOU_TARGET);
                    float transferAngle = TurnAngleUtil.getTransferAngle(currentAngle-nextDirection);
                    mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':"+transferAngle+"}}");
                }
                else {//夹角小于允许范围，直接前进
                    mBrain.setStateMapValue(mBrain.MANYOU,mBrain.MANYOU_TARGET);
                    mBrain.sendCommand("{'type':'command','function':'walk','data':{'distance':"+pxTrandferToDistance(Math.abs(currentPoint.y-nextPoint.y))+"}}");
                }
            }
        }
        else if (Math.abs(currentPoint.y - nextPoint .y) < 0.2){//说明不需要增加一个中间点,y坐标重合
            LogUtil.debugLog("y坐标重合");
            if (currentPoint.x > nextPoint.x){//往左走
                setNextDirection(270);
                setDirection(2);
                if (Math.abs(nextDirection-currentAngle) > Constant.TOLERANCEANGLE ){//夹角大于允许范围，先转正再前进
                    mBrain.setStateMapValue(mBrain.MANYOU,mBrain.MANYOU_TARGET);
                    float transferAngle = TurnAngleUtil.getTransferAngle(currentAngle-nextDirection);
                    mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':"+transferAngle+"}}");
                }
                else {//夹角小于允许范围，直接前进
                    mBrain.setStateMapValue(mBrain.MANYOU,mBrain.MANYOU_TARGET);
                    mBrain.sendCommand("{'type':'command','function':'walk','data':{'distance':"+pxTrandferToDistance(Math.abs(currentPoint.x-nextPoint.x))+"}}");
                }
            }
            else if (currentPoint.x < nextPoint.x){//往右走

                setNextDirection(90);
                setDirection(3);
                if (Math.abs(nextDirection-currentAngle) > Constant.TOLERANCEANGLE ){//夹角大于5°，先转正再前进
                    mBrain.setStateMapValue(mBrain.MANYOU,mBrain.MANYOU_TARGET);
                    float transferAngle = TurnAngleUtil.getTransferAngle(currentAngle-nextDirection);
                    mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':"+transferAngle+"}}");
                }
                else {//夹角小于允许范围，直接前进
                    mBrain.setStateMapValue(mBrain.MANYOU,mBrain.MANYOU_TARGET);
                    mBrain.sendCommand("{'type':'command','function':'walk','data':{'distance':"+pxTrandferToDistance(Math.abs(currentPoint.x-nextPoint.x))+"}}");
                }
            }
        }
        else { // 需要中间点

            LogUtil.debugLog("需要中间点");
            tempIntermediate = new Point(nextPoint.x, currentPoint.y);

            if (currentPoint.x > nextPoint.x) {//往左走
                setNextDirection(270);
                setDirection(2);
                if (Math.abs(nextDirection - currentAngle) > Constant.TOLERANCEANGLE) {//夹角大于允许范围，先转正再前进
                    mBrain.setStateMapValue(mBrain.MANYOU, mBrain.MANYOU_TEMP);
                    float transferAngle = TurnAngleUtil.getTransferAngle(currentAngle-nextDirection);
                    mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':" + transferAngle + "}}");
                } else {//夹角小于允许范围，直接前进
                    mBrain.setStateMapValue(mBrain.MANYOU, mBrain.MANYOU_TEMP);
                    mBrain.sendCommand("{'type':'command','function':'walk','data':{'distance':" + pxTrandferToDistance(Math.abs(currentPoint.x - nextPoint.x)) + "}}");
                }
            } else if (currentPoint.x < nextPoint.x) {//往右走
                LogUtil.debugLog("方向：往右走");

                setNextDirection(90);
                setDirection(3);
                if (Math.abs(nextDirection - currentAngle) > Constant.TOLERANCEANGLE) {//夹角大于允许范围，先转正再前进
                    mBrain.setStateMapValue(mBrain.MANYOU, mBrain.MANYOU_TEMP);
                    float transferAngle = TurnAngleUtil.getTransferAngle(currentAngle-nextDirection);
                    mBrain.sendCommand("{'type':'command','function':'turn','data':{'degree':" + transferAngle + "}}");
                } else {//夹角小于允许范围，直接前进
                    mBrain.setStateMapValue(mBrain.MANYOU, mBrain.MANYOU_TEMP);
                    mBrain.sendCommand("{'type':'command','function':'walk','data':{'distance':" + pxTrandferToDistance(Math.abs(currentPoint.x - nextPoint.x)) + "}}");
                }
            }
        }
    }

    public void isSetRobotPoint(boolean flag){

        if (flag){
            mEt_robot_x.setVisibility(View.VISIBLE);
            mEt_robot_y.setVisibility(View.VISIBLE);
            mBt_confirmrobotpoint.setVisibility(View.VISIBLE);

        }else {

            mEt_robot_x.setVisibility(View.GONE);
            mEt_robot_y.setVisibility(View.GONE);
            mBt_confirmrobotpoint.setVisibility(View.GONE);

        }
    }

    @Override
    public void onPause() {
        mIMapFragmentPresent.closeIMUSerialPort();
        if (mTimer!=null){
            mTimer.cancel();
            mTimer=null;
        }
        super.onPause();
    }


    public void setPathPointInfo(Point pointInView){


        if (mEt_pathpoint_x==null||mEt_pathpoint_y==null){
            return;
        }

        Point centerPoint = mMapview.getCenterPoint();
        String x = mFormat.format((pointInView.x - centerPoint.x) / 1.0/MapView.mGridWidth * Constant.DISTANCEPERGRID * mActivity.getMapScaleFactor() / 100);
        String y = mFormat.format((-pointInView.y + centerPoint.y) / 1.0 /MapView.mGridWidth * Constant.DISTANCEPERGRID * mActivity.getMapScaleFactor() / 100);

        mEt_pathpoint_x.setText(x);
        mEt_pathpoint_x.setSelection(x.length());

        mEt_pathpoint_y.setText(y);
        mEt_pathpoint_y.setSelection(y.length());

    }


    public void setRobotPointInfo() {

        if (mEt_robot_x==null||mEt_robot_y==null){
            return;
        }

        Point point = mMapview.getRobotPointInMap();
        Point centerPoint = mMapview.getCenterPoint();

        if (centerPoint == null){
            return;
        }
//        point.x=(point.x-centerPoint.x)/40.0*10*mActivity.getMapScaleFactor()/100;
//        point.y=(point.y-centerPoint.y)/40.0*10*mActivity.getMapScaleFactor()/100;

        String x = mFormat.format((point.x - centerPoint.x) / 1.0/MapView.mGridWidth * Constant.DISTANCEPERGRID * mActivity.getMapScaleFactor() / 100);
        String y = mFormat.format((-point.y + centerPoint.y) / 1.0 /MapView.mGridWidth * Constant.DISTANCEPERGRID * mActivity.getMapScaleFactor() / 100);

        mEt_robot_x.setText(x);
        mEt_robot_x.setSelection(x.length());

        mEt_robot_y.setText(y);
        mEt_robot_y.setSelection(y.length());

        mActivity.setPosition("当前位置："+x+" , "+y);
//        mEt_robot_x.setText(point.x/40.0*10*mActivity.getMapScaleFactor()/100+"");
//        mEt_robot_x.setSelection(mEt_robot_x.getText().length());
//
//        mEt_robot_y.setText(point.y/40.0*10*mActivity.getMapScaleFactor()/100+"");
//        mEt_robot_y.setSelection(mEt_robot_y.getText().length());
//
//        mTv_robotpoint.setText("当前位置："+point.x/40.0*10*mActivity.getMapScaleFactor()/100+","+point.y/40.0*10*mActivity.getMapScaleFactor()/100);

    }

    public float pxTrandferToDistance(float px){

        return px/MapView.mGridWidth*0.1f*mActivity.getMapScaleFactor();
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

            mTv_pressure.setText(bean.pressure + " Pa");
        }
    }

    @Override
    public void showChoicePathDialog() {

        DialogFragment choicePath = new ChoicePathDialog();
        choicePath.show(getActivity().getFragmentManager(),"choicepath");

    }

    @Override
    public void showChoiceScaleDialog() {

        DialogFragment choiceScale = new ChoiceScaleDialog();
        choiceScale.show(getActivity().getFragmentManager(),"choicescale");
    }

    @Override
    public void setCurrentListPosition(int position) {
        mMapview.setCurrentListPosition(position);
    }

    @Override
    public int getCurrentListPosition() {
        return mMapview.getCurrentListPosition();
    }

    @Override
    public void setRobotPointInMap(Point point,boolean flag) {
        mMapview.setRobotPointInMap(point,flag);
    }

    @Override
    public Point getRobotPointInMap() {
        return mMapview.getRobotPointInMap();
    }

    @Override
    public Point getCenterPoint() {
        return mMapview.getCenterPoint();
    }

    @Override
    public Point getNextPoint() {
        return mMapview.getNextPoint(0);
    }

    @Override
    public Point getStartManYouPoint() {
        return mMapview.getStartManYouPoint();
    }

}
