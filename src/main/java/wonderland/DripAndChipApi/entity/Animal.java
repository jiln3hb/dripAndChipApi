package wonderland.DripAndChipApi.entity;


import jakarta.persistence.*;

import java.util.*;

@Entity
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ElementCollection
    private Long[] animalTypes;
    private float weight;
    private float length;
    private float height;
    private String gender;
    private String lifeStatus;
    private Date chippingDateTime;
    private int chipperId;
    private long chippingLocationId;
    @ElementCollection
    private Long[] visitedLocations;
    private Date deathDateTime;

    public Animal() {
    }

    public Animal(Long[]  animalTypes, float weight, float length, float height, String gender,
                  int chipperId, long chippingLocationId, Long[] visitedLocations) {
        this.animalTypes = animalTypes;
        this.weight = weight;
        this.length = length;
        this.height = height;
        this.gender = gender;
        this.lifeStatus = "ALIVE"; //при создании животного его жизненный статус по умолчанию ALIVE
        this.chippingDateTime = new Date(); //задаётся текущее время
        this.chipperId = chipperId;
        this.chippingLocationId = chippingLocationId;
        this.visitedLocations = visitedLocations;
        this.deathDateTime = null; //время смерти null пока животное не умрёт
    }

    public long getId() {
        return id;
    }

    public Long[]  getAnimTypes() {
        return animalTypes;
    }

    public float getWeight() {
        return weight;
    }

    public float getLength() {
        return length;
    }

    public float getHeight() {
        return height;
    }

    public String getGender() {
        return gender;
    }

    public String getLifeStatus() {
        return lifeStatus;
    }

    public Date getChippingDateTime() {
        return chippingDateTime;
    }

    public int getChipperId() {
        return chipperId;
    }

    public long getChippingLocationId() {
        return chippingLocationId;
    }

    public Long[] getVisitedLocations() {
        return visitedLocations;
    }

    public Date getDeathDateTime() {
        return deathDateTime;
    }

    public void setAnimalTypes(Long[]  animalTypes) {
        this.animalTypes = animalTypes;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLifeStatus(String lifeStatus) {
        this.lifeStatus = lifeStatus;
    }

    public void setChippingDateTime(Date chippingDateTime) {
        this.chippingDateTime = chippingDateTime;
    }

    public void setChipperId(int chipperId) {
        this.chipperId = chipperId;
    }

    public void setChippingLocationId(long chippingLocationId) {
        this.chippingLocationId = chippingLocationId;
    }

    public void setVisitedLocations(Long[] visitedLocations) {
        this.visitedLocations = visitedLocations;
    }

    public void setDeathDateTime(Date deathDateTime) {
        this.deathDateTime = deathDateTime;
    }
}