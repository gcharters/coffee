package com.sebastian_daschner.coffee_shop.control;

import com.sebastian_daschner.coffee_shop.entity.CoffeeType;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class Barista {

    private Client client;
    private WebTarget target;
    
    @Inject
    @ConfigProperty(name="default.barista.http.port")
    String baristaHttpPort;

    @PostConstruct
    private void initClient() {
        client = ClientBuilder.newClient();
        String url = "http://localhost:" +
                     Integer.parseInt(baristaHttpPort) +
                     "/barista/resources/brews";
        target = client.target(url);
    }

    public void startCoffeeBrew(CoffeeType type) {
        JsonObject requestBody = createRequestBody(type);
        Response response = sendRequest(requestBody);
        validateResponse(response);
    }

    private JsonObject createRequestBody(CoffeeType type) {
        return Json.createObjectBuilder()
                .add("type", type.name().toLowerCase())
                .build();
    }

    private Response sendRequest(JsonObject requestBody) {
        try {
            return target.request().post(Entity.json(requestBody));
        } catch (Exception e) {
            throw new IllegalStateException("Could not start coffee brew, reason: " + e.getMessage(), e);
        }
    }

    private void validateResponse(Response response) {
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL)
            throw new IllegalStateException("Could not start coffee brew, status: " + response.getStatus());
    }

    @PreDestroy
    private void closeClient() {
        client.close();
    }

}
