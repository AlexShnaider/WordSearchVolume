package com.sellics.controller;

import com.sellics.model.SearchVolumeResult;
import com.sellics.service.SearchVolumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchVolumeController {

    private SearchVolumeService service;

    @Autowired
    public SearchVolumeController(SearchVolumeService service) {
        this.service = service;
    }

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/estimate")
    SearchVolumeResult estimate(@RequestParam("word") String word) throws Exception {
        return service.estimate(word);
    }

    @GetMapping("/estimateLinear")
    SearchVolumeResult estimateLinear(@RequestParam("word") String word) throws Exception {
        return service.estimateLinear(word);
    }
}
