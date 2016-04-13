package com.example.user.flyboat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.example.user.flyboat.VerticalSeekBar;
import com.example.user.flyboat.VerticalSeekBar_Reverse;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import coolerd.uart.driver.device.UARTOutputDevice;
import tw.com.flag.api.FlagBt;
import tw.com.flag.api.FlagTank;
import tw.com.flag.api.OnFlagMsgListener;

public class MainActivity extends Activity implements SensorEventListener, OnFlagMsgListener {
    VerticalSeekBar verticalSeekBar = null;

    TextView status, txv, h, vsProgress, Regarten;
    Button Changactivity, Controller;
    private ImageView imageView;
    private float currentDegree = 0f;
    SensorManager sm;
    Sensor sr;
    Rocker rocker;

    FlagBt bt,bt2;      //宣告藍牙物件
    FlagTank tank,tank2;  //宣告 iTank 物件
    String btdata, heigh, lenth, angle1, reciveString,reciveString2;

    //找到UI工人的經紀人，這樣才能派遣工作  (找到顯示畫面的UI Thread上的Handler)
    private Handler mUI_Handler = new Handler();
    //private Handler mUI_Handler2 = new Handler();
    //宣告特約工人的經紀人
    private Handler mThreadHandler;
    //private Handler mThreadHandler2;
    //宣告特約工人
    private HandlerThread mThread;
    //private HandlerThread mThread2;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView( R.layout.activity_main);
        findviewid();
        seekBarActivity();

        bt.connect();

        //聘請一個特約工人，有其經紀人派遣其工人做事 (另起一個有Handler的Thread)
        mThread = new HandlerThread("name");
        //讓Worker待命，等待其工作 (開啟Thread)
        mThread.start();
        //找到特約工人的經紀人，這樣才能派遣工作 (找到Thread上的Handler)
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(r1);

        //聘請一個特約工人，有其經紀人派遣其工人做事 (另起一個有Handler的Thread)
        //mThread2 = new HandlerThread("name2");
        //讓Worker待命，等待其工作 (開啟Thread)
       // mThread2.start();
        //找到特約工人的經紀人，這樣才能派遣工作 (找到Thread上的Handler)
        //mThreadHandler2 = new Handler(mThread2.getLooper());
       // mThreadHandler2.post(r2);


        Bitmap rocker_bg = BitmapFactory.decodeResource(getResources(), R.drawable.rocker_bg1);
        Bitmap rocker_ctrl = BitmapFactory.decodeResource(getResources(), R.drawable.rocker_ctrl);
        rocker.setRockerBg(rocker_bg);
        rocker.setRockerCtrl(rocker_ctrl);
        rocker.setRudderListener2(new Rocker.RudderListener2() {
            @Override
            public void onSteeringWheelChanged(int action, int l) {
                if (action == Rocker.ACTION_RUDDER) {
                    //TODO:事件实现
                    Log.e("夹角", "angle" + l);
                    lenth = Integer.toString(l);
                }
            }
        });
        rocker.setRudderListener(new Rocker.RudderListener() {
            @Override
            public void onSteeringWheelChanged(int action, int angle) {
                if (action == Rocker.ACTION_RUDDER) {
                    //TODO:事件实现
                    //	Log.e("夹角", "angle"+angle);
                    status.setText("角度:" + angle);
                    angle1 = Integer.toString(angle);
                    bt.write("A" + ":" + lenth + ":" + angle1 + ";");
                }
            }
        });

        Changactivity.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MainActivity.this, UratActivity.class);
                startActivity(intent);
            }
        });

        Controller.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                bt2.connect();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void findviewid() {
        Changactivity = (Button) findViewById(R.id.button);
        Controller = (Button) findViewById(R.id.controller);
        imageView = (ImageView) findViewById(R.id.imageView);
        rocker = (Rocker) findViewById(R.id.rudder);
        status = (TextView) findViewById(R.id.status);
        Regarten = (TextView) findViewById(R.id.regarten);
        verticalSeekBar = (VerticalSeekBar) findViewById(R.id.vertical_Seekbar);
        vsProgress = (TextView) findViewById(R.id.vertical_sb_progresstext);
        txv = (TextView) findViewById(R.id.btlight);
        h = (TextView) findViewById(R.id.hight);
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(MainActivity.this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
        bt = new FlagBt(this);      // 建立藍牙物件
        bt2 = new  FlagBt(this);
        tank = new FlagTank(bt);    // 建立 iTank 物件
        tank2 = new FlagTank(bt2);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sr = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private Runnable r1 = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            reciveString = bt.getReadString();
            h.setText(reciveString);
           /* while (true) {
                reciveString = bt.getReadString();
                //
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                //if (reciveString = ) //要寫code的地方，與解析方法在這邊寫
            }*/
            mUI_Handler.post(r1);
            //做了很多事
        }
    };

    /*private Runnable r2 = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            reciveString2 = bt2.getReadString();
            Regarten.setText(reciveString2);

            mUI_Handler2.post(r2);
            //做了很多事
        }
    };*/

    public void onResume() {
        super.onResume();
        /*Toast toast = Toast.makeText(MainActivity.this,
                lenth, Toast.LENGTH_LONG);
        //顯示Toast
        toast.show();*/
    }

    public void onPause() {
        super.onPause();
        finish();
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stop(); // 確保程式結束前會停止藍牙連線
        bt2.stop(); // 確保程式結束前會停止藍牙連線
        //移除工人上的工作
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacks(r1);
        }
        //解聘工人 (關閉Thread)
        if (mThread != null) {
            mThread.quit();
        }
        /*if (mThreadHandler2 != null) {
            mThreadHandler2.removeCallbacks(r2);
        }
        //解聘工人 (關閉Thread)
        if (mThread2 != null) {
            mThread2.quit();
        }*/
        finish();
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float degree = event.values[0];
			/*
            RotateAnimation类：旋转变化动画类
			参数说明:
			fromDegrees：旋转的开始角度。
			toDegrees：旋转的结束角度。
			pivotXType：X轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
			pivotXValue：X坐标的伸缩值。
			pivotYType：Y轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
			pivotYValue：Y坐标的伸缩值
			*/
            RotateAnimation ra = new RotateAnimation(currentDegree, -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            //旋转过程持续时间
            ra.setDuration(200);
            //罗盘图片使用旋转动画
            imageView.startAnimation(ra);

            currentDegree = -degree;
        }
    }

    //传感器精度的改变
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void onFlagMsg(Message msg) {
        switch (msg.what) {
            case FlagBt.CONNECTING: // 嘗試與已配對裝置連線
                txv.setText("正在連線到：" + bt.getDeviceName());
                break;
            case FlagBt.CONNECTED:  // 與已配對裝置連線成功
                txv.setText("已連線到：" + bt.getDeviceName());
                break;
            case FlagBt.CONNECT_FAIL: // 連線失敗
                txv.setText("連線失敗！請重連");
                break;
            case FlagBt.CONNECT_LOST: // 目前連線意外中斷
                txv.setText("連線中斷!請重連");
                break;
        }
    }

    private void seekBarActivity() {
        verticalSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                vsProgress.setText(progress + "");
                bt.write("B" + ":" + progress + ";");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.user.flyboat/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.user.flyboat/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}