package com.famoco.morphodemo.fingerprint.enroll;

import android.os.Handler;
import android.util.Log;

import com.famoco.morphodemo.fingerprint.MorphoUtils;
import com.famoco.morphodemo.utils.morpho.ProcessInfo;
import com.morpho.morphosmart.sdk.CallbackMessage;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.EnrollmentType;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoWakeUpMode;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateFVP;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.morpho.morphosmart.sdk.CompressionAlgorithm.MORPHO_COMPRESS_WSQ;
import static com.morpho.morphosmart.sdk.CompressionAlgorithm.MORPHO_NO_COMPRESS;
import static com.morpho.morphosmart.sdk.TemplateFVPType.MORPHO_NO_PK_FVP;

/**
 * Presenter of EnrollFragment
 *
 * @author Yoann
 * @version DEMO
 */
class EnrollPresenter implements EnrollContract.Presenter, Observer {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = EnrollPresenter.class.getSimpleName();

    /**
     * Morpho Device Capture Configuration
     */
    private final String ID_USER = "test";
    private final TemplateType TEMPLATE_TYPE = TemplateType.MORPHO_PK_ISO_FMC_CS;
    private final TemplateFVPType TEMPLATE_FVP_TYPE = MORPHO_NO_PK_FVP;
    private final EnrollmentType ENROLL_TYPE = EnrollmentType.ONE_ACQUISITIONS;
    private final int MAX_SIZE_TEMPLATE = 255;
    private final LatentDetection LATENT_DETECTION = LatentDetection.LATENT_DETECT_ENABLE;
    private final int NB_FINGER = 1;

    /**
     * View of the MVP pattern
     */
    private EnrollContract.View view;

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
    EnrollPresenter(EnrollContract.View view) {
        this.view = checkNotNull(view);
        mHandler = new Handler();
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
    }

    @Override
    public void processFingerCapture() {
        morphoDeviceCapture(this);
    }

    @Override
    public void stopProcess() {
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Start the capture of a fingerprint with Morpho device
     *
     * @param observer that will be notified in real time of the device acquisition
     */
    private void morphoDeviceCapture(final Observer observer) {
        //Background thread to capture a new fingerprint
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProcessInfo processInfo = ProcessInfo.getInstance();
                final int timeout = processInfo.getTimeout();
                TemplateList templateList = new TemplateList();

                int acquisitionThreshold = (processInfo.isFingerprintQualityThreshold()) ?
                        processInfo.getFingerprintQualityThresholdvalue() : 0;
                int advancedSecurityLevelsRequired = (processInfo.isAdvancedSecLevCompReq()) ?
                        1 : 0xFF;

                int callbackCmd = processInfo.getCallbackCmd();
                Coder coderChoice = processInfo.getCoder();

                int detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
                if (processInfo.isForceFingerPlacementOnTop())
                    detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                if (processInfo.isWakeUpWithLedOff())
                    detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();

                int ret = morphoDevice.setStrategyAcquisitionMode(processInfo.getStrategyAcquisitionMode());
                if (ret == ErrorCodes.MORPHO_OK) {
                    ret = morphoDevice.capture(timeout, acquisitionThreshold, advancedSecurityLevelsRequired,
                            NB_FINGER,
                            TEMPLATE_TYPE, TEMPLATE_FVP_TYPE, MAX_SIZE_TEMPLATE, ENROLL_TYPE,
                            LATENT_DETECTION, coderChoice, detectModeChoice,
                            MORPHO_NO_COMPRESS, 0, templateList, callbackCmd, observer);

                    //Test of getImage method from SDK
//                    final MorphoImage[] morphoImage = new MorphoImage[] {new MorphoImage()};
//                    ret = morphoDevice.getImage(timeout, acquisitionThreshold,
//                            MORPHO_COMPRESS_WSQ, 0, detectModeChoice, LATENT_DETECTION, morphoImage[0], callbackCmd, observer);
                }

                processInfo.setCommandBioStart(false);

                MorphoUtils.storeFFDLogs(morphoDevice);

                if (ret == ErrorCodes.MORPHO_OK) {
                    exportFVP(templateList);
                    exportFP(templateList);
                }

                final int internalError = morphoDevice.getInternalError();
                final int retValue = ret;

                mHandler.post(new Runnable() {
                    @Override
                    public synchronized void run() {
                        if (retValue != ErrorCodes.MORPHOERR_CMDE_ABORTED) {
                            view.alert(retValue, internalError);
                            view.onCaptureCompleted();
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * Write data in a file with FP format
     *
     * @param templateList containing data
     */
    private void exportFP(TemplateList templateList) {
        int nbTemplate = templateList.getNbTemplate();
        for (int i = 0; i < nbTemplate; i++) {
            try (FileOutputStream fos = new FileOutputStream("sdcard/TemplateFP_" + ID_USER + "_f" + (i + 1) + TEMPLATE_TYPE.getExtension())) {
                Template t = templateList.getTemplate(i);
                byte[] data = t.getData();
                Log.d(TAG, "Writing data in file with FP format : " + Arrays.toString(data));
                fos.write(data);
            } catch (IOException e) {
                Log.e(TAG, "An error has occurred while manipulating files " + e.getMessage());
            }
        }
    }

    /**
     * Write data in a file with FVP format
     *
     * @param templateList containing data
     */
    private void exportFVP(TemplateList templateList) {
        int nbTemplateFVP = templateList.getNbFVPTemplate();
        for (int i = 0; i < nbTemplateFVP; i++) {
            try (FileOutputStream fos = new FileOutputStream("sdcard/TemplateFVP_" + ID_USER + "_f" + (i + 1) + TEMPLATE_FVP_TYPE.getExtension())) {
                TemplateFVP t = templateList.getFVPTemplate(i);
                byte[] data = t.getData();
                Log.d(TAG, "Writing data in file with FVP format : " + Arrays.toString(data));
                fos.write(data);
            } catch (IOException e) {
                Log.e(TAG, "An error has occurred while manipulating files " + e.getMessage());
            }
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
