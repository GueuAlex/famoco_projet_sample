package com.famoco.morphodemo.fingerprint.verify;

import android.os.Handler;
import android.util.Log;

import com.famoco.morphodemo.fingerprint.MorphoFragment;
import com.famoco.morphodemo.fingerprint.MorphoUtils;
import com.famoco.morphodemo.utils.morpho.ProcessInfo;
import com.morpho.morphosmart.sdk.CallbackMask;
import com.morpho.morphosmart.sdk.CallbackMessage;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.FalseAcceptanceRate;
import com.morpho.morphosmart.sdk.ITemplateType;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.ResultMatching;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateFVP;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Presenter of VerifyFragment
 *
 * @author Yoann
 * @version DEMO
 */
public class VerifyPresenter implements Observer, VerifyContract.Presenter {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = VerifyPresenter.class.getSimpleName();

    /**
     * Default delay to retry verification
     */
    private static final int RETRY_DELAY = 2000;

    /**
     * Handler to update UI Thread
     */
    private Handler mHandler;

    /**
     * View of the MVP pattern
     */
    private VerifyContract.View view;

    /**
     * Morpho device
     */
    private MorphoDevice morphoDevice;

    /**
     * Constructor of the Presenter
     *
     * @param view of the MVP pattern
     */
    VerifyPresenter(VerifyContract.View view) {
        this.view = checkNotNull(view);
        this.mHandler = new Handler();
        this.morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
    }

    @Override
    public void processFingerVerification() {
        morphoDeviceVerify(this);
    }

    @Override
    public void stopProcess() {
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Start the verification of a fingerprint with Morpho device
     *
     * @param observer that will be notified in real time of the device acquisition
     */
    private void morphoDeviceVerify(final Observer observer) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(MorphoFragment.FILEPATH))) {

            int length = dis.available();
            final byte[] buffer = new byte[length];
            dis.readFully(buffer);

            // Background thread to process the verification of the fingerprint
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Template template = new Template();
                    TemplateFVP templateFVP = new TemplateFVP();
                    TemplateList templateList = new TemplateList();

                    ITemplateType iTemplateType = getTemplateTypeFromExtension(getFileExtension(MorphoFragment.FILENAME));
                    if (iTemplateType instanceof TemplateFVPType) {
                        templateFVP.setData(buffer);
                        templateFVP.setTemplateFVPType((TemplateFVPType) iTemplateType);
                        templateList.putFVPTemplate(templateFVP);
                    } else {
                        template.setData(buffer);
                        template.setTemplateType((TemplateType) iTemplateType);
                        templateList.putTemplate(template);
                    }

                    int timeOut = 0;
                    int far = FalseAcceptanceRate.MORPHO_FAR_5;
                    Coder coderChoice = Coder.MORPHO_DEFAULT_CODER;
                    int detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
                    int matchingStrategy = 0;

                    int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

                    callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();

                    ResultMatching resultMatching = new ResultMatching();

                    int ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());

                    if (ret == 0) {
                        ret = morphoDevice.verify(timeOut, far,
                                coderChoice, detectModeChoice,
                                matchingStrategy, templateList,
                                callbackCmd, observer, resultMatching);
                    }

                    ProcessInfo.getInstance().setCommandBioStart(false);

                    MorphoUtils.storeFFDLogs(morphoDevice);

                    final int l_ret = ret;
                    final int internalError = morphoDevice.getInternalError();
                    mHandler.post(new Runnable() {
                        @Override
                        public synchronized void run() {
                            if (l_ret != ErrorCodes.MORPHOERR_CMDE_ABORTED) {
                                view.alert(l_ret, internalError);
                                view.onVerificationCompleted();
                            }
                        }
                    });
                }
            }).start();
        } catch (IOException e) {
            Log.d(TAG, "Exception encountered with file verification : " + e.getMessage());
        }
    }

    /**
     * Determines whether the Template is of type Template FP or Template FVP (depending on the
     * different Morpho devices)
     *
     * @param extension of the file
     * @return the type of Template
     */
    private ITemplateType getTemplateTypeFromExtension(String extension) {
        for (TemplateType templateType : TemplateType.values()) {
            if (templateType.getExtension().equalsIgnoreCase(extension)) {
                return templateType;
            }
        }
        for (TemplateFVPType templateFVPType : TemplateFVPType.values()) {
            if (templateFVPType.getExtension().equalsIgnoreCase(extension)) {
                return templateFVPType;
            }
        }
        return TemplateType.MORPHO_NO_PK_FP;
    }

    /**
     * Subtract the extension of a file and return it
     *
     * @param fileName to isolate extension
     * @return the extension
     */
    private String getFileExtension(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex);
        }
        return extension;
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
