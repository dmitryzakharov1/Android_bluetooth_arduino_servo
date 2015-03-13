package com.btservocontroller.dimon.btservocontroller;

import android.support.v7.app.ActionBarActivity;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.bluetooth.*;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



public class MainActivity extends ActionBarActivity {

    private TextView mTextValue;
    private SeekBar mSeekBar1;

    final int ArduinoData = 1;
    private static final int REQUEST_ENABLE_BT = 0;
    final String LOG_TAG = "myLogs";
    public BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket = null;
    // MAC-адрес Bluetooth модуля
    private static String MacAdress = "10:14:05:22:12:52";
    // SPP UUID сервиса
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectedThred MyThred = null;

    public TextView mytext;

    Handler h;

    private int servo1pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mytext = (TextView) findViewById(R.id.txtrobot);


        if (btAdapter != null){
            if (btAdapter.isEnabled()){
                mytext.setText("Bluetooth включен. Все отлично.");
            }else
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

        }else
        {
            MyError("Fatal Error", "Bluetooth ОТСУТСТВУЕТ");
        }

        mTextValue = (TextView)findViewById(R.id.textView);


        mSeekBar1 = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                mTextValue.setText(String.valueOf(seekBar.getProgress()));
                servo1pos = seekBar.getProgress();
                MyThred.sendData(servo1pos + " :");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                servo1pos = seekBar.getProgress();
                MyThred.sendData(servo1pos + " :");
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();

        BluetoothDevice device = btAdapter.getRemoteDevice(MacAdress);
        Log.d(LOG_TAG, "***Получили удаленный Device***"+device.getName());

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d(LOG_TAG, "...Создали сокет...");
        } catch (IOException e) {
            MyError("Fatal Error", "В onResume() Не могу создать сокет: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();
        Log.d(LOG_TAG, "***Отменили поиск других устройств***");

        Log.d(LOG_TAG, "***Соединяемся...***");
        try {
            btSocket.connect();
            Log.d(LOG_TAG, "***Соединение успешно установлено***");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                MyError("Fatal Error", "В onResume() не могу закрыть сокет" + e2.getMessage() + ".");
            }
        }

        MyThred = new ConnectedThred(btSocket);
        MyThred.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "...In onPause()...");

        if (MyThred.status_OutStrem() != null) {
            MyThred.cancel();
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            MyError("Fatal Error", "В onPause() Не могу закрыть сокет" + e2.getMessage() + ".");
        }
    }

    private void MyError(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
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


    //start new class
    //Класс отдельного потока для передачи данных
    private class ConnectedThred extends Thread{
        private final BluetoothSocket copyBtSocket;
        private final OutputStream OutStrem;
        //private final InputStream InStrem;

        public ConnectedThred(BluetoothSocket socket){
            copyBtSocket = socket;
            OutputStream tmpOut = null;
            //InputStream tmpIn = null;
            try{
                tmpOut = socket.getOutputStream();
                //tmpIn = socket.getInputStream();
            } catch (IOException e){}

            OutStrem = tmpOut;
            //InStrem = tmpIn;
        }

        /*
        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try{
                    bytes = InStrem.read(buffer);
                    h.obtainMessage(ArduinoData, bytes, -1, buffer).sendToTarget();
                }catch(IOException e){break;}

            }

        }

        */

        public void sendData(String message) {
            byte[] msgBuffer = message.getBytes();
            Log.d(LOG_TAG, "***Отправляем данные: " + message + "***"  );

            try {
                OutStrem.write(msgBuffer);
            } catch (IOException e) {}
        }

        public void cancel(){
            try {
                copyBtSocket.close();
            }catch(IOException e){}
        }

        public Object status_OutStrem(){
            if (OutStrem == null){return null;
            }else{return OutStrem;}
        }
    }


    //end of program
}


