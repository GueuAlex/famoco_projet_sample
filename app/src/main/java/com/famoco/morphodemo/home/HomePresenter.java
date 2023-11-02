package com.famoco.morphodemo.home;

import android.util.Log;

import com.famoco.morphodemo.fingerprint.MorphoFragment;
import com.famoco.morphodemo.utils.Constants;
import com.famoco.morphodemo.utils.Utils;
import com.famoco.morphodemo.utils.morpho.ProcessInfo;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.MorphoDevice;

import java.io.File;
import java.util.Observer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Presenter of HomeActivity
 *
 * @author Yoann
 * @version DEMO
 */
public class HomePresenter implements HomeContract.Presenter {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = HomePresenter.class.getSimpleName();

    /**
     * View of the MVP pattern
     */
    private HomeContract.View view;

    /**
     * Morpho device
     */
    private MorphoDevice morphoDevice;

    HomePresenter(HomeContract.View view) {
        this.view = checkNotNull(view);
    }

    @Override
    public void openConnection() {
        this.morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        if (!Utils.isFP200()) {
            if (this.morphoDevice.openUsbDevice(
                    ProcessInfo.getInstance().getMSOSerialNumber(), 0) != ErrorCodes.MORPHO_OK) {
                closeConnection();
                Log.e(TAG, "\t--> Error opening device in DeviceDetectionMode.SdkDetection");
            }
        } else {
            if (this.morphoDevice.openDeviceWithUart(
                    Constants.UART_PORT, Constants.UART_SPEED) != ErrorCodes.MORPHO_OK) {
            }
        }
        Log.d(TAG, "\t--> Opening device in DeviceDetectionMode.SdkDetection");
    }

    @Override
    public void closeConnection() {
        this.morphoDevice.cancelLiveAcquisition();
        this.morphoDevice.closeDevice();
    }

    @Override
    public void handleInteraction(HomeAction action) {
        Log.d(TAG, "\t--> onFragmentInteraction : " + action);
        switch (action) {
            case ENROLL:
                view.processEnroll();
                break;
            case VERIFY:
                if (fingerPrintExist())
                    view.processVerify();
                else
                    view.displayNoFingerAvailable();
                break;
            case IMAGE:
                view.processImage();
                break;
            case REBOOT:
                rebootSoft(HomeActivity.getCallback());
                break;
            default:
                Log.e(TAG, "Unknown interaction : " + action);
                break;
        }
    }

    @Override
    public void cancelConnection() {
        Log.d(TAG, "\t--> Closing Morpho device");
        ProcessInfo.getInstance().getMorphoDevice().cancelLiveAcquisition();
    }

    @Override
    public int rebootSoft(Observer callback) {
        return morphoDevice.rebootSoft(30, callback);
    }

    @Override
    public void updateMorphoDeviceInfo() {
        view.setMorphoDeviceInfo(morphoDevice.getProductDescriptor(), morphoDevice.getSoftwareDescriptor());
    }

    /**
     * Determines whether a fingerprint exist or not (file with content exist)
     *
     * @return a boolean value
     */
    private boolean fingerPrintExist() {
        return (new File(MorphoFragment.FILEPATH).length() != 0);
    }
}
