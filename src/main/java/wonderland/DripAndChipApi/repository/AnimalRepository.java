package wonderland.DripAndChipApi.repository;

import org.springframework.data.repository.CrudRepository;
import wonderland.DripAndChipApi.entity.Animal;

import java.util.List;

public interface AnimalRepository extends CrudRepository<Animal, Long> {

    List<Animal> findByChipperId(Integer chipperId);
    List<Animal> findByAnimalTypesContains(Long id);
    List<Animal> findByVisitedLocationsContains(Long id);
    List<Animal> findByChippingLocationId(Long id);
}
