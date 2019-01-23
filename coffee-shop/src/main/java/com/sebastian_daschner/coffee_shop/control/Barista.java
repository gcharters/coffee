package com.sebastian_daschner.coffee_shop.control;

import com.sebastian_daschner.coffee_shop.entity.CoffeeBrew;
import com.sebastian_daschner.coffee_shop.entity.CoffeeType;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;

@ApplicationScoped
public class Barista {

    URL url;
    
    @Inject
    @ConfigProperty(name="default.barista.http.port")
    String baristaHttpPort;

    @PostConstruct
    private void initClient() {
        try {
            url = new URL("http://localhost:" +
                    Integer.parseInt(baristaHttpPort) +
                    "/barista");
        } catch (NumberFormatException | MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public void startCoffeeBrew(CoffeeBrew brew) {
        // TODO: remove thread when this is fixed - https://github.com/OpenLiberty/open-liberty/issues/6273
        new Thread(() -> {
            try {
                BaristaClient baristaClient = RestClientBuilder.newBuilder()
                    .baseUrl(url)
                    .build(BaristaClient.class);
                Response response = baristaClient.startCoffeeBrew(brew);
                System.out.println("BaristaClient response: " + response.getStatus());
            } catch (IllegalStateException | RestClientDefinitionException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
