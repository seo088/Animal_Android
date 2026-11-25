package com.animalloo.data.model;

public class Symptom {

    private final String id;
    private final String name;
    private final String category;

    public Symptom(String id, String name) {
        this(id, name, null);
    }

    public Symptom(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
