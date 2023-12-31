package com.famoco.morphodemo.fingerprint;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.ProgressBar;

import com.famoco.morphodemo.R;
import com.famoco.morphodemo.utils.Constants;
import com.famoco.morphodemo.utils.DialogUtils;
import com.morpho.morphosmart.sdk.ErrorCodes;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility Fragment inherited by EnrollFragment and VerifyFragment
 *
 * @version DEMO
 * @author Yoann
 */
public class MorphoFragment extends Fragment {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = MorphoFragment.class.getSimpleName();

    /**
     * Name of the Template file containing all the fingerprint data
     */
    public static final String FILENAME = "TemplateFP_test_f1.iso-fmc-cs";
    /**
     * Path of the Template file containing all the fingerprint data
     */
    public static String FILEPATH = Environment.getExternalStorageDirectory().getPath() + "/" + FILENAME;

    private AlertDialog alertDialog;

    /**
     * Trigger an AlertDialog depending on the params
     * @param errorCode returned by Morpho after some interaction (capture, verify ...)
     * @param internalError that might occur for the device
     * @param type of the Fingerprint capture
     */
    public void alert(int errorCode, int internalError, String type) {
        Log.d(TAG, "\t --> Alert : " + "\n\tError Code : " + errorCode +
                "\n\tInternal Error : " + internalError + "\n\tType : " + type);
        if(getActivity() != null) {
            if (errorCode == ErrorCodes.MORPHO_OK)
                success(type);
            else
                error(getString(R.string.OP_FAILED) + "\n" + convertErrorMessage(errorCode, internalError));
        }
    }

    /**
     * Update the progress of the progressBar in param with custom background and drawables
     * @param progressBar to update
     * @param progress to set
     */
    public void updateProgress(ProgressBar progressBar, int progress) {
        if(getActivity() != null) {
            final float[] roundedCorners = new float[]{2, 2, 2, 2, 2, 2, 2, 2};
            ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));
            int color = (progress <= 50) ? (progress <= 25) ?
                    Color.parseColor("#E57373") :
                    Color.parseColor("#FFB74D") :
                    Color.parseColor("#AED581");

            pgDrawable.getPaint().setColor(color);
            ClipDrawable progressDrawable = new ClipDrawable(pgDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
            progressBar.setProgressDrawable(progressDrawable);
            progressBar.setBackground(getResources().getDrawable(R.drawable.secondary_button_background_unpressed));
            progressBar.setProgress(progress);
        }
    }

    /**
     * Display an AlertDialog that auto dismiss after a specific amount of time
     * @param success if true, no message and no button
     * @param message to display
     */
    private void showAlertDialogWithAutoDismiss(boolean success, String message) {
        if(success) {
            alertDialog = DialogUtils.createValidDialog(getActivity(), message);
        } else {
            alertDialog = DialogUtils.createErrorDialog(getActivity(), message);
        }
        this.alertDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alertDialog.isShowing())
                    alertDialog.dismiss();
            }
        }, Constants.DELAY_DIALOG_DISMISS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null && alertDialog.isShowing())
                this.alertDialog.dismiss();
    }

    /**
     * Handle error case for alert method
     * @param errorMessage to display
     */
    private void error(String errorMessage) {
        Log.d(TAG, "\t --> return codes contain error(s)");
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = { 0, 500, 250, 500};
        if (v != null)
            v.vibrate(pattern,-1);
        showAlertDialogWithAutoDismiss(false, errorMessage);
    }

    /**
     * Handle success case for alert method
     * @param type of the capture (ENROLL or VERIFY)
     */
    private void success(String type) {
        Log.d(TAG, "\t --> return codes are successful");
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification).play();
        if (v != null)
            v.vibrate(750);
        String message = "";
        switch (type) {
            case "ENROLL" :
                message = "Your fingerprint has been registered.";
                break;
            case "VERIFY" :
                message = "Your fingerprint has matched.";
                break;
        }
        showAlertDialogWithAutoDismiss(true, message);
    }

    /**
     * Translate error codes in String messages
     * @param iCodeError to translate in a String
     * @param iInternalError encountered by the device
     * @return the appropriate message
     */
    public String convertErrorMessage(int iCodeError, int iInternalError)
    {
        switch (iCodeError)
        {
            case ErrorCodes.MORPHO_OK:
                return getString(R.string.MORPHO_OK);
            case ErrorCodes.MORPHOERR_INTERNAL:
                return getString(R.string.MORPHOERR_INTERNAL);
            case ErrorCodes.MORPHOERR_PROTOCOLE:
                return getString(R.string.MORPHOERR_PROTOCOLE);
            case ErrorCodes.MORPHOERR_CONNECT:
                return getString(R.string.MORPHOERR_CONNECT);
            case ErrorCodes.MORPHOERR_CLOSE_COM:
                return getString(R.string.MORPHOERR_CLOSE_COM);
            case ErrorCodes.MORPHOERR_BADPARAMETER:
                return getString(R.string.MORPHOERR_BADPARAMETER);
            case ErrorCodes.MORPHOERR_MEMORY_PC:
                return getString(R.string.MORPHOERR_MEMORY_PC);
            case ErrorCodes.MORPHOERR_MEMORY_DEVICE:
                return getString(R.string.MORPHOERR_MEMORY_DEVICE);
            case ErrorCodes.MORPHOERR_NO_HIT:
                return getString(R.string.MORPHOERR_NO_HIT);
            case ErrorCodes.MORPHOERR_STATUS:
                return getString(R.string.MORPHOERR_STATUS);
            case ErrorCodes.MORPHOERR_DB_FULL:
                return getString(R.string.MORPHOERR_DB_FULL);
            case ErrorCodes.MORPHOERR_DB_EMPTY:
                return getString(R.string.MORPHOERR_DB_EMPTY);
            case ErrorCodes.MORPHOERR_ALREADY_ENROLLED:
                return getString(R.string.MORPHOERR_ALREADY_ENROLLED);
            case ErrorCodes.MORPHOERR_BASE_NOT_FOUND:
                return getString(R.string.MORPHOERR_BASE_NOT_FOUND);
            case ErrorCodes.MORPHOERR_BASE_ALREADY_EXISTS:
                return getString(R.string.MORPHOERR_BASE_ALREADY_EXISTS);
            case ErrorCodes.MORPHOERR_NO_ASSOCIATED_DB:
                return getString(R.string.MORPHOERR_NO_ASSOCIATED_DB);
            case ErrorCodes.MORPHOERR_NO_ASSOCIATED_DEVICE:
                return getString(R.string.MORPHOERR_NO_ASSOCIATED_DEVICE);
            case ErrorCodes.MORPHOERR_INVALID_TEMPLATE:
                return getString(R.string.MORPHOERR_INVALID_TEMPLATE);
            case ErrorCodes.MORPHOERR_NOT_IMPLEMENTED:
                return getString(R.string.MORPHOERR_NOT_IMPLEMENTED);
            case ErrorCodes.MORPHOERR_TIMEOUT:
                return getString(R.string.MORPHOERR_TIMEOUT);
            case ErrorCodes.MORPHOERR_NO_REGISTERED_TEMPLATE:
                return getString(R.string.MORPHOERR_NO_REGISTERED_TEMPLATE);
            case ErrorCodes.MORPHOERR_FIELD_NOT_FOUND:
                return getString(R.string.MORPHOERR_FIELD_NOT_FOUND);
            case ErrorCodes.MORPHOERR_CORRUPTED_CLASS:
                return getString(R.string.MORPHOERR_CORRUPTED_CLASS);
            case ErrorCodes.MORPHOERR_TO_MANY_TEMPLATE:
                return getString(R.string.MORPHOERR_TO_MANY_TEMPLATE);
            case ErrorCodes.MORPHOERR_TO_MANY_FIELD:
                return getString(R.string.MORPHOERR_TO_MANY_FIELD);
            case ErrorCodes.MORPHOERR_MIXED_TEMPLATE:
                return getString(R.string.MORPHOERR_MIXED_TEMPLATE);
            case ErrorCodes.MORPHOERR_CMDE_ABORTED:
                return getString(R.string.MORPHOERR_CMDE_ABORTED);
            case ErrorCodes.MORPHOERR_INVALID_PK_FORMAT:
                return getString(R.string.MORPHOERR_INVALID_PK_FORMAT);
            case ErrorCodes.MORPHOERR_SAME_FINGER:
                return getString(R.string.MORPHOERR_SAME_FINGER);
            case ErrorCodes.MORPHOERR_OUT_OF_FIELD:
                return getString(R.string.MORPHOERR_OUT_OF_FIELD);
            case ErrorCodes.MORPHOERR_INVALID_USER_ID:
                return getString(R.string.MORPHOERR_INVALID_USER_ID);
            case ErrorCodes.MORPHOERR_INVALID_USER_DATA:
                return getString(R.string.MORPHOERR_INVALID_USER_DATA);
            case ErrorCodes.MORPHOERR_FIELD_INVALID:
                return getString(R.string.MORPHOERR_FIELD_INVALID);
            case ErrorCodes.MORPHOERR_USER_NOT_FOUND:
                return getString(R.string.MORPHOERR_USER_NOT_FOUND);
            case ErrorCodes.MORPHOERR_COM_NOT_OPEN:
                return getString(R.string.MORPHOERR_COM_NOT_OPEN);
            case ErrorCodes.MORPHOERR_ELT_ALREADY_PRESENT:
                return getString(R.string.MORPHOERR_ELT_ALREADY_PRESENT);
            case ErrorCodes.MORPHOERR_NOCALLTO_DBQUERRYFIRST:
                return getString(R.string.MORPHOERR_NOCALLTO_DBQUERRYFIRST);
            case ErrorCodes.MORPHOERR_USER:
                return getString(R.string.MORPHOERR_USER);
            case ErrorCodes.MORPHOERR_BAD_COMPRESSION:
                return getString(R.string.MORPHOERR_BAD_COMPRESSION);
            case ErrorCodes.MORPHOERR_SECU:
                return getString(R.string.MORPHOERR_SECU);
            case ErrorCodes.MORPHOERR_CERTIF_UNKNOW:
                return getString(R.string.MORPHOERR_CERTIF_UNKNOW);
            case ErrorCodes.MORPHOERR_INVALID_CLASS:
                return getString(R.string.MORPHOERR_INVALID_CLASS);
            case ErrorCodes.MORPHOERR_USB_DEVICE_NAME_UNKNOWN:
                return getString(R.string.MORPHOERR_USB_DEVICE_NAME_UNKNOWN);
            case ErrorCodes.MORPHOERR_CERTIF_INVALID:
                return getString(R.string.MORPHOERR_CERTIF_INVALID);
            case ErrorCodes.MORPHOERR_SIGNER_ID:
                return getString(R.string.MORPHOERR_SIGNER_ID);
            case ErrorCodes.MORPHOERR_SIGNER_ID_INVALID:
                return getString(R.string.MORPHOERR_SIGNER_ID_INVALID);
            case ErrorCodes.MORPHOERR_FFD:
                return getString(R.string.MORPHOERR_FFD);
            case ErrorCodes.MORPHOERR_MOIST_FINGER:
                return getString(R.string.MORPHOERR_MOIST_FINGER);
            case ErrorCodes.MORPHOERR_NO_SERVER:
                return getString(R.string.MORPHOERR_NO_SERVER);
            case ErrorCodes.MORPHOERR_OTP_NOT_INITIALIZED:
                return getString(R.string.MORPHOERR_OTP_NOT_INITIALIZED);
            case ErrorCodes.MORPHOERR_OTP_PIN_NEEDED:
                return getString(R.string.MORPHOERR_OTP_PIN_NEEDED);
            case ErrorCodes.MORPHOERR_OTP_REENROLL_NOT_ALLOWED:
                return getString(R.string.MORPHOERR_OTP_REENROLL_NOT_ALLOWED);
            case ErrorCodes.MORPHOERR_OTP_ENROLL_FAILED:
                return getString(R.string.MORPHOERR_OTP_ENROLL_FAILED);
            case ErrorCodes.MORPHOERR_OTP_IDENT_FAILED:
                return getString(R.string.MORPHOERR_OTP_IDENT_FAILED);
            case ErrorCodes.MORPHOERR_NO_MORE_OTP:
                return getString(R.string.MORPHOERR_NO_MORE_OTP);
            case ErrorCodes.MORPHOERR_OTP_NO_HIT:
                return getString(R.string.MORPHOERR_OTP_NO_HIT);
            case ErrorCodes.MORPHOERR_OTP_ENROLL_NEEDED:
                return getString(R.string.MORPHOERR_OTP_ENROLL_NEEDED);
            case ErrorCodes.MORPHOERR_DEVICE_LOCKED:
                return getString(R.string.MORPHOERR_DEVICE_LOCKED);
            case ErrorCodes.MORPHOERR_DEVICE_NOT_LOCK:
                return getString(R.string.MORPHOERR_DEVICE_NOT_LOCK);
            case ErrorCodes.MORPHOERR_OTP_LOCK_GEN_OTP:
                return getString(R.string.MORPHOERR_OTP_LOCK_GEN_OTP);
            case ErrorCodes.MORPHOERR_OTP_LOCK_SET_PARAM:
                return getString(R.string.MORPHOERR_OTP_LOCK_SET_PARAM);
            case ErrorCodes.MORPHOERR_OTP_LOCK_ENROLL:
                return getString(R.string.MORPHOERR_OTP_LOCK_ENROLL);
            case ErrorCodes.MORPHOERR_FVP_MINUTIAE_SECURITY_MISMATCH:
                return getString(R.string.MORPHOERR_FVP_MINUTIAE_SECURITY_MISMATCH);
            case ErrorCodes.MORPHOERR_FVP_FINGER_MISPLACED_OR_WITHDRAWN:
                return getString(R.string.MORPHOERR_FVP_FINGER_MISPLACED_OR_WITHDRAWN);
            case ErrorCodes.MORPHOERR_LICENSE_MISSING:
                return getString(R.string.MORPHOERR_LICENSE_MISSING);
            case ErrorCodes.MORPHOERR_CANT_GRAN_PERMISSION_USB:
                return getString(R.string.MORPHOERR_CANT_GRAN_PERMISSION_USB);
            default:
                return String.format(Locale.FRANCE,"Unknown error %d, Internal Error = %d",
                        iCodeError, iInternalError);
        }
    }
}
