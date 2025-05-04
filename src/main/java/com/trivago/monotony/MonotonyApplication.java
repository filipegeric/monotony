/*
  _    _                 _                     _                           __  __
 | |  | |               | |                   | |                         |  \/  |
 | |__| | _____      __ | |_ ___     __ _  ___| |_    _____   _____ _ __  | \  / | ___  _ __   ___
 |  __  |/ _ \ \ /\ / / | __/ _ \   / _` |/ _ \ __|  / _ \ \ / / _ \ '__| | |\/| |/ _ \| '_ \ / _ \
 | |  | | (_) \ V  V /  | || (_) | | (_| |  __/ |_  | (_) \ V /  __/ |    | |  | | (_) | | | | (_) |
 |_|  |_|\___/ \_/\_/    \__\___/   \__, |\___|\__|  \___/ \_/ \___|_|    |_|  |_|\___/|_| |_|\___/
                                     __/ |
                                    |___/

                  _            ___  _  _  _         ___                  _
                 | |__  _  _  | __|(_)| |(_) _ __  | __| __ _  ___  _ _ (_) __
                 | '_ \| || | | _| | || || || '_ \ | _| / _` |/ -_)| '_|| |/ _|
                 |_.__/ \_, | |_|  |_||_||_|| .__/ |___|\__, |\___||_|  |_|\__|
                        |__/                |_|         |___/






























__ This talk is about: __

* Backend/Servers
* Java, Kotlin, and JVM
* Concurrency
* SpringBoot
* Reactive programming




























__ What do servers do these days? __

* Receive requests
* Process requests
* Read from database
* Write to database
* Call third party services
* Log messages

** But most of the time, SERVERS WAIT...

















__ How do they do it? -> Thread per request __

       ___________
  1   |           |
 -->  |  server   | --|a1|--------------------|b1|---->
      |           |
  2   |           |
 -->  |___________| --|a2|--------------------|b2|---->


a = process request and send a query to the database
b = process the data returned from the database and send response

# Imagine you go into a restaurant with many waiters...



























__ Alternative for Threads -> Reactive __

       ___________
  1   |           |
 -->  |  server   |
      |           | --|a1||a2|----------------|b1||b2|---->
  2   |           |
 -->  |___________|


a = process request and send a query to the database
b = process the data returned from the database and send response

# Imagine you go into a restaurant with JUST ONE waiter...



















__ The box is Mono... __

User getUser() {                      ->        Mono<User> getUser() {
    return new User(1, "Filip");      ->            return Mono.just(new User(1, "Filip"));
}                                     ->        }

Mono is a reactive primitive from project reactor (used by SpringBoot webflux)
And there is also Flux...

























ProductDetails getProduct(int id) {                      ->  Mono<ProductDetails> getProduct(int id) {
    var product = getProductById(id);                    ->      return findProductById(id)

    if (product == null) {                               ->          .switchIfEmpty(
        throw new ProductNotFoundException(id);          ->              Mono.error(new ProductNotFoundException(id))
    }                                                    ->          )
                                                         ->          .flatMap(product -> {
    var recommendations =                                ->              var recommendationsMono =
        getRecommendations(product.categoryId());        ->                  findRecommendations(product.categoryId())
                                                         ->                  .collectList();
    return new ProductDetails(product, recommendations); ->              return recommendationsMono.zipWith(Mono.just(product),
                                                         ->                  (recommendationsList, prod) -> new ProductDetails(prod, recommendationsList)
                                                         ->              );
                                                         ->          });
}                                                        ->  }







































__ This turns out to be more performant __

| Language | Framework            | req/sec (64) | req/sec (256) | req/sec (512) |
|----------|----------------------|--------------|---------------|---------------|
| Java     | Vertx (4.5)          | 144.393      | 154.513       | 158.532       |
| Java     | Vertx4web (4.5)      | 132.199      | 143.462       | 146.709       |
| Java     | Spring-Webflux (3.4) | 61.497       | 65.403        | 64.451        |
| Java     | Spring (3.4)*        | 52.513       | 47.846        | 48.424        |

*spring.threads.virtual.enabled=true





















__ Can we have the cake and eat it too? __























*/
package com.trivago.monotony;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonotonyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonotonyApplication.class, args);
    }
}
