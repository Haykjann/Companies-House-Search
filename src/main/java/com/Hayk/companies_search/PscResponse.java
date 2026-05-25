package com.Hayk.companies_search;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PscResponse {
    @JsonProperty("items")
    private List<PscItem> items;

    @Data
    public static class PscItem {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("natures_of_control")
        private List<String> naturesOfControl;
    }
}
