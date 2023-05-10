package wonderland.DripAndChipApi.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;

@Entity
public class AnimalVisitedLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private Date dateTimeOfVisitLocationPoint;
    private long locationPointId;

    public AnimalVisitedLocation() {
    }

    public AnimalVisitedLocation(Date dateTimeOfVisitLocationPoint, long locationPointId) {
        this.dateTimeOfVisitLocationPoint = new Date();
        this.locationPointId = locationPointId;
    }

    public long getId() {
        return id;
    }

    public Date getDateTimeOfVisitLocationPoint() {
        return dateTimeOfVisitLocationPoint;
    }

    public void setDateTimeOfVisitLocationPoint(Date dateTimeOfVisitLocationPoint) {
        this.dateTimeOfVisitLocationPoint = dateTimeOfVisitLocationPoint;
    }

    public long getLocationPointId() {
        return locationPointId;
    }

    public void setLocationPointId(long locationPointId) {
        this.locationPointId = locationPointId;
    }
}