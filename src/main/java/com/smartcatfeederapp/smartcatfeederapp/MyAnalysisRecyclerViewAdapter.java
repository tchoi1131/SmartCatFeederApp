package com.smartcatfeederapp.smartcatfeederapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartcatfeederapp.smartcatfeederapp.AnalysisFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AnalysisResult} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAnalysisRecyclerViewAdapter extends RecyclerView.Adapter<MyAnalysisRecyclerViewAdapter.ViewHolder> {

    private final List<AnalysisResult> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyAnalysisRecyclerViewAdapter(List<AnalysisResult> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_analysis, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mMonthView.setText(String.valueOf(mValues.get(position).getMonth()));
        holder.mAvgConsumptionView.setText(String.valueOf(mValues.get(position).getAvgConsumption()));
        holder.mNetCatWeightView.setText(String.valueOf(mValues.get(position).getNetCatWeight()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMonthView;
        public final TextView mAvgConsumptionView;
        public final TextView mNetCatWeightView;
        public AnalysisResult mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMonthView = (TextView) view.findViewById(R.id.month);
            mAvgConsumptionView = (TextView) view.findViewById(R.id.avg_consumption);
            mNetCatWeightView = (TextView) view.findViewById(R.id.net_cat_weight);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mMonthView.getText() + "'";
        }
    }
}
