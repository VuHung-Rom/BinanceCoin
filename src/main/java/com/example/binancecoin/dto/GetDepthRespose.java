package com.example.binancecoin.dto;

import lombok.Data;

import java.util.Map;

@Data
public class GetDepthRespose {
    Map<Double,Double> bids;
    Map<Double,Double> asks;


}
