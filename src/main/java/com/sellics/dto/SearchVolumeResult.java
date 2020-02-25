package com.sellics.dto;

public class SearchVolumeResult {

    private String word;
    private int searchVolume;

    public SearchVolumeResult(String word) {
        this.word = word;
        this.searchVolume = -1;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getSearchVolume() {
        return searchVolume;
    }

    public void setSearchVolume(int searchVolume) {
        this.searchVolume = searchVolume;
    }
}
