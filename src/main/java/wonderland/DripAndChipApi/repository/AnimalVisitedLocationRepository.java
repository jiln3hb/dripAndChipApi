package wonderland.DripAndChipApi.repository;

import org.springframework.data.repository.CrudRepository;
import wonderland.DripAndChipApi.entity.AnimalVisitedLocation;

import java.util.Optional;

public interface AnimalVisitedLocationRepository extends CrudRepository<AnimalVisitedLocation, Long> {

    Optional<AnimalVisitedLocation> findByLocationPointId(Long locPointId);
}
