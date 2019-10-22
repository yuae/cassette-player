package com.example.groupproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

import fi.iki.elonen.NanoHTTPD;

/*
    Code reference: http://android-er.blogspot.com/2015/01/simple-web-server-using.html
 */

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

        infoIp.setText(getIpAddress() + ":" + PORT + "\n");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cs != null) {
            cs.closeAllConnections();
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

        @Override
        public Response serve(IHTTPSession session){
            return newFixedLengthResponse(constructHTMl());
        }

        private String constructHTMl(){
            String html = "<html><head><meta charset=\\\"UTF-8\\\">" +
                    "<title>Android Server</title>" +
                    "<script type=\\\"text/javascript\\\">"+
                    "function sendUpdate(){" +
                    "var xmlhttp;" +
                    "if (window.XMLHttpRequest)xmlhttp=new XMLHttpRequest();" +
                    "else xmlhttp=new ActiveXObject(\\\"Microsoft.XMLHTTP\\\");" +
                    "xmlhttp.open(\\\"GET\\\",\\\"?getFile\\\",true); " +
                    "xmlhttp.send();" +
                    "xmlhttp.onreadystatechange=function(){" +
                    "if (xmlhttp.readyState==4 && xmlhttp.status==200)" +
                    "document.getElementById(\\\"info\\\").innerHTML=xmlhttp.responseText;};" +
                    "}</script>"+"</head>" +
                    "<body>" +
                    "<h1>Welcome to the market.</h1>" +
                    "</div><button id=\"update\" type=\"button\" onclick=\"sendUpdate()\">get</button>"+
                    "</body></html>";
            return html;
        }

    }

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
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }


}
