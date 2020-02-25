package com.sellics.service;

import com.sellics.model.Suggestion;
import com.sellics.model.SuggestionsResponse;
import com.sellics.model.SearchVolumeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class SearchVolumeServiceImpl implements SearchVolumeService {

    private static final int WIDGET_WEIGHT_KOEF = 2;
    private static final int WIDGET_SUGGESTIONS_LENGTH = 11;
    private static final int INITIAL_WEIGHT_KOEF = 5;
    private static final double NON_LINEAR_NORM_KOEF = 0.83;
    private static final double LINEAR_NORM_KOEF = 2.27;

    private RestTemplate restTemplate;


    @Autowired
    public SearchVolumeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calculate search-volume of a given {@code word} based on Amazon suggestions.
     *
     * <p>Search-volume is calculated as sum of part search-volume
     * ({@link #getPartSearchVolume(String, int) getPartSearchVolume}) for the {@code prefixNum}
     * in set {@code {1,2,3,4}}, where {@code prefixNum = 4} means the whole word.
     * Each {@code prefixNum} carry different weight, lower {@code prefixNum} - higher weight.
     *
     * <p>Search-volume is normalized by {@value NON_LINEAR_NORM_KOEF},
     * which set the max return value to  {@code 100}
     *
     * @param  word
     *         the word for which is search-volume calculated.
     *
     * @return search-volume of given {@code word} in range 0..100.
     */
    @Override
    public SearchVolumeResult estimate(String word) throws Exception {
        SearchVolumeResult result = new SearchVolumeResult(word);
        int volume = getPartSearchVolume(word, 1);
        volume += getPartSearchVolume(word, 2);
        volume += getPartSearchVolume(word, 3);
        //whole word
        volume += getPartSearchVolume(word, 4);
        //normalization
        volume = (int) Math.ceil(volume * NON_LINEAR_NORM_KOEF);
        result.setSearchVolume(volume);
        return result;
    }

    /**
     * Calculate search-volume of a given {@code word} based on Amazon suggestions.
     *
     * <p>Search-volume is calculated as sum of part search-volume
     * ({@link #getPartSearchVolumeLinear(String, int)}  getPartSearchVolumeLinear}) for the {@code prefixNum}
     * in set {@code {1,2,3,4}}, where {@code prefixNum = 4} means the whole word.
     * Each {@code prefixNum} carry same weight.
     *
     * <p>Search-volume is normalized by {@value LINEAR_NORM_KOEF},
     * which set the max return value to  {@code 100}
     *
     * @param  word
     *         the word for which is search-volume calculated.
     *
     * @return search-volume of given {@code word} in range 0..100.
     */
    @Override
    public SearchVolumeResult estimateLinear(String word) throws Exception {
        SearchVolumeResult result = new SearchVolumeResult(word);
        int volume = getPartSearchVolumeLinear(word, 1);
        volume += getPartSearchVolumeLinear(word, 2);
        volume += getPartSearchVolumeLinear(word, 3);
        //full word
        volume += getPartSearchVolumeLinear(word, 4);
        //normalization
        volume = (int) Math.ceil(volume * LINEAR_NORM_KOEF);
        result.setSearchVolume(volume);
        return result;
    }

    /**
     * Calculate part of search-volume of a given {@code word} based on Amazon suggestions
     * for the first {@code prefixNum} symbols of the {@code word}.
     *
     * <p>Suggestions of a lower word prefix is carrying more weight in a result search-volume
     * and calculated as {@value #INITIAL_WEIGHT_KOEF} - {@code prefixNum}.
     * 
     * <p>Suggestion for the widget (first suggestion of all suggestions 
     * if the size of the list equals {@value WIDGET_SUGGESTIONS_LENGTH})  
     * is multiplied by {@value #WIDGET_WEIGHT_KOEF}.
     *
     * @param  word
     *         the word for which is search-volume calculated.
     *
     * @param  prefixNum
     *         Amount of the symbols from the beginning of the {@code word} for which is Amazon suggestions requested.
     *         If prefixNum higher then {@code word} size, suggestions is requested for whole {@code word}.
     *
     * @return  part of a search-volume of given {@code word}.       
     */
    private int getPartSearchVolume(String word, int prefixNum) throws Exception {
        List<Suggestion> suggestions = getAmazonSuggestions(word, prefixNum);
        int volume = 0;
        if (suggestions.size() == WIDGET_SUGGESTIONS_LENGTH
                && suggestions.get(0).getValue().split(" ", 2)[0].equals(word)) {
            volume = WIDGET_WEIGHT_KOEF * (INITIAL_WEIGHT_KOEF - prefixNum);
            suggestions.remove(0);
        }
        for (Suggestion suggestion : suggestions) {
            if (suggestion.getValue().split(" ", 2)[0].equals(word)) {
                volume += INITIAL_WEIGHT_KOEF - prefixNum;
            }
        }
        return volume;
    }

    /**
     * Calculate part of search-volume of a given {@code word} based on Amazon suggestions
     * for the first {@code prefixNum} symbols of the {@code word}.
     *
     * @param  word
     *         the word for which is search-volume calculated.
     *
     * @param  prefixNum
     *         Amount of the symbols from the beginning of the {@code word} for which is Amazon suggestions requested.
     *         If prefixNum higher then {@code word} size, suggestions is requested for whole {@code word}.
     *
     * @return  part of a search-volume of given {@code word}.
     */
    private int getPartSearchVolumeLinear(String word, int prefixNum) throws Exception {
        return (int) getAmazonSuggestions(word, prefixNum).stream()
                .filter(s -> s.getValue().split(" ", 2)[0].equals(word)).count();
    }

    private List<Suggestion> getAmazonSuggestions(String word, int prefixNum) throws Exception {
        String url = "https://completion.amazon.com/api/2017/suggestions";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                // Add query parameter
                .queryParam("session-id", "133-2360562-2568702")
                .queryParam("customer-id", "A2VCUQ5JYOOPZP")
                .queryParam("request-id", "TP165XCXDXNK73KM312B")
                .queryParam("mid", "ATVPDKIKX0DER")
                .queryParam("alias", "aps")
                .queryParam("suggestion-type", "KEYWORD")
                .queryParam("suggestion-type", "WIDGET")
                .queryParam("page-type", "Search")
                .queryParam("lop", "en_US")
                .queryParam("site-variant", "desktop")
                .queryParam("client-info", "amazon-search-ui")
                .queryParam("b2b", 0)
                .queryParam("fresh", 0);
        if (word.length() > prefixNum && prefixNum < 4) {
            builder.queryParam("prefix", word.substring(0, prefixNum));
        } else {
            builder.queryParam("prefix", word);
        }
        ResponseEntity<SuggestionsResponse> response =
                restTemplate.getForEntity(builder.toUriString(), SuggestionsResponse.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody().getSuggestions() != null) {
            return response.getBody().getSuggestions();
        } else {
            throw new Exception("Couldn't calculate word volume");
        }
    }
}
