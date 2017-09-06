package com.edu.udea.serverppt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int SocketServerPORT = 8080;

    TextView ipTextView, portTextView, msgTexView;
    String msgLog = "";
    List<ChatClient> userList;
    List<ChatClient> waitList;
    ServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipTextView = (TextView) findViewById(R.id.ip);
        portTextView = (TextView) findViewById(R.id.port);
        msgTexView = (TextView) findViewById(R.id.msg);
        userList = new ArrayList<ChatClient>();
        waitList = new ArrayList<ChatClient>();

       // spUsers = (Spinner) findViewById(R.id.spusers);

//        spUsersAdapter = new ArrayAdapter<ChatClient>(
//                MainActivity.this, android.R.layout.simple_spinner_item, userList);
//        spUsersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spUsers.setAdapter(spUsersAdapter);


        ipTextView.setText(getIpAddress());

        ServerThread serverThread = new ServerThread();
        serverThread.start();
    }


//    View.OnClickListener btnSentToOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            ChatClient client = (ChatClient) spUsers.getSelectedItem();
//            if (client != null) {
//                String dummyMsg = "Dummy message from server.\n";
//                client.chatThread.sendMsg(dummyMsg);
//                msgLog += "- Dummy message to " + client.name + "\n";
//                chatMsg.setText(msgLog);
//
//            } else {
//                Toast.makeText(MainActivity.this, "No user connected", Toast.LENGTH_LONG).show();
//            }
//        }
//    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class ServerThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        portTextView.setText("Puerto: "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    socket = serverSocket.accept();
                    ChatClient client = new ChatClient();
                    userList.add(client);
                    ConnectThread connectThread = new ConnectThread(client, socket);
                    connectThread.start();
                                                                                                                                                                                                                      }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    private class ConnectThread extends Thread {

        Socket socket;
        ChatClient connectClient;
        String msgToSend = "";
        Boolean go = true;

        ConnectThread(ChatClient client, Socket socket) {
            connectClient = client;
            this.socket = socket;
            client.socket = socket;
            client.chatThread = this;
        }

        @Override
        public void run() {
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                String n = dataInputStream.readUTF();

                connectClient.name = n;

                msgLog += "Nuevo usuario conectado: "+connectClient.name + "" +
                        connectClient.socket.getInetAddress() +
                        ":" + connectClient.socket.getPort() + "\n";
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        msgTexView.setText(msgLog);
                    }
                });

                dataOutputStream.writeUTF("1");
                dataOutputStream.flush();


                while (go) {
                    if (dataInputStream.available() > 0) {
                        String msg = dataInputStream.readUTF();
                        switch (msg.charAt(0)) {
                            case '2':
                                msgLog += n + ": Solicitud de nueva partida"+ "\n";
                                MainActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        msgTexView.setText(msgLog);
                                    }
                                });
                                if(waitList.isEmpty()){
                                    waitList.add(connectClient);
                                    msgLog += n + ": Esperando Compañero"+ "\n";
                                    MainActivity.this.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            msgTexView.setText(msgLog);
                                        }
                                    });
                                }else{
                                    ChatClient client2 = waitList.get(0);
                                    ClientPair clientPair = new ClientPair(connectClient,client2);
                                    connectClient.pair = clientPair;
                                    client2.pair = clientPair;
                                    connectClient.num = 1;
                                    client2.num = 2;
                                    waitList.remove(client2);
                                    msgLog += n + " y "+client2.name+" ahora estan conectados"+ "\n";
                                    MainActivity.this.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            msgTexView.setText(msgLog);
                                        }
                                    });
                                    sendMsg("#" + client2.name);
                                    client2.chatThread.sendMsg("#" + n);
                                }
                                break;
                            case '#':
                                if (connectClient.num==1){
                                    connectClient.pair.res1 = Integer.parseInt(""+msg.charAt(1));

                                }else if (connectClient.num==2){
                                    connectClient.pair.res2 = Integer.parseInt(""+msg.charAt(1));
                                }else{
                                    MainActivity.this.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Somethis was Wrong!", Toast.LENGTH_LONG).show();
                                        }

                                    });
                                    break;
                                }
                                String m;
                                switch (msg.charAt(1)){
                                    case '1':
                                        m = "piedra";
                                        break;

                                    case '2':
                                        m = "papel";
                                        break;

                                    case '3':
                                        m = "tijeras";
                                        break;
                                    default:
                                        m = "Error";
                                        break;
                                }

                                msgLog += n + " escogio " + m + "\n";
                                MainActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        msgTexView.setText(msgLog);
                                    }
                                });


                                if(connectClient.pair.response()){

                                    int code1 = connectClient.pair.res1;
                                    int code2 = connectClient.pair.res2;

                                    switch (code1){
                                        case 1:
                                            if(code2==3){
                                                connectClient.pair.client1.score++;
                                                if(connectClient.pair.client1.score == 2){
                                                    connectClient.pair.client1.chatThread.sendMsg("!4");
                                                    connectClient.pair.client2.chatThread.sendMsg("!6");
                                                    msgLog += "Ganador de la partida: " + connectClient.pair.client1.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                    connectClient.pair.client1.score = 0;
                                                    connectClient.pair.client2.score = 0;

                                                }else{
                                                    connectClient.pair.client1.chatThread.sendMsg("!1");
                                                    connectClient.pair.client2.chatThread.sendMsg("!2");
                                                    msgLog += "Ganador: " + connectClient.pair.client1.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                }


                                            }else if(code2 == 1){
                                                connectClient.pair.BroadcastMsg("!5");
                                                msgLog += connectClient.pair.client1.name +" y " +connectClient.pair.client2.name+" empataron \n";
                                                MainActivity.this.runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        msgTexView.setText(msgLog);
                                                    }
                                                });

                                            }else{
                                                connectClient.pair.client2.score++;
                                                if(connectClient.pair.client2.score == 2){
                                                    connectClient.pair.client2.chatThread.sendMsg("!4");
                                                    connectClient.pair.client1.chatThread.sendMsg("!6");
                                                    msgLog += "Ganador de la partida: " + connectClient.pair.client2.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                    connectClient.pair.client1.score = 0;
                                                    connectClient.pair.client2.score = 0;

                                                }else{
                                                    connectClient.pair.client2.chatThread.sendMsg("!1");
                                                    connectClient.pair.client1.chatThread.sendMsg("!2");
                                                    msgLog += "Ganador: " + connectClient.pair.client2.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                }
                                            }
                                            break;
                                        case 2:
                                            if(code2==1){
                                                connectClient.pair.client1.score++;
                                                if(connectClient.pair.client1.score == 2){
                                                    connectClient.pair.client1.chatThread.sendMsg("!4");
                                                    connectClient.pair.client2.chatThread.sendMsg("!6");
                                                    msgLog += "Ganador de la partida: " + connectClient.pair.client1.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                    connectClient.pair.client1.score = 0;
                                                    connectClient.pair.client2.score = 0;

                                                }else{
                                                    connectClient.pair.client1.chatThread.sendMsg("!1");
                                                    connectClient.pair.client2.chatThread.sendMsg("!2");
                                                    msgLog += "Ganador: " + connectClient.pair.client1.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                }


                                            }else if(code2 == 2){
                                                connectClient.pair.BroadcastMsg("!5");
                                                msgLog += connectClient.pair.client1.name +" y " +connectClient.pair.client2.name+" empataron \n";
                                                MainActivity.this.runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        msgTexView.setText(msgLog);
                                                    }
                                                });

                                            }else{
                                                connectClient.pair.client2.score++;
                                                if(connectClient.pair.client2.score == 2){
                                                    connectClient.pair.client2.chatThread.sendMsg("!4");
                                                    connectClient.pair.client1.chatThread.sendMsg("!6");
                                                    msgLog += "Ganador de la partida: " + connectClient.pair.client2.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                    connectClient.pair.client1.score = 0;
                                                    connectClient.pair.client2.score = 0;

                                                }else{
                                                    connectClient.pair.client2.chatThread.sendMsg("!1");
                                                    connectClient.pair.client1.chatThread.sendMsg("!2");
                                                    msgLog += "Ganador: " + connectClient.pair.client2.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                }
                                            }
                                            break;
                                        case 3:
                                            if(code2==2){
                                                connectClient.pair.client1.score++;
                                                if(connectClient.pair.client1.score == 2){
                                                    connectClient.pair.client1.chatThread.sendMsg("!4");
                                                    connectClient.pair.client2.chatThread.sendMsg("!6");
                                                    msgLog += "Ganador de la partida: " + connectClient.pair.client1.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                    connectClient.pair.client1.score = 0;
                                                    connectClient.pair.client2.score = 0;

                                                }else{
                                                    connectClient.pair.client1.chatThread.sendMsg("!1");
                                                    connectClient.pair.client2.chatThread.sendMsg("!2");
                                                    msgLog += "Ganador: " + connectClient.pair.client1.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                }


                                            }else if(code2 == 3){
                                                connectClient.pair.BroadcastMsg("!5");
                                                msgLog += connectClient.pair.client1.name +" y " +connectClient.pair.client2.name+" empataron \n";
                                                MainActivity.this.runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        msgTexView.setText(msgLog);
                                                    }
                                                });

                                            }else{
                                                connectClient.pair.client2.score++;
                                                if(connectClient.pair.client2.score == 2){
                                                    connectClient.pair.client2.chatThread.sendMsg("!4");
                                                    connectClient.pair.client1.chatThread.sendMsg("!6");
                                                    msgLog += "Ganador de la partida: " + connectClient.pair.client2.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                    connectClient.pair.client1.score = 0;
                                                    connectClient.pair.client2.score = 0;

                                                }else{
                                                    connectClient.pair.client2.chatThread.sendMsg("!1");
                                                    connectClient.pair.client1.chatThread.sendMsg("!2");
                                                    msgLog += "Ganador: " + connectClient.pair.client2.name + " \n";
                                                    MainActivity.this.runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            msgTexView.setText(msgLog);
                                                        }
                                                    });
                                                }
                                            }
                                            break;
                                        default:
                                            msgLog += "Error \n";
                                            MainActivity.this.runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    msgTexView.setText(msgLog);
                                                }
                                            });
                                            break;

                                    }
                                    connectClient.pair.res1 = 0;
                                    connectClient.pair.res2 = 0;
                                }
                                break;
                            case '!':
                                disconnect();
                                break;

                        }


//                        msgLog += n + ": " + msg;
//                        MainActivity.this.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                msgTexView.setText(msgLog);
//                            }
//                        });

                    }

                    if (!msgToSend.equals("")) {
                        dataOutputStream.writeUTF(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = "";
                    }

                }



            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
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

                userList.remove(connectClient);

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        msgLog += "-- " + connectClient.name + " abandono la partida\n";
                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                msgTexView.setText(msgLog);
                            }
                        });

                    }
                });
            }

        }

        private void sendMsg(String msg) {
            msgToSend = msg;
        }

        private void disconnect(){
            ChatClient client2;
            if(connectClient.num == 0){
                waitList.remove(connectClient);
                go = false;
                return;
            }
            if(connectClient.num == 1){
                client2 = connectClient.pair.client2;
            }else{

                client2 = connectClient.pair.client1;
            }
            client2.chatThread.sendMsg("!3");
            client2.pair = null;
            connectClient.pair = null;
            client2.num = 0;
            connectClient.num = 0;
            go = false;
        }

    }

//    private void broadcastMsg(String msg) {
//        for (int i = 0; i < userList.size(); i++) {
//            userList.get(i).chatThread.sendMsg(msg);
//            msgLog += "- send to " + userList.get(i).name + "\n";
//        }
//
//        MainActivity.this.runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                msgTexView.setText(msgLog);
//            }
//        });
//    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Dirección Ip del Servidor: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    class ChatClient {
        String name;
        Socket socket;
        ConnectThread chatThread;
        ClientPair pair;
        int score = 0, num = 0;

        @Override
        public String toString() {
            return name + ": " + socket.getInetAddress().getHostAddress();
        }
    }


    class ClientPair {
        ChatClient client1, client2;
        public int res1,res2, score;

        public ClientPair(ChatClient client1, ChatClient client2) {
            this.client1 = client1;
            this.client2 = client2;
            res1 = 0;
            res2 = 0;
            score = 0;
        }

        public void BroadcastMsg (String msg){
            client1.chatThread.sendMsg(msg);
            client2.chatThread.sendMsg(msg);
        }
        public boolean response(){
            return (res1!=0 && res2!=0);
        }

    }
}
