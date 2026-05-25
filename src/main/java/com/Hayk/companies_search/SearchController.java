package com.Hayk.companies_search;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    private final CompaniesHouseService companiesHouseService;

    public SearchController(CompaniesHouseService companiesHouseService) {
        this.companiesHouseService = companiesHouseService;
    }
    
    @GetMapping("/search")
    public List<Company> search(@RequestParam String query,
                                @RequestParam(defaultValue = "false") boolean forceRefresh) {
        return companiesHouseService.fetchAndBuildCompanies(query, forceRefresh);
    }
}
