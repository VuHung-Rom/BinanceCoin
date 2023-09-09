package com.example.binancecoin.dto;

import lombok.Data;

@Data
public class GetMarket24HRespose {
    String symbol;
    Double priceChange;
    Double priceChangePercent;
    Double weightedAvgPrice;
    Double prevClosePrice;
    Double lastPrice;
    Double lastQty;
    Double bidPrice;
    Double bidQty;
    Double askPrice;
    Double askQty;
    Double openPrice;
    Double highPrice;
    Double lowPrice;
    Double volume;
    Double quoteVolume;
    Long openTime;
    Long closeTime;
    Long firstId;
    Long lastId;
    Long count;

}
