package wonderland.DripAndChipApi.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wonderland.DripAndChipApi.Auth;
import wonderland.DripAndChipApi.entity.Account;
import wonderland.DripAndChipApi.exceptions.*;
import wonderland.DripAndChipApi.repository.AccountRepository;
import wonderland.DripAndChipApi.repository.AnimalRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Validated
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AnimalRepository animalRepository;

    private final String emailRegex = "^(?!.*@.*@.*$)(?!.*@.*--.*\\..*$)(?!.*@.*-\\..*$)(?!.*@.*-$)(?!@\\.$)((.*)?@.+(\\..{1,11})?)$";

    @GetMapping("/accounts") //возвращает лист аккаунтов при переходе на /accounts
    public Iterable<Account> getAccounts(){
        return accountRepository.findAll();
    }

    @PostMapping(value = "/registration") //регистрация аккаунта
    @ResponseStatus(HttpStatus.CREATED)
    public LinkedHashMap<String, Object> accountRegistration(@RequestBody Map<String,String> accountData,
                                                             @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, ForbiddenException, ConflictException {

        if (accountData.get("firstName") == null || accountData.get("firstName").isBlank() || accountData.get("lastName") == null || accountData.get("lastName").isBlank()
                || accountData.get("email") == null || accountData.get("email").isBlank() || !accountData.get("email").matches(emailRegex) || accountData.get("password") == null
                || accountData.get("password").isBlank()) {
            throw new BadRequestException(); //400
        }

        if (!loginPass.isEmpty()) {
            throw new ForbiddenException();
        }

        if (!accountRepository.findByEmail(accountData.get("email")).isEmpty()) { //если аккаунт с таким email уже существует
            throw new ConflictException(); //409
        }

        Account newAccount = new Account(accountData.get("firstName"), accountData.get("lastName"), accountData.get("email"), accountData.get("password"));
        accountRepository.save(newAccount); //сохранение аккаунта в БД

        LinkedHashMap<String, Object> resData = new LinkedHashMap<>();

        resData.put("id", newAccount.getId());
        resData.put("firstName", newAccount.getFirstName());
        resData.put("lastName", newAccount.getLastName());
        resData.put("email", newAccount.getEmail());

        return resData;
    }

    @GetMapping("/accounts/{id}") //просмотр информации об аккаунте по id
    public LinkedHashMap<String, Object> getAccountById(@PathVariable Integer id,
                                                        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException{

        if (id == null || id <= 0) {
            throw new BadRequestException(); //400
        }

        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); //если в header есть поле Authorization

        Optional<Account> foundAcc = accountRepository.findById(id.longValue());

        if (foundAcc.isEmpty()) {
            throw new NotFoundException(); //404
        }

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", foundAcc.get().getId());
        result.put("firstName", foundAcc.get().getFirstName());
        result.put("lastName", foundAcc.get().getLastName());
        result.put("email", foundAcc.get().getEmail());

        return result;
    }

    @GetMapping("/accounts/search") //поиск аккаунта
    public ArrayList<LinkedHashMap<String, Object>> searchAccount(@RequestParam(required = false) String firstName, @RequestParam(required = false) String lastName,
                                                                  @RequestParam(required = false) String email, @RequestParam(required = false, defaultValue = "0") Integer from,
                                                                  @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                  @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass) throws BadRequestException {

        if (from < 0 || size <= 0) {
            throw new BadRequestException();
        }

        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); //если в header есть поле Authorization

        ArrayList<Account> accList = (ArrayList<Account>) accountRepository.findAll();
        Stream<Account> accStream = accList.stream();

        if (firstName != null) {
            if (!firstName.isEmpty()) {
                accStream = accStream.filter(a -> StringUtils.containsIgnoreCase(a.getFirstName(), firstName));
            }
        }

        if (lastName != null) {
            if (!lastName.isEmpty()) {
                accStream = accStream.filter(a -> StringUtils.containsIgnoreCase(a.getLastName(), lastName));
            }
        }

        if (email != null) {
            if (!email.isEmpty()) {
                accStream = accStream.filter(a -> StringUtils.containsIgnoreCase(a.getEmail().split("@")[0], email.split("@")[0]));
            }
        }

        accList = accStream.sorted(Comparator.comparingInt(Account::getId)).skip(from).limit(size).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<LinkedHashMap<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < accList.size(); i++) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();

            map.put("id", accList.get(i).getId());
            map.put("firstName", accList.get(i).getFirstName());
            map.put("lastName", accList.get(i).getLastName());
            map.put("email", accList.get(i).getEmail());

            result.add(map);
        }

        return result;
    }

   @PutMapping("/accounts/{id}") //изменение данных аккаунта
    public LinkedHashMap<String, Object> updateAccount(@PathVariable Integer id, @RequestBody Map<String, String> accountData,
                                                       @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
           throws BadRequestException, UnauthorizedException, ForbiddenException, ConflictException {

        if (accountRepository.findById(Long.valueOf(id)).isEmpty()) { //аккаунт не найден
            throw new ForbiddenException();//403
        }

        if (id == null || id <= 0 || accountData.get("firstName") == null || accountData.get("firstName").isBlank() || accountData.get("lastName") == null
                || accountData.get("lastName").isBlank() || accountData.get("email") == null || accountData.get("email").isBlank() || !accountData.get("email").matches(emailRegex)
                || accountData.get("password") == null || accountData.get("password").isBlank()) {
             throw new BadRequestException(); //400
        }

        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException();//запрос от неавторизованного аккаунта
        new Auth(accountRepository).testAccountYours(loginPass, id); //изменение не своего аккаунта


        Account updatedAcc = accountRepository.findById(Long.valueOf(id)).get();

        if (!new ArrayList<Account>((Collection) accountRepository.findAll()).stream()
                .filter(mail -> mail.getEmail().equals(accountData.get("email")) && !mail.getId().equals(id)).toList().isEmpty()) {
            throw new ConflictException();
        } else {
            updatedAcc.setFirstName(accountData.get("firstName"));
            updatedAcc.setLastName(accountData.get("lastName"));
            updatedAcc.setEmail(accountData.get("email"));
            updatedAcc.setPassword(accountData.get("password"));

            accountRepository.save(updatedAcc);
        }

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", updatedAcc.getId());
        result.put("firstName", updatedAcc.getFirstName());
        result.put("lastName", updatedAcc.getLastName());
        result.put("email", updatedAcc.getEmail());

        return result;

    }

    @DeleteMapping("/accounts/{id}")
    public void deleteAccount(@PathVariable Integer id, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, ForbiddenException, UnauthorizedException {

        if (id == null || id <= 0) {
            throw new BadRequestException();
        }

        Optional<Account> acc = accountRepository.findById(id.longValue());

        if (acc.isEmpty()) { //аккаунт не найден
            throw new ForbiddenException();
        }

        if (!animalRepository.findByChipperId(id).isEmpty()) { //аккаунт связан с животным
            throw new BadRequestException();
        }

        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //запрос от неавторизованного аккаунта, неверные авторизационные данные
        new Auth(accountRepository).testAccountYours(loginPass,id);

        accountRepository.delete(acc.get());
    }
}