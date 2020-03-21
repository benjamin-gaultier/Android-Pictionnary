package com.example.benjamin.googlefirebasetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.BoringLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PaintView extends View {

    public static final int PIXEL_SIZE = 1;
    public static int BRUSH_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private DatabaseReference mDataBase;
    private ArrayList<String> sb = new ArrayList<>();
    private ChildEventListener mListener;
    private Set<String> mOutstandingSegments;
    private Segment mCurrentSegment;
    private float mScale = 1.0f;
    public int numInjections = 0;
    public String canvas_name;
    public int height;
    public int width;
    public boolean isDrawable = false;

    public PaintView(Context context) {
        this(context,null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context,attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
        mCurrentSegment = new Segment();
        mOutstandingSegments = new HashSet<String>();
        System.out.println("Width: " + width + " height: " + height);



        this.canvas_name = canvas_name;
        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
        System.out.println("canvas Name: " + canvas_name);

        mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
        mPath = new Path();


    }

    public void addListener(DatabaseReference ref,DisplayMetrics metrics,int height,String player){
        this.height = height;
        this.width = metrics.widthPixels;
        if (player.equals("creator"))
            isDrawable = true;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mDataBase = ref.child("path");
        mListener = mDataBase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("Database: " + mDataBase.getKey());
                if (!mOutstandingSegments.contains(dataSnapshot.getKey())) {
                    Iterator<DataSnapshot> childrens = dataSnapshot.getChildren().iterator();
                    FingerPath firstfp = childrens.next().getValue(FingerPath.class);
                    //touchStart(firstfp.x,firstfp.y);
                    paths.add(firstfp);

                    while (childrens.hasNext()) {
                        DataSnapshot child = childrens.next();
                        FingerPath fp = child.getValue(FingerPath.class);
                        paths.add(fp);

                    }

                    System.out.println("Num of children: " + dataSnapshot.getChildrenCount());
                    System.out.println("Paths: " + paths.size());
                    System.out.println("Paths description: " + paths.toString());
                    if (paths.size() != 0)
                        drawFingerPath(paths, paintFromColor(firstfp.color));

                    invalidate();

                    paths.clear();
                }
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void drawFingerPath(List<FingerPath> fp, Paint paint) {
        if (mCanvas != null){
            mCanvas.drawPath(getPathForPoints(fp,mScale),paint);
        }

    }

//    private void drawSegment(ArrayList<FingerPath> fp) {
//        if (mCanvas != null) {
//            mCanvas.drawPath(getPathForPoints(fp, mScale), paint);
//        }
//    }

    public static Paint paintFromColor(int color) {
        return paintFromColor(color, Paint.Style.STROKE);
    }

    public static Paint paintFromColor(int color, Paint.Style style) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setColor(color);
        p.setStyle(style);
        return p;
    }

    public void cleanup() {
        mDataBase.removeEventListener(mListener);
    }

    public Path getPathForPoints(List<FingerPath> points, double scale) {
        Path path = new Path();
        //scale = scale * BRUSH_SIZE;
        FingerPath current = points.get(0);
        path.moveTo(Math.round(width * current.x), Math.round(height * current.y));
        FingerPath next = null;
        for (int i = 1; i < points.size(); ++i) {
            next = points.get(i);
            path.quadTo(
                    Math.round(width * current.x), Math.round(height * current.y),
                    Math.round(width * (next.x + current.x) / 2), Math.round(height * (next.y + current.y) / 2)
            );
            System.out.println("FP: " + next);
            current = next;
        }
        if (next != null) {
            path.lineTo(Math.round(width * next.x), Math.round(height * next.y));
        }
        //points.clear();
        //System.out.println(path.toString());
        return path;
    }

    public void init(DisplayMetrics metrics,String canvas_name,DatabaseReference ref) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        this.canvas_name = canvas_name;
        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
        System.out.println("canvas Name: " + canvas_name);
        mDataBase = ref;
        System.out.println("Changed Canvas Name: " + mDataBase.getRef());
    }

    public void addCoordinates(float x, float y){

    }

    public void normal(){
        emboss = false;
        blur = false;
    }

    public  void emboss(){
        emboss = true;
        blur = false;
    }

    public void blur(){
        emboss = false;
        blur = true;
    }

    public void clear(){
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        normal();
        mOutstandingSegments.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawColor(backgroundColor);
        //canvas.drawRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), paintFromColor(Color.WHITE, Paint.Style.FILL_AND_STROKE));
        for(FingerPath fp : paths){
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            mCanvas.drawPath(mPath, mPaint);

        }
        System.out.println("Went through onDraw");
        canvas.drawBitmap(mBitmap, 0 , 0 ,mBitmapPaint);
        //canvas.restore();

    }

    private void touchStart(float x, float y){
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur,strokeWidth,x,y);
        paths.add(fp);
        mCurrentSegment = new Segment(currentColor);
        mPath.reset();
        mPath.moveTo(x, y);
        mX = (int) x / PIXEL_SIZE;
        mY = (int) y / PIXEL_SIZE;
        mCurrentSegment.addPoint(mX, mY);
        System.out.println("Touch Start    x: " + mX + " y: " + mY);
    }

    private void touchMove(float x, float y){

        int x1 = (int) x / PIXEL_SIZE;
        int y1 = (int) y / PIXEL_SIZE;
        float dx = Math.abs(x1 - mX);
        float dy = Math.abs(y1 - mY);


        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(Math.round(mX * PIXEL_SIZE), Math.round(mY * PIXEL_SIZE), Math.round(((x1 + mX) * PIXEL_SIZE) / 2), Math.round(((y1 + mY) * PIXEL_SIZE) / 2));
            mX = x1;
            mY = y1;
            FingerPath fp = new FingerPath(currentColor, emboss, blur,strokeWidth, mX,mY);
            paths.add(fp);
            mCurrentSegment.addPoint(mX,mY);
        }
        //System.out.println("Touch Move    x: " + x1 + " y: " + y1);
    }

    private void touchUp() {
        mPath.lineTo(mX*PIXEL_SIZE, mY*PIXEL_SIZE);
        if (paths.size() > 1)
            sendToDatabase(paths);
        System.out.println(paths);
        paths.clear();
//        final Segment segment = new Segment(mCurrentSegment.getColor());
//        for (Point point : mCurrentSegment.getPoints()){
//            segment.addPoint(point.x,point.y);
//        }
//        segmentRef.setValue(segment, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                if (databaseError != null){
//                    Log.e("AndroidDrawing Etna ",databaseError.toString());
//                    throw databaseError.toException();
//                }
        //mOutstandingSegments.remove(segmentName);
//            }
//        });
    }

    private void sendToDatabase(final ArrayList<FingerPath> fpList){
        final String id  = mDataBase.push().getKey();
        for (FingerPath fp :fpList){
            fp.x = (int)Math.round(fp.x) / (float)getWidth();
            fp.y = (int)Math.round(fp.y) /(float)getHeight();
        }

        mOutstandingSegments.add(id);
        mDataBase.child(id).child("path").setValue(fpList, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError!=null){
                    Log.e("Paint view failure ", databaseError.toString());
                    throw databaseError.toException();
                }
                mOutstandingSegments.remove(id);
                numInjections+=fpList.size();
                System.out.println("Number of injections: " + numInjections);
                fpList.clear();
            }
        });
        System.out.println("Id of segment: " + id);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        if (!isDrawable)
            return false;
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }


}
