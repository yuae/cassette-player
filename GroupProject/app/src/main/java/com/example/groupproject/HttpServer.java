package com.example.groupproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.iki.elonen.NanoHTTPD;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.*;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


public class HttpServer extends AppCompatActivity {

    private static final int WIDTH = 200;
    TextView infoIp;
    ImageView qrView;
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
        qrView = (ImageView) findViewById(R.id.qr);
        cs = new CassetteServer(PORT);
        //display full http address on the android device
        String msg = "Please type this address to enter the Black Market:\n "
                + getIpAddress() + ":" + PORT + "\n";
        infoIp.setText(msg);
        //create and display the
        try{
            Bitmap bm = encodeAsBitMap(getIpAddress() + ":" + PORT );
            qrView.setImageBitmap(bm);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }

    }

    /**
     *  get the ip address of the device running the server
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
                        ip += inetAddress.getHostAddress();
                    }

                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }



    private Bitmap encodeAsBitMap(String str) throws WriterException {
        BitMatrix result;
        try{
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        }
        catch (IllegalArgumentException e){
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w*h];
        for (int y = 0; y<h; y++){
            int offset = y*w;
            for(int x = 0; x<w; x++){
                pixels[offset + x] = result.get(x, y) ? BLACK:WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);
        return bitmap;
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
        public Response serve(IHTTPSession session) {
            Map<String, String> headers = session.getHeaders();
            Map<String, String> parms = session.getParms();
            String uri = session.getUri();
            Map<String, String> files = new HashMap<>();

            Log.d("init", "CassetteServer:" + uri);
            String filename = uri.substring(1);
            String msg = "<html><body><h1>Error</h1>\n</body></html>\\n";

            File[] arrayfile;



            if(Method.POST.equals(session.getMethod()) || Method.PUT.equals(session.getMethod())){
                //parse body to hash map
                try {
                    session.parseBody(files);
                    Log.d("post","post file");
                    Log.d("param",files.toString());
                    String tmpFilePath = files.get("cassette");
                    Log.d("tmpPath",tmpFilePath);
                    File root = getApplicationContext().getExternalFilesDir(null);
                    if (tmpFilePath == null) {
                        // Response for invalid parameters
                        return newFixedLengthResponse("Not valid parameters");
                    }
                    String ext = parms.get("extension");
                    String name = ext;
                    Log.d("name",name);
                    File dst = new File(root.getAbsolutePath() + "/music/", name);
                    if (dst.exists()) {
                        return newFixedLengthResponse("We don't receive cassette we already have.");
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
                        return newFixedLengthResponse("Transaction failed");
                    }
                    return newFixedLengthResponse("Your cassette have been accepted and proceed. Thank you.");
                } catch (ResponseException | IOException e) {
                    e.printStackTrace();
                }
                return newFixedLengthResponse("Your cassette have been accepted and proceed. Thank you.");

            }

            //return index, css, script file when accessing root
            if (uri.equals("/")) {
                filename = "index.html";
                boolean is_ascii = true;
                String mimetype = "text/html";
                //set MIME type
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
                //check if text file
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
                //return file in response with MIME type
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
            //put received file from cache to the music folder
            else if (uri.equals("/upload")) {
                String tmpFilePath = files.get("myfile");
                Log.d("tmpPath",tmpFilePath);
                filename = parms.get("filename");
                Log.d("fpath",filename);
                File root = getApplicationContext().getExternalFilesDir(null);
                if (null == filename || null == tmpFilePath) {
                    // Response for invalid parameters
                    return newFixedLengthResponse("Not valid parameters");
                }
                File dst = new File(root.getAbsolutePath() + "/music/", filename);
                if (dst.exists()) {
                    return newFixedLengthResponse("We don't receive cassette we already have.");
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
                    return newFixedLengthResponse("Transaction failed");
                }
                return newFixedLengthResponse("Your cassette have been accepted and proceed. Thank you.");
            }
            //read external memory folder and print the list of files with the uri
            else if (uri.equals("/downloads")) {
                File root = getApplicationContext().getExternalFilesDir(null);

                FileInputStream fis = null;
                File file = new File(root.getAbsolutePath() + "/music/");
                Log.d("serve: ", file.toString());
                arrayfile = file.listFiles();
                String html = "<html><body><h1>List Of All Cassettes</h1>";
                for (int i = 0; i < arrayfile.length; i++) {
                    html += "<li><a href='/music/" + arrayfile[i].getName()+"'>"+arrayfile[i].getName()+"</a></li>";
                }
                html += "</body></html>";

                return newFixedLengthResponse(html);
            }
            //return requested file
            else if (uri.contains(".")) {

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
        private boolean copyFile(File source, File target) {
            if (source.isDirectory()) {
                if (!target.exists()) {
                    if (!target.mkdir()) {
                        return false;
                    }
                }
                String[] children = source.list();
                for (int i = 0; i < source.listFiles().length; i++) {
                    if (!copyFile(new File(source, children[i]), new File(target, children[i]))) {
                        return false;
                    }
                }
            } else {
                try {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);

                    byte[] buf = new byte[65536];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (IOException ioe) {
                    return false;
                }
            }
            return true;
        }
    }



}
