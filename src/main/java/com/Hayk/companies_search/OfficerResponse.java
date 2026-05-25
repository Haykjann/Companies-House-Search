package com.Hayk.companies_search;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OfficerResponse {
    @JsonProperty("items")
    private List<OfficerItem> items;

    @Data
    public static class OfficerItem {
        @JsonProperty("name")
        private String name;

        @JsonProperty("officer_role")
        private String officerRole;

        @JsonProperty("appointed_on")
        private String appointedOn;
    }
}
