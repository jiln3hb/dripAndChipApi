package wonderland.DripAndChipApi;

import java.util.ArrayList;

public class AnimalDTO {

    private ArrayList<Long> animalTypes;

    private Float weight;

    private Float length;

    private Float height;

    private String gender;

    private String lifeStatus;

    private Integer chipperId;

    private Long chippingLocationId;

    public AnimalDTO() {

    }

    public AnimalDTO(ArrayList<Long> animalTypes, Float weight, Float length, Float height, String gender, Integer chipperId, Long chippingLocationId) {
        this.animalTypes = animalTypes;
        this.weight = weight;
        this.length = length;
        this.height = height;
        this.gender = gender;
        this.chipperId = chipperId;
        this.chippingLocationId = chippingLocationId;
    }

    public AnimalDTO(Float weight, Float length, Float height, String gender, String lifeStatus, Integer chipperId, Long chippingLocationId) {
        this.weight = weight;
        this.length = length;
        this.height = height;
        this.gender = gender;
        this.chipperId = chipperId;
        this.chippingLocationId = chippingLocationId;
    }

    public ArrayList<Long> getAnimalTypes() {
        return animalTypes;
    }

    public void setAnimalTypes(ArrayList<Long> animalTypes) {
        this.animalTypes = animalTypes;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Float getLength() {
        return length;
    }

    public void setLength(Float length) {
        this.length = length;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLifeStatus() {
        return lifeStatus;
    }

    public void setLifeStatus(String lifeStatus) {
        this.lifeStatus = lifeStatus;
    }

    public Integer getChipperId() {
        return chipperId;
    }

    public void setChipperId(Integer chipperId) {
        this.chipperId = chipperId;
    }

    public Long getChippingLocationId() {
        return chippingLocationId;
    }

    public void setChippingLocationId(Long chippingLocationId) {
        this.chippingLocationId = chippingLocationId;
    }

}