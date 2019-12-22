package com.example.calmdine.models;

public class Restaurant {
    private String name;
    private double noise;
    private double light;
    private double rating;

    public Restaurant() {
    }

    public Restaurant(String name, double noise, double light, double rating) {
        this.name = name;
        this.noise = noise;
        this.light = light;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getNoise() {
        return noise;
    }

    public void setNoise(double noise) {
        this.noise = noise;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
