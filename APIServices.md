# Research paper on using multiple API services

# **Introduction**

For the purposes of the MR-Worldwide project, the goal was to build a system that allows the user to obtain key information for travel planning. This includes finding plane tickets, viewing the most famous sights in the destination city and searching for accommodation.
Three independent and specialized API services were used to implement this system:

1. Amadeus API - for searching plane tickets
2. Foursquare - for finding landmarks
3. StayAPI - for accommodation search

Each of these services covers a specific segment of travel, and their combination enables complete functionality.

# Amadeus API - Flight ticket search

The Amadeus API enables obtaining information about available flights between the departure and destination points (eg Belgrade → Paris). It was used to search for flights, display prices, dates and all necessary data for air travel.

Features I used:

- Search flights by destination
- Filter by departure and return date
- Overview of basic information about the flight and price

Amadeus provides accurate information about flights, such as ticket prices, departure time, whether it is a direct flight or if there are layovers. The data was checked and validated with the help of Google Flight.

Before calling the flight finder API, it is necessary to obtain an access token first. We get it by first calling the POST route and passing grant_type, client_id and client_secret. We get client_id and client_secret when we create an account on Amadeus for developers. After that, when we get the token, we send a GET to get the flights.

Request for access_token:

```json
curl --location '[https://test.api.amadeus.com/v1/security/oauth2/token](https://test.api.amadeus.com/v1/security/oauth2/token)' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'client_id=59Gpne0LjgAUzfcZGVs4r9AZNKzr2sov' \
--data-urlencode 'client_secret=Mh2hr18MedaBGG2t'
```

Response for access token:

```json
{
"type": "amadeusOAuth2Token",
"username": "example[@gmail.com](mailto:ooggii05@gmail.com)",
"application_name": "Test",
"client_id": "59Gpne0LjgAUzfcZGVs4r9AZNKzr2sov",
"token_type": "Bearer",
"access_token": "4Ypd9qKGWsYm90QH8HeAdhCpSJAb",
"expires_in": 1799,
"state": "approved",
"scope": ""
}
```

Request for flight search:

[https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode=BEG&destinationLocationCode=PRG&departureDate=2026-03-15&returnDate=2026-03-25&adults=1&max=3](https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode=BEG&destinationLocationCode=PRG&departureDate=2026-03-15&returnDate=2026-03-25&adults=1&max=3)

Response for flight search:

```json
    "data": [
        {
            "type": "flight-offer",
            "id": "1",
            "source": "GDS",
            "instantTicketingRequired": false,
            "nonHomogeneous": false,
            "oneWay": false,
            "isUpsellOffer": false,
            "lastTicketingDate": "2026-03-15",
            "lastTicketingDateTime": "2026-03-15",
            "numberOfBookableSeats": 7,
            "itineraries": [
                {
                    "duration": "PT2H10M",
                    "segments": [
                        {
                            "departure": {
                                "iataCode": "BEG",
                                "terminal": "2",
                                "at": "2026-03-15T17:40:00"
                            },
                            "arrival": {
                                "iataCode": "PRG",
                                "terminal": "1",
                                "at": "2026-03-15T19:50:00"
                            },
                            "carrierCode": "JU",
                            "number": "174",
                            "aircraft": {
                                "code": "AT7"
                            },
                            "operating": {
                                "carrierCode": "JU"
                            },
                            "duration": "PT2H10M",
                            "id": "1",
                            "numberOfStops": 0,
                            "blacklistedInEU": false
                        }
                    ]
                },
                {
                    "duration": "PT1H55M",
                    "segments": [
                        {
                            "departure": {
                                "iataCode": "PRG",
                                "terminal": "1",
                                "at": "2026-03-25T20:30:00"
                            },
                            "arrival": {
                                "iataCode": "BEG",
                                "terminal": "2",
                                "at": "2026-03-25T22:25:00"
                            },
                            "carrierCode": "JU",
                            "number": "175",
                            "aircraft": {
                                "code": "AT7"
                            },
                            "operating": {
                                "carrierCode": "JU"
                            },
                            "duration": "PT1H55M",
                            "id": "8",
                            "numberOfStops": 0,
                            "blacklistedInEU": false
                        }
                    ]
                }
            ],
            "price": {
                "currency": "EUR",
                "total": "196.78"
```

# Foursquare - Finding landmarks

After the user arrives in a certain city, he needs to have recommendations on what he can visit. The Foursquare API was used to find the most famous attractions and locations.

First the user has to create a Service API in order to use it as a token, and after that he has more options that he can use. Sights are classified into categories and can be searched depending on what the user is interested in. An example as well as complete documentation is provided below.

Request:

[https://places-api.foursquare.com/places/search?fsq_category_ids=4d4b7104d754a06370d81259&radius=5000&sort=RATING&ll=44.8125,20.4612](https://places-api.foursquare.com/places/search?fsq_category_ids=4d4b7104d754a06370d81259&radius=5000&sort=RATING&ll=44.8125,20.4612)

Response:

```json
 "results": [
        {
            "fsq_place_id": "4d41b8c490f9224b9397121e",
            "latitude": 44.816782331547365,
            "longitude": 20.460695028305054,
            "categories": [
                {
                    "fsq_category_id": "4bf58dd8d48988d137941735",
                    "name": "Theater",
                    "short_name": "Theater",
                    "plural_name": "Theaters",
                    "icon": {
                        "prefix": "https://ss3.4sqi.net/img/categories_v2/arts_entertainment/performingarts_theater_",
                        "suffix": ".png"
                    }
                }
            ],
            "date_created": "2011-01-27",
            "date_refreshed": "2025-10-19",
            "distance": 477,
            "extended_location": {},
            "link": "/places/4d41b8c490f9224b9397121e",
            "location": {
                "address": "Francuska 3",
                "locality": "Београд",
                "region": "Central Serbia",
                "postcode": "11000",
                "country": "RS",
                "formatted_address": "Francuska 3 (Vase Čarapića), 11000 Београд"
            },
            "name": "National Theatre (Narodno pozorište)",
            "placemaker_url": "https://foursquare.com/placemakers/review-place/4d41b8c490f9224b9397121e",
            "related_places": {
                "children": [
                    {
                        "fsq_place_id": "556de4fb498e82526d393a42",
                        "categories": [
                            {
                                "fsq_category_id": "4bf58dd8d48988d116941735",
                                "name": "Bar",
                                "short_name": "Bar",
                                "plural_name": "Bars",
                                "icon": {
                                    "prefix": "https://ss3.4sqi.net/img/categories_v2/nightlife/pub_",
                                    "suffix": ".png"
                                }
                            },
                            {
                                "fsq_category_id": "4bf58dd8d48988d1e2931735",
                                "name": "Art Gallery",
                                "short_name": "Art Gallery",
                                "plural_name": "Art Galleries",
                                "icon": {
                                    "prefix": "https://ss3.4sqi.net/img/categories_v2/arts_entertainment/artgallery_",
                                    "suffix": ".png"
                                }
                            }
                        ],
                        "name": "Klub Narodnog pozorišta"
                    },
                    {
                        "fsq_place_id": "5eaaae222531f90008ce253d",
                        "categories": [
                            {
                                "fsq_category_id": "4bf58dd8d48988d18f941735",
                                "name": "Art Museum",
                                "short_name": "Art Museum",
                                "plural_name": "Art Museums",
                                "icon": {
                                    "prefix": "https://ss3.4sqi.net/img/categories_v2/arts_entertainment/museum_art_",
                                    "suffix": ".png"
                                }
                            }
                        ],
                        "name": "Museum of the National Theatre"
                    }
                ]
            },
            "social_media": {
                "facebook_id": "171050519623290"
            },
            "tel": "011 3281333",
            "website": "https://www.narodnopozoriste.rs"
        }
```

Docs:

- Category: [https://docs.foursquare.com/data-products/docs/categories](https://docs.foursquare.com/data-products/docs/categories)
- API-s: [https://docs.foursquare.com/fsq-developers-places/reference/place-search](https://docs.foursquare.com/fsq-developers-places/reference/place-search)

# StayAPI - **Accommodation search**

In order for the user to be able to find suitable accommodation in the city he plans to visit, the Stay API was used, which offers a list of hotels, apartments and other types of accommodation.

StayAPI is integrated with Booking, TripAdvisor, Google Hotels... The API offers various functionalities such as: hotel search, hotel contents as well as rooms... If Booking is used, it is first necessary to find the dest_id for the city where the user wants to find accommodation, after which the API is called to find available accommodation in that period.

Request for dest_id:

curl -X GET "[https://api.stayapi.com/v1/booking/destinations/lookup?query=Barcelona](https://api.stayapi.com/v1/booking/destinations/lookup?query=Barcelona)" \
-H "x-api-key: YOUR_API_KEY"

Response for dest_id:

```json
{
  "success": true,
  "query": "Barcelona",
  "normalized_query": "Barcelona, Catalonia, Spain",
  "dest_id": -372490,
  "dest_type": "CITY",
  "suggestions": [],
  "message": "Resolved destination"
}
```

Request for accommodation:

curl -X GET "[https://api.stayapi.com/v1/booking/search?dest_id=-3233180&checkin=2024-06-01&checkout=2024-06-07&adults=2&rooms=1](https://api.stayapi.com/v1/booking/search?dest_id=-3233180&checkin=2024-06-01&checkout=2024-06-07&adults=2&rooms=1)" \
-H "x-api-key: YOUR_API_KEY"

Response for accommodation:

```json
{
  "success": true,
  "data": [
    {
      "hotel_id": 302297,
      "hotel_name": "Banyan Tree Phuket",
      "url": "https://www.booking.com/hotel/th/banyan-tree-phuket.html",
      "image_url": "https://cf.bstatic.com/xdata/images/hotel/square60/495123456.jpg",
      "star_rating": 5,
      "review_score": 8.7,
      "review_count": 1234,
      "review_score_word": "Fabulous",
      "address": "33, 33/27 Moo 4, Srisoonthorn Road Cherngtalay, Amphur Talang, 83110 Bang Tao Beach, Thailand",
      "distance_from_center": 18.2,
      "unit_configuration_label": "Entire villa",
      "min_total_price": 450.50,
      "currency_code": "USD",
      "is_free_cancellable": 1,
      "is_no_prepayment_block": 1,
      "checkin": "2024-06-01",
      "checkout": "2024-06-07"
    },
    {
      "hotel_id": 232813,
      "hotel_name": "The Slate",
      "url": "https://www.booking.com/hotel/th/indigo-pearl.html",
      "image_url": "https://cf.bstatic.com/xdata/images/hotel/square60/678901234.jpg",
      "star_rating": 5,
      "review_score": 8.5,
      "review_count": 987,
      "review_score_word": "Very Good",
      "address": "Nai Yang Beach, 83110 Nai Yang Beach, Thailand",
      "distance_from_center": 22.5,
      "unit_configuration_label": "Deluxe Room",
      "min_total_price": 320.00,
      "currency_code": "USD",
      "is_free_cancellable": 1,
      "is_no_prepayment_block": 0,
      "checkin": "2024-06-01",
      "checkout": "2024-06-07"
    }
  ],
  "pagination": {
    "rows_per_page": 25,
    "current_offset": 0,
    "total_count_with_filters": 1247
  },
  "search_metadata": {
    "dest_id": "-3233180",
    "dest_type": "CITY",
    "search_type": "regular"
  },
  "message": "Successfully retrieved search results",
  "retrieved_at": "2024-01-15T10:30:00Z"
}
```

Docs: [https://stayapi.com/docs](https://stayapi.com/docs)