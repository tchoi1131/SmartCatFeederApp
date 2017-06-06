package com.smartcatfeederapp.smartcatfeederapp;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Tom Wong on 6/5/2017.
 */
@DynamoDBTable(tableName = "CatFeederDailyFeedingAmount")
public class DailyFeedingAmount {
    private String feederId;
    private int date;
    private double feedingAmount;

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

    @DynamoDBAttribute(attributeName = "feedingAmount")
    public double getFeedingAmount() {
        return feedingAmount;
    }

    public void setFeedingAmount(double feedingAmount) {
        this.feedingAmount = feedingAmount;
    }
}
