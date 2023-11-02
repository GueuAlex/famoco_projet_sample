package com.famoco.morphodemo.initialization;

import android.content.Context;

/**
 * Contract between View and Presenter (MVP Pattern)
 *
 * @version DEMO
 * @author Yoann
 */
public interface ConnectionContract {

    /**
     * VIEW
     */
    interface View {

        /**
         * Inform User of the current state of the app through a TextView
         * @param message to display
         */
        void informUserOfCurrentProgress(String message, int progress);

        /**
         * Set message for the AlertDialog and display it
         * @param message to display
         */
        void displayDialogWithMessage(String message);

        /**
         * Start next HomeActivity
         */
        void startNextActivity();

        /**
         * USB permission denied by User
         */
        void permissionDenied();

        /**
         * No Morpho device found
         */
        void deviceNotFound();
    }

    /**
     * Presenter
     */
    interface Presenter {

        /**
         * Instantiate a Morpho Device
         */
        void createMorphoDevice();

        /**
         * Test USB permission.
         * If granted, enumerate device and starts connection with it
         */
        void initiateMorphoDevice();

        /**
         * Require User USB permission
         * @param context of the Application required to call Morpho SDK method initialize
         */
        void askUSBPermission(Context context);
    }

}
