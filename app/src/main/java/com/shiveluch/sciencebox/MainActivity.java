package com.shiveluch.sciencebox;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.util.Log;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;


import java.util.zip.GZIPOutputStream;

/**
 * Simple UI demonstrating how to connect to a Bluetooth device,
 * send and receive messages using Handlers, and update the UI.
 */
public class MainActivity extends Activity {

    // Tag for logging
    private static final String TAG = "BluetoothActivity";
    boolean isOpen=false, searched=false;
    LinearLayout infoside, passwordSide;
    EditText passcode;
    ImageView artefact, scan;
    ConstraintLayout web;
    TextView desc;
    Button deadbut, approve;
    TextView searchInfo;


    // MAC address of remote Bluetooth device
    // Replace this with the address of your own module
    private final String address = "37:00:00:00:00:00";

    // The thread that does all the work
    BluetoothThread btt;

    // Handler for writing messages to the Bluetooth connection
    Handler writeHandler;

    /**
     * Launch the Bluetooth thread.
     */
    public void connectButtonPressed(View v) {
        Log.v(TAG, "Connect button pressed.");

        // Only one thread at a time
        if (btt != null) {
            Log.w(TAG, "Already connected!");
            return;
        }

        // Initialize the Bluetooth thread, passing in a MAC address
        // and a Handler that will receive incoming messages
        btt = new BluetoothThread(address, new Handler() {

            @Override
            public void handleMessage(Message message) {

                String s = (String) message.obj;

                Log.d("TAKEN",s);

                if (s.contains("$") && !isOpen && searched)
                {
                    s=s.replace("$","");
                    if (s.contains("*"))
                    {
                        passcode.setText("");
                    }else
                    if (!s.contains("#"))
                    {
                        passcode.setText(passcode.getText().toString()+s);
                    }
                }

                if (s.contains(":"))
                {
                    TextView sysInfo=findViewById(R.id.systemInfo);

                    s=s.replace(":","");
                    Log.d("TAKEN",s);
                    if (s.contains("OPEN"))
                    {
                        sysInfo.setText("БОКС ОТКРЫТ!");
                        sysInfo.setTextColor(getResources().getColor(R.color.red));
                        isOpen=true;
                        passcode.setText("");
                        Log.d("TAKEN","isOpen");
                    }
                    if (s.contains("CLOSE"))
                    {
                        sysInfo.setText("БОКС ЗАКРЫТ");
                        sysInfo.setTextColor(getResources().getColor(R.color.system));
                        isOpen=false;
                        Log.d("TAKEN","isClose");
                    }

                    if (s.contains("START"))
                    {
                        searchInfo.setText("ИССЛЕДОВАНИЕ...");
                        searchInfo.setTextColor(getResources().getColor(R.color.red));
                        passcode.setText("");
                        audio.playbegin();
                        hideView(scan);
                        searched=false;

                    }
                    if (s.contains("FINISH"))
                    {
                        searchInfo.setText("ИССЛЕДОВАНИЕ ЗАВЕРШЕНО.\nВВЕДИТЕ КОД");
                        searchInfo.setTextColor(getResources().getColor(R.color.system));
                        searched=true;
                        audio.playend();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        audio.playcode();

                    }




                }

                // Do something with the message
                if (s.equals("CONNECTED")) {
                    TextView tv = (TextView) findViewById(R.id.statusText);
                    tv.setText("Исследовательский бокс подключен.");
                    Button b = (Button) findViewById(R.id.writeButton);
                    b.setEnabled(true);

                } else if (s.equals("DISCONNECTED")) {
                    TextView tv = (TextView) findViewById(R.id.statusText);
                    Button b = (Button) findViewById(R.id.writeButton);
                    b.setEnabled(false);
                    tv.setText("Исследовательский бокс отключен.");
                } else if (s.equals("CONNECTION FAILED")) {
                    TextView tv = (TextView) findViewById(R.id.statusText);
                    tv.setText("Неудачное подключение");
                    btt = null;
                } else {
                    TextView tv = (TextView) findViewById(R.id.readField);
                    tv.setText(s);
                }
            }
        });

        // Get the handler that is used to send messages
        writeHandler = btt.getWriteHandler();

        // Run the thread
        btt.start();

        TextView tv = (TextView) findViewById(R.id.statusText);
        tv.setText("Connecting...");
    }

    /**
     * Kill the Bluetooth thread.
     */
    public void disconnectButtonPressed(View v) {
        Log.v(TAG, "Disconnect button pressed.");

        if(btt != null) {
            btt.interrupt();
            btt = null;
        }
    }

    /**
     * Send a message using the Bluetooth thread's write handler.
     */
    public void writeButtonPressed(View v) {
        Log.v(TAG, "Write button pressed.");

        TextView tv = (TextView)findViewById(R.id.writeField);
        String data = tv.getText().toString();

        Message msg = Message.obtain();
        msg.obj = data;
        writeHandler.sendMessage(msg);
    }

    public void scanButtonPressed(View v) {
        Log.v(TAG, "Scan button pressed.");

        String data = "scan";

        Message msg = Message.obtain();
        msg.obj = data;
        writeHandler.sendMessage(msg);
        artefact.setVisibility(View.GONE);
        desc.setVisibility(View.GONE);
        deadbut.setVisibility(View.INVISIBLE);
        searchInfo.setVisibility(View.VISIBLE);
        passcode.setVisibility(View.VISIBLE);
        approve.setVisibility(View.VISIBLE);

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String string=passcode.getText().toString();
                String result="";
                int command=0;
                for (int i=0;i<string.length();i++)
                {
                    if (string.charAt(i)=='1' ||string.charAt(i)=='2' ||string.charAt(i)=='3'
                            ||string.charAt(i)=='4' ||string.charAt(i)=='5' ||string.charAt(i)=='6'
                            ||string.charAt(i)=='7' ||string.charAt(i)=='8' ||string.charAt(i)=='9'
                            ||string.charAt(i)=='0')
                    {
                        result+=string.charAt(i);

                    }
                    if (result.length()>0)
                    {
                        command=Integer.parseInt(result);
                    }
                }

                Log.d("String ", string+", "+string.length());
//                int command=0;
//
//
//                if (string.contains("1235")) command=1235;
//                Log.d("Command", ""+command);
                if (command==1235)
                {
                    artefact.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pustyshka));
                    desc.setText(getResources().getString(R.string.pust));
                    Log.d("Command", ""+command);
                }
                else
                {
                    desc.setText("Исследование не удалось");
                }
                artefact.setVisibility(View.VISIBLE);
                desc.setVisibility(View.VISIBLE);
                deadbut.setVisibility(View.GONE);
                searchInfo.setVisibility(View.GONE);
                passcode.setVisibility(View.GONE);
                approve.setVisibility(View.GONE);
            }
        });


    }

Context context;
    Audio audio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

infoside=findViewById(R.id.infoside);
passcode=findViewById(R.id.passcode);
context=getApplicationContext();
        searchInfo = findViewById(R.id.searchInfo);


        audio = new Audio(context);


        Button b = (Button) findViewById(R.id.writeButton);
        b.setEnabled(false);

        desc = findViewById(R.id.description);
        artefact = findViewById(R.id.artefact);
        deadbut=findViewById(R.id.deadbut);
        approve=findViewById(R.id.approve);
        web=findViewById(R.id.web);
        scan = findViewById(R.id.scan);

        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideView(scan);
            }
        });
      //  desc.setText(getResources().getText(R.string.pust));
    }

    /**
     * Kill the thread when we leave the activity.
     */
    protected void onPause() {
        super.onPause();

        if(btt != null) {
            btt.interrupt();
            btt = null;
        }
    }

    private void hideView(final View view){
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.down);
        view.setVisibility(View.VISIBLE);
        //animation.setDuration(1000);
        //animation.getDuration()
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
                web.setVisibility(View.VISIBLE);
                Log.d("vis", ""+web.getVisibility());
            }
        });

        view.startAnimation(animation);
    }
}