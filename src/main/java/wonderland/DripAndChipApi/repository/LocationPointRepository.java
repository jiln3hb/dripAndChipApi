package wonderland.DripAndChipApi.repository;

import org.springframework.data.repository.CrudRepository;
import wonderland.DripAndChipApi.entity.LocationPoint;

public interface LocationPointRepository extends CrudRepository<LocationPoint, Long> {

    LocationPoint findByLatitudeAndLongitude(double latitude, double longitude);
}
