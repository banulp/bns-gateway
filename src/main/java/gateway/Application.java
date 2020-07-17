package gateway;

import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// tag::code[]
@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // tag::route-locator[]
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
        String httpUri = uriConfiguration.getHttpbin();
        return builder.routes()
                .route(p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(httpUri))
                .route(p -> p
                        .path("/hello/helloj")
                        //.filters(f -> f.addRequestHeader("Hello", "World"))
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .modifyResponseBody(Map.class, Map.class,
                                        (exchange, s) -> {
                                            Map<String, Object> m = new HashMap<>();
                                            m.put("charactername", s.get("name"));
                                            m.put("words", s.get("say"));
                                            m.put("target", s.get("to"));
                                            return Mono.just(m);
                                        }
                                ))
                        .uri("http://localhost:8080/hello/helloj"))
                .route(p -> p
                        .path("/hello/hellom")
                        //.filters(f -> f.addRequestHeader("Hello", "World"))
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .modifyResponseBody(Map.class, Map.class,
                                        (exchange, s) -> {
                                            Map<String, Object> m = new HashMap<>();
                                            m.put("charactername", s.get("name"));
                                            m.put("words", s.get("say"));
                                            m.put("target", s.get("to"));

                                            List<Map<String, Object>> lm = (List<Map<String, Object>>)s.get("lm");
                                            Map<String, Object> lmm = lm.get(1);
                                            m.put("username", lmm.get("username2"));

                                            return Mono.just(m);
                                        }
                                ))
                        .uri("http://localhost:8080/hello/hellom"))
                .route(p -> p
                        .path("/hello")
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .modifyResponseBody(String.class, String.class,
                                        (exchange, s) -> {
                                            return Mono.just(s.toUpperCase());
                                        }
                                ))
                        .uri("http://localhost:8080/hello"))
                .route(p -> p
                        .host("*.hystrix.com")
                        .filters(f -> f
                                .hystrix(config -> config
                                        .setName("mycmd")
                                        .setFallbackUri("forward:/fallback")))
                        .uri(httpUri))
                .build();
    }
    // end::route-locator[]

    // tag::fallback[]
    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }
    // end::fallback[]
}

// tag::uri-configuration[]
@ConfigurationProperties
class UriConfiguration {

    private String httpbin = "http://httpbin.org:80";

    public String getHttpbin() {
        return httpbin;
    }

    public void setHttpbin(String httpbin) {
        this.httpbin = httpbin;
    }
}
// end::uri-configuration[]
// end::code[]