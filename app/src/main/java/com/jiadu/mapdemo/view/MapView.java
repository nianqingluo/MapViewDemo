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
import com.jiadu.mapdemo.util.Constant;
import com.jiadu.mapdemo.util.MyDataBaseUtil;

import java.util.ArrayList;
import java.util.List;

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
    private Point mRobortPoint = new Point();  //记录robort在地图中的位置

    private float mDirectionAngle = 0;

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
    private Bitmap mRobortBitMapJainTou;

    private boolean canSetRobotPoint = false;
    private MapFragment mMapFragment;

    private Bitmap mCenterPointBitMap =null;
    private Bitmap mRobotBitMapTouXiang =null;

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
     */
    public void setRobortPointInMap(Point point){

        if (point==null){
            return;
        }

        mRobortPoint = point;
        if (mMapFragment!=null){
            mMapFragment.setRobotPointInfo();
        }
        invalidate();
    }


    /**
     * @param point:在View位置中的点
     */
    public void setRobortPointInView(Point point){

        if (point==null){
            return;
        }

        mRobortPoint = transferCoordinateToMap(point);
        
        if (mMapFragment!=null){
            mMapFragment.setRobotPointInfo();
        }

        invalidate();
    }

    /**
     * @return robot在map中的点
     */
    public Point getRobortPointInMap(){

        return mRobortPoint;
    }

    /**
     * @return robot在View中的位置
     */
    public Point getRobortPointInView(){

        return transferCoordinateToView(mRobortPoint);
    }

    public void setDirectionAngle(float directionAngle) {
        this.mDirectionAngle = directionAngle;

        invalidate();
    }

    public void setPathNum(int pathNum) {
        mPathNum = pathNum;
    }

    public void addPathPoint(int pathNum, Point point){
        Point point1 = transferCoordinateToMap(point);
        mDbUtil.addPoint(point.x,point.y,pathNum);
        switch (pathNum){
            case 1:
//                pathList1.add(point);
                pathListAfterTransfer1.add(point1);
                path1.lineTo(point1.x,point1.y);
                path1Arrow = pointToArrow(pathListAfterTransfer1);

            break;
            case 2:
//                pathList2.add(point);
                pathListAfterTransfer2.add(point1);
                path2.lineTo(point1.x,point1.y);
                path2Arrow = pointToArrow(pathListAfterTransfer2);

            break;
            case 3:
//                pathList3.add(point);
                pathListAfterTransfer3.add(point1);
                path3.lineTo(point1.x,point1.y);
                path3Arrow = pointToArrow(pathListAfterTransfer3);
            break;
            default:
            break;
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

                    path1.reset();
                    path1.moveTo(mCenterPoint.x ,mCenterPoint.y);
                    for (Point p:pathListAfterTransfer1) {
                        path1.lineTo(p.x,p.y);
                    }

                    path1Arrow = pointToArrow(pathListAfterTransfer1);
                }

                break;
            case 2:
                if (pathListAfterTransfer2.size()>=1){

                    Point pt = pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1);

                    mDbUtil.deletePoint(pt.x,pt.y,pathNum);

                    pathListAfterTransfer2.remove(pathListAfterTransfer2.size()-1);
                    path2.reset();
                    path2.moveTo(mCenterPoint.x ,mCenterPoint.y);
                    for (Point p:pathListAfterTransfer2) {
                        path2.lineTo(p.x,p.y);
                    }
                    path2Arrow = pointToArrow(pathListAfterTransfer2);
                }

                break;
            case 3:
                if (pathListAfterTransfer3.size()>=1){

                    Point pt = pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1);

                    mDbUtil.deletePoint(pt.x,pt.y,pathNum);

                    pathListAfterTransfer3.remove(pathListAfterTransfer3.size()-1);
                    path3.reset();
                    path3.moveTo(mCenterPoint.x ,mCenterPoint.y);
                    for (Point p:pathListAfterTransfer3) {
                        path3.lineTo(p.x,p.y);
                    }

                    path3Arrow = pointToArrow(pathListAfterTransfer3);
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

        mRobortBitMapJainTou = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.jiantou);

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

        setRobortPointInMap(mCenterPoint);

        pathListAfterTransfer1 = mDbUtil.queryPathPoint(1);
        pathListAfterTransfer2 = mDbUtil.queryPathPoint(2);
        pathListAfterTransfer3 = mDbUtil.queryPathPoint(3);

        path1.moveTo(mCenterPoint.x ,mCenterPoint.y);
        for (Point p:pathListAfterTransfer1) {
            path1.lineTo(p.x,p.y);
        }

        path1Arrow = pointToArrow(pathListAfterTransfer1);


        path2.moveTo(mCenterPoint.x ,mCenterPoint.y);
        for (Point p:pathListAfterTransfer2) {
            path2.lineTo(p.x,p.y);
        }

        path2Arrow = pointToArrow(pathListAfterTransfer2);

        path3.moveTo(mCenterPoint.x ,mCenterPoint.y);
        for (Point p:pathListAfterTransfer3) {
            path3.lineTo(p.x,p.y);
        }

        path3Arrow = pointToArrow(pathListAfterTransfer3);

    }

    private List<Path> pointToArrow(List<Point> points) {
        List<Path> list = new ArrayList<Path>();
        
        if (points == null || points.size()==0){
            return list;
        }
        
        if (mCenterPoint!=null){
            list.add(drawArrow(mCenterPoint.x,mCenterPoint.y,points.get(0).x,points.get(0).y));
        }

        for (int i = 1; i < points.size(); i++) {
            Point p = points.get(i);
            Point lp = points.get(i-1);
            list.add(drawArrow(lp.x,lp.y,p.x,p.y));
        }

        if (mCenterPoint!=null){
            list.add(drawArrow(points.get(points.size()-1).x,points.get(points.size()-1).y,mCenterPoint.x,mCenterPoint.y));
        }
        return list;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);

        int temp=width>height?heightMeasureSpec:widthMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        //绘制path2的point
        mPaint.setColor(Constant.PATHCOLOR2);
        for (Point p: pathListAfterTransfer2) {
            canvas.drawCircle(p.x, p.y, 10, mPaint);
        }
        //绘制path3的point
        mPaint.setColor(Constant.PATHCOLOR3);
        for (Point p: pathListAfterTransfer3) {
            canvas.drawCircle(p.x, p.y, 10, mPaint);
        }

        //绘制path1
        mPathPaint.setColor(Constant.PATHCOLOR1);
        canvas.drawPath(path1,mPathPaint);
        if (pathListAfterTransfer1.size()>1) {

            canvas.drawLine(pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).x,pathListAfterTransfer1.get(pathListAfterTransfer1.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);

        }
        //绘制arrow1
        mArrowPaint.setColor(Constant.PATHCOLOR1);
        for (Path path: path1Arrow) {
            canvas.drawPath(path,mArrowPaint);
        }
        //绘制arrow1的开始箭头和结束箭头


        //绘制path2
        mPathPaint.setColor(Constant.PATHCOLOR2);
        canvas.drawPath(path2,mPathPaint);
        if (pathListAfterTransfer2.size()>1) {
            canvas.drawLine(pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).x,pathListAfterTransfer2.get(pathListAfterTransfer2.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);
        }
        //绘制arrow2
        mArrowPaint.setColor(Constant.PATHCOLOR2);
        for (Path path: path2Arrow) {
            canvas.drawPath(path,mArrowPaint);
        }

        //绘制path3
        mPathPaint.setColor(Constant.PATHCOLOR3);
        canvas.drawPath(path3,mPathPaint);
        if (pathListAfterTransfer3.size()>1) {
            canvas.drawLine(pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).x,pathListAfterTransfer3.get(pathListAfterTransfer3.size()-1).y,mCenterPoint.x,mCenterPoint.y,mPathPaint);
        }
        //绘制arrow3
        mArrowPaint.setColor(Constant.PATHCOLOR3);
        for (Path path: path3Arrow) {
            canvas.drawPath(path,mArrowPaint);
        }

        canvas.restore();

        //Draw机器人所在的位置
        Matrix matrix = canvas.getMatrix();

        matrix.setRotate(mDirectionAngle , mRobortBitMapJainTou.getWidth()/2.0f, mRobortBitMapJainTou.getHeight()/2.0f);

        matrix.postScale(mScale,mScale, mRobortBitMapJainTou.getWidth()/2.0f, mRobortBitMapJainTou.getHeight()/2.0f);

        canvas.save();

        Point point = transferCoordinateToView(mRobortPoint);

        canvas.translate(point.x- mRobortBitMapJainTou.getWidth()/2.0f,point.y- mRobortBitMapJainTou.getHeight()/2.0f);

        canvas.drawBitmap(mRobortBitMapJainTou,matrix,null);
        canvas.drawBitmap(mRobotBitMapTouXiang,matrix,null);
        canvas.restore();

    }

    private Point matchClosestPoint(Point centerPoint) {

        return centerPoint;
    }

    public void setCenterPoint(Point point){

        if (!canSetCenterPoint){
              return;
           }

        Point closestPoint =  matchClosestPoint(point);
        //坐标转换
        mCenterPoint = transferCoordinateToMap(closestPoint);

        cleanPathPointAndPath(1);
        cleanPathPointAndPath(2);
        cleanPathPointAndPath(3);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                setRobortPointInMap(mCenterPoint);
            }
        },200);


        invalidate();

        mDbUtil.deleteAll();

        mDbUtil.addPoint(mCenterPoint.x,mCenterPoint.y, TYPE_CENTERPOINT);

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
            case MotionEvent.ACTION_DOWN:

                if(isCanSetCenterPoint()){//说明是要设置原点

                    float x = event.getX();
                    float y = event.getY();

                    setCenterPoint(new Point((int)(x+0.5),(int)(y+0.5)));

                    mContext.findViewById(R.id.ll_path).setVisibility(View.VISIBLE);

                    //                    mContext.mLl_path.setVisibility(View.VISIBLE);
                    setCanSetCenterPoint(false);
                    return false;
                }else if(isCanSetPath()){//说明是要设置路径
                    float x = event.getX();
                    float y = event.getY();

                    addPathPoint(mPathNum,new Point((int)(x+0.5),(int)(y+0.5)));
                    return false;
                }else if (isCanSetRobotPoint()){ //说明是要设置机器人图标位置

                    setRobortPointInView(new Point((int)event.getX(),(int)event.getY()));
                    return true;
                }
                else {//说明不是在设置原点

                    mStartX = event.getX();
                    mStartY = event.getY();
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (isCanSetRobotPoint()){

                    setRobortPointInView(new Point((int)event.getX(),(int)event.getY()));

                    return true;
                }


                float endX = event.getX();
                float endY = event.getY();

                float deltaX = endX - mStartX;
                float deltaY = endY - mStartY;

                mStartX = endX;
                mStartY = endY;

                setTranslate(deltaX,deltaY);

                break;
            case MotionEvent.ACTION_UP:

                setCanSetRobotPoint(false);

                break;


            default:
                break;
        }
        return true;
    }

    public Path drawArrow(int sx, int sy, int ex, int ey)
    {
        double H = 28; // 箭头高度
        double L = 11; // 底边的一半
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        double awrad = Math.atan(L / H); // 箭头角度
        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点
        double y_3 = ey - arrXY_1[1];
        double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点
        double y_4 = ey - arrXY_2[1];
        Double X3 = new Double(x_3);
        x3 = X3.intValue();
        Double Y3 = new Double(y_3);
        y3 = Y3.intValue();
        Double X4 = new Double(x_4);
        x4 = X4.intValue();
        Double Y4 = new Double(y_4);
        y4 = Y4.intValue();
        // 画线
        //        canvas.drawLine(sx, sy, ex, ey,mPaint);
        Path triangle = new Path();
        triangle.moveTo(ex, ey);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.close();
//        canvas.drawPath(triangle,mPaint);
        return triangle;
    }

    // 计算
    public double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen)
    {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }
}