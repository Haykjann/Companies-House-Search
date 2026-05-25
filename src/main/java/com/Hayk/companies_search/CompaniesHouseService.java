package com.Hayk.companies_search;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CompaniesHouseService {
    private final RestClient restClient;
    private final CompanyRepository companyRepository;

    public CompaniesHouseService(@Value("${companies.api.key}") String apiKey, CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
        this.restClient = RestClient.builder()
            .baseUrl("https://api.company-information.service.gov.uk")
            .defaultHeader("Authorization", "Basic " + 
                java.util.Base64.getEncoder().encodeToString((apiKey + ":").getBytes()))
            .defaultHeader("User-Agent", "companies-search-app/1.0 (contact: hayk_mkrtchyan3@edu.aua.am)")
            .build();
    }

    public CompanySearchResponse searchCompanies(String query) {
        return restClient.get()
                .uri("/search/companies?q={query}&items_per_page=100", query)
                .retrieve()
                .body(CompanySearchResponse.class);
    }

    public OfficerResponse getOfficers(String companyNumber) {
    return restClient.get()
            .uri("/company/{number}/officers", companyNumber)
            .retrieve()
            .body(OfficerResponse.class);
    }

    public PscResponse getPscs(String companyNumber) {
        return restClient.get()
                .uri("/company/{number}/persons-with-significant-control", companyNumber)
                .retrieve()
                .body(PscResponse.class);
    }

    public List<Company> fetchAndBuildCompanies(String query, boolean forceRefresh) {
        List<Company> cached = companyRepository.findBySearchQuery(query);
        if (!forceRefresh && !cached.isEmpty()) {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            boolean isFresh = cached.stream()
                .allMatch(c -> c.getFetchedAt() != null && c.getFetchedAt().isAfter(cutoff));
            if (isFresh) {
                return cached;
            }
        }
        CompanySearchResponse searchResponse = searchCompanies(query);
        
        List<Company> companies = new ArrayList<>();
        
        for (CompanySearchResponse.CompanyItem item : searchResponse.getItems()) {
            try {
                Company company = new Company();
                company.setSearchQuery(query);
                company.setCompanyNumber(item.getCompanyNumber());
                company.setName(item.getTitle());
                company.setStatus(item.getCompanyStatus());
                company.setCompanyType(item.getCompanyType());
                company.setAddress(item.getAddressSnippet());
                
                if (item.getDateOfCreation() != null) {
                    company.setIncorporatedOn(LocalDate.parse(item.getDateOfCreation()));
                }

                OfficerResponse officerResponse = getOfficers(item.getCompanyNumber());
                if (officerResponse.getItems() != null) {
                    List<Officer> officers = officerResponse.getItems().stream()
                        .map(o -> {
                            Officer officer = new Officer();
                            officer.setName(o.getName());
                            officer.setRole(o.getOfficerRole());
                            officer.setCompany(company);
                            if (o.getAppointedOn() != null) {
                                officer.setAppointedOn(LocalDate.parse(o.getAppointedOn()));
                            }
                            return officer;
                        }).collect(Collectors.toList());
                    company.setOfficers(officers);
                }

                PscResponse pscResponse = getPscs(item.getCompanyNumber());
                if (pscResponse.getItems() != null) {
                    List<PersonWithSignificantControl> pscs = pscResponse.getItems().stream()
                        .map(p -> {
                            PersonWithSignificantControl psc = new PersonWithSignificantControl();
                            psc.setName(p.getName());
                            psc.setNatureOfControl(p.getNaturesOfControl() != null ? 
                                String.join(", ", p.getNaturesOfControl()) : null);
                            psc.setCompany(company);
                            return psc;
                        }).collect(Collectors.toList());
                    company.setPersonsWithSignificantControl(pscs);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                companies.add(company);

            } catch (Exception e) {
                System.err.println("Failed to fetch company " + item.getCompanyNumber() + ": " + e.getMessage());
            }
        }

        if (!cached.isEmpty()) {
            companyRepository.deleteAll(cached);
        }
        
        companyRepository.saveAll(companies);
        return companies;
    }
}
