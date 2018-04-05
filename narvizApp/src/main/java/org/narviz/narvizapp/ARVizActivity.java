package org.narviz.narvizapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class ARVizActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    WifiManager wifiManager;
    Handler h = new Handler();
    Handler vizHandler = new Handler();
    int delay = 1000; //1 second
    Runnable runnable;
    private VisualizerView mVisualizerView;
    int level = 0;
    int apState = 0;

    private FrameLayout mContentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arviz);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // this section has to be called before the various Camera and VisualizerView are up!
        // without this section the variables needed are set to their defaults
        {
            wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            List<ScanResult> wifiList = wifiManager.getScanResults();
            level = WifiManager.calculateSignalLevel(wifiList.get(0).level, 4);
            System.out.println("Level is " + level + " out of 4");

            if (wifiList.get(0).BSSID.matches(wifiManager.getConnectionInfo().getBSSID())) {
                apState = 1;
                //System.out.println("AP STATE " + apState);
            }

            else if (wifiList.get(0).capabilities.contains("^.*?(WEP|WPA|WPA2|WPA_EAP|IEEE8021X).*$")) {
                apState = 2;
                //System.out.println("AP STATE " + apState);
            }

            else {
                apState = 3;
                //System.out.println("AP STATE " + apState);
            }

        }

        mContentView = findViewById(R.id.fullscreen_content);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);

        mContentView.addView(mPreview);

        mVisualizerView = new VisualizerView(this);
        mContentView.addView(mVisualizerView);



        /*int rssi = wifiManager.getConnectionInfo().getRssi();
        level = WifiManager.calculateSignalLevel(rssi, 4);
        System.out.println("Level is " + level + " out of 4");*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        //start handler as activity become visible

        h.postDelayed(new Runnable() {
            public void run() {
                //Level of  Connected Network

                /*int rssi = wifiManager.getConnectionInfo().getRssi();
                level = WifiManager.calculateSignalLevel(rssi, 4);
                System.out.println("Level is " + level + " out of 4");*/

                // Level of a Scan Result

                /*List<ScanResult> wifiList = wifiManager.getScanResults();
                for (ScanResult scanResult : wifiList) {
                    int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
                    System.out.println("Level is " + level + " out of 5");
                }*/

                List<ScanResult> wifiList = wifiManager.getScanResults();
                level = WifiManager.calculateSignalLevel(wifiList.get(0).level, 4);
                System.out.println("Level is " + level + " out of 4");

                if (wifiList.get(0).BSSID.matches(wifiManager.getConnectionInfo().getBSSID())) {
                    apState = 1;
                    //System.out.println("AP STATE " + apState);
                }

                else if (wifiList.get(0).capabilities.contains("^.*?(WEP|WPA|WPA2|WPA_EAP|IEEE8021X).*$")) {
                    apState = 2;
                    //System.out.println("AP STATE " + apState);
                }

                else {
                    apState = 3;
                    //System.out.println("AP STATE " + apState);
                }

                runnable = this;

                h.postDelayed(runnable, delay);
            }
        }, delay);
    }

    @Override
    protected void onPause() {
        super.onPause();

        h.removeCallbacks(runnable); //stop handler when activity not visible
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private android.hardware.Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.

        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    /** Visualizer View class */
    public class VisualizerView extends View {
        //private SurfaceHolder mHolder;
        private Paint paintHeader = new Paint();
        private Paint paintBar = new Paint();
        private int i = 0;
        private ArrayList<String> barTimeList = new ArrayList<String>();
        private ArrayList<Float> barXYList = new ArrayList<Float>();
        private String vizType = "LINEAR"; //LINEAR, INTERMITTENT

        private void init() {
            paintHeader.setColor(Color.WHITE);

            switch(apState) {
                case 1 :
                                    paintBar.setColor(Color.GREEN);
                                    break;

                case 2 :
                                    paintBar.setColor(Color.RED);
                                    break;

                case 3 :
                                    paintBar.setColor(Color.BLUE);
                                    break;

                 //default        :
                 //                   paintBar.setColor(Color.YELLOW);
                 //                   break;

            }


            //toggle();
            Timer refreshTimer = new Timer();
            refreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    postInvalidate();
                    i = i + 50;
                }
            }, 0, 1 * 1000L);// every second*/

            /*h.postDelayed(new Runnable() {
                public void run() {
                    postInvalidate();
                    i = i + 50;
                }
            }, delay);*/

        }

        public VisualizerView(Context context) {
            super(context);
            init();
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            System.out.println("AP STATE " + apState);
            final int mCanvasWidth = canvas.getWidth();
            final int mCanvasHeight = canvas.getHeight();
            //paint = new Paint(Color.RED);
            paintHeader.setTextSize(72);
            canvas.drawText("NARVIZ_LAYER", 10, 100, paintHeader);
            canvas.drawText(vizType, mCanvasWidth / 2, 100, paintHeader);
            canvas.drawText(new SimpleDateFormat("hh:mm:ss").format(new Date()),
                    mCanvasWidth - (mCanvasWidth / 4),
                    100,
                    paintHeader);
            //canvas.drawLine(0, 0, 200, 200, paint);
            //
            paintBar.setTextSize(9);

            barTimeList.add(new SimpleDateFormat("mm:ss").format(new Date()));
            barTimeList.add(Float.toString((mCanvasWidth * 0.1f) + i));
            barTimeList.add(Float.toString(mCanvasHeight * ((0.8f / level) - 0.1f)));

            barXYList.add((mCanvasWidth * 0.1f) + i);
            barXYList.add(mCanvasHeight * 0.9f); //LINEAR
            //barXYList.add(mCanvasHeight * (0.8f - 0.2f) / level); //INTERMITTENT
            barXYList.add((mCanvasWidth * 0.1f) + i);
            barXYList.add(mCanvasHeight * (0.8f / level));

            for (int j = 0, k = 0; j < barTimeList.size(); j = j + 3, k = k + 4) {
                //if (i < canvas.getWidth()) {
                canvas.drawText(barTimeList.get(j),
                        Float.parseFloat(barTimeList.get(j + 1)),
                        Float.parseFloat(barTimeList.get(j + 2)),
                        paintBar);
                canvas.drawLine(barXYList.get(k),
                        barXYList.get(k + 1),
                        barXYList.get(k + 2),
                        barXYList.get(k + 3),
                        paintBar);
                //}
            }
        }
    }
}
