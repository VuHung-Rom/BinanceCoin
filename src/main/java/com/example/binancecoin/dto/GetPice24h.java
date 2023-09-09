package com.example.binancecoin.dto;

import lombok.Data;

@Data
public class GetPice24h {
    String code;
    String message;
    Double priceChangePercent;
}
