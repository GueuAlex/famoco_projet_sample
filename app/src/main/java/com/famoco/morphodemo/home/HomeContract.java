package com.famoco.morphodemo.home;

import java.util.Observer;

/**
 * Contract between View and Presenter (MVP Pattern)
 *
 * @version DEMO
 * @author Yoann
 */
public interface HomeContract {

    interface View {

        /**
         * Commit the EnrollFragment
         */
        void processEnroll();

        /**
         * Commit the VerifyFragment
         */
        void processVerify();

        /**
         * Commit the VerifyFragment
         */
        void processImage();

        /**
         * Display error
         */
        void displayNoFingerAvailable();

        /**
         * Setting the firmware information and the product information of th Morpho device in the texts views
         */
        void setMorphoDeviceInfo(String productInfo, String softwareInfo);
    }



    interface Presenter {

        /**
         * Open connection with Morpho device
         */
        void openConnection();

        /**
         * Close connection with Morpho device
         */
        void closeConnection();

        /**
         * Handle actions on child Fragment
         * @param action {ENROLL, VERIFY, IMAGE, HOME}
         */
        void handleInteraction(HomeAction action);

        /**
         * Cancel the connection with Morpho device
         */
        void cancelConnection();

        /**
         * Can rebbot the software when the Morpho device is Attached
         * @param callback
         * @return
         */
        int rebootSoft(Observer callback);

        /**
         * Update product and firmware information about the Morpho device
         */
        void updateMorphoDeviceInfo();
    }
}
