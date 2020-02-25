package com.sellics.model;

public class SearchVolumeResult {

    private String word;
    private int searchVolume;

    public SearchVolumeResult(String word) {
        this.word = word;
        this.searchVolume = -1;
    }

    public void setSearchVolume(int searchVolume) {
        this.searchVolume = searchVolume;
    }
}
