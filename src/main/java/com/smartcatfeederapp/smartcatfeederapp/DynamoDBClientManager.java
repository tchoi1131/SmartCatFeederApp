package com.smartcatfeederapp.smartcatfeederapp;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by Tom Wong on 5/13/2017.
 */

public class DynamoDBClientManager {
    private Context context;
    private String identityPoolId;
    private AmazonDynamoDBClient ddb;

    public DynamoDBClientManager() {
        ddb = null;
        context = null;
        identityPoolId = null;
    }

    public DynamoDBClientManager(Context context, String identityPoolId) {
        this.context = context;
        this.identityPoolId = identityPoolId;
        initClients();
    }

    public AmazonDynamoDBClient getDdb() {
        if (ddb == null) {
            initClients();
        }
        return ddb;
    }

    private void initClients() {
        CognitoCachingCredentialsProvider credentials = new CognitoCachingCredentialsProvider(
                context, identityPoolId, Regions.US_WEST_2);

        ddb = new AmazonDynamoDBClient(credentials);
        ddb.setRegion(Region.getRegion(Regions.US_WEST_2));
    }

}
