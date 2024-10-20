package client;

import config.WebClientConfig;
import model.Beer;
import model.BeerPagedList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BeerClientImplTest {

    BeerClientImpl beerClient;

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().webClient());
    }

    @Test
    void testListBeers() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

        BeerPagedList pageList = beerPagedListMono.block();
        assertNotNull(pageList);
        Assertions.assertTrue(pageList.getContent()
                                      .size() > 0);
        System.out.println(pageList.getSize());
    }

    @Test
    void testListBeers_PageSize() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(1, 10, null, null, null);

        BeerPagedList pageList = beerPagedListMono.block();
        assertNotNull(pageList);
        assertEquals(10, pageList.getContent()
                                 .size());
    }

    @Test
    void testListBeers_NoRecord() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(3, 10, null, null, null);

        BeerPagedList pageList = beerPagedListMono.block();
        assertNotNull(pageList);
        assertEquals(0, pageList.getContent()
                                .size());
    }

    private Beer getFirstBearForTesting() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
        BeerPagedList pagedList = beerPagedListMono.block();
        return pagedList.getContent()
                        .get(0);
    }

    @Test
    void testGetBeerById() {
        final var testingBeer = getFirstBearForTesting();

        Mono<Beer> beerMono = beerClient.getBeerById(testingBeer.getId(), true);
        Beer beer = beerMono.block();
        assertEquals(testingBeer.getId(), beer.getId());
        assertEquals(testingBeer.getBeerName(), beer.getBeerName());
        assertEquals(testingBeer.getPrice(), beer.getPrice());
    }

    @Test
    void testGetBeerByUPC() {
        final var testingBeer = getFirstBearForTesting();

        Mono<Beer> beerMono = beerClient.getBeerByUPC(testingBeer.getUpc());
        Beer beer = beerMono.block();
        assertEquals(testingBeer.getId(), beer.getId());
        assertEquals(testingBeer.getUpc(), beer.getUpc());
        assertEquals(testingBeer.getBeerName(), beer.getBeerName());
        assertEquals(testingBeer.getPrice(), beer.getPrice());
    }

    @Test
    void testCreateBeer() {
        String upc = String.valueOf(System.currentTimeMillis());
        Beer beer = Beer.builder()
                        .beerName("Sai Gon Xanh")
                        .beerStyle("LAGER")
                        .upc(upc)
                        .quantityOnHand(1000)
                        .price(new BigDecimal("10.15"))
                        .build();
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.createBeer(beer);
        ResponseEntity response = responseEntityMono.block();
        System.out.println(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testUpdateBeer() {
        Beer current = beerClient.getBeerById(UUID.fromString("e2735cc7-6958-4da9-966e-715dcc1dc859"), false)
                                 .block();
        Beer beer = Beer.builder()
                        .beerName("Sai Gon Do")
                        .beerStyle(current.getBeerStyle())
                        .price(current.getPrice())
                        .upc(String.valueOf(System.currentTimeMillis()))
                        .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeer(current.getId(), beer);
        ResponseEntity response = responseEntityMono.block();
        System.out.println(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteBeer() {
        //Insert a beer for deletion first
        Beer beer = Beer.builder()
                        .beerName("Sai Gon Xanh")
                        .beerStyle("LAGER")
                        .upc(String.valueOf(System.currentTimeMillis()))
                        .quantityOnHand(1000)
                        .price(new BigDecimal("10.15"))
                        .build();
        Mono<ResponseEntity<Void>> dataResponseMono = beerClient.createBeer(beer);
        ResponseEntity dataResponse = dataResponseMono.block();
        String[] items = dataResponse.getHeaders()
                                     .get("Location")
                                     .get(0)
                                     .split("/");
        String id = items[items.length - 1];

        System.out.println("Deletion id: " + id);

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeer(UUID.fromString(id));

        ResponseEntity response = responseEntityMono.block();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteBeerIdNotFound() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeer(UUID.randomUUID());

        assertThrows(WebClientResponseException.class, () -> {
            ResponseEntity<Void> responseEntity = responseEntityMono.block();
            assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        });
    }

    @Test
    void testDeleteBeerWithExceptionHandling() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeer(UUID.randomUUID());

        ResponseEntity<Void> responseEntity = responseEntityMono.onErrorResume(throwable -> {
                                                                    HttpStatusCode status = throwable instanceof WebClientResponseException w ? w.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR;
                                                                    return Mono.just(ResponseEntity.status(status)
                                                                                                   .build());
                                                                })
                                                                .block();

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void functionalTestGetBeerById() throws InterruptedException {
        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        beerClient.listBeers(null, null, null, null, null)
                .map(beerPageList -> beerPageList.getContent().get(0).getId())
                .map(beerId -> beerClient.getBeerById(beerId, false))
                .flatMap(beer -> beer)
                .subscribe(beer -> {
                    beerName.set(beer.getBeerName());
                    assertEquals("Mango Bobs", beer.getBeerName());
                    countDownLatch.countDown();
                });

        countDownLatch.await();
        assertEquals("Mango Bobs", beerName.get());

    }
}