package com.smartcatfeederapp.smartcatfeederapp;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdHocFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdHocFeedFragment extends Fragment {

    private static final String SET_SHADOW_TOPIC = "$aws/things/SmartCatFeeder/shadow/update";
    private static final String GET_SHADOW_TOPIC = "$aws/things/SmartCatFeeder/shadow/get";
    private static final String GET_SHADOW_ACCEPTED_TOPIC = "$aws/things/SmartCatFeeder/shadow/get/accepted";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "AndroidPubSubIotPolicy";

    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";
    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_WEST_2;

    private CognitoCachingCredentialsProvider credentialsProvider;;
    private DynamoDBClientManager ddbClientMgr;
    private AWSIotMqttManager mqttManager;
    private AWSIotClient mIotAndroidClient;
    private String keystorePath;
    private String keystoreName;
    private String keystorePassword;
    private KeyStore clientKeyStore = null;
    private String certificateId;

    private TextView feedingAmtTV;
    private EditText addfeedingAmtET;
    private Button feedBtn;
    private Button refreshBtn;

    private DailyFeedingAmount dailyFeedingAmount;
    private boolean connected = false;

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
        View view = inflater.inflate(R.layout.fragment_ad_hoc_feed, container, false);
        feedingAmtTV = (TextView) view.findViewById(R.id.feeding_amt_tv);
        feedBtn = (Button) view.findViewById(R.id.feed_btn);
        refreshBtn = (Button) view.findViewById(R.id.refresh_btn);
        addfeedingAmtET = (EditText) view.findViewById(R.id.add_feedin_amt_et);

        ddbClientMgr = new DynamoDBClientManager(getContext().getApplicationContext(),
                getResources().getString(R.string.identity_pool_id));

        feedingAmtTV.setText("0");
        new GetFeedingAmountTask().execute();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetFeedingAmountTask().execute();
            }
        });

        feedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new SetFeedingAmountTask().execute();
                if(addfeedingAmtET.getText() != null && String.valueOf(addfeedingAmtET.getText()) != ""){
                    connectToIot();
                    while(connected == false){
                    }
                    subscribe(GET_SHADOW_ACCEPTED_TOPIC);
                    publish(GET_SHADOW_TOPIC, "");
                }
            }
        });

        return view;
    }

    private boolean connectToIot(){
        if(mqttManager!=null){
            mqttManager.disconnect();
        }
        mqttManager = new AWSIotMqttManager("CatFeederApp", getString(R.string.iot_end_point));
        mqttManager.setKeepAlive(10);

        try {
            // load keystore from file into memory to pass on
            // connection

            setupClientKeyStore();
            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    if (status == AWSIotMqttClientStatus.Connected) {
                        connected = true;
                    }
                    else if (status == AWSIotMqttClientStatus.ConnectionLost){
                        connected = false;
                    }
                }
            });
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    private void setupClientKeyStore(){
        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getContext(), // context
                getString(R.string.identity_pool_id), // Identity Pool ID
                MY_REGION // Region
        );

        Region region = Region.getRegion(MY_REGION);
        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        keystorePath = getActivity().getFilesDir().getPath();
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
                } else {
                }
            } else {
            }
        } catch (Exception e) {
        }

        if (clientKeyStore == null) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application
                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.
                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);
                    } catch (Exception e) {
                    }
                }
            }).start();
        }
    }

    private void publish(String topic, String msg){
        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
        }
    }

    private void subscribe(String topic){
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            try {
                                String message = new String(data, "UTF-8");

                                if(topic.equals(GET_SHADOW_ACCEPTED_TOPIC)){
                                    //update value;
                                    JSONObject reader = new JSONObject(message);
                                    JSONObject desiredState = reader.getJSONObject("state").getJSONObject("desired");
                                    desiredState.put("addFoodWeight",Double.parseDouble(String.valueOf(addfeedingAmtET.getText())));
                                    String temp = reader.toString();
                                    publish(SET_SHADOW_TOPIC,reader.toString());
                                }
                            } catch (UnsupportedEncodingException e) {
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
        }
    }

    private class GetFeedingAmountTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... inputs) {
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClientMgr.getDdb());
            Calendar today = Calendar.getInstance();
            int date = today.get(Calendar.YEAR) * 10000 + (today.get(Calendar.MONTH) + 1) * 100
                    + today.get(Calendar.DAY_OF_MONTH);

            dailyFeedingAmount = mapper.load(DailyFeedingAmount.class, "SmartCatFeeder", date);

            return null;
        }

        protected void onPostExecute(Void result) {
            if(dailyFeedingAmount != null) {
                feedingAmtTV.setText(String.valueOf(dailyFeedingAmount.getFeedingAmount()));
            }
        }
    }
}
