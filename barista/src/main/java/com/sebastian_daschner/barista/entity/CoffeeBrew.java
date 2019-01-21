package com.sebastian_daschner.barista.entity;

import javax.json.bind.annotation.JsonbProperty;

public class CoffeeBrew {

    private CoffeeType type;
    private OrderStatus status;
    
    @JsonbProperty("_self")
    private String self;
    
    public CoffeeType getType() {
        return type;
    }
    
    public void setType(CoffeeType type) {
        this.type = type;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    @JsonbProperty("_self")
    public String getSelf() {
        return self;
    }
    
    @JsonbProperty("_self")
    public void setSelf(String self) {
        this.self = self;
    }

    
}
