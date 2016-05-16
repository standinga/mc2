package co.borama.mirrorCoach;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;


public class MirrorCoach extends Activity implements SurfaceHolder.Callback
{
    private static final String LOGTAG = "MirrorCoach";
    private static final int MENU_EXIT = 0xCC882201;
	private static final String TAG = "MirrorCoach" ;

	private Camera mCam = null;
    private SurfaceView mCamSV;
    private PowerManager.WakeLock mLock;
    private OverlayView mOverSV;
    private SurfaceHolder mCamSH;
    private OrientationEventListener mOEL;
    private Camera.Size mPreviewSize = null;
    private boolean mPreview = false;
    private int mOrient = 0;
	ImageView outputImageView;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	
	//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	getWindow().setFormat(PixelFormat.TRANSLUCENT);
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
	mLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Mirror Coach");
	mLock.acquire();
	    
        setContentView(R.layout.main);
    }

    public boolean onCreateOptionsMenu(Menu m)
    {
    	m.add(0, MENU_EXIT, 0, "Exit");
	return true;
    }

    public boolean onOptionsItemSelected(MenuItem i)
    {
    	switch(i.getItemId())
	{
	    case MENU_EXIT:
	    	this.finish();
		return true;
	    default:
	    	return false;
	}
    }

    @Override
    public void onDestroy()
    {
		if (mCam != null)
		mCam.release();
	mLock.release();
	super.onDestroy();
    }

    @Override
    public void onStart()
    {
    	initCamera();
	super.onStart();
    }

    @Override
    public void onResume()
    {
    	initCamera();
	super.onResume();
    }

    @Override
    public void onPause()
    {
    	stopCamera();
    	mCamSH.removeCallback(this);
    	this.finish();
	super.onPause();
    }

    public void surfaceCreated(SurfaceHolder sh)
    {
    }

    public void surfaceChanged(SurfaceHolder sh, int format, int w, int h)
    {

	if(mCam != null)
	{
            if(mPreview) mCam.stopPreview();
	    Camera.Parameters p = mCam.getParameters();
	    p.setRotation(mOrient);

//		for(int i: p.getSupportedPreviewFormats()) { Log.v(TAG, "preview format supported are = "+i);}

	    for(Camera.Size s : p.getSupportedPreviewSizes())
	    {
	        p.setPreviewSize(s.width, s.height);
		mOverSV.setPreviewSize(s);
		mPreviewSize = s;

	        break;
	    }
		p.setPreviewFormat(ImageFormat.NV21);
	    mCam.setParameters(p);
	    try
	    {
//		mCam.setPreviewDisplay(sh); // don't show org preview
	    }
	    catch(Exception e)
	    {
//	    	Log.e(LOGTAG, "Camera preview not set");
	    }
	    mCam.startPreview();
	    mPreview = true;
	}
    }

    public void surfaceDestroyed(SurfaceHolder sh)
    {
		// release camera
		if (mCam != null) {
			// Call stopPreview() to stop updating the preview surface.
			mCam.stopPreview();
			mCam.release();
			mCam = null;
		}

    }

    private void initCamera()
    {
	mCamSV = (SurfaceView)findViewById(R.id.surface_camera);
//		outputImageView = (ImageView) findViewById((R.id.outputImageView));



	mCamSH = mCamSV.getHolder();
	mCamSH.addCallback(this);
	mCamSH.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	mOverSV = (OverlayView)findViewById(R.id.surface_overlay);
	mOverSV.getHolder().setFormat(PixelFormat.TRANSLUCENT);

//		if (mCam != null) mCam = null;

    	if(mCam == null)
	{
	    mCam = Camera.open(0); // 0 - front 1 back (mirror)
	}
	if(mPreviewSize != null && mPreviewSize.width > 0 && mPreviewSize.height > 0)
	{
	    mOverSV.setPreviewSize(mPreviewSize);
	}

	mOverSV.setCamera(mCam);
	mOverSV.setRunning(true);
	mPreview = false;
	mOEL = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL)
	{
	    @Override
	    public void onOrientationChanged(int o)
	    {
		if(o == ORIENTATION_UNKNOWN) return;
		o = (o+45)/90*90;
		mOrient = o%360; // was 360
	    }
	};
	if(mOEL.canDetectOrientation()) mOEL.enable();
    }

    private void stopCamera()
    {
    	mOEL.disable();
	mOverSV.setRunning(false);
	mCam.stopPreview();
	mPreview = false;
	mCam.setPreviewCallback(null);
	mCam.release();
	mCam = null;
    }
}
