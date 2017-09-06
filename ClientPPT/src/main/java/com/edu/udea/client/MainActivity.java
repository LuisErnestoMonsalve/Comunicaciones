package com.edu.udea.client;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    static final int SocketServerPORT = 8080;

    LinearLayout userLinealPanel, pptLinearPanel, resultLinearPanel;
    FrameLayout waitFramePanel;
    RelativeLayout startRelativeLayout;
    EditText userEditText, addressEditText;
    Button buttonConnect, buttonStart;
    ImageButton stoneButton, paperButton, scissorButton;
    TextView scoreTextView, textPort, responsetext, resultTextView;
    int win = 0, score = 0;

    //EditText editTextSay;
    //Button buttonSend;
    // Button buttonDisconnect;

    String msgLog = "";

    ClientThread clientThread = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userLinealPanel = (LinearLayout) findViewById(R.id.userP);
        waitFramePanel = (FrameLayout) findViewById(R.id.waitP);
        startRelativeLayout = (RelativeLayout) findViewById(R.id.start);
        pptLinearPanel = (LinearLayout) findViewById(R.id.pptP);
        resultLinearPanel = (LinearLayout) findViewById(R.id.resultP);
        userEditText = (EditText) findViewById(R.id.user);
        addressEditText = (EditText) findViewById(R.id.ipaddres);
        textPort = (TextView) findViewById(R.id.port);
        textPort.setText("Puerto: " + SocketServerPORT);
        buttonConnect = (Button) findViewById(R.id.connect);
        buttonStart = (Button) findViewById(R.id.startButton);
        stoneButton = (ImageButton) findViewById(R.id.stone);
        paperButton = (ImageButton) findViewById(R.id.paper);
        scissorButton = (ImageButton) findViewById(R.id.scissor);
        responsetext = (TextView) findViewById(R.id.response);
        resultTextView = (TextView) findViewById(R.id.result);
        scoreTextView = (TextView) findViewById(R.id.score);
        userLinealPanel.setVisibility(View.VISIBLE);
        scoreTextView.setText("Score: " + score);
    }





    public void onClick(View view) {
        switch (view.getId()){
            case R.id.connect:
                String textUserName = userEditText.getText().toString();
                if (textUserName.equals("")) {
                    Toast.makeText(MainActivity.this, "Ingrese un Nombre de Usuario",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                String textAddress = addressEditText.getText().toString();
                if (textAddress.equals("")) {
                    Toast.makeText(MainActivity.this, "Ingrese direccion Ip del Servidor",
                            Toast.LENGTH_LONG).show();
                    return;
                }


                //    msgLog = "";
                //  chatMsg.setText(msgLog);

                clientThread = new ClientThread(
                        textUserName, textAddress, SocketServerPORT);
                clientThread.start();
                break;
            case R.id.startButton:
                if(clientThread==null){
                    return;
                }


                clientThread.sendMsg("2");
                startRelativeLayout.setVisibility(View.GONE);
                waitFramePanel.setVisibility(View.VISIBLE);
                break;
            case R.id.stone:
                if(clientThread==null){
                    return;
                }

                clientThread.sendMsg("#1");
                pptLinearPanel.setVisibility(View.GONE);
                waitFramePanel.setVisibility(View.VISIBLE);
                break;
            case R.id.paper:
                if(clientThread==null){
                    return;
                }

                clientThread.sendMsg("#2");
                pptLinearPanel.setVisibility(View.GONE);
                waitFramePanel.setVisibility(View.VISIBLE);
                break;
            case R.id.scissor:
                if(clientThread==null){
                    return;
                }

                clientThread.sendMsg("#3");
                pptLinearPanel.setVisibility(View.GONE);
                waitFramePanel.setVisibility(View.VISIBLE);
                break;
            case  R.id.exit:
                if(clientThread==null){
                    return;
                }
                clientThread.disconnect();
                break;
            case R.id.next:
                if(clientThread==null){
                    return;
                }
                if(win == 0){
                    resultLinearPanel.setVisibility(View.GONE);
                    pptLinearPanel.setVisibility(View.VISIBLE);
                }else{
                    if(win==1){score++;}
                    scoreTextView.setText("Score: " + score);
                    resultLinearPanel.setVisibility(View.GONE);
                    startRelativeLayout.setVisibility(View.VISIBLE);
                    win = 0;
                }

                break;

        }

    }


    private class ClientThread extends Thread {

        String name;
        String dstAddress;
        int dstPort;

        String msgToSend = "";
        boolean goOut = false;

        ClientThread(String name, String address, int port) {
            this.name = name;
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream.writeUTF(name);
                dataOutputStream.flush();

                while (!goOut) {
                    if (dataInputStream.available() > 0) {
                        final String msg = dataInputStream.readUTF();
                        switch (msg.charAt(0)) {
                            case '1':
                                MainActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        userLinealPanel.setVisibility(View.GONE);
                                        startRelativeLayout.setVisibility(View.VISIBLE);
                                    }

                                });
                                break;
                            case '#':
                                MainActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Conectado con:" + msg, Toast.LENGTH_LONG).show();
                                        waitFramePanel.setVisibility(View.GONE);
                                        pptLinearPanel.setVisibility(View.VISIBLE);
                                    }

                                });
                                break;
                            case '!':
                                final int code = Integer.parseInt(""+msg.charAt(1));
                                switch (code){
                                    case 1:
                                        MainActivity.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                resultTextView.setText("Ganaste!");
                                                waitFramePanel.setVisibility(View.GONE);
                                                resultLinearPanel.setVisibility(View.VISIBLE);

                                            }

                                        });
                                        break;

                                    case 2:
                                        MainActivity.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                resultTextView.setText("Perdiste!");
                                                waitFramePanel.setVisibility(View.GONE);
                                                resultLinearPanel.setVisibility(View.VISIBLE);


                                            }



                                        });
                                        break;
                                    case 3:
                                        win = 1;
                                        MainActivity.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                Toast.makeText(MainActivity.this, "Compañero Abandono La Partida",
                                                        Toast.LENGTH_SHORT).show();
                                                resultTextView.setText("Ganaste la Partida!");
                                                pptLinearPanel.setVisibility(View.GONE);
                                                waitFramePanel.setVisibility(View.GONE);
                                                resultLinearPanel.setVisibility(View.VISIBLE);


                                            }

                                        });
                                        break;
                                    case 4:
                                        win = 1;
                                        MainActivity.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                resultTextView.setText("Ganaste la Partida!");
                                                waitFramePanel.setVisibility(View.GONE);
                                                resultLinearPanel.setVisibility(View.VISIBLE);


                                            }

                                        });
                                        break;
                                    case 5:
                                        MainActivity.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                resultTextView.setText("Empate!");
                                                waitFramePanel.setVisibility(View.GONE);
                                                resultLinearPanel.setVisibility(View.VISIBLE);


                                            }

                                        });
                                        break;
                                    case 6:
                                        win = 2;
                                        MainActivity.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                resultTextView.setText("Perdiste la Partida!");
                                                waitFramePanel.setVisibility(View.GONE);
                                                resultLinearPanel.setVisibility(View.VISIBLE);


                                            }

                                        });
                                        break;



                                }
                                break;


                        }


//                        MainActivity.this.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                chatMsg.setText(msgLog);
//                            }
//                        });
                    }

                    if (!msgToSend.equals("")) {
                        dataOutputStream.writeUTF(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = "";
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, eString, Toast.LENGTH_LONG).show();
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, eString, Toast.LENGTH_LONG).show();
                    }

                });
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        userLinealPanel.setVisibility(View.VISIBLE);
                        waitFramePanel.setVisibility(View.GONE);
                        startRelativeLayout.setVisibility(View.GONE);
                        pptLinearPanel.setVisibility(View.GONE);
                    }

                });

            }

        }

        private void sendMsg(String msg) {
            msgToSend = msg;
        }

        private void disconnect() {
            clientThread.sendMsg("!");
            goOut = true;
            pptLinearPanel.setVisibility(View.GONE);
            userLinealPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        if (clientThread!=null){
            clientThread.disconnect();
        }
        super.onDestroy();
    }
}


