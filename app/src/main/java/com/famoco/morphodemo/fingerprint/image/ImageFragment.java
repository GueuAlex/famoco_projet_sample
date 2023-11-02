package com.famoco.morphodemo.fingerprint.image;

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

import com.famoco.morphodemo.databinding.FragmentImageBinding;
import com.famoco.morphodemo.fingerprint.MorphoFragment;
import com.famoco.morphodemo.utils.Constants;

public class ImageFragment extends MorphoFragment implements ImageContract.View {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = ImageFragment.class.getSimpleName();

    ImageView fingerImage;
    TextView sensorMessage;
    ProgressBar sensorProgress;

    private ImageContract.Presenter presenter;

    /**
     * Default constructor
     */
    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * Might be useful in the future to pass argument through Bundle of the new Fragment
     *
     * @return A new instance of fragment EnrollFragment.
     */
    public static ImageFragment newInstance() {
        Log.d(TAG, "\t --> new Fragment returned");
        return new ImageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentImageBinding binding = FragmentImageBinding.inflate(inflater, container, false);
        fingerImage = binding.fingerImageImage;
        sensorMessage = binding.sensorImageMessage;
        sensorProgress = binding.sensorImageProgress;
        binding.imageHomeButton.setOnClickListener(view -> {
            if(getActivity() != null)
                getActivity().onBackPressed();
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.presenter = new ImagePresenter(this);
        this.presenter.processImageCapture();
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
    public void onImageCaptureCompleted() {
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
