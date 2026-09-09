package com.animalloo.data.model;

public class Symptom {

    private final String id;
    private final String name;

    public Symptom(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
