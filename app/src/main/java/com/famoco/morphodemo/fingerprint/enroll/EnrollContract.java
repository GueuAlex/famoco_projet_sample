package com.famoco.morphodemo.fingerprint.enroll;

import android.graphics.Bitmap;

/**
 * Contract between View and Presenter (MVP Pattern)
 *
 * @version DEMO
 * @author Yoann
 */
public interface EnrollContract {

    interface View {

        /**
         * Display AlertDialog
         * @param errorCode returned by Morpho after some interaction (capture, verify ...)
         * @param internalError that might occur for the device
         */
        void alert(int errorCode, int internalError);

        /**
         * Update the progress of the ProgressBar
         * @param quality as the new progress to set
         */
        void updateSensorProgressBar(Integer quality);

        /**
         * Update the message displayed to User
         * @param commandMessage as the new message to set
         */
        void updateSensorMessage(String commandMessage);

        /**
         * Update the image displayed to User
         * @param imageBmp as the new image to set
         */
        void updateImage(Bitmap imageBmp);

        /**
         * Callback that indicates that the fingerprint capture process has ended
         */
        void onCaptureCompleted();

    }

    interface Presenter {

        /**
         * Start the capture of a fingerprint
         */
        void processFingerCapture();


        /**
         * Remove callback and messages on Handler
         */
        void stopProcess();
    }
}
