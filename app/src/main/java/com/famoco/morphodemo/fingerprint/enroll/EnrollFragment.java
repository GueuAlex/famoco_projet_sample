package com.famoco.morphodemo.fingerprint.enroll;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.famoco.morphodemo.databinding.FragmentEnrollBinding;
import com.famoco.morphodemo.fingerprint.MorphoFragment;
import com.famoco.morphodemo.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EnrollFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @version DEMO
 * @author Yoann
 */
public class EnrollFragment extends MorphoFragment implements EnrollContract.View {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = EnrollFragment.class.getSimpleName();

    ImageView fingerImage;
    TextView sensorMessage;
    ProgressBar sensorProgress;

    private EnrollContract.Presenter presenter;

    /**
     * Default constructor
     */
    public EnrollFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * Might be useful in the future to pass argument through Bundle of the new Fragment
     *
     * @return A new instance of fragment EnrollFragment.
     */
    public static EnrollFragment newInstance() {
        Log.d(TAG, "\t --> new Fragment returned");
        return new EnrollFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentEnrollBinding binding = FragmentEnrollBinding.inflate(inflater, container, false);
        fingerImage = binding.fingerEnrollImage;
        sensorMessage = binding.sensorEnrollMessage;
        sensorProgress = binding.sensorEnrollProgress;
        binding.enrollHomeButton.setOnClickListener(view -> {
            if(getActivity() != null)
                getActivity().onBackPressed();
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.presenter = new EnrollPresenter(this);
        this.presenter.processFingerCapture();
    }

    @Override
    public void alert(int errorCode, int internalError) {
        super.alert(errorCode, internalError, "ENROLL");
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.stopProcess();
    }

    @Override
    public void updateSensorMessage(String commandMessage) {
        this.sensorMessage.setText(commandMessage);
    }

    @Override
    public void updateImage(Bitmap imageBmp) {
        this.fingerImage.setImageBitmap(imageBmp);
    }

    @Override
    public void onCaptureCompleted() {
        Log.d(TAG, "onCaptureCompleted");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getActivity() != null)
                    getActivity().onBackPressed();
            }
        }, Constants.DELAY_DIALOG_DISMISS);
    }

    @Override
    public void updateSensorProgressBar(Integer progress) {
        updateProgress(this.sensorProgress, progress);
    }

}
