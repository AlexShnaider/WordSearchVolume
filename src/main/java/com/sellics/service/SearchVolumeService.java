package com.sellics.service;

import com.sellics.model.SearchVolumeResult;

public interface SearchVolumeService {

    SearchVolumeResult estimate(String word) throws Exception;

    SearchVolumeResult estimateLinear(String word) throws Exception;
}
