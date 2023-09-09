package com.example.binancecoin.service;

import com.example.binancecoin.dto.GetDepthRespose;
import com.example.binancecoin.dto.GetPice24h;
import com.example.binancecoin.dto.ProductResponse;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface BinanceClient {
    @RequestLine("GET /bapi/asset/v1/public/asset-service/product/get-products?includeEtf=true")
    ProductResponse getProducts();

    @RequestLine("GET api/v3/ticker/24hr?symbol={coin}")
    GetPice24h getMarket24H(@Param("coin") String coin);

    @RequestLine("GET /api/v3/klines?limit={totalRow}&symbol={coin}&interval={dataRange}")
    List<List<String>> getHistory(@Param("totalRow") int totalRow, @Param("coin") String coin, @Param("dataRange") String dataRange);

    @RequestLine("GET /api/v3/depth?symbol={coin}&limit={totalRow}")
    GetDepthRespose getDepth(@Param("coin") String coin, @Param("totalRow") int totalRow);
}
