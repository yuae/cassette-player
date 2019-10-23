package com.example.groupproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

import fi.iki.elonen.NanoHTTPD;


public class HttpServer extends AppCompatActivity {

    TextView infoIp;
    TextView infoMsg;
    private final int PORT = 8888;
    private CassetteServer cs;

    ServerSocket httpServerSocket;

    private static Intent intent;

    public static Intent getIntent(Context c){
        intent = new Intent(c, HttpServer.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);

        infoIp = (TextView) findViewById(R.id.infoip);
        infoMsg = (TextView) findViewById(R.id.msg);
        cs = new CassetteServer(PORT);

        //display full http address on the android device
        infoIp.setText(getIpAddress() + ":" + PORT + "\n");

    }

    /**
     * terminate server
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cs != null) {
            cs.stop();
        }
    }


    private class CassetteServer extends NanoHTTPD {
        public CassetteServer(int port){
            super(port);
            try{
                start();
            }
            catch (IOException e){
                Log.d("socketERR","connection failed");
            }
        }

        /**
         *
         * @param session
         * @return response
         *
         * response based on request
         */
        @Override
        public Response serve(IHTTPSession session){
            String uri = session.getUri();
            String filename = uri.substring(1);

            if (uri.equals("/"))
                filename = "index.html";

            boolean is_ascii = true;
            String mimetype = "text/html";
            if (filename.contains(".html") || filename.contains(".htm")) {
                mimetype = "text/html";
                is_ascii = true;
            } else if (filename.contains(".js")) {
                mimetype = "text/javascript";
                is_ascii = true;
            } else if (filename.contains(".css")) {
                mimetype = "text/css";
                is_ascii = true;
            } else if (filename.contains(".gif")) {
                mimetype = "text/gif";
                is_ascii = false;
            } else if (filename.contains(".jpeg") || filename.contains(".jpg")) {
                mimetype = "text/jpeg";
                is_ascii = false;
            } else if (filename.contains(".png")) {
                mimetype = "image/png";
                is_ascii = false;
            } else {
                filename = "index.html";
                mimetype = "text/html";
            }

            if (is_ascii) {
                String response = "";
                String line = "";
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open(filename)));

                    while ((line = reader.readLine()) != null) {
                        response += line;
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return newFixedLengthResponse(Response.Status.OK, mimetype, response);
            }
            else {
                InputStream isr;
                try {
                    isr = getApplicationContext().getAssets().open(filename);
                    return newFixedLengthResponse(Response.Status.OK, mimetype, isr, isr.available());
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.OK, mimetype, "");
                }
            }
        }
    }

    /**
     *
     * @return local ip address for link
     */
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
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }


}
