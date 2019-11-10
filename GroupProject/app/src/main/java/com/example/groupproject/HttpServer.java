package com.example.groupproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


public class HttpServer extends AppCompatActivity {

    private static final int WIDTH = 200;
    TextView infoIp;
    ImageView qrView;
    private final int PORT = 8888;
    private CassetteServer cs;

    private static Intent intent;

    public static Intent getIntent(Context c) {
        intent = new Intent(c, HttpServer.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);
        getSupportActionBar().setTitle("Welcome to the Black Market");

        infoIp = (TextView) findViewById(R.id.infoip);
        qrView = (ImageView) findViewById(R.id.qr);
        cs = new CassetteServer(PORT);
        //display full http address on the android device
        String msg = "Or type this address to enter\n "
                + getIpAddress() + ":" + PORT + "\n";
        infoIp.setText(msg);
        //create and display the
        try {
            Bitmap bm = encodeAsBitMap(getIpAddress() + ":" + PORT);
            qrView.setImageBitmap(bm);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    /**
     * get the ip address of the device running the server
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


    //private method for QR code generating
    private Bitmap encodeAsBitMap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException e) {
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
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

    /**
     * private class for implementing NanoHTTPD
     */
    private class CassetteServer extends NanoHTTPD {
        public CassetteServer(int port) {
            super(port);
            try {
                start();
            } catch (IOException e) {
                Log.d("socketERR", "connection failed");
            }
        }

        /**
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


            //if a file is uploaded, move it from the cache to the external memory
            if (Method.POST.equals(session.getMethod()) || Method.PUT.equals(session.getMethod())) {
                //parse body to hash map
                try {
                    session.parseBody(files);
                    Log.d("post", "post file");
                    Log.d("param", files.toString());
                    String tmpFilePath = files.get("cassette");
                    Log.d("tmpPath", tmpFilePath);
                    File root = getApplicationContext().getExternalFilesDir(null);
                    if (tmpFilePath == null) {
                        // Response for invalid parameters
                        return newFixedLengthResponse("Not valid parameters");
                    }
                    String name = parms.get("filename");
                    Log.d("name", name);
                    File dst = new File(root.getAbsolutePath(), name);
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
                //return newFixedLengthResponse("Your cassette have been accepted and proceed. Thank you.");

            }

            //set to read index.html
            if (uri.equals("/")) {
                filename = "index.html";
            }
            //return upload page
            else if (uri.equals("/upload")) {
                String html = "<html><body>" +
                        "<form id='upload-cassette' enctype='multipart/form-data' method='post' >" +
                        "<input id='file-upload' name='cassette' type='file' onchange='setFilename(this.value);'/>" +
                        "<input id='file-name' type='hidden' name='filename' value='' />" +
                        "<input type='submit' value='submit' id='submit' /></form>" +
                        "</body></html>" +
                        "<script>function setFilename(val){" +
                        "var fileName = val.substr(val.lastIndexOf(\"\\\\\")+1, val.length);" +
                        "document.getElementById(\"file-name\").value = fileName;}</script>";
                return newFixedLengthResponse(html);
            }
            //read external memory folder and print the list of files with the uri
            else if (uri.equals("/downloads")) {
                File root = getApplicationContext().getExternalFilesDir(null);

                File file = new File(root.getAbsolutePath());
                Log.d("serve: ", file.toString());
                arrayfile = file.listFiles();
                String html = "<html><body><h1>List Of All Cassettes</h1>";
                for (int i = 0; i < arrayfile.length; i++) {
                    html += "<li><a href='/" + arrayfile[i].getName() + "'>" + arrayfile[i].getName() + "</a></li>";
                }
                html += "</body></html>";

                return newFixedLengthResponse(html);
            }
            //return mp3 file
            else if (uri.contains(".mp3")) {

                String[] split = uri.split("/");
                String s = "";
                for (int i = 0; i < split.length; i++) {
                    s = s + "/" + split[i];
                }
                String fileName = s.substring(1, s.length());
                Log.d("FileName", fileName);
                String mimetype = NanoHTTPD.getMimeTypeForFile(fileName);
                Log.d("MIME-TYPE", mimetype);
                File root = getApplicationContext().getExternalFilesDir(null);
                FileInputStream fis = null;
                File file = new File(root.getAbsolutePath() + fileName);
                Log.d("read", file.toString());
                try {
                    if (file.exists()) {
                        fis = new FileInputStream(file);

                    } else
                        Log.d("FOF :", "File Not exists:");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return newFixedLengthResponse(Response.Status.OK, mimetype, fis, file.length());

            }

            //set MIME type for index.html or files used by it
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
    }
}