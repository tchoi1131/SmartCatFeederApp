package com.smartcatfeederapp.smartcatfeederapp;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Tom Wong on 5/13/2017.
 */

@DynamoDBTable(tableName = "CatFeederDailyConsumption")
public class DailyConsumption {
    private String feederId;
    private int date;
    private double consumption;

    @DynamoDBHashKey(attributeName = "feederId")
    public String getFeederId() {
        return feederId;
    }

    public void setFeederId(String feederId) {
        this.feederId = feederId;
    }

    @DynamoDBRangeKey(attributeName = "date")
    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    @DynamoDBAttribute(attributeName = "dailyConsumption")
    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }
}
