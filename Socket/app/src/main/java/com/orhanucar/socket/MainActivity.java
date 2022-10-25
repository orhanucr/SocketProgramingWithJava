package com.orhanucar.socket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.orhanucar.socket.databinding.ActivityMainBinding;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Socket socket;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        sharedPreferences = this.getSharedPreferences("ip", Context.MODE_PRIVATE);
        String saveIp = sharedPreferences.getString("serverIp", null);
        String savePort = sharedPreferences.getString("serverPort", null);
        binding.ipText.setText(saveIp);
        binding.indikatorPortText.setText(savePort);

        values = binding.valuessText.getText().toString();

        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, getLocalIpAddress(), Snackbar.LENGTH_INDEFINITE)
                .setAction("CLOSE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        System.out.println(inetAddress.getHostAddress());
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            System.out.println("Connection Failed!");
        }
        return null;
    }

    public void connected(View view) throws SocketException, UnknownHostException, ClassNotFoundException {

        String SERVER_IP = binding.ipText.getText().toString();
        String SERVERPORT = binding.indikatorPortText.getText().toString();

        if(SERVER_IP.equals("") || SERVERPORT.equals("")) {
            Toast.makeText(this, "Enter ip and port!", Toast.LENGTH_LONG).show();
        } else {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                SocketAddress SockAddr = new InetSocketAddress(SERVER_IP, Integer.parseInt(SERVERPORT));

                int SERVER = Integer.parseInt(SERVERPORT);
                try {

                    socket = new Socket();

                    if (!socket.isClosed()){
                        socket.connect(SockAddr, 2500 );
                        // socket = new Socket(serverAddr, SERVER);
                    }

                }
                catch (SocketException e){
                    e.printStackTrace();
                }
                catch (SocketTimeoutException e){
                    if (socket.isConnected()) {
                        System.out.println("Connection Established");
                    }
                    else{
                        System.out.println("Connection Failed!");
                    }
                }
                if (socket.isConnected()) {
                    System.out.println(serverAddr);
                    editor = sharedPreferences.edit();
                    editor.putString("serverIp", SERVER_IP);
                    editor.putString("serverPort", SERVERPORT);
                    editor.apply();
                    Toast.makeText(this, "Connection Established!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "Connection Failed!", Toast.LENGTH_LONG).show();
                }
            }catch (SocketException ex){
                ex.printStackTrace();
            }catch (UnknownHostException e1) {
                e1.printStackTrace();
                Toast.makeText(this, "Connection Failed!", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Connection Failed!", Toast.LENGTH_LONG).show();
            }

        }
    }

    public class Client {
        private DatagramSocket datagramSocket;
        private InetAddress inetAddress;
        private byte[] buffer;


        public Client(DatagramSocket datagramSocket, InetAddress inetAddress) {
            this.datagramSocket = datagramSocket;
            this.inetAddress = inetAddress;

        }

        public void sendThenReceive() {

            if (values.equals("")) {
                while (true) {
                    try {

                        buffer = values.getBytes();
                        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, 8081);
                        datagramSocket.send(datagramPacket);
                        datagramSocket.receive(datagramPacket);
                        String messageFromServer = new String(datagramPacket.getData(), 8, datagramPacket.getLength());

                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }

                }
            }
        }
    }

    public void send(View view) {
        values = binding.valuessText.getText().toString();
        if (!values.equals("")) {
            StringWriter sw = new StringWriter();

            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                out.print(values);
                out.flush();
                Toast.makeText(this,"Forwarded!",Toast.LENGTH_SHORT).show();
            } catch (UnknownHostException e) {
                e.printStackTrace(new PrintWriter(sw));
            } catch (IOException e) {
                e.printStackTrace(new PrintWriter(sw));
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
            }
        } else {
            Toast.makeText(this,"Failed To Send!!!",Toast.LENGTH_LONG).show();
        }
    }
}