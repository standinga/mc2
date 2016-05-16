package co.borama.mirrorCoach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.lang.System;
// for delay
import java.util.LinkedList;
import java.util.Queue;

public class OverlayView extends SurfaceView implements SurfaceHolder.Callback {
    Paint pt = new Paint();
    private static final String TAG = "OverlayView" ;
    private Camera mCam;
    private SurfaceHolder mOverSH;
    private byte[] mFrame;
    private Camera.Size mFrameSize;
    private boolean mRunning;
    private int[] mRGBarray;
    private long sTime = System.currentTimeMillis();
    int j = 1;
    private int mWHsize;
    Camera.Parameters p;
    Camera.Size fs;
    Canvas cOver;
    Rect src;
    RectF dst;
    private Queue <Bitmap>mQueue = new LinkedList(); // used as buffer
    private Queue <byte[]>mByteQueue = new LinkedList();
    private int mbufferSize = 23; // how much delay to display
    private double mAvFps = 0;
    long average = 0;
    int insideAv = 0;

    int flipped = 1;




    Bitmap bmp;



    public static native  void doNativeProcessing(byte[] data, int[] rgb,int width, int height, int flipped);
//    public native void doNativeProcessing(Bitmap pTarget, byte[] pSource);
//    public static native int[] doNativeProcessing(byte[] data, int width, int height);
    static
    {
        System.loadLibrary("mirrorcoach");
    }

    public OverlayView(Context c, AttributeSet attr)
    {
        super(c, attr);
        mOverSH = getHolder();
        mOverSH.addCallback(this);
    }

    public void setCamera(Camera c)
    {


        mCam = c;
        if(mCam == null) return;

        Log.v(TAG,"setCamera");

        mCam.setPreviewCallback(new PreviewCallback()
        {
            public void onPreviewFrame(byte[] yuv, Camera c)
            {

                if(yuv.length != (mFrame.length*1.5))
                {
                    return;
                }

                p = mCam.getParameters();
                fs = p.getPreviewSize();
                if(fs.width != mFrameSize.width || fs.height != mFrameSize.height)
                {
                    setPreviewSize(fs);
                }


                cOver = null;

                if(mRunning) cOver = mOverSH.lockCanvas(null);

                if(mRunning && cOver!=null)
                {
                    try
                    {
                        // try add yuvs to queue instead of bitmaps


//                        bmp = Bitmap.createBitmap(mFrameSize.width,
//                                mFrameSize.height, Bitmap.Config.ARGB_8888);
                         bmp = Bitmap.createBitmap(mRGBarray, mFrameSize.width,
                                mFrameSize.height, Bitmap.Config.ARGB_8888);



                        long start =  System.nanoTime();
                      doNativeProcessing(yuv,mRGBarray,mFrameSize.width, mFrameSize.height, flipped);
//                        doNativeProcessing(bmp, yuv);
                        long end =  System.nanoTime();
                        long pTime = end/1000 - start/1000;
                        average = (average * (j -1) + pTime)/j;
                        if (j % 40 == 0)
                        Log.v(TAG, " yuv takes: " +String.valueOf(j) + " avYuv:  "
                                + String.valueOf(average)
                        );

                        showLOGandFPS ();

                        src = new Rect(0, 0, mFrameSize.width-1, mFrameSize.height-1);
                        dst = new RectF(0, 0, cOver.getWidth()-1, cOver.getHeight()-1);

                        pt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                        pt.setColor(Color.WHITE);
                        pt.setAlpha(0xFF);

                        // queue for delaying preview

//                 if (j < mbufferSize) { // old version using only buffer
//                mQueue.add(bmp.copy(bmp.getConfig(), true));
//                cOver.drawBitmap(bmp, src, dst, pt);
//                    Log.v(TAG, " mQueue in  " + String.valueOf(mQueue.size()) + " j " + String.valueOf(j));
//                 }
//                mQueue.add(bmp.copy(bmp.getConfig(), true));
//                Log.v(TAG, " mQueue " + String.valueOf(mQueue.size()));
//                cOver.drawBitmap(mQueue.poll(), src, dst, pt);
//
//                        Log.v(TAG, " size " + String.valueOf(bmp.getByteCount()) + " yuv: " +
//                         String.valueOf(yuv.length));



                        cOver.drawBitmap(bmp, src, dst, pt); // this is testing without queue
                    }
                    finally
                    {
                        mOverSH.unlockCanvasAndPost(cOver);
                    }
                }
            }
        });
    }

    public void setRunning(boolean r) { mRunning = r;}

    public void setPreviewSize(Camera.Size s)
    {
        mFrameSize = s;
        mFrame = new byte[s.width*s.height];
        // Allocate a 32-bit buffer as large as the preview
        mRGBarray = new int[s.width * s.height];
    }

    public void surfaceCreated(SurfaceHolder sh) {}

    public void surfaceDestroyed(SurfaceHolder sh) {}

    public void surfaceChanged(SurfaceHolder sh, int format, int w, int h) {
    }

    private void showLOGandFPS () {
        int interval = 30;
        if (j % interval == 0) {
            long eTime = System.currentTimeMillis();
            long pTime = eTime - sTime;
            double FPS = interval * 1000.0 / (double) pTime;
            mAvFps = (mAvFps * (j/interval -1) + FPS)/j*interval;
            sTime = eTime;
            Log.v(TAG, "onPreviewFrame" + " FPS: "
                    + String.valueOf(FPS) + " i: "
                    + String.valueOf(j) +" avFPS: "
                    + String.valueOf(mAvFps) + "  mFrameSize.width "
                    + String.valueOf( mFrameSize.width) + " H "
            + String.valueOf(mFrameSize.height));
        }
        j++;
    }


}