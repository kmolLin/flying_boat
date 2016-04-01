package com.example.user.flyboat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.example.user.flyboat.VerticalSeekBar;
import com.example.user.flyboat.VerticalSeekBar_Reverse;

public class MainActivity extends Activity {
<<<<<<< HEAD


    /** Called when the activity is first created. */
=======
    VerticalSeekBar verticalSeekBar = null;
    TextView vsProgress;

    Rocker rocker;
    TextView status;

>>>>>>> 0d18d6df0b2fa237f9896462493dc6f18c4f7277
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        findviewid();

<<<<<<< HEAD
=======
        seekBarActivity();
>>>>>>> 0d18d6df0b2fa237f9896462493dc6f18c4f7277

        Rocker rocker = (Rocker) findViewById(R.id.rudder);
        Bitmap rocker_bg = BitmapFactory.decodeResource(getResources(), R.drawable.rocker_bg1);
        Bitmap rocker_ctrl = BitmapFactory.decodeResource(getResources(), R.drawable.rocker_ctrl);
        rocker.setRockerBg(rocker_bg);
        rocker.setRockerCtrl(rocker_ctrl);
        rocker.setRudderListener(new Rocker.RudderListener() {

            @Override
            public void onSteeringWheelChanged(int action, int angle) {
                if (action == Rocker.ACTION_RUDDER) {
                    //TODO:事件实现
                    //	Log.e("夹角", "angle"+angle);
                    status.setText("角度:" + angle);
                }
            }
        });
    }

    private void findviewid(){
        status = (TextView) findViewById(R.id.status);
        rocker = (Rocker) findViewById(R.id.rudder);
        verticalSeekBar=(VerticalSeekBar)findViewById(R.id.vertical_Seekbar);
        vsProgress=(TextView)findViewById(R.id.vertical_sb_progresstext);
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
                vsProgress.setText(progress+"");
            }
        });
    }
}