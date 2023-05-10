package wonderland.DripAndChipApi.repository;

import org.springframework.data.repository.CrudRepository;
import wonderland.DripAndChipApi.entity.Account;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Long> {
    List<Account> findByEmail(String email);

    Account findByEmailAndPassword(String email, String password);

}
