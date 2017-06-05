package com.smartcatfeederapp.smartcatfeederapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdHocFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdHocFeedFragment extends Fragment {

    public AdHocFeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdHocFeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdHocFeedFragment newInstance() {
        AdHocFeedFragment fragment = new AdHocFeedFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ad_hoc_feed, container, false);
    }
}
