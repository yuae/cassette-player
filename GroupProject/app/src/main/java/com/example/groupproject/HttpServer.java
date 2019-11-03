package com.example.groupproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static java.nio.charset.StandardCharsets.UTF_8;


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
        //@Override
        public Response serve(IHTTPSession session) {
            Map<String, String> headers = session.getHeaders();
            Map<String, String> parms = session.getParms();
            String uri = session.getUri();
            Map<String, String> files = new HashMap<>();

            Log.d("init", "CassetteServer:" + uri);
            String filename = uri.substring(1);
            String msg = "<html><body><h1>Error</h1>\n</body></html>\\n";

            File[] arrayfile;


            try {
                session.parseBody(new HashMap<String, String>());
            } catch (ResponseException | IOException r) {
                r.printStackTrace();
            }


            if (uri.equals("/")) {
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
                } else {
                    InputStream isr;
                    try {
                        isr = getApplicationContext().getAssets().open(filename);
                        return newFixedLengthResponse(Response.Status.OK, mimetype, isr, isr.available());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return newFixedLengthResponse(Response.Status.OK, mimetype, "");
                    }
                }
            } else if ("/uploadfile".equalsIgnoreCase(uri)) {
                filename = parms.get("filename");
                File root = getApplicationContext().getExternalFilesDir(null);
                String tmpFilePath = files.get("filename");
                if (null == filename || null == tmpFilePath) {
                    // Response for invalid parameters
                }
                File dst = new File(root.getAbsolutePath() + "/music/", filename);
                if (dst.exists()) {
                    // Response for confirm to overwrite
                }
                File src = new File(tmpFilePath);
                try {
                    InputStream in = new FileInputStream(src);
                    OutputStream out = new FileOutputStream(dst);
                    byte[] buf = new byte[65536];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (IOException ioe) {
                    // Response for failed
                }
                // Response for success
            } else if (uri.equals("/downloads")) {
                File root = getApplicationContext().getExternalFilesDir(null);

                FileInputStream fis = null;
                File file = new File(root.getAbsolutePath() + "/music/");
                Log.d("serve: ", file.toString());
                arrayfile = file.listFiles();
                String html = "<html><body><h1>List Of All Cassettes</h1>";
                for (int i = 0; i < arrayfile.length; i++) {
                    html += "<a href='/music/" + arrayfile[i].getName()+"'>"+arrayfile[i].getName()+"</a>";

                }
                html += "</body></html>";

                return newFixedLengthResponse(html);
            } else if (uri.contains(".")) {

                String[] split = uri.split("/");
                String s = "";
                for (int i = 0; i < split.length; i++) {
                    Log.d("String", "" + split[i] + "" + i);
                    s = s + "/" + split[i];
                }
                String x = s.substring(1, s.length());
                Log.d("String2", s);
                Log.d("String2", x + "  " + x.length());
                String y = NanoHTTPD.getMimeTypeForFile(x);
                Log.d("MIME-TYPE", y);
                File root = getApplicationContext().getExternalFilesDir(null);
                FileInputStream fis = null;
                File file = new File(root.getAbsolutePath() + x);
                Log.d("read",file.toString());
                try {
                    if (file.exists()) {
                        fis = new FileInputStream(file);

                    } else
                        Log.d("FOF :", "File Not exists:");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return newFixedLengthResponse(Response.Status.OK, "audio/mpeg", fis, file.length());

            }
            return newFixedLengthResponse(msg);
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
