package com.famoco.morphodemo.home;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.famoco.morphodemo.BuildConfig;
import com.famoco.morphodemo.R;
import com.famoco.morphodemo.databinding.ActivityHomeBinding;
import com.famoco.morphodemo.fingerprint.enroll.EnrollFragment;
import com.famoco.morphodemo.fingerprint.image.ImageFragment;
import com.famoco.morphodemo.fingerprint.verify.VerifyFragment;
import com.famoco.morphodemo.utils.DialogUtils;
import com.famoco.morphodemo.utils.Utils;
import com.morpho.morphosmart.sdk.ErrorCodes;

import java.util.Observable;
import java.util.Observer;

/**
 * Main Activity of the App that switch between Fragments
 * to either register a new fingerprint or check one
 *
 * @author Yoann
 * @version DEMO
 */
public class HomeActivity extends AppCompatActivity implements
        HomeContract.View,
        HomeFragment.OnFragmentInteractionListener, Observer {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = HomeActivity.class.getSimpleName();

    private HomeContract.Presenter presenter;
    private BroadcastReceiver onAttachUSBBroadcastReceiver;
    private BroadcastReceiver onDetachUSBBroadcastReceiver;

    private HomeFragment homeFragment;
    private EnrollFragment enrollFragment;

    private static Observer callback;

    Toolbar toolbar;
    TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        toolbar = binding.toolbar;
        versionText = binding.appVersionTextView;

        setSupportActionBar(toolbar);

        if (!Utils.isFP200()) {
            // Create a BroadcastReceiver for USB permission
            this.onAttachUSBBroadcastReceiver = createOnAttachUSBBroadcastReceiver();
            this.onDetachUSBBroadcastReceiver = createOnDetachedUSBBroadcastReceiver();
        }

        this.homeFragment = HomeFragment.newInstance();
        this.enrollFragment = EnrollFragment.newInstance();
        this.presenter = new HomePresenter(this);

        setCallback(this);

        commitFragment(homeFragment, false);

        this.presenter.openConnection();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_about:
                this.presenter.updateMorphoDeviceInfo();
                break;
        }
        return true;
    }
    /**
     * Accessors of the callback variable used to know if the device is well reconnected or not
     */
    public static Observer getCallback() {
        return callback;
    }

    public static void setCallback(Observer callback) {
        HomeActivity.callback = callback;
    }

    private void commitFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getTag());

        if (addToBackStack)
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());

        fragmentTransaction.commit();
    }

    private void initUSBBroadcastReceiver(BroadcastReceiver onAttach, BroadcastReceiver onDetach) {
        // Detach events are sent as a system-wide broadcast
        Log.d(TAG, "\t--> Register Receiver");
        registerReceiver(onAttach, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        registerReceiver(onDetach, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }

    /**
     * Broadcast receiver to handle USB connection events.
     */
    private BroadcastReceiver createOnAttachUSBBroadcastReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        Log.i(TAG, "\t--> onReceive: USB Attached " + device.toString());
                        if (device.getVendorId() == 8797) {
                            rebootSoft();
                        } else {
                            Log.e(TAG, "onReceive: Device attached : Not a Morpho");
                        }
                    }
                }
            }
        };
    }

    /**
     * Broadcast receiver to handle USB disconnection events.
     */
    private BroadcastReceiver createOnDetachedUSBBroadcastReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        Log.i(TAG, "\t--> onReceive: USB Detached " + device.toString());
                        if (device.getVendorId() == 8797) {
                            presenter.cancelConnection();
                            Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
                            if ((f instanceof EnrollFragment) || (f instanceof VerifyFragment) || (f instanceof ImageFragment)) {
                                onBackPressed();
                            }
                            displayDetachToast();
                        } else {
                            Log.e(TAG, "onReceive: Device Detached : Not a Morpho");
                        }
                    }
                }
            }
        };
    }

    /**
     * Method used to reboot the Morpho device
     */
    private void rebootSoft() {
        displayAttachToast();
        int ret = presenter.rebootSoft(callback);
        if (ErrorCodes.MORPHO_OK == ret) {
            Log.d(TAG, "onReceive: device reconnected");
        } else {
            Log.e(TAG, "onReceive: ErrorCodes : " + ret);
        }
    }

    @Override
    public void onHomeFragmentInteraction(HomeAction action) {
        this.presenter.handleInteraction(action);
    }

    @Override
    protected void onResume() {
        super.onResume();
        versionText.setText(String.format(getString(R.string.VERSION), BuildConfig.VERSION_NAME));
        if (!Utils.isFP200()) {
            initUSBBroadcastReceiver(onAttachUSBBroadcastReceiver, onDetachUSBBroadcastReceiver);
        }
        this.presenter.openConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.presenter.cancelConnection();
        // Unregister the Receivers for USB
        if (!Utils.isFP200()) {
            unregisterReceiver(this.onAttachUSBBroadcastReceiver);
            unregisterReceiver(this.onDetachUSBBroadcastReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.presenter.closeConnection();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ! Prevent memory leaks !

    }

    @Override
    public void processEnroll() {
        toolbar.setTitle("Registration");
        commitFragment(enrollFragment, true);
    }

    @Override
    public void processVerify() {
        toolbar.setTitle("Verification");
        commitFragment(VerifyFragment.newInstance(), true);
    }

    @Override
    public void processImage() {
        toolbar.setTitle("WSQ Registration");
        commitFragment(ImageFragment.newInstance(), true);
    }

    @Override
    public void update(Observable o, Object rebootOK) {
        boolean isRebootOK = (Boolean) rebootOK;
        if (isRebootOK) {
            Log.d(TAG, "onReceive: update : device reconnected");
            displayValidReconnectingToast();
        } else {
            displayErrorReconnectingToast();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        toolbar.setTitle("Biometric Fingerprint");
        this.presenter.cancelConnection();
    }

    @Override
    public void setMorphoDeviceInfo(String productInfo, String softwareInfo) {
        DialogUtils.createInfoDialog(this, productInfo, softwareInfo).show();
    }

    /**
     * Display a Toast and disable capture buttons when the Morpho device is detached
     */
    private void displayDetachToast() {
//        this.homeFragment.toggleCaptureButtons(false);
        Toast.makeText(this, R.string.MSG_ERROR_DEVICE_DISCONNECTED, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display a Toast when the Morpho device is attached
     */
    private void displayAttachToast() {
        Toast.makeText(this, R.string.RECONNECTING_SOFTWARE, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display a Toast and enable capture buttons when the Morpho device has been correctly reconnected
     */
    private void displayValidReconnectingToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                homeFragment.toggleCaptureButtons(true);
                Toast.makeText(HomeActivity.this, R.string.VALID_RECONNECTING_SOFTWARE, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Display a Toast when the Morpho device cannot be reconnected
     */
    private void displayErrorReconnectingToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HomeActivity.this, R.string.ERROR_RECONNECTING_SOFTWARE, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void displayNoFingerAvailable() {
        Toast.makeText(HomeActivity.this, R.string.NO_FINGERPRINT_STORED, Toast.LENGTH_SHORT).show();
    }
}
