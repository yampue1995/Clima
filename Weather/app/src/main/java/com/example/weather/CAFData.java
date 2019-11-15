//
//  CAFData.java
//
//  Created by Joan Ayala on 10/07/17.
//  Version 1.0 Alpha 1
//  Copyright © 2015 Joan C. Ayala. All rights reserved.
//

package com.example.estudiantes.demoweather;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

public class CAFData {
    private static final int bufferSize = 100 * 1024; // ~130K.
    private ByteBuffer byteData = null;


    //Constructors

    public CAFData(byte[] bytes, int length) {

        byteData = ByteBuffer.wrap(bytes, 0, length);
    }


    public CAFData(String path) {
        File inFile = new File(path);
        InputStream inStream = null;
        //byteData = null;
        try {
            inStream = new FileInputStream(inFile);
            byteData = getBytesFromInputStream(inStream);
            inStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (IOException e) {

        }

    }

    public CAFData(URL url, String args) {
        InputStream inStream = null;
        int response = -1;
        URLConnection conn = null;

        try {
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpConn.setReadTimeout(30000);
                httpConn.setConnectTimeout(15000);
                httpConn.setAllowUserInteraction(false);
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                os.write(args.getBytes());
                os.flush();

                response = httpConn.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    inStream = httpConn.getInputStream();
                    byteData = getBytesFromInputStream(inStream);
                    inStream.close();
                }
                httpConn.disconnect();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public CAFData(URL url) {
        InputStream inStream = null;
        int response = -1;
        URLConnection conn = null;

        try {
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setReadTimeout(10000);
                httpConn.setConnectTimeout(15000);
                httpConn.setAllowUserInteraction(false);
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setRequestMethod("GET");
                httpConn.setDoInput(true);
                httpConn.connect();
                response = httpConn.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    //try {
                    inStream = httpConn.getInputStream();
                    byteData = getBytesFromInputStream(inStream);
                    inStream.close();
                    //} catch (){


                    //}
                }
                httpConn.disconnect();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // Private methods

    protected static ByteBuffer getBytesFromInputStream(InputStream inStream) {
        byte[] buffer = new byte[bufferSize];
        ByteArrayOutputStream outStream = null;
        ByteBuffer byteData = null;

        try {
            outStream = new ByteArrayOutputStream(buffer.length);
            int bytesRead = 0;
            while (bytesRead != -1) {
                bytesRead = inStream.read(buffer);
                if (bytesRead > 0)
                    outStream.write(buffer, 0, bytesRead);
            }
            byteData = ByteBuffer.wrap(outStream.toByteArray());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            byteData = null;
        }

        return byteData;
    }


    // Public methods

    public static CAFData dataWithContentsOfURL(URL url, String args) {

        CAFData data = new CAFData(url, args);
        if (data.length() == 0)
            data = null;

        return data;
    }

    public static CAFData dataWithContentsOfURL(URL url) {

        CAFData data = new CAFData(url);
        if (data != null && data.length() == 0)
            data = null;

        return data;
    }

    public static CAFData dataWithContentsOfFile(String fullFilename) {

        CAFData data = new CAFData(fullFilename);
        if (data != null && data.length() == 0)
            data = null;

        return data;
    }

    public Bitmap toImage() {
        Bitmap img = null;
        byte[] bytes = byteData.array();
        try {
            img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            img = null;
        }
        return img;
    }

    // Write content in local file
    public boolean writeToFile(String path, boolean atomically) {
        boolean wasWritten = false;
        File inFile = null;
        FileOutputStream fis = null;

        try {
            if (atomically) {
                inFile = File.createTempFile("tmp", ".tmp");
                fis = new FileOutputStream(inFile);
            } else
                inFile = new File(path);

            fis.write(byteData.array());
            Log.d("CAFData", inFile.getName() + " (" + byteData.array().length + " bytes) saved.");
            boolean b = inFile.setLastModified(new Date().getTime());
            fis.close();

            if (atomically) {
                if (inFile.renameTo(new File(path)))
                    Log.d("CAFData", "Renamed to " + path);

            }

            //Log.d("CAFData","Bytes writed = "+byteData.array().length);
            wasWritten = true;
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        return wasWritten;
    }

    public String toText() {
        return new String(byteData.array(), Charset.forName("UTF-8"));
    }


    public int length() {
        int length = 0;

        if (byteData != null)
            length = byteData.array().length;

        return length;
    }

    public boolean isEqualToData(CAFData other) {
        return byteData.equals(other.byteData);
    }
}