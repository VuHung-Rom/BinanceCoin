package com.example.binancecoin.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ProductController {
    private static Map<String, Integer> luuKho = new HashMap<>();

    @PostMapping("/v3/products/insert")
    public void MatHangInsert(@RequestBody ProductRequest productRequest) {
        String product = productRequest.getProduct();
        int quantity = productRequest.getQuantity();
        luuKho.put(product, quantity);

    }

    @DeleteMapping("/v3/products/delete")
    public void DeleteMatHang(@RequestParam String product) {
        luuKho.remove(product);
    }

    @GetMapping("/v3/products/search")
    public List<ProductRequest> searchProduct(@RequestParam String product) {
        return luuKho.entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains(product.toUpperCase()))
                .map(entry -> new ProductRequest(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @PutMapping("/v3/products/consume")
    public void consumeProduct(@RequestParam String product, @RequestParam int consumeAmount) {
        String key = product.toUpperCase();
        if (luuKho.containsKey(product)) {
            int soLuong = luuKho.get(key);
            if (soLuong >= consumeAmount) {
                luuKho.put(key, soLuong - consumeAmount);

            }
        }

    }

    public static class ProductRequest {
        String product;
        int quantity;

        public ProductRequest(String product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

}
