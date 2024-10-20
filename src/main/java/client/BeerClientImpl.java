package client;

import config.WebClientProperties;
import model.Beer;
import model.BeerPagedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
public class BeerClientImpl implements BeerClient {

    private final WebClient webClient;

    @Autowired
    public BeerClientImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Beer> getBeerById(UUID id, Boolean showInventoryOnHand) {
        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH_WITH_ID)
                                                     .queryParamIfPresent("showInventoryOnHand", Optional.ofNullable(showInventoryOnHand))
                                                     .build(id.toString()))
                        .retrieve()
                        .bodyToMono(Beer.class);
    }

    @Override
    public Mono<Beer> getBeerByUPC(String upc) {
        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH_WITH_UPC)
                                                     .build(upc))
                        .retrieve()
                        .bodyToMono(Beer.class);
    }

    @Override
    public Mono<BeerPagedList> listBeers(Integer pageNumber, Integer pageSize,
                                         String beerName, String beerStyle,
                                         Boolean showInventoryOnHand) {
        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH)
                                                     .queryParamIfPresent("pageNumber", Optional.ofNullable(pageNumber))
                                                     .queryParamIfPresent("pageSize", Optional.ofNullable(pageSize))
                                                     .queryParamIfPresent("beerName", Optional.ofNullable(beerName))
                                                     .queryParamIfPresent("beerStyle", Optional.ofNullable(beerStyle))
                                                     .queryParamIfPresent("showInventoryOnHand", Optional.ofNullable(showInventoryOnHand))
                                                     .build())
                        .retrieve()
                        .bodyToMono(BeerPagedList.class);
    }

    @Override
    public Mono<ResponseEntity<Void>> createBeer(Beer beer) {
        return webClient.post()
                        .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH)
                                                     .build())
                        .body(BodyInserters.fromValue(beer))
                        .retrieve()
                        .toBodilessEntity();
    }

    @Override
    public Mono<ResponseEntity<Void>> updateBeer(UUID id, Beer beerToUpdate) {
        return webClient.put()
                        .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH_WITH_ID)
                                                     .build(id))
                        .body(BodyInserters.fromValue(beerToUpdate))
                        .retrieve()
                        .toBodilessEntity();
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteBeer(UUID id) {
        return webClient.delete()
                        .uri(uriBuilder -> uriBuilder.path(WebClientProperties.BEER_V1_PATH_WITH_ID)
                                                     .build(id))
                        .retrieve()
                        .toBodilessEntity();
    }


}
