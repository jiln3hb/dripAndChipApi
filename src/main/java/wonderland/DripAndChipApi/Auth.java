package wonderland.DripAndChipApi;

import org.springframework.beans.factory.annotation.Autowired;
import wonderland.DripAndChipApi.exceptions.ForbiddenException;
import wonderland.DripAndChipApi.exceptions.UnauthorizedException;
import wonderland.DripAndChipApi.repository.AccountRepository;

import java.util.Base64;

public class Auth {

    private final String regex = "^[a-z0-9,!#$%&'*+/=?^_`{|}~-]+(\\.[a-z0-9,!#$%&'*+/=?^_`{|}~-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.([a-z]{2,})?:.*$";

    @Autowired
    AccountRepository accountRepository;

    public Auth(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void testAuthorization (String loginPass) throws UnauthorizedException, ForbiddenException {
        loginPass = loginPass.split(" ")[1]; // отделение login:password от Base
        String res = new String(Base64.getDecoder().decode(loginPass));
        if (!res.matches(regex) || (accountRepository.findByEmailAndPassword(res.split(":")[0], res.split(":")[1]) == null)) {
            //неправильный формат данных для входа, неверные авторизационные данные
            throw new UnauthorizedException(); //401
        }

    }

    public void testAccountYours (String loginPass, Integer id) throws UnauthorizedException, ForbiddenException {
        loginPass = loginPass.split(" ")[1]; // отделение login:password от Base
        String res = new String(Base64.getDecoder().decode(loginPass));
        if ((!accountRepository.findById(id.longValue()).get().getEmail().equals(res.split(":")[0])
                && !accountRepository.findById(id.longValue()).get().getPassword().equals(res.split(":")[1]))
                && (!res.split(":")[0].split("@")[0].equals("admin") && !res.split(":")[1].equals("admin"))) { //аккаунт администратора
            //обновление не своего аккаунта
            throw  new ForbiddenException();
        }
    }
}