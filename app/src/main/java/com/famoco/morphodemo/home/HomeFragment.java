package com.famoco.morphodemo.home;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.famoco.morphodemo.databinding.FragmentHomeBinding;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @version DEMO
 * @author Yoann
 */
public class HomeFragment extends Fragment {

    /**
     * Constant Tag for debug purpose
     */
    private static final String TAG = HomeFragment.class.getSimpleName();

    /**
     * Listener of interaction inside this Fragment
     */
    private OnFragmentInteractionListener mListener;

    /**
     * Default constructor
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     * Might be useful in the future to pass argument through Bundle of the new Fragment
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        Log.d(TAG, "\t --> new Fragment returned");
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);

        binding.enrollFingerprintLayout.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onHomeFragmentInteraction(HomeAction.ENROLL);
            }
        });

        binding.verifyFingerprintLayout.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onHomeFragmentInteraction(HomeAction.VERIFY);
            }
        });

        binding.imageFingerprintLayout.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onHomeFragmentInteraction(HomeAction.IMAGE);
            }
        });

        binding.rebootSoftLayout.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onHomeFragmentInteraction(HomeAction.REBOOT);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
            Log.d(TAG, "\t--> OnFragmentInteractionListener found : " + mListener);
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onHomeFragmentInteraction(HomeAction action);
    }
}
