package com.famoco.morphodemo.fingerprint.image;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.famoco.fingerprintimageheaderlib.WSQUtils;
import com.famoco.morphodemo.R;
import com.famoco.morphodemo.fingerprint.MorphoUtils;
import com.famoco.morphodemo.utils.morpho.FingerPrintInfo;
import com.famoco.morphodemo.utils.morpho.FingerPrintMode;
import com.famoco.morphodemo.utils.morpho.MorphoInfo;
import com.famoco.morphodemo.utils.morpho.ProcessInfo;
import com.morpho.morphosmart.sdk.CallbackMask;
import com.morpho.morphosmart.sdk.CallbackMessage;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoImage;
import com.morpho.morphosmart.sdk.MorphoWakeUpMode;
import com.morpho.morphosmart.sdk.Template;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImagePresenter implements ImageContract.Presenter, Observer {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = ImagePresenter.class.getSimpleName();

    /**
     * View of the MVP pattern
     */
    private ImageContract.View view;

    /**
     * Morpho device
     */
    private MorphoDevice morphoDevice;

    /**
     * Handler to update UI Thread
     */
    private Handler mHandler;

    /**
     * Constructor of the Presenter
     *
     * @param view of the MVP pattern
     */
    ImagePresenter(ImageContract.View view) {
        this.view = checkNotNull(view);
        mHandler = new Handler();
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
    }

    @Override
    public void processImageCapture() {
        morphoDeviceGetImage(this);
    }

    @Override
    public void stopProcess() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void morphoDeviceGetImage(final Observer observer) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                ProcessInfo processInfo = ProcessInfo.getInstance();

                MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();
                int timeOut = processInfo.getTimeout();
                int acquisitionThreshold = 0;
                final CompressionAlgorithm compressAlgo = CompressionAlgorithm.MORPHO_COMPRESS_WSQ;
                int compressRate = 10;
                int detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
                LatentDetection latentDetection = LatentDetection.LATENT_DETECT_ENABLE;
                final MorphoImage[] morphoImage = new MorphoImage[] {new MorphoImage()};
                int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

                callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();

                if(ProcessInfo.getInstance().isFingerprintQualityThreshold()) {
                    acquisitionThreshold = ProcessInfo.getInstance().getFingerprintQualityThresholdvalue();
                }

                final int ret = morphoDevice.getImage(timeOut, acquisitionThreshold,
                        compressAlgo,
                        compressRate,
                        detectModeChoice,
                        latentDetection,
                        morphoImage[0],
                        callbackCmd,
                        observer);

                ProcessInfo.getInstance().setCommandBioStart(false);

                MorphoUtils.storeFFDLogs(morphoDevice);

                if (ret == ErrorCodes.MORPHO_OK) {
//                    exportNoCompressedImage(morphoImage[0]);
                    exportWSQCompressedImage(morphoImage[0]);
//                    exportWSQCompressedImageWithHeader(morphoImage[0]);
                    exportWSQCompressedImageWithNewHeader(morphoImage[0]);
//                    saveImage(morphoImage);
                }

                final int internalError = morphoDevice.getInternalError();
                final int retValue = ret;

                mHandler.post(new Runnable() {
                    @Override
                    public synchronized void run() {
                        if (retValue != ErrorCodes.MORPHOERR_CMDE_ABORTED) {
                            view.alert(retValue, internalError);
                            view.onImageCaptureCompleted();
                        }
                    }
                });
            }
        }).start();
    }

    private void exportNoCompressedImage(final MorphoImage morphoImage) {
//        morphoImage.setCompressionAlgorithm(CompressionAlgorithm.MORPHO_NO_COMPRESS);
        try (FileOutputStream fos = new FileOutputStream("sdcard/TemplateFP_no_WSQ" + CompressionAlgorithm.MORPHO_NO_COMPRESS.getExtension())) {
            byte[] data = morphoImage.getImage();
            Log.d(TAG, "Writing data in file with WSQ format : " + Arrays.toString(data));
            fos.write(data);
        } catch (IOException e) {
            Log.e(TAG, "An error has occurred while manipulating files " + e.getMessage());
        }
    }

    private void exportWSQCompressedImageWithHeader(final MorphoImage morphoImage) {
        try (FileOutputStream fos = new FileOutputStream("sdcard/TemplateFP_WSQ_header" + CompressionAlgorithm.MORPHO_COMPRESS_WSQ.getExtension())) {
            byte[] data = morphoImage.getCompressedImage();
            byte[] result = WSQUtils.setHeader(data);
            Log.d(TAG, "Writing data in file with WSQ format : " + Arrays.toString(data));
            fos.write(result);
        } catch (IOException e) {
            Log.e(TAG, "An error has occurred while manipulating files " + e.getMessage());
        }
    }

    private void exportWSQCompressedImageWithNewHeader(final MorphoImage morphoImage) {
        try (FileOutputStream fos = new FileOutputStream("sdcard/TemplateFP_WSQ_newHeader" + CompressionAlgorithm.MORPHO_COMPRESS_WSQ.getExtension())) {
            byte[] data = morphoImage.getCompressedImage();
            byte[] result = WSQUtils.setNewHeader(data);
            Log.d(TAG, "Writing data in file with WSQ format : " + Arrays.toString(data));
            fos.write(result);
        } catch (IOException e) {
            Log.e(TAG, "An error has occurred while manipulating files " + e.getMessage());
        }
    }

    private void exportWSQCompressedImage(final MorphoImage morphoImage) {
        try (FileOutputStream fos = new FileOutputStream("sdcard/TemplateFP_WSQ" + CompressionAlgorithm.MORPHO_COMPRESS_WSQ.getExtension())) {
            byte[] data = morphoImage.getCompressedImage();
            Log.d(TAG, "Writing data in file with WSQ format : " + Arrays.toString(data));
            fos.write(data);
        } catch (IOException e) {
            Log.e(TAG, "An error has occurred while manipulating files " + e.getMessage());
        }
    }

    @Override
    public void update(Observable o, Object arg) {
// Convert the object to a callback message.
        CallbackMessage message = (CallbackMessage) arg;
        int type = message.getMessageType();

        switch (type) {
            case 1: // Message is a command.
                handleCommand((Integer) message.getMessage());
                break;
            case 2: // Message is a low resolution image
                handleImage((byte[]) message.getMessage());
                break;
            case 3: // Message is the coded image quality.
                handleQuality((Integer) message.getMessage());
                break;
            default:
                Log.e(TAG, "Unknown message received from Morpho device : " + arg);
                break;
        }
    }

    /**
     * Update UI Thread with quality return by Morpho device
     *
     * @param quality used to set new progress
     */
    private void handleQuality(final Integer quality) {
        mHandler.post(new Runnable() {
            @Override
            public synchronized void run() {
                view.updateSensorProgressBar(quality);
            }
        });
    }

    /**
     * Update UI Thread with image return by Morpho device
     *
     * @param image to display
     */
    private void handleImage(final byte[] image) {
        mHandler.post(new Runnable() {
            @Override
            public synchronized void run() {
                view.updateImage(MorphoUtils.createBitmap(image));
            }
        });
    }

    /**
     * Update UI Thread with message return by Morpho device
     *
     * @param command corresponding to a String to display
     */
    private void handleCommand(final Integer command) {
        mHandler.post(new Runnable() {
            @Override
            public synchronized void run() {
                view.updateSensorMessage(MorphoUtils.createMessage(command));
            }
        });
    }
}
