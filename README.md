# MUSIFY

API of artist data. Provides a lookup endpoint based on artist id (MBID from [Music Brainz](https://musicbrainz.org/)). It combines
data from different [source applications](#Sources) in a unified entity. After the requests to systems upstream are done, the object
combining the data is cached for a day to reduce load on sources application.

## Running the application

### Requirements

* [Java 11](https://www.oracle.com/in/java/technologies/javase/jdk11-archive-downloads.html)
* [Gradle](https://gradle.org/install/)

### Running

* Run  `./gradlew bootRun` from the root folder of the project.

## Endpoint

* http://localhost:8080/artist/{id} : returns the artist summary as represented in the [API Response section](#API_Response) based
  on the given id ([some sample ids](Sample Artist Ids))
* http://localhost:8080/actuator/health : returns the health status of the application

## Processing flow

* Request come in with id
* Make request to [Music Brainz](#Music_Brainz) with given id
    * Asynchronously request [Cover Art Archive](#Cover_Art_Archive) for each album cover image URL
    * Asynchronously request [Wikidata](#Wikidata) to get id for Wikipedia summary
        * Request [Wikipedia](#Wikipedia) summary

## Comments

### General

* The implementation uses __SpringBoot Webflux__ + __Kotlin Coroutines__ to handles requests in a non-blocking way.

### Cache

* The caching solution was implemented in memory for simplicity using __Caffeine__. In case the application needs to be scaled,
  another approach could be taken (e.i. an external cache system that could be shared between multiple instances, in case the
  application is scaled up). Also, the cache was not tuned in any way (it's simply caching the responses for a day).
    * Good to notice that a simplified version of `decorator` pattern was used in the service layer so the caching and the logic of
      the service are kept separated. The interface [IMusifyService](src/main/kotlin/me/henriquestein/service/IMusifyService.kt)
      gives flexibility to the controller to use either one of the services,
      whereas [CachedMusifyService](src/main/kotlin/me/henriquestein/service/CachedMusifyService.kt) can
      decorate [MusifyService](src/main/kotlin/me/henriquestein/service/MusifyService.kt) with the caching behavior.
    * This approach makes it easier to replace the caching with different implementation; One could think of using more elaborated
      Spring profiles to start the application with the cache service loaded or not.

### Implementation / clean code

* The [MusifyService](src/main/kotlin/me/henriquestein/service/MusifyService.kt) could be refactored into smaller files for improved
  testability and to have single responsibility. That's a trade-off, but again in the context of this exercise I decided to go with
  a single service with all logic in it.

* Some parts of the application could be implemented in more robust ways (e.g. exception handling, timeouts), but for the purpose of
  the exercise I decided to keep it in a simpler way and don't invest more time on that.

### Testing

* For testing, I've opted to use __TestNG__ as library for testing, but ended up regretting a bit later on when I figured Spring
  seems to have better integration with JUnit.
    * The unit test coverage is pretty high (>95%).
    * The integration test has only the happy path ant has the dependencies are mocked. I struggled to create the application
      context in the test using the Spring annotations, so I decided to define the whole application context manually. I suspect
      there were some issues because of __TestNG__, and I decided not to invest more time investigating it.

* __MockWebServer__ is used in some unit tests for the clients as well as in the integration test to mock the responses from source
  systems.

### API Documentation

* __OpenAPI__ documentation could be implemented in combination with some spring modules to provide a UI to interact with the API.
  It improves the experience for consumers of the API.

### Monitoring and Containerization

* __Spring Actuator__ is enabled so the application can be eventually containerised and monitored.

* The application properties being used could be easily injected into a container as environment variables and properly configured
  in case the application would be deployed to multiple environments (e.g. testing, staging, prod)

## API Response

```json
{
  "mbid": "id from MB",
  "name": "name from MB",
  "gender": "nullable gender from MB",
  "country": "country from MB",
  "disambiguation": "disambiguation from MB",
  "description": "description from Wikipedia",
  "albums": [
    {
      "id": "id from MB release-groups",
      "title": "title from MB release-groups",
      "imageUrl": "imageUrl from CAA images.image (front)"
    }
  ]
}
```

## Sample Artist Ids

* Michael Jackson: f27ec8db-af05-4f36-916e-3d57f91ecf5e
* Pennywise: 5c210861-2ce2-4be3-9307-bbcfc361cc01
* Madonna: 79239441-bfd5-4981-a70c-55c3f15c1287
* Red Hot Chilli Pipers: 5d341431-7002-4dcf-9da8-1d25f5bd0ed7

> NOTE: you can search other artists and get their ids using:
> http://musicbrainz.org/ws/2/artist?query=<artist_search_string>&fmt=json
> (replace `<artist_search_string>` by the term you want to search for)

## Sources

### Music Brainz

* API documentation: https://musicbrainz.org/doc/MusicBrainz_API
* Requires `User-Agent` in the request to control request rate.
    * https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting
* Search endpoint to get `MBID` for artists:
    * http://musicbrainz.org/ws/2/artist?query=pennywise&fmt=json
* Lookup endpoint:
    * http://musicbrainz.org/ws/2/artist/f27ec8db-af05-4f36-916e-3d57f91ecf5e?&fmt=json&inc=url-rels+release-groups
    * to request data from Wikidata the response provides:
        * when `relations.type` == `wikidata`; take `url.resource`
        * i.e. https://www.wikidata.org/wiki/Q754051
    * to request data from Cover Art Archive take `release-groups.id` from the response

### Wikidata

* API documentation: https://www.wikidata.org/wiki/Wikidata:Data_access
* API URL: https://www.wikidata.org/wiki/Special:EntityData/Q2831.json
* Sample request: https://www.wikidata.org/wiki/Special:EntityData/Q2831.json
    * from the response take `sitelinks.enwiki.url` and use the string after `/`
    * i.e. https://en.wikipedia.org/wiki/Michael_Jackson

### Wikipedia

* API documentation: https://www.mediawiki.org/wiki/Special:MyLanguage/Wikimedia_REST_API
* API URL: https://en.wikipedia.org/api/rest_v1/page/summary
* Sample request: https://en.wikipedia.org/api/rest_v1/page/summary/Michael_Jackson
    * take from response:
        * `description`
        * `extract` - the doc with requirement mentions the `description` should be used, but the value do no match. It seems in
          fact that it could be concatenated with extract.

### Cover Art Archive

* API documentation: https://wiki.musicbrainz.org/Cover_Art_Archive/API
* API URL: http://coverartarchive.org/
* Sample request: http://coverartarchive.org/release-group/c31a5e2b-0bf8-32e0-8aeb-ef4ba9973932
    * From the response, when `images.front` == `true`; take `image`
