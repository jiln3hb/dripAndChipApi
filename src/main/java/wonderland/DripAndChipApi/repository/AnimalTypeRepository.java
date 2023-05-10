package wonderland.DripAndChipApi.repository;

import org.springframework.data.repository.CrudRepository;
import wonderland.DripAndChipApi.entity.AnimalType;

import java.util.List;

public interface AnimalTypeRepository extends CrudRepository<AnimalType, Long> {

    List<AnimalType> findByType(String type);
}
