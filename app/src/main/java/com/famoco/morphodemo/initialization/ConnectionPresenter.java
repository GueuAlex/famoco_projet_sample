package com.famoco.morphodemo.initialization;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.famoco.morphodemo.R;
import com.famoco.morphodemo.utils.Constants;
import com.famoco.morphodemo.utils.Utils;
import com.famoco.morphodemo.utils.morpho.DeviceDetectionMode;
import com.famoco.morphodemo.utils.morpho.MorphoInfo;
import com.famoco.morphodemo.utils.morpho.ProcessInfo;
import com.morpho.android.usb.USBManager;
import com.morpho.morphosmart.sdk.CustomInteger;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.MorphoDevice;

/**
 * Presenter of ConnectionActivity
 *
 * @version DEMO
 * @author Yoann
 */
class ConnectionPresenter implements ConnectionContract.Presenter {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = ConnectionPresenter.class.getSimpleName();

    /**
     * Messages to User through View
     */
    private static final String MSG_USER_PROGRESS_USB_PERMISSION = "Check USB permission ...";
    private static final String MSG_USER_PROGRESS_DEVICE_FOUND = "Morpho device found ...";
    private static final String MSG_USER_PROGRESS_ENUMERATION = "Enumerate devices ...";
    private static final String MSG_USER_PROGRESS_CONNECTION = "Connection with Morpho device ...";
    private static final String MSG_USER_PROGRESS_CONNECTION_ESTABLISHED = "Connection established successfully !";

    private static final int FIRST_STEP = 20;
    private static final int SECOND_STEP = 40;
    private static final int THIRD_STEP = 60;
    private static final int FOURTH_STEP = 80;
    private static final int LAST_STEP = 100;
    /**
     * Detection mode for the Morpho device
     */
    private DeviceDetectionMode detectionMode = DeviceDetectionMode.SdkDetection;

    /**
     * Name of the Morpho sensor
     * Can be obtained with {@link MorphoDevice#getUsbDeviceName(int)}
     */
    private String sensorName;

    /**
     * Morpho Device
     */
    private MorphoDevice morphoDevice;

    /**
     * View of this presenter
     */
    private ConnectionContract.View view;

    /**
     * Constructor of the Presenter
     * @param view of the MVP pattern
     */
    ConnectionPresenter(ConnectionContract.View view) {
        this.view = view;
    }

    /**
     * Enumerate the devices connected
     * Suppression of Warning on purpose to avoid suppression of new Integer(0) line
     * which is useful to store object in memory
     */
    @SuppressWarnings("all")
    private int enumerate() {

        // The creation of an Integer is required here.
        // It is used as a pointer for memory uses in the call of C native functions in
        // Morpho SDK
        CustomInteger nbUsbDevice = new CustomInteger();
        Log.d(TAG, "\t --> Start initUSBDevicesNameEnum");
        int ret = this.morphoDevice.initUsbDevicesNameEnum(nbUsbDevice);
        Log.d(TAG, "\t --> End initUSBDevicesNameEnum");

        if (ret == ErrorCodes.MORPHO_OK) {
            Log.d(TAG, "\t --> MORPHO OK");
            if (nbUsbDevice.getValueOf() > 0) {
                this.sensorName = morphoDevice.getUsbDeviceName(0);
                view.informUserOfCurrentProgress(MSG_USER_PROGRESS_DEVICE_FOUND, THIRD_STEP);
                Log.i(TAG, "\t --> Enumerate : SensorName : " + sensorName);
            } else {
                ret = -1;
                Log.d(TAG, "\t --> NO DEVICE FOUND");
                view.deviceNotFound();
            }
        } else {
            Log.d(TAG, "\t --> MORPHO NOT OK");
            ret = -1;
            view.displayDialogWithMessage(ErrorCodes.getError(ret, morphoDevice.getInternalError()));
        }
        return ret;
    }

    /**
     * Connection with Morpho device and set data
     * If all initialization succeed, starts next HomeActivity
     */
    private void connection() {
        int ret;
        if (!Utils.isFP200()) {
            // Open USB connection with Morpho device
            ret = morphoDevice.openUsbDevice(sensorName, 2000);
        } else {
            ret = morphoDevice.openDeviceWithUart(Constants.UART_PORT, Constants.UART_SPEED);
        }
        Log.d(TAG, "\t --> Open Device returned : " + ret);

        if (ret == ErrorCodes.MORPHO_OK) {
            // Set Morpho device data
            initMorphoDeviceData();

            view.informUserOfCurrentProgress(MSG_USER_PROGRESS_CONNECTION_ESTABLISHED, LAST_STEP);

            String productDescriptor = morphoDevice.getProductDescriptor();
            java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(productDescriptor, "\n");
            if (tokenizer.hasMoreTokens())
            {
                String l_s_current = tokenizer.nextToken();
                if (l_s_current.contains("FINGER VP") || l_s_current.contains("FVP"))
                    MorphoInfo.setM_b_fvp(true);
            }

            // Close USB connection with Morpho device
            morphoDevice.closeDevice();

            // Start next HomeActivity
            view.startNextActivity();

        } else {
            // Close USB connection with Morpho device
            morphoDevice.closeDevice();
            view.displayDialogWithMessage(ErrorCodes.getError(ret, morphoDevice.getInternalError()));
        }
    }

    /**
     * Initiate the Morpho device data
     */
    private void initMorphoDeviceData() {
        // Default configuration of the Morpho device
        int sensorBus = -1, sensorAddress = -1, sensorFileDescriptor = -1;

        ProcessInfo.getInstance().setMSOSerialNumber(sensorName);
        ProcessInfo.getInstance().setMSOBus(sensorBus);
        ProcessInfo.getInstance().setMSOAddress(sensorAddress);
        ProcessInfo.getInstance().setMSOFD(sensorFileDescriptor);
        ProcessInfo.getInstance().setMsoDetectionMode(detectionMode);

        if (Utils.isFP200()) {
            int ret = this.morphoDevice.setConfigParam(MorphoDevice.CONFIG_RS232_PREVIEW_BPP, new byte[] { 4 });
            if (ret != ErrorCodes.MORPHO_OK) {
                Log.d(TAG, "\t--> (B) RS232 set preview BPP returned error " + ret);
            }

            byte[] r = this.morphoDevice.getConfigParam(MorphoDevice.CONFIG_RS232_PREVIEW_BPP);
            if (r == null) {
                Log.d(TAG, "\t--> (B) RS232 get preview BPP returned null");
            } else {
                Log.d(TAG, "\t--> (B) RS232 preview BPP set to " + r[0]);
            }

            ret = this.morphoDevice.setConfigParam(MorphoDevice.CONFIG_RS232_PREVIEW_DR, new byte[] { 2 });
            if (ret != ErrorCodes.MORPHO_OK) {
                Log.d(TAG, "\t--> (B) RS232 set preview DR returned error " + ret);
            }
            r = this.morphoDevice.getConfigParam(MorphoDevice.CONFIG_RS232_PREVIEW_DR);
            if (r == null) {
                Log.d(TAG, "\t--> (B) RS232 get preview DR returned null");
            } else {
                Log.d(TAG, "\t--> (B) RS232 preview DR set to " + r[0]);
            }
        }
    }

    @Override
    public void createMorphoDevice() {
        // Instantiation of Morpho Device
        this.morphoDevice = new MorphoDevice();
        ProcessInfo.getInstance().setMorphoDevice(this.morphoDevice);
    }

    @Override
    public void initiateMorphoDevice() {
        // {Morpho SDK method} to check the USB permission
        // (call to USBManager.getInstance().initialize(...) required
        if (!Utils.isFP200()) {
            if (USBManager.getInstance().isDevicesHasPermission()) {
                Log.d(TAG, "\t --> Start enumeration of devices");
                view.informUserOfCurrentProgress(MSG_USER_PROGRESS_ENUMERATION, SECOND_STEP);
                if (enumerate() == ErrorCodes.MORPHO_OK) {
                    Log.d(TAG, "\t --> Start connection");
                    view.informUserOfCurrentProgress(MSG_USER_PROGRESS_CONNECTION, FOURTH_STEP);
                    connection();
                }
            }
        } else {
            Log.d(TAG, "\t --> Start connection");
            view.informUserOfCurrentProgress(MSG_USER_PROGRESS_CONNECTION, FOURTH_STEP);
            connection();
        }
    }

    @Override
    public void askUSBPermission(@NonNull Context context) {
        view.informUserOfCurrentProgress(MSG_USER_PROGRESS_USB_PERMISSION, FIRST_STEP);
        // {Morpho SDK method} that initialize Morpho device and ask for USB permission
        USBManager.getInstance().initialize(context, context.getResources().getString(R.string.ACTION_USB_PERMISSION));
    }
}