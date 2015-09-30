package com.alperez.imageloader.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import com.alperez.imageloader.helpers.Size;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by stanislav.perchenko on 29-Sep-15.
 */
public class Utils {

    public static String getSizedImageLink(String link, @Nullable Size size) {
        return (size != null) ? (link + size.getSuffixForLink()) : link;
    }

    /**
     * Creates new options with downscale factor set.
     * @param inOpts Options with decoded bounds
     * @param targX requested width
     * @param targY requested height
     * @return new instance of Options with inJustDecodeBounds = false and properly set up inSampleSize (downscale factor)
     */
    public static BitmapFactory.Options getScaledOptions(BitmapFactory.Options inOpts, int targX, int targY) {
        float[] factors = new float[]{1f, 2f, 4f, 8f, 16f, 32f, 64f, 128f, 256f, 1024f, 2048f, 4096f};
        int index = 0;
        BitmapFactory.Options outOpts = new BitmapFactory.Options();
        outOpts.inJustDecodeBounds = false;
        outOpts.inSampleSize = (int)factors[index];
        try {
            while (true) {
                final float nextFactor = factors[index+1];
                if ((Math.round((float)inOpts.outWidth / nextFactor) < targX) || (Math.round((float)inOpts.outHeight / nextFactor) < targY)) {
                    break;
                }
                outOpts.inSampleSize = (int)nextFactor;
                index++;
            }
        } catch(IndexOutOfBoundsException e) {}
        return outOpts;
    }

    public static boolean isNetworkAvailable(Context context, boolean includeConnectingStage) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (info != null) && (includeConnectingStage ? info.isConnectedOrConnecting() : info.isConnected());
    }

    public static byte[] loadDataFromNet(String link) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            URL preparedURL = new URL(link);
            HttpURLConnection connection = (link.toLowerCase().startsWith("https://")) ? ((HttpsURLConnection) preparedURL.openConnection()) : ((HttpURLConnection) preparedURL.openConnection());
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(3500);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");

            if ((connection.getResponseCode() < 200) || (connection.getResponseCode() >= 300)) {
                connection.disconnect();
                return null;
            }

            readDataStream(connection, buffer);
            return buffer.toByteArray();

        } catch(MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            try { buffer.close(); } catch (IOException e) {}
            System.gc();
        }
    }

    private static void readDataStream(HttpURLConnection connection, OutputStream outStream) throws  IOException {
            InputStream is = connection.getInputStream();
            try {
                byte[] bbb = new byte[512];
                int nRead;
                while ((nRead = is.read(bbb, 0, bbb.length)) > 0) {
                    outStream.write(bbb, 0, nRead);
                }
                outStream.flush();

            } finally {
                is.close();
            }
    }

}
