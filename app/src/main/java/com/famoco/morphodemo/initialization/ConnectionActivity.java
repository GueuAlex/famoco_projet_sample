package com.famoco.morphodemo.initialization;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.famoco.morphodemo.R;
import com.famoco.morphodemo.databinding.ActivityConnectionBinding;
import com.famoco.morphodemo.home.HomeActivity;
import com.famoco.morphodemo.utils.Constants;
import com.famoco.morphodemo.utils.DialogUtils;
import com.famoco.morphodemo.utils.Utils;

/**
 * First HomeActivity displayed to initiate connection with Morpho device
 * and ask User permissions.
 *
 * @version DEMO
 * @author Yoann
 */
public class ConnectionActivity extends android.app.Activity implements ConnectionContract.View {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = ConnectionActivity.class.getSimpleName();

    /**
     * ButterKnife View Binding
     */
    ProgressBar progressBar;
    TextView infoTextView;

    /**
     * BroadcastReceiver for USB permission
     */
    private BroadcastReceiver usbPermissionBroadcastReceiver;

    /**
     * Alert Dialog for User feedback
     */
    private AlertDialog alertDialog;

    /**
     * Presenter of this View
     */
    private ConnectionContract.Presenter presenter;

    /**
     * Handler to communicate with UI Thread
     */
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityConnectionBinding binding = ActivityConnectionBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        Utils.verifyStoragePermissions(this);

        Log.d(TAG, "onCreate");

        // ButterKnife binding for Views
        progressBar = binding.ConnectionActivityProgressBar;
        infoTextView = binding.ConnectionActivityTextView;

        // Presenter of this View
        this.presenter = new ConnectionPresenter(this);
        this.presenter.createMorphoDevice();

        if (!Utils.isFP200()) {
            // Create a BroadcastReceiver for USB permission
            this.usbPermissionBroadcastReceiver = createUSBPermissionBroadcastReceiver();
            // Register a BroadcastReceiver for USB permission (Notified when Dialog of USB permission
            // is dismissed)
            this.registerReceiver(this.usbPermissionBroadcastReceiver, new IntentFilter(Constants.ACTION_USB_PERMISSION));
        }
        this.mHandler = initHandler();
        // Start the process of initialization of Morpho device
        new ProcessThread().start();
    }

    @SuppressLint("HandlerLeak")
    private Handler initHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String message = (String) msg.obj;

                Log.d(TAG, "\t --> Message received from thread : " + msg);

                switch (ConnectionAction.valueOf(msg.arg2)) {
                    case INFO:
                        progressBar.setProgress(msg.arg1);
                        infoTextView.setText(message);
                        break;
                    case DIALOG_MESSAGE:
                        alertDialog = DialogUtils.createErrorDialog(ConnectionActivity.this, message);
                        alertDialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                    onBackPressed();
                                }
                            }
                        }, Constants.DELAY_DIALOG_DISMISS);
                        break;
                    case NEXT_ACTIVITY:
                        Intent homeIntent = new Intent(ConnectionActivity.this, HomeActivity.class);
                        startActivity(homeIntent);
                        finish(); // End this HomeActivity so User can not return to it on back pressed
                        break;
                    case PERMISSION_DENIED:
                        Toast.makeText(ConnectionActivity.this,
                                getString(R.string.MSG_ERROR_PERMISSION_REQUIRED),
                                Toast.LENGTH_LONG).show();
                        finish();
                        break;
                }
            }
        };
    }

    /**
     * Create a BroadcastReceiver for USB permissions
     * @return the BroadcastReceiver
     */
    private BroadcastReceiver createUSBPermissionBroadcastReceiver() {
        return new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                if (Constants.ACTION_USB_PERMISSION.equals(intent.getAction())) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.d(TAG, "\t --> Permission granted for device" + device);
                        if(device != null){
                            Log.d(TAG, "\t --> Device USB found ");
                            // Initiate if permission granted
                            presenter.initiateMorphoDevice();
                        }
                    } else {
                        Log.d(TAG, "\t --> Permission denied for device " + device);
                        permissionDenied();
                    }
                }
            }
        };
    }

    /**
     * Send message to the handler in the main Thread
     * @param connectionAction to execute
     * @param message to display
     */
    private void informUIThread(ConnectionAction connectionAction, String message, int progress) {
        Log.d(TAG, "Action : " + connectionAction + "\nMessage : " + message);
        Message msg = Message.obtain();
        msg.obj = message;
        msg.arg1 = progress;
        msg.arg2 = connectionAction.getValue();
        mHandler.sendMessage(msg);
    }

    @Override
    public void displayDialogWithMessage(String message) {
       informUIThread(ConnectionAction.DIALOG_MESSAGE, message, 0);
    }

    @Override
    public void startNextActivity() {
        Log.d(TAG,"\t--> Starting next Activity");
        informUIThread(ConnectionAction.NEXT_ACTIVITY, "", 0);
    }

    @Override
    public void permissionDenied() {
        Log.d(TAG,"\t--> Permission denied Activity");
        informUIThread(ConnectionAction.PERMISSION_DENIED, "", 0);
    }

    @Override
    public void deviceNotFound() {
        displayDialogWithMessage(getString(R.string.MSG_ERROR_NO_DEVICE_FOUND));
    }

    @Override
    public void informUserOfCurrentProgress(String message, int progress) {
        informUIThread(ConnectionAction.INFO, message, progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!Utils.isFP200()) {
            // ! Prevent memory leaks !
            // Unregister the Receiver for USB permission
            unregisterReceiver(usbPermissionBroadcastReceiver);
        }
        // Dismiss alertDialog
        if(null != alertDialog && this.alertDialog.isShowing()) {
            this.alertDialog.dismiss();
        }
    }

    /**
     * Ask permission to User for USB uses the first time.
     * If already granted, initiate Morpho device.
     * initialize() also method set stuff for Morpho device (native C functions)
     */
    private class ProcessThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (!Utils.isFP200()) {
                presenter.askUSBPermission(getApplicationContext());
            }
            presenter.initiateMorphoDevice();
        }
    }
}
