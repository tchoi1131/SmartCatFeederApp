package com.smartcatfeederapp.smartcatfeederapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AnalysisFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private MyAnalysisRecyclerViewAdapter adapter;

    private DynamoDBClientManager ddbClientMgr;
    private ArrayList<AnalysisResult> analysisResults = new ArrayList<AnalysisResult>();

    private EditText fromMonthET;
    private EditText toMonthET;
    private Button refreshBtn;

    private String fromMonthStr;
    private String toMonthStr;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AnalysisFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AnalysisFragment newInstance(int columnCount) {
        AnalysisFragment fragment = new AnalysisFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View outerView = inflater.inflate(R.layout.fragment_analysis_list, container, false);
        View listView = outerView.findViewById(R.id.analysis_list);
        fromMonthET = (EditText) outerView.findViewById(R.id.from_month_et);
        toMonthET = (EditText) outerView.findViewById(R.id.to_month_et);
        refreshBtn = (Button) outerView.findViewById(R.id.refresh_btn);

        ddbClientMgr = new DynamoDBClientManager(getContext().getApplicationContext(),
                getResources().getString(R.string.identity_pool_id));

        // Set the adapter
        if (listView instanceof RecyclerView) {
            Context context = listView.getContext();
            RecyclerView recyclerView = (RecyclerView) listView;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new MyAnalysisRecyclerViewAdapter(analysisResults, mListener);
            recyclerView.setAdapter(adapter);
        }

        fromMonthStr = String.valueOf(fromMonthET.getText()) + "00";
        toMonthStr = String.valueOf(toMonthET.getText()) + "99";
        new GetAnalysisResultsTask().execute();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromMonthStr = String.valueOf(fromMonthET.getText()) + "00";
                toMonthStr = String.valueOf(toMonthET.getText()) + "99";
                new GetAnalysisResultsTask().execute();
            }
        });

        return outerView;
    }

    private class GetAnalysisResultsTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... inputs) {
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClientMgr.getDdb());
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
            eav.put(":feederId", new AttributeValue().withS("SmartCatFeeder"));
            eav.put(":fromDate", new AttributeValue().withN(fromMonthStr));
            eav.put(":toDate", new AttributeValue().withN(toMonthStr));

            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .addExpressionAttributeNamesEntry("#feedDate","date")
                    .withFilterExpression("feederId = :feederId and #feedDate >= :fromDate and #feedDate <= :toDate")
                    .withExpressionAttributeValues(eav);

            PaginatedScanList<DailyConsumption> result = mapper.scan(
                    DailyConsumption.class, scanExpression);

            analysisResults.clear();
            int prevMonth = 0;
            double curCatWeight = 0;
            double numOfDays = 0;
            double consumptionSum = 0;
            double prevCatWeight = 0;
            Boolean firstMonth = true;

            for (DailyConsumption dc : result) {
                int curMonth = dc.getDate()/100;
                curCatWeight = dc.getCatWeight();

                if(firstMonth){
                    prevMonth = curMonth;
                    prevCatWeight = curCatWeight;
                    firstMonth = false;
                }
                if(prevMonth != curMonth) {
                    AnalysisResult analysisResult = new AnalysisResult(prevMonth,consumptionSum / numOfDays,curCatWeight - prevCatWeight);
                    analysisResults.add(analysisResult);
                    consumptionSum = 0;
                    numOfDays = 0;
                    prevCatWeight = curCatWeight;
                    prevMonth = curMonth;
                }

                consumptionSum += dc.getConsumption();
                numOfDays++;
            }

            if(!firstMonth){
                AnalysisResult analysisResult = new AnalysisResult(prevMonth,consumptionSum/numOfDays, curCatWeight - prevCatWeight);
                analysisResults.add(analysisResult);
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(AnalysisResult item);
    }
}
