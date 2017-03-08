package com.jiadu.mapdemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.jiadu.fragment.MapFragment;
import com.jiadu.mapdemo.MainActivity;
import com.jiadu.mapdemo.R;
import com.jiadu.mapdemo.util.ArrowUtil;
import com.jiadu.mapdemo.util.Constant;
import com.jiadu.mapdemo.util.MyDataBaseUtil;
import com.jiadu.mapdemo.util.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2017/2/13.
 */

public class MapView extends ImageView {

    public static final int TYPE_CENTERPOINT = 0;
    public static final int TYPE_PATH1 = 1;
    public static final int TYPE_PATH2 = 2;
    public static final int TYPE_PATH3 = 3;

    private int mViewWidth ;
    private int mViewHeight ;
    public static int mGridWidth = 40;
    private Bitmap mGridBitmap;
    private Paint mPaint;
    private Paint mPathPaint;
    private Point mCenterPoint = null;
    private boolean canSetCenterPoint = false;
    private Point mRobotPoint = new Point();  //记录robort在地图中的位置

    private float mDirectionAngle = 0;
    private Random mRandom = new Random();
    private boolean canSetPath = false;
    private float mScale = 1;     //地图缩放比例
    private float mTranslateX = 0;//X平移的距离
    private float mTranslateY = 0;//Y平移的距离

    private List<Point> pathListAfterTransfer1 = new ArrayList<Point>();
    private List<Point> pathListAfterTransfer2 = new ArrayList<Point>();
    private List<Point> pathListAfterTransfer3 = new ArrayList<Point>();

    private Path path1=new Path();
    private Path path2=new Path();
    private Path path3=new Path();

    private List<Path> path1Arrow = new ArrayList<Path>();
    private List<Path> path2Arrow = new ArrayList<Path>();
    private List<Path> path3Arrow = new ArrayList<Path>();

    private MainActivity mContext;
    private float mStartX;
    private float mStartY;

    private MyDataBaseUtil mDbUtil;
    private Paint mArrowPaint;
    private boolean hadDrawGridBitmap = false;

    private int mPathNum =1;
    private Bitmap mRobotBitMapJainTou;

    private boolean canSetRobotPoint = false;
    private MapFragment mMapFragment;

    private Bitmap mCenterPointBitMap =null;
    private Bitmap mRobotBitMapTouXiang =null;

    private Point tempPoint = null;

    private int mCurrentPath=1; //当前漫游的路径
    private int mCurrentListPosition=-1;//当前漫游路劲中的点
    private boolean hasFinishLoop = false;//表示漫游1圈是否完成回到原点

    private Point intermediatePoint1 = null;
    private Point intermediatePoint2 = null;
    private Point intermediatePoint3 = null;


    public int getCurrentListPosition() {
        return mCurrentListPosition;
    }
    public void setCurrentListPosition(int currentListPosition) {

        if (currentListPosition >= pathListAfterTransfer1.size()){

            this.mCurrentListPosition = -1;
        }else {

            this.mCurrentListPosition = currentListPosition;
        }
    }
    public void setCurrentPath(int currentPath) {

        this.mCurrentPath = currentPath;
    }


    public boolean isCanSetRobotPoint() {
        return canSetRobotPoint;
    }

    public void setCanSetRobotPoint(boolean canSetRobotPoint) {
        this.canSetRobotPoint = canSetRobotPoint;
        
        if (mMapFragment!=null){
                    
            mMapFragment.isSetRobotPoint(canSetRobotPoint);
        }
    }

    /**
     * @param point:在map中的点
     * @param flag:是否需要匹配最近的格子顶点
     */
    public void setRobotPointInMap(Point point,boolean flag){

        if (point==null){
            return;
        }
        if (flag){
            mRobotPoint = matchClosestPoint(point);
        }else {
            mRobotPoint = point;
        }

        if (mMapFragment!=null){
            mMapFragment.setRobotPointInfo();
        }
        invalidate();
    }


    /**
     * @param point:在View位置中的点
     */
    public void setRobotPointInView(Point point){

        if (point==null){
            return;
        }

        Point point1 = transferCoordinateToMap(point);

        mRobotPoint = matchClosestPoint(point1);

        if (mMapFragment!=null){
            mMapFragment.setRobotPointInfo();
        }

        invalidate();
    }

    /**
     * @return robot在map中的点
     */
    public Point getRobotPointInMap(){

        return mRobotPoint;
    }

    /**
     * @return robot在View中的位置
     */
    public Point getRobotPointInView(){

        return transferCoordinateToView(mRobotPoint);
    }

    public void setDirectionAngle(float directionAngle) {
        this.mDirectionAngle = directionAngle;

        invalidate();
    }

    public void setPathNum(int pathNum) {
        mPathNum = pathNum;
    }

    private double getDistance(Point point1,Point point2){

        return  Math.sqrt((point1.x-point2.x)*(point1.x-point2.x)+(point1.y-point2.y)*(point1.y-point2.y));
    }

    /**
     * @param pathNum:路径号
     * @param pointx:要增加的点
     * @param flag1:是否需要更加入数据库
     * @param flag2:是否需要进行坐标转换，如果点对应的坐标是View中的坐标，则flag为true，如果点对应的坐标是map中的坐标，则flag为false
     */
    public void addPathPoint(int pathNum, Point pointx,boolean flag1,boolean flag2){

            Point pointOnMap = pointx;
        if (flag2){
            pointOnMap = transferCoordinateToMap(pointx);
        }


        Point point1 = matchClosestPoint(pointOnMap);

        switch (pathNum){
            case 1:
//               保证相邻的两个点不重复
                if (pathListAfterTransfer1.size()>0 && getDistance(pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1),point1)<mGridWidth){
                    return;
                }
                if (pathListAfterTransfer1.size() == 0){

                    if (getDistance(point1,mCenterPoint) <mGridWidth){
                        return;
                    }
                }

                if (pathListAfterTransfer1.size() == 0){//说明此前没有路径点

                    if (point1.x == mCenterPoint.x || point1.y == mCenterPoint.y){//说明x轴或者y轴有一个是Ok的，不需要增加中间点
                        pathListAfterTransfer1.add(point1);
                        path1.lineTo(point1.x,point1.y);
                        path1Arrow = pointToArrow(pathListAfterTransfer1);
                        break;
                    }else { //说明需要增加一个中间点

                        Point pointtemp = new Point(point1.x,mCenterPoint.y);
//                        pathListAfterTransfer1.add(pointtemp);
                        path1.lineTo(pointtemp.x,pointtemp.y);
//                        mDbUtil.addPoint(pointtemp.x,pointtemp.y,pathNum);
                    }
                }

                else if (pathListAfterTransfer1.size() != 0){//说明之前有路径点
                    //说明不需要增加一个中间点
                    if (point1.x == pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).x || point1.y == pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).y){//说明x轴或者y轴有一个是Ok的
                        pathListAfterTransfer1.add(point1);
                        path1.lineTo(point1.x,point1.y);
                        path1Arrow = pointToArrow(pathListAfterTransfer1);
                        break;

                    }else { //说明需要增加一个中间点

                        Point pointtemp = new Point(point1.x,pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).y);
//                        pathListAfterTransfer1.add(pointtemp);
                        path1.lineTo(pointtemp.x,pointtemp.y);
//                        mDbUtil.addPoint(pointtemp.x,pointtemp.y,pathNum);
                    }
                }

                pathListAfterTransfer1.add(point1);
                path1.lineTo(point1.x,point1.y);
                path1Arrow = pointToArrow(pathListAfterTransfer1);


            break;
            case 2:
//                保证相邻的两个点不重复
                if (pathListAfterTransfer2.size()>0 && getDistance(pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1),point1)<mGridWidth){
                    return;
                }
                if (pathListAfterTransfer2.size() == 0){
                    if (getDistance(point1,mCenterPoint) <mGridWidth){
                        return;
                    }
                }


                if (pathListAfterTransfer2.size() == 0){//说明此前没有路径点

                    if (point1.x == mCenterPoint.x || point1.y == mCenterPoint.y){//说明x轴或者y轴有一个是Ok的，不需要增加中间点
                        pathListAfterTransfer2.add(point1);
                        path2.lineTo(point1.x,point1.y);
                        path2Arrow = pointToArrow(pathListAfterTransfer2);
                        break;
                    }else { //说明需要增加一个中间点

                        Point pointtemp = new Point(point1.x,mCenterPoint.y);
//                        pathListAfterTransfer2.add(pointtemp);
                        path2.lineTo(pointtemp.x,pointtemp.y);
//                        mDbUtil.addPoint(pointtemp.x,pointtemp.y,pathNum);
                    }
                }

                else if (pathListAfterTransfer2.size() != 0){//说明之前有路径点
                    //说明不需要增加中间点
                    if (point1.x == pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).x || point1.y == pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).y){//说明x轴或者y轴有一个是Ok的
                        pathListAfterTransfer2.add(point1);
                        path2.lineTo(point1.x,point1.y);
                        path2Arrow = pointToArrow(pathListAfterTransfer2);
                        break;

                    }else { //说明需要增加一个中间点

                        Point pointtemp = new Point(point1.x,pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).y);
//                        pathListAfterTransfer2.add(pointtemp);
                        path2.lineTo(pointtemp.x,pointtemp.y);
//                        mDbUtil.addPoint(pointtemp.x,pointtemp.y,pathNum);
                    }
                }

                pathListAfterTransfer2.add(point1);
                path2.lineTo(point1.x,point1.y);
                path2Arrow = pointToArrow(pathListAfterTransfer2);

            break;

            case 3:
//                保证相邻的两个点不重复
                if (pathListAfterTransfer3.size()>0 && getDistance(pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1),point1)<mGridWidth){
                    return;
                }
                if (pathListAfterTransfer3.size() == 0){
                    if (getDistance(point1,mCenterPoint) <mGridWidth){
                        return;
                    }
                }


                if (pathListAfterTransfer3.size() == 0){//说明此前没有路径点

                    if (point1.x == mCenterPoint.x || point1.y == mCenterPoint.y){//说明x轴或者y轴有一个是Ok的，不需要增加中间点
                        pathListAfterTransfer3.add(point1);
                        path3.lineTo(point1.x,point1.y);
                        path3Arrow = pointToArrow(pathListAfterTransfer3);
                        break;
                    }else { //说明需要增加一个中间点

                        Point pointtemp = new Point(point1.x,mCenterPoint.y);
//                        pathListAfterTransfer3.add(pointtemp);
                        path3.lineTo(pointtemp.x,pointtemp.y);
//                        mDbUtil.addPoint(pointtemp.x,pointtemp.y,pathNum);
                    }
                }

                else if (pathListAfterTransfer3.size() != 0){//说明之前有路径点

                    //说明不需要增加中间点
                    if (point1.x == pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).x || point1.y == pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).y){//说明x轴或者y轴有一个是Ok的
                        pathListAfterTransfer3.add(point1);
                        path3.lineTo(point1.x,point1.y);
                        path3Arrow = pointToArrow(pathListAfterTransfer3);
                        break;

                    }else { //说明需要增加一个中间点

                        Point pointtemp = new Point(point1.x,pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).y);
//                        pathListAfterTransfer3.add(pointtemp);
                        path3.lineTo(pointtemp.x,pointtemp.y);
//                        mDbUtil.addPoint(pointtemp.x,pointtemp.y,pathNum);
                    }
                }

                pathListAfterTransfer3.add(point1);
                path3.lineTo(point1.x,point1.y);
                path3Arrow = pointToArrow(pathListAfterTransfer3);
                break;
            default:
            break;
        }

        drawLastPointAndArrow(pathNum,point1);
        if (flag1){
            mDbUtil.addPoint(point1.x,point1.y,pathNum);
        }
            invalidate();
    }

    public void deletePathPoint(int pathNum){
        switch (pathNum){
            case 1:
                if (pathListAfterTransfer1.size()>=1){

                    Point pt = pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1);

                    mDbUtil.deletePoint(pt.x,pt.y,pathNum);

                    pathListAfterTransfer1.remove(pathListAfterTransfer1.size()-1);

                    List<Point> pathListAfterTransfertemp1 =pathListAfterTransfer1;
                    pathListAfterTransfer1 = new ArrayList<Point>();

                    path1.reset();

                    path1.moveTo(mCenterPoint.x ,mCenterPoint.y);
                    for (Point p:pathListAfterTransfertemp1) {
                        addPathPoint(pathNum,p,false,false);
                    }

                    path1Arrow = pointToArrow(pathListAfterTransfer1);
                }
                
                if (pathListAfterTransfer1==null||pathListAfterTransfer1.size()==0){
                    drawLastPointAndArrow(pathNum,null);
                }else {
                    drawLastPointAndArrow(pathNum,pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1));
                }


                break;
            case 2:
                if (pathListAfterTransfer2.size()>=1){

                    Point pt = pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1);

                    mDbUtil.deletePoint(pt.x,pt.y,pathNum);

                    pathListAfterTransfer2.remove(pathListAfterTransfer2.size()-1);

                    List<Point> pathListAfterTransferTemp2 = pathListAfterTransfer2;

                    path2.reset();
                    path2.moveTo(mCenterPoint.x ,mCenterPoint.y);
                    pathListAfterTransfer2 = new ArrayList<Point>();
                    for (Point p:pathListAfterTransferTemp2) {
                        addPathPoint(pathNum,p,false,false);
                    }
                    path2Arrow = pointToArrow(pathListAfterTransfer2);
                }
                if (pathListAfterTransfer2==null||pathListAfterTransfer2.size()==0){
                    drawLastPointAndArrow(pathNum,null);
                }else {
                    drawLastPointAndArrow(pathNum,pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1));
                }

                break;
            case 3:
                if (pathListAfterTransfer3.size()>=1){

                    Point pt = pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1);

                    mDbUtil.deletePoint(pt.x,pt.y,pathNum);

                    pathListAfterTransfer3.remove(pathListAfterTransfer3.size()-1);

                    List<Point> pathListAfterTransferTemp3 = pathListAfterTransfer3;

                    pathListAfterTransfer3 = new ArrayList<Point>();

                    path3.reset();
                    path3.moveTo(mCenterPoint.x ,mCenterPoint.y);
                    for (Point p:pathListAfterTransferTemp3) {
                        addPathPoint(pathNum,p,false,false);
                    }

                    path3Arrow = pointToArrow(pathListAfterTransfer3);
                }
                if (pathListAfterTransfer3==null||pathListAfterTransfer3.size()==0){
                    drawLastPointAndArrow(pathNum,null);
                }else {
                    drawLastPointAndArrow(pathNum,pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1));
                }
            break;
            default:
            break;
        }

        invalidate();
    }

    public boolean isCanSetPath() {
        return canSetPath;
    }

    public void setCanSetPath(boolean canSetPath) {
        this.canSetPath = canSetPath;
        invalidate();
    }

    public void setTranslate(float translateX,float translateY) {
        mTranslateX += translateX;

        if (mTranslateX > mViewWidth*(mScale-1)/2){
            mTranslateX = mViewWidth*(mScale-1)/2;
           }

        if (mTranslateX < -mViewWidth*(mScale-1)/2){
            mTranslateX = -mViewWidth*(mScale-1)/2;
           }

        mTranslateY += translateY;
        if (mTranslateY > mViewHeight*(mScale-1)/2){
            mTranslateY = mViewHeight*(mScale-1)/2;
           }

        if (mTranslateY < -mViewHeight*(mScale-1)/2){
            mTranslateY = -mViewHeight*(mScale-1)/2;
           }

        invalidate();
    }

    public boolean isCanSetCenterPoint() {
        return canSetCenterPoint;
    }

    public void setCanSetCenterPoint(boolean canSetCenterPoint) {
        this.canSetCenterPoint = canSetCenterPoint;
    }

    public float getScale() {
        return mScale;
    }
    public void setScale(float scale) {
        mScale = scale;
        mTranslateY = 0;
        mTranslateX = 0;
        invalidate();
    }

    public MapView(Context context) {
        this(context,null);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = (MainActivity) context;

        mMapFragment = (MapFragment) mContext.getFragmentManager().findFragmentByTag(MainActivity.TAG_FRAGMENT_MAP);

        mPaint = new Paint();

        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPathPaint.setColor(Color.rgb(0XFF,0X00,0X00));
        mPathPaint.setStrokeWidth(4);
        mPathPaint.setStyle(Paint.Style.STROKE);

        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mArrowPaint.setStyle(Paint.Style.FILL);

    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //画网格的bitmap
//        drawGridBitmap();

        mRobotBitMapJainTou = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.jiantou);

        mCenterPointBitMap = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.qi);

        mRobotBitMapTouXiang = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.touxiang);

        //初始化硬盘数据
        initDataFromDataBase();
    }

    private void drawGridBitmap() {

        Bitmap.Config conf = Bitmap.Config.ARGB_4444;

        mGridBitmap = Bitmap.createBitmap(mViewWidth,mViewHeight, conf);

        Canvas canvas = new Canvas(mGridBitmap);

        Paint paint = new Paint();

        paint.setStrokeWidth(2);

        canvas.drawARGB(Color.TRANSPARENT,0,0,0);

        paint.setColor(Color.rgb(29,28,28));

        paint.setStyle(Paint.Style.STROKE);

        for (int i =0 ;i<(mViewHeight*1.0/mGridWidth) ;i++){

            canvas.drawLine(0, i* mGridWidth,mViewWidth, i* mGridWidth,paint);
        }
        for (int i = 0 ; i<(mViewWidth*1.0/mGridWidth);i++){

            canvas.drawLine(i* mGridWidth,0, i* mGridWidth,mViewHeight,paint);
        }
    }

    private void initDataFromDataBase() {
        mDbUtil = new MyDataBaseUtil(mContext);

        mCenterPoint = mDbUtil.queryCenterPoint(TYPE_CENTERPOINT);
        setRobotPointInMap(mCenterPoint,true);

        if (mCenterPoint == null){

            mDbUtil.deleteAll();

            return;
        }


        List<Point> pathListAfterTransferTemp1 = mDbUtil.queryPathPoint(1);
        List<Point> pathListAfterTransferTemp2 = mDbUtil.queryPathPoint(2);
        List<Point> pathListAfterTransferTemp3 = mDbUtil.queryPathPoint(3);

        path1.moveTo(mCenterPoint.x ,mCenterPoint.y);
        for (Point p:pathListAfterTransferTemp1) {
            addPathPoint(1,p,false,false);
//            path1.lineTo(p.x,p.y);
        }

        path1Arrow = pointToArrow(pathListAfterTransfer1);

        path2.moveTo(mCenterPoint.x ,mCenterPoint.y);
        for (Point p:pathListAfterTransferTemp2) {
            addPathPoint(2,p,false,false);
        }

        path2Arrow = pointToArrow(pathListAfterTransfer2);

        path3.moveTo(mCenterPoint.x ,mCenterPoint.y);
        for (Point p:pathListAfterTransferTemp3) {
            addPathPoint(3,p,false,false);
        }
        path3Arrow = pointToArrow(pathListAfterTransfer3);

    }

    private List<Path> pointToArrow(List<Point> points) {
        List<Path> list = new ArrayList<Path>();

        if (points == null || points.size() == 0) {
            return list;
        }

        if (mCenterPoint != null) {
            if (points.get(0).y == mCenterPoint.y) {

                if (points.get(0).x < mCenterPoint.x) {
                    list.add(ArrowUtil.getLeftArrow(points.get(0)));
                } else if (points.get(0).x > mCenterPoint.x) {
                    list.add(ArrowUtil.getRightArrow(points.get(0)));
                }
            } else if (points.get(0).y > mCenterPoint.y) {

                list.add(ArrowUtil.getDownArrow(points.get(0)));
            } else {
                list.add(ArrowUtil.getUpArrow(points.get(0)));
            }
        }

        for (int i = 1; i < points.size(); i++) {

            if (points.get(i).y == points.get(i-1).y) {

                if (points.get(i).x < points.get(i-1).x) {
                    list.add(ArrowUtil.getLeftArrow(points.get(i)));
                } else if (points.get(i).x > points.get(i-1).x) {
                    list.add(ArrowUtil.getRightArrow(points.get(i)));
                }
            } else if (points.get(i).y > points.get(i-1).y) {

                list.add(ArrowUtil.getDownArrow(points.get(i)));
            } else {
                list.add(ArrowUtil.getUpArrow(points.get(i)));
            }
        }

        return list;
    }
//    private List<Path> pointToArrow(List<Point> points) {
//        List<Path> list = new ArrayList<Path>();
//
//        if (points == null || points.size()==0){
//            return list;
//        }
//
//        if (mCenterPoint!=null){
//            list.add(drawArrow(mCenterPoint.x,mCenterPoint.y,points.get(0).x,points.get(0).y));
//
//        }
//
//        for (int i = 1; i < points.size(); i++) {
//            Point p = points.get(i);
//            Point lp = points.get(i-1);
//            list.add(drawArrow(lp.x,lp.y,p.x,p.y));
//        }
//

        //画pathPoint最后一个点到center点的arrow
//        if (mCenterPoint!=null){
//            list.add(drawArrow(points.get(points.size()-1).x,points.get(points.size()-1).y,mCenterPoint.x,mCenterPoint.y));
//        }
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);

        int temp=width>height?heightMeasureSpec:widthMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private Point matchClosestPoint(Point centerPoint) {

        int x = centerPoint.x / mGridWidth;
        int y = centerPoint.y / mGridWidth;

        Point point1 = new Point(mGridWidth*x,mGridWidth*y);
        Point point2 = new Point(mGridWidth*(x+1),mGridWidth*y);
        Point point3 = new Point(mGridWidth*x,mGridWidth*(y+1));
        Point point4 = new Point(mGridWidth*(x+1),mGridWidth*(y+1));

        double distance1 = getDistance(centerPoint, point1);
        double distance2 = getDistance(centerPoint, point2);
        double distance3 = getDistance(centerPoint, point3);
        double distance4 = getDistance(centerPoint, point4);

        double[] datas = {distance1,distance2,distance3,distance4};

        Arrays.sort(datas);

        if (datas[0] == distance1){
            return point1;

        }else if (datas[0] == distance2){
            return point2;

        }else if (datas[0] == distance3){
            return point3;

        }else if (datas[0] == distance4){

            return point4;
        }

        return centerPoint;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (!hadDrawGridBitmap){

            hadDrawGridBitmap=true;

            mViewWidth = getWidth();
            mViewHeight = getHeight();
            drawGridBitmap();
        }

        canvas.save();

        canvas.scale(mScale,mScale,mViewWidth/2,mViewHeight/2);

        canvas.translate(mTranslateX/mScale, mTranslateY/mScale);

        canvas.drawBitmap(mGridBitmap,0,0,mPaint);

        //绘制中心点头像和箭头
        if(mCenterPoint != null){

            mPaint.setColor(Color.BLACK);

//            canvas.translate(mCenterPoint.x-mCenterPointBitMap.getWidth()/2.0f,mCenterPoint.y-mCenterPointBitMap.getHeight()/2.0f);
            //绘制中心点的圆
//            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y,15,mPaint);

            canvas.drawBitmap(mCenterPointBitMap,mCenterPoint.x- mCenterPointBitMap.getWidth()/2.0f,mCenterPoint.y- mCenterPointBitMap.getHeight()/2.0f,mPaint);

        }

        //绘制path1的point
        mPaint.setColor(Constant.PATHCOLOR1);
        for (Point p: pathListAfterTransfer1) {
            canvas.drawCircle(p.x, p.y, 10, mPaint);
        }
        //绘制path1
        mPathPaint.setColor(Constant.PATHCOLOR1);
        canvas.drawPath(path1,mPathPaint);
        //绘制最后一个点到centerPoint的路线
//        if (pathListAfterTransfer1.size()>1) {
//            canvas.drawLine(pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).x,pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);
//        }
        //绘制arrow1
        mArrowPaint.setColor(Constant.PATHCOLOR1);
        for (Path path: path1Arrow) {
            canvas.drawPath(path,mArrowPaint);
        }
        //绘制path1最后一个点到mcenterPoint的path
        if (intermediatePoint1!=null){

            canvas.drawLine(pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).x,pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).y,intermediatePoint1.x,intermediatePoint1.y,mPathPaint);

            canvas.drawLine(intermediatePoint1.x,intermediatePoint1.y,mCenterPoint.x,mCenterPoint.y,mPathPaint);

//            canvas.drawPath(drawArrow(pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).x,pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).y,intermediatePoint1.x,intermediatePoint1.y),mArrowPaint);

            canvas.drawPath(drawArrow(intermediatePoint1.x,intermediatePoint1.y,mCenterPoint.x,mCenterPoint.y),mArrowPaint);
        }else {

            if (pathListAfterTransfer1!=null && pathListAfterTransfer1.size()>0 ){

            canvas.drawLine(pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).x,pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);

            canvas.drawPath(drawArrow(pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).x,pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).y,mCenterPoint.x,mCenterPoint.y),mArrowPaint);
            }
        }

        //绘制path2的point
        mPaint.setColor(Constant.PATHCOLOR2);
        for (Point p: pathListAfterTransfer2) {
            canvas.drawCircle(p.x, p.y, 10, mPaint);
        }
        //绘制path2
        mPathPaint.setColor(Constant.PATHCOLOR2);
        canvas.drawPath(path2,mPathPaint);
//        if (pathListAfterTransfer2.size()>1) {
//            canvas.drawLine(pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).x,pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);
//        }
        //绘制arrow2
        mArrowPaint.setColor(Constant.PATHCOLOR2);
        for (Path path: path2Arrow) {
            canvas.drawPath(path,mArrowPaint);
        }

        //绘制path2最后一个点到mcenterPoint的path
        if (intermediatePoint2!=null){

            canvas.drawLine(pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).x,pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).y,intermediatePoint2.x,intermediatePoint2.y,mPathPaint);

            canvas.drawLine(intermediatePoint2.x,intermediatePoint2.y,mCenterPoint.x,mCenterPoint.y,mPathPaint);

//            canvas.drawPath(drawArrow(pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).x,pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).y,intermediatePoint2.x,intermediatePoint2.y),mArrowPaint);

            canvas.drawPath(drawArrow(intermediatePoint2.x,intermediatePoint2.y,mCenterPoint.x,mCenterPoint.y),mArrowPaint);
        }else {

            if (pathListAfterTransfer2!=null && pathListAfterTransfer2.size()>0 ){

                canvas.drawLine(pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).x,pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);

                canvas.drawPath(drawArrow(pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).x,pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).y,mCenterPoint.x,mCenterPoint.y),mArrowPaint);
            }
        }


        //绘制path3的point
        mPaint.setColor(Constant.PATHCOLOR3);
        for (Point p: pathListAfterTransfer3) {
            canvas.drawCircle(p.x, p.y, 10, mPaint);
        }
        //绘制path3
        mPathPaint.setColor(Constant.PATHCOLOR3);
        canvas.drawPath(path3,mPathPaint);
//        if (pathListAfterTransfer3.size()>1) {
//            canvas.drawLine(pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).x,pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);
//        }
        //绘制arrow3
        mArrowPaint.setColor(Constant.PATHCOLOR3);
        for (Path path: path3Arrow) {
            canvas.drawPath(path,mArrowPaint);
        }

        //绘制path3最后一个点到mcenterPoint的path
        if (intermediatePoint3!=null){

            canvas.drawLine(pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).x,pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).y,intermediatePoint3.x,intermediatePoint3.y,mPathPaint);

            canvas.drawLine(intermediatePoint3.x,intermediatePoint3.y,mCenterPoint.x,mCenterPoint.y,mPathPaint);

//            canvas.drawPath(drawArrow(pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).x,pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).y,intermediatePoint3.x,intermediatePoint3.y),mArrowPaint);

            canvas.drawPath(drawArrow(intermediatePoint3.x,intermediatePoint3.y,mCenterPoint.x,mCenterPoint.y),mArrowPaint);
        }else {
            if (pathListAfterTransfer3!=null && pathListAfterTransfer3.size()>0 ){
                canvas.drawLine(pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).x,pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);

                canvas.drawPath(drawArrow(pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).x,pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).y,mCenterPoint.x,mCenterPoint.y),mArrowPaint);
            }


        }
        //在设置路径的时候，预显示点
        if (tempPoint !=null){

            switch (mPathNum){
                case 1:
                    mPaint.setColor(Constant.PATHCOLOR1);
                    canvas.drawCircle(tempPoint.x, tempPoint.y,10,mPaint);

                    break;
                case 2:
                    mPaint.setColor(Constant.PATHCOLOR2);
                    canvas.drawCircle(tempPoint.x, tempPoint.y,10,mPaint);

                    break;
                case 3:
                    mPaint.setColor(Constant.PATHCOLOR3);
                    canvas.drawCircle(tempPoint.x, tempPoint.y,10,mPaint);

                    break;

                default:
                    break;
            }
        }
        
        canvas.restore();

        //Draw机器人所在的位置
        Matrix matrix = canvas.getMatrix();

        matrix.setRotate(mDirectionAngle , mRobotBitMapJainTou.getWidth()/2.0f, mRobotBitMapJainTou.getHeight()/2.0f);

        matrix.postScale(mScale,mScale, mRobotBitMapJainTou.getWidth()/2.0f, mRobotBitMapJainTou.getHeight()/2.0f);

        canvas.save();

        Point point = transferCoordinateToView(mRobotPoint);

        canvas.translate(point.x- mRobotBitMapJainTou.getWidth()/2.0f,point.y- mRobotBitMapJainTou.getHeight()/2.0f);

        canvas.drawBitmap(mRobotBitMapJainTou,matrix,null);
        canvas.drawBitmap(mRobotBitMapTouXiang,matrix,null);
        canvas.restore();

    }

    private void drawLastPointAndArrow(int pathNum,Point point) {

        switch (pathNum){
            case 1:{
                if (point == null){
                    intermediatePoint1 = null;
                    return;
                }else {

                    //说明需要不增加一个中间点
                    if (point.x == mCenterPoint.x && point.y == mCenterPoint.y){
                        intermediatePoint1 = null;
                        return;
                    }
                    else if (point.x == mCenterPoint.x || point.y == mCenterPoint.y){//说明x轴或者y轴有一个是Ok的,不需要增加中间点
                        intermediatePoint1 = null;
                        return;
                    }
                    else {//说明需要增加中间点
                        intermediatePoint1 = new Point(mCenterPoint.x,point.y);
                        return;
                    }
                }
            }
            case 2: {
                if (point == null) {
                    intermediatePoint2 = null;
                    return;
                } else {
                    //说明需要不增加一个中间点
                    if (point.x == mCenterPoint.x && point.y == mCenterPoint.y) {
                        intermediatePoint2 = null;
                        return;
                    } else if (point.x == mCenterPoint.x || point.y == mCenterPoint.y) {//说明x轴或者y轴有一个是Ok的,不需要增加中间点
                        intermediatePoint2 = null;
                        return;
                    } else {//说明需要增加中间点
                        intermediatePoint2 = new Point(mCenterPoint.x, point.y);
                        return;
                    }
                }
            }
            case 3: {
                if (point == null) {
                    intermediatePoint3 = null;
                    return;
                } else {
                    //说明需要不增加一个中间点
                    if (point.x == mCenterPoint.x && point.y == mCenterPoint.y) {
                        intermediatePoint3 = null;
                        return;
                    } else if (point.x == mCenterPoint.x || point.y == mCenterPoint.y) {//说明x轴或者y轴有一个是Ok的,不需要增加中间点
                        intermediatePoint3 = null;
                        return;
                    } else {//说明需要增加中间点
                        intermediatePoint3 = new Point(mCenterPoint.x, point.y);
                        return;
                    }
                }
            }
            default:
            break;
        }
    }

    /**
     * @param point:在view中的位置
     * @param isSave:是否保存数据到数据库
     */
    public void setCenterPoint(Point point,boolean isSave){

        if (!canSetCenterPoint){
              return;
           }

        //坐标转换
        Point point1 = transferCoordinateToMap(point);

        mCenterPoint = matchClosestPoint(point1);

        cleanPathPointAndPath(1);
        cleanPathPointAndPath(2);
        cleanPathPointAndPath(3);
        intermediatePoint1 = null;
        intermediatePoint2 = null;
        intermediatePoint3 = null;

        invalidate();

        if (isSave){

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRobotPointInMap(mCenterPoint,true);
                }
            },200);

            mDbUtil.deleteAll();
            mDbUtil.addPoint(mCenterPoint.x,mCenterPoint.y, TYPE_CENTERPOINT);
        }

    }


    /**
     * 将view中位置的点转化成map中的点
     * @param point:view位置中的点
     * @return 返回view位置对应于map中的点
     */
    private Point transferCoordinateToMap(Point point) {
        int x = (int) ((mViewWidth/2*mScale-(mViewWidth/2-point.x+mTranslateX))/mScale);
        int y = (int) ((mViewHeight/2*mScale-(mViewHeight/2-point.y+mTranslateY))/mScale);
        return new Point(x,y);
    }

    /**
     * 将map中的点转化重view位置中的点
     * @param point:map中的点
     * @return 返回map中的点对应于view中的位置
     */
    private Point transferCoordinateToView(Point point){

//        int x = (int) ((mViewWidth/2*mScale-(mViewWidth/2-point.x+mTranslateX))/mScale);
//        int y = (int) ((mViewHeight/2*mScale-(mViewHeight/2-point.y+mTranslateY))/mScale);

        int x = (int) (point.x*mScale+mViewWidth/2.0+mTranslateX-mViewWidth/2.0*mScale);

        int y = (int) (point.y*mScale+mViewHeight/2.0+mTranslateY-mViewHeight/2.0*mScale);

        return new Point(x,y);
    }

    public Point getCenterPoint(){

        return mCenterPoint;
    }

    private void cleanPathPointAndPath(int pathNum){

        switch (pathNum){
            case 1:
                pathListAfterTransfer1.clear();
                path1Arrow.clear();
                path1.reset();
                path1.moveTo(mCenterPoint.x,mCenterPoint.y);

            break;
            case 2:
                pathListAfterTransfer2.clear();
                path2Arrow.clear();
                path2.reset();
                path2.moveTo(mCenterPoint.x,mCenterPoint.y);

            break;
            case 3:
                pathListAfterTransfer3.clear();
                path3Arrow.clear();
                path3.reset();
                path3.moveTo(mCenterPoint.x,mCenterPoint.y);

            break;
            default:
            break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{

                    float x = event.getX();
                    float y = event.getY();

                if(isCanSetCenterPoint()){//说明是要设置原点

                    setCenterPoint(new Point((int)x,(int)y),false);
//                    mMapFragment.mLl_path.setVisibility(View.VISIBLE);
//                    setCanSetCenterPoint(false);
//                    return false;
                    return true;
                }
                else if (isCanSetRobotPoint()){ //说明是要设置机器人图标位置

                    setRobotPointInView(new Point((int)x,(int)y));
                    return true;
                }

                else if(isCanSetPath()){//说明是要设置路径

                    tempPoint =matchClosestPoint(transferCoordinateToMap(new Point((int)x,(int)y)));
                    mMapFragment.setPathPointInfo(transferCoordinateToMap(tempPoint));

                    invalidate();
                    return true;
//                    addPathPoint(mPathNum,new Point((int)(x+0.5),(int)(y+0.5)));
//                    return false;
                }
                else {//说明不是在设置原点
                    mStartX = event.getX();
                    mStartY = event.getY();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE:{

                float x =event.getX();
                float y = event.getY();

                if (isCanSetCenterPoint()){//说明是要设置原点
                    setCenterPoint(new Point((int)x,(int)y),false);
                    return true;
                }
                else if (isCanSetRobotPoint()){//说明是要设置机器人图标位置
                    setRobotPointInView(new Point((int)x,(int)y));
                    return true;
                }
                else if (isCanSetPath()){//说明是要设置路径
                    tempPoint =matchClosestPoint(transferCoordinateToMap(new Point((int)x,(int)y)));
                    mMapFragment.setPathPointInfo(transferCoordinateToMap(tempPoint));
                    invalidate();
                    return true;
                }

                //说明要平移地图
                float endX = event.getX();
                float endY = event.getY();
                float deltaX = endX - mStartX;
                float deltaY = endY - mStartY;
                mStartX = endX;
                mStartY = endY;
                setTranslate(deltaX,deltaY);

                break;
            }
            case MotionEvent.ACTION_UP:{
                float x = event.getX();
                float y = event.getY();

                if (isCanSetCenterPoint()){//如果是在设置原点，取消可以设置原点
                    mMapFragment.mLl_path.setVisibility(View.VISIBLE);
                    setCenterPoint(new Point((int)(x+0.5f),(int)(y+0.5f)),true);
                    setCanSetCenterPoint(false);
                }
                else if (isCanSetRobotPoint()){//如果是在设置机器人位置，取消可以设置机器人位置
                    setCanSetRobotPoint(false);
                    setRobotPointInView(new Point((int)(x+0.5f),(int)(y+0.5f)));
                }
                else if (isCanSetPath()){//说明是要设置路径
                    tempPoint =matchClosestPoint(transferCoordinateToMap(new Point((int)x,(int)y)));
                    mMapFragment.setPathPointInfo(transferCoordinateToMap(tempPoint));
                    addPathPoint(mPathNum,tempPoint,true,false);
                    tempPoint = null;
                }
                break;
            }
            default:
                break;
        }
        return true;
    }

    public Path drawArrow(int sx, int sy, int ex, int ey) {

        return ArrowUtil.drawArrow(sx,sy,ex,ey);

    }

    /**
     * @param walkModel:运动模式
     * @return 返回下一个目标点的Point
     */
    public Point getNextPoint(int walkModel){

        if ( pathListAfterTransfer1==null || pathListAfterTransfer1.size() == 0){//说明此路径没有设置

            ToastUtils.makeToast(mContext,"请设置路径点");

            return null;
        }else {
            if (mCurrentListPosition+1 >= pathListAfterTransfer1.size()){//说明要回原点了
                return mCenterPoint;
            }
            else {

                return pathListAfterTransfer1.get(mCurrentListPosition+1);
            }
        }
    }

    /**
     * @return 返回上一个漫游路径点
     */
    public Point getStartManYouPoint(){
        if (mCurrentListPosition == -1){
            return mCenterPoint;
        }

        return pathListAfterTransfer1.get(mCurrentListPosition);
    }
}