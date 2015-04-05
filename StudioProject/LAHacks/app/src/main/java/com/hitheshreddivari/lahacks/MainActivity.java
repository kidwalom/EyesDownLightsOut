package com.hitheshreddivari.lahacks;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.android.volley.Response;
import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {
    private PostActivity pa;
    private Context mContext = this;
    private TextView tv;
    private TGDevice tgDevice;
    private double[] rawData=new double[512];
    private int rawDataIndex=0;
    private FFT fft=new FFT(512);
    private double[] window=fft.getWindow();
    private double[] re=new double[512];
    private double[] im=new double[512];
    private int eye_state = 0;
    private File file=null;
    private BufferedWriter writer=null;
    private boolean pebble_flag=false;
    private String fileName="default.csv";
    private Response.Listener rListener = new Response.Listener<String>(){
        @Override
        public void onResponse(String response){
            tv.setText(response);
        }
    };
    private boolean raw_close = false;
    private Timer mTimer;
    private PebbleMessenger mPebbleMessenger;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case TGDevice.MSG_STATE_CHANGE:
                    switch(msg.arg1){
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            break;
                        case TGDevice.STATE_CONNECTED:
                            tgDevice.start();
                            tv.setText("connected");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            tv.setText("disconnected");
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            break;
                    }
                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    break;
                case TGDevice.MSG_RAW_DATA:
                    if (rawDataIndex==512){
                        rawDataIndex=0;
                        re = rawData;

                        for (int i=0;i<512;i++){
                            im[i]=0;
                            re[i]=re[i]*window[i];
                        }
                        fft.fft(re, im);
                        Log.d("fft", "done");
                    }
                    double data=msg.arg1;
                    if (data>200){
                        raw_close=true;
                    }
                    rawData[rawDataIndex]=data;
                    try {
                        writer.write(Double.toString(data)+"\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    rawDataIndex++;
                    break;
                case TGDevice.MSG_ATTENTION:
                    Log.d("attention",""+msg.arg1);
                    if (msg.arg1<30 && msg.arg1!=0){
                        if(!pebble_flag){
                            mPebbleMessenger.sendAlert();
                            pebble_flag=true;
                            mTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    pebble_flag=false;
                                }
                            },10000);
                        }

                    }
                    break;
                case TGDevice.MSG_BLINK:
                    int blink = msg.arg1;
                    Log.d("blink",""+blink);
                    if(eye_state==0 && blink>50){
                        eye_state=1;
                        pa.postData("on",rListener);
                    }else if(eye_state==1 && blink>50){
                        eye_state=0;
                        pa.postData("off",rListener);
                    }
                    break;
                case TGDevice.MSG_EEG_POWER:
                    double pow=0;
                    for (int j=18;j<31;j++){
                        pow=pow+(re[j]*re[j]+im[j]*im[j])/512;
                    }
                    TGEegPower ep = (TGEegPower) msg.obj;
                    double beta_pow= ep.highAlpha;
                    Log.d("beta_ratio",""+pow);
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        pa = new PostActivity(mContext);
        tv = (TextView)findViewById(R.id.respTxt);

        BluetoothAdapter btAdapter= BluetoothAdapter.getDefaultAdapter();
        if(btAdapter!=null){
            tgDevice=new TGDevice(btAdapter,handler);
        }

        boolean writable=isExternalStorageWritable();
        File path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        path.mkdirs();
        file=new File(path,fileName);
        try{
            writer = new BufferedWriter(new FileWriter(file,true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mTimer=new Timer();
        mPebbleMessenger=new PebbleMessenger(mContext);
    }

    public void postData(View view){
        boolean state = ((ToggleButton)view).isChecked();
        String value;
        if (state){
            value = "on";
        } else {
            value="off";
        }
        Log.d("value",""+value);
        pa.postData(value,rListener);
    }

    public void connectClick(View view){
        if(tgDevice!=null){
            tgDevice.connect(true);
        }
    }
    public void disconnectClick(View view){
        tgDevice.close();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (writer!=null){
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("destroy","destroyed");
        tgDevice.close();
    }
}
