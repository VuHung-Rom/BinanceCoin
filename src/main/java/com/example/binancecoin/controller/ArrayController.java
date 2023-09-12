package com.example.binancecoin.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("v3/array-list")
public class ArrayController {
    private static List<String> array = new ArrayList<>();

    @PostMapping("/add")
    public void add(@RequestParam String nhap) {
        array.add(nhap);
    }

    @GetMapping("/print")
    public void print() {
        for (String arr : array) {
            System.out.println(arr);
        }
    }

    @PostMapping("/insert")
    public void insert(@RequestParam String value, @RequestParam int index) {
        array.add(index, value);
    }

    @GetMapping("/get")
    public String get(@RequestParam int index) {
        if (index >= 0 && index <= array.size()) {
            return array.get(index);
        }
        return null;
    }

    @GetMapping("/swap")
    public void swap() {
        Collections.reverse(array);
    }

    @GetMapping("/search")
    public Boolean search(@RequestParam String gianhap) {
        return array.contains(gianhap);
    }

    @GetMapping("/clone")
    public List<String> cloner() {
        List<String> copy = new ArrayList<>(array);
        return copy;

    }

    @PostMapping("/compare")
    public boolean compare(@RequestBody List<String> inputList) {
        return array.equals(inputList);
    }

    @PostMapping("/addall")
    public void addAll(@RequestBody List<String> values) {
        array.addAll(values);
    }

    @GetMapping("/sort")
    public List<String> sort() {
        Collections.sort(array);
        return array;
    }
}
