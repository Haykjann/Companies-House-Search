# Companies Search Service

A Spring Boot service that searches UK companies using the Companies House API and returns structured JSON data.

# How to Run

1. Clone the repository using `git clone https://github.com/Haykjann/Companies-House-Search`
2. Get a free API key from the [Companies House Developer Hub](https://developer.company-information.service.gov.uk/get-started/)
3. Add your API key to `src/main/resources/application.properties` instead of the placeholder
4. Start the database: `docker-compose up -d`
5. Start the application: `.\mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run` (Mac/Linux)

The database runs in Docker with its own credentials, no need to change anything database-related. I chose PostgreSQL as the database as I was familiar with it and given the structure of the information on Companies House web site NoSQL would not provide advantages. The database is run via Docker Compose to ensure it runs on any computer without the need of configuring the database.

# API Endpoint

GET /search?query={query}

Optional parameter: 'forceRefresh=true' bypasses the cache and fetches fresh data.

Example request:

```
GET http://localhost:8081/search?query=Apple
GET http://localhost:8081/search?query=Apple&forceRefresh=true
```

Example response (single company):
```json
{
  "address": "85 Great Portland Street, London, England, W1W 7LT",
  "companyNumber": "05588682",
  "companyType": "ltd",
  "fetchedAt": "2026-05-25T03:34:48.690948",
  "incorporatedOn": "2005-10-11",
  "name": "APPLE LTD",
  "status": "dissolved",
  "officers": [
    {
      "name": "KHAN, Waris, Mr.",
      "role": "director",
      "appointedOn": "2013-07-04"
    }
  ],
  "personsWithSignificantControl": [
    {
      "name": "Mr. Waris Khan",
      "natureOfControl": "right-to-appoint-and-remove-directors"
    }
  ]
}
```

# Caching Strategy

When a search request is made, the service first checks whether the database already contains records for that exact query. If records exist and were fetched within the last 24 hours, they are returned directly from the database without making any requests to Companies House. Otherwise, fresh data is fetched from the API and the database is updated.

# What Was Hardest

This was my first practical project using Spring Boot, so getting familiar with the different annotations and understanding how the framework wires everything together took some time. The planning stage was also challenging, deciding on the data model and caching strategy before writing any code required thinking through tradeoffs.

# What I Would Improve With More Time

The caching implementation is basic, it only returns cached results on an exact query match. Searching "Appl" and "Apple" would hit the API twice even though they return overlapping results. I would research fuzzy query matching to make this smarter and minimize the number of API calls.

I would also try to contact Companies House to find out how frequently their data is actually updated, so the 24-hour freshness threshold could be replaced with a more accurate value, minimizing unnecessary API calls.

Additionally, I noticed Companies House has an official REST API which I used instead of scraping HTML. In a production system I would store the API key as an environment variable rather than in a properties file, and add proper structured logging instead of 'System.err'.
