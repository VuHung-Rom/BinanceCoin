package com.example.binancecoin.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
@Data
public class ProductDetail {

    @SerializedName("s")
    String tokenName;
    @SerializedName("b")
    String firstCoin;
    @SerializedName("q")
    String secondCoin;


}
