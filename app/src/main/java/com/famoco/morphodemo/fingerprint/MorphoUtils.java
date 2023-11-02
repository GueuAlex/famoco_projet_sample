package com.famoco.morphodemo.fingerprint;

import android.graphics.Bitmap;
import android.util.Log;

import com.famoco.morphodemo.utils.morpho.ProcessInfo;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoImage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class
 *
 * @version DEMO
 * @author Yoann
 */
public class MorphoUtils {

    private static final String TAG = MorphoUtils.class.getSimpleName();

    /**
     * Write logs in a file
     */
    public static void storeFFDLogs(MorphoDevice morphoDevice) {
        String ffdLogs = morphoDevice.getFFDLogs();

        if(ffdLogs != null) {
            String serialNbr = ProcessInfo.getInstance().getMSOSerialNumber();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.FRANCE);
            String currentDate = sdf.format(new Date());
            String saveFile = "sdcard/" + serialNbr + "_" + currentDate + "_Audit.log";

            try(FileWriter fileStream = new FileWriter(saveFile,true);
                BufferedWriter out = new BufferedWriter(fileStream)) {
                out.write(ffdLogs);
                Log.d(TAG, "Writing logs : " + ffdLogs);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * Create a Bitmap from an array of byte
     * @param image to transform
     */
    public static Bitmap createBitmap(byte[] image) {
        MorphoImage morphoImage = MorphoImage.getMorphoImageFromLive(image);
        int imageRowNumber = morphoImage.getMorphoImageHeader().getNbRow();
        int imageColumnNumber = morphoImage.getMorphoImageHeader().getNbColumn();
        final Bitmap imageBmp = Bitmap.createBitmap(imageColumnNumber, imageRowNumber, Bitmap.Config.ALPHA_8);
        imageBmp.copyPixelsFromBuffer(ByteBuffer.wrap(morphoImage.getImage(), 0, morphoImage.getImage().length));
        return imageBmp;
    }

    /**
     * Create a message from a command
     * @param command corresponding to a String
     */
    public static String createMessage(Integer command) {
        String sensorMessage = "";
        switch (command) {
            case 0:
                sensorMessage = "No finger detected";
                break;
            case 1:
                sensorMessage = "Move your finger up";
                break;
            case 2:
                sensorMessage = "Move your finger down";
                break;
            case 3:
                sensorMessage = "Move your finger left";
                break;
            case 4:
                sensorMessage = "Move your finger right";
                break;
            case 5:
                sensorMessage = "Press harder";
                break;
            case 6:
                sensorMessage = "Move latent";
                break;
            case 7:
                sensorMessage = "Remove your finger";
                break;
            case 8:
                sensorMessage = "Fingerprint scanned successfully !";
        }
        return sensorMessage;
    }
}
