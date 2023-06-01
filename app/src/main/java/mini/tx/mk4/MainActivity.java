package mini.tx.mk4;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import mini.tx.mk4.R;
import mini.tx.mk4.UDPReceiveServer;

public class MainActivity extends AppCompatActivity {

    //output to UDP and input from UDP respectively
    public static int TOUCHPAD_RANGE = 100;
    private mini.tx.mk4.UDPSendServer sendServer;

    private Thread sendThread;
    private UDPReceiveServer receiveServer;
    private Thread receiveThread;
    TextView telemetryText;

    // declare the variables used for storing touch inputs and button commands.
    private int leftStickX = 0;
    private int leftStickY = 0;
    private int rightStickX = 0;
    private int rightStickY = 0;
    private int b1 = 0;
    private int b2 = 0;
    private int b3 = 0;
    private int b4 = 0;
    private int b5 = 0;
    private int b6 = 0;

    // Create an array called "messageToSend" to store the touch inputs/button commands in for sending over UDP.
    private int[] rawInputs = new int[10];
    private byte[] messsageToSend = new byte[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d("CommandServer","oncreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        String controller_ip = spref.getString("controller_ip", "192.168.141.103");
        int sendPort = spref.getInt("controller_send_port", 2390);
        int receivePort = spref.getInt("controller_receive_port", 6000);
        int updateRate = spref.getInt("controller_update_rate", 60);

        sendServer = new mini.tx.mk4.UDPSendServer(messsageToSend);
        sendServer.setDestination(controller_ip, sendPort);
        sendServer.setUpdateRate(updateRate);

        receiveServer = new UDPReceiveServer(new UDPReceiveServer.RecievedMessageListener() {
            @Override
            public void onReceiveMessage(String message) {
                updateTelemetryText(message);
            }
        });
        receiveServer.setReceivePort(receivePort);

        sendThread = new Thread(sendServer);
        receiveThread = new Thread(receiveServer);
        sendThread.start();
        receiveThread.start();

        //telemetryText = (TextView) findViewById(R.id.telemetryText);
        FrameLayout ctrl_leftTouchPad = (FrameLayout) findViewById(R.id.ctrl_leftStick);
        FrameLayout ctrl_rightTouchPad = (FrameLayout) findViewById(R.id.ctrl_rightStick);
        ctrl_leftTouchPad.setOnTouchListener(joystickHandler);
        ctrl_rightTouchPad.setOnTouchListener(joystickHandler);
        findViewById(R.id.ctrl_b1).setOnTouchListener(tapButtonsHandler);
        findViewById(R.id.ctrl_b2).setOnTouchListener(tapButtonsHandler);
        findViewById(R.id.ctrl_b3).setOnTouchListener(tapButtonsHandler);
    }


    private View.OnTouchListener joystickHandler = new View.OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {

            int normalizedXPos = 0;
            int normalizedYPos = 0;

            if (event.getAction() != event.ACTION_UP) {
                float relativeXPos = event.getX();
                float relativeYPos = event.getY();

                //Centered and normalized to +- 100
                normalizedXPos = (int) ((event.getX() - view.getWidth() / 2)
                        * TOUCHPAD_RANGE * 2 / view.getWidth());
                normalizedYPos = (int) ((-(event.getY() - view.getHeight() / 2))
                        * TOUCHPAD_RANGE * 2 / view.getHeight());

                normalizedXPos = limitRange(normalizedXPos, -TOUCHPAD_RANGE, TOUCHPAD_RANGE);
                normalizedYPos = limitRange(normalizedYPos, -TOUCHPAD_RANGE, TOUCHPAD_RANGE);
            } else {
                normalizedXPos = 0;
                normalizedYPos = 0 ;
            }

            switch (view.getId()) {
                case R.id.ctrl_leftStick:
                    leftStickX = normalizedXPos;
                    updateArray(1, leftStickX);
                    leftStickY = normalizedYPos;
                    updateArray(2, leftStickY);
                    break;
                case R.id.ctrl_rightStick:
                    rightStickX = normalizedXPos;
                    updateArray(3, rightStickX);
                    rightStickY = normalizedYPos;
                    updateArray(4, rightStickY);
                    break;
            }
            updateJoystickMarkers();
            return true;
        }


    };

    private View.OnTouchListener tapButtonsHandler = new View.OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            int btnVal;
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                btnVal = 1;
            } else {
                btnVal = 0;
            }


            switch (view.getId()) {
                case R.id.ctrl_b1:
                    b1 = btnVal;
                    updateArray(5, b1);
                    break;
                case R.id.ctrl_b2:
                    b2 = btnVal;
                    updateArray(6, b2);
                    break;
                case R.id.ctrl_b3:
                    b3 = btnVal;
                    updateArray(7, b3);
                    break;
            }
            //Log.d("Touch",Integer.toString(ctrl_state_outgoing.get("b1")));
            return false;
        }
    };

    private View.OnTouchListener tglButtonsHandler = new View.OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            //Opposite, because the event is triggered before the button changes state
            int btnVal = !((ToggleButton) view).isChecked() ? 1 : 0;

            switch (view.getId()) {
                case R.id.ctrl_b1:
                    b4 = btnVal;
                    updateArray(8, b4);
                    break;
                case R.id.ctrl_b2:
                    b5 = btnVal;
                    updateArray(9, b5);
                    break;
                case R.id.ctrl_b3:
                    b6 = btnVal;
                    break;
            }
            return false;
        }
    };

    public void updateJoystickMarkers() {
        View lPad = findViewById(R.id.ctrl_leftStick);
        View lMarker = findViewById(R.id.leftStickTop);
        lMarker.setX(lPad.getWidth() * leftStickX / (TOUCHPAD_RANGE * 2) + lPad.getWidth() / 2 - lMarker.getWidth() / 2);
        lMarker.setY(lPad.getHeight() * -leftStickY/ (TOUCHPAD_RANGE * 2) + lPad.getHeight() / 2 - lMarker.getHeight() / 2);

        View rPad = findViewById(R.id.ctrl_rightStick);
        View rMarker = findViewById(R.id.rightStickTop);
        rMarker.setX(rightStickX * rPad.getWidth()/ (TOUCHPAD_RANGE * 2) + rPad.getWidth() / 2 - rMarker.getWidth() / 2);
        rMarker.setY(-rightStickY * rPad.getHeight() / (TOUCHPAD_RANGE * 2) + rPad.getHeight() / 2 - rMarker.getHeight() / 2);
    }

    public void updateTelemetryText(final String s) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                //Log.d("CommandServer","View height: " + ((View)telemetryText.getParent()).getHeight());
                //Log.d("CommandServer","Text Height:" + Integer.toString(telemetryText.getHeight()));
                telemetryText.setText(s);

                if (telemetryText.getHeight() == ((View) telemetryText.getParent()).getHeight()) {
                    //Log.d("CommandServer",telemetryText.getTextSize() + " " + Integer.toString(telemetryText.getHeight()));
                    telemetryText.setTextSize(TypedValue.COMPLEX_UNIT_PX, telemetryText.getTextSize() - 1);
                }
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d("CommandServer","Activity destroyed, killing servers...");
        sendThread.interrupt();
        receiveThread.interrupt();
        while (sendThread.isAlive() || receiveThread.isAlive()) {

        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Exit controller?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public int limitRange(int n, int min, int max) {
        if (n > max) return max;
        else if (n < min) return min;
        else return n;
    }


    public byte[] updateArray(int i, int data) {

        //Log.d("ControllerActivity", " Current modified byte = " + String.valueOf(messageToSend[i]));

        rawInputs[i] = (int) data;
        //Log.d("ControllerActivity", " Current modified byte = " + String.valueOf(messageToSend[i]));

        int LeftX = rawInputs[1];
        int LeftY = rawInputs[2];
        int RightX = rawInputs[3];
        int RightY = rawInputs[4];

        int leftMotor;
        int rightMotor;
        int leftServo;
        int rightServo;
        leftMotor = LeftY + LeftX / 2;
        rightMotor = LeftY - LeftX / 2;
        leftServo = RightY + RightX / 2;
        rightServo = RightY - RightX / 2;

        messsageToSend[1] = (byte) leftMotor;
        messsageToSend[2] = (byte) rightMotor;
        messsageToSend[3] = (byte) leftServo;
        messsageToSend[4] = (byte) rightServo;
        //Log.d("ControllerActivity", " Current modified bytes = " + leftMotor);

        return messsageToSend;
    }


    public void wifiSettings(View view) {
        Intent intent = new Intent(this, WifiManagerActivity.class);
        startActivity(intent);
    }


}

