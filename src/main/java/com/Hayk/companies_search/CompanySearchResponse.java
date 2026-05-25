package com.Hayk.companies_search;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CompanySearchResponse {
    
    @JsonProperty("items")
    private List<CompanyItem> items;
    
    @Data
    public static class CompanyItem {

        @JsonProperty("company_number")
        private String companyNumber;

        @JsonProperty("title")
        private String title;

        @JsonProperty("company_status")
        private String companyStatus;

        @JsonProperty("company_type")
        private String companyType;

        @JsonProperty("date_of_creation")
        private String dateOfCreation;

        @JsonProperty("address_snippet")
        private String addressSnippet;
    }
}
