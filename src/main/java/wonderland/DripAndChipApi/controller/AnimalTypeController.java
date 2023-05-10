package wonderland.DripAndChipApi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wonderland.DripAndChipApi.Auth;
import wonderland.DripAndChipApi.entity.AnimalType;
import wonderland.DripAndChipApi.exceptions.BadRequestException;
import wonderland.DripAndChipApi.exceptions.ConflictException;
import wonderland.DripAndChipApi.exceptions.NotFoundException;
import wonderland.DripAndChipApi.exceptions.UnauthorizedException;
import wonderland.DripAndChipApi.repository.AccountRepository;
import wonderland.DripAndChipApi.repository.AnimalRepository;
import wonderland.DripAndChipApi.repository.AnimalTypeRepository;

import java.util.Map;
import java.util.Optional;

@RestController
public class AnimalTypeController {

    @Autowired
    private AnimalTypeRepository animalTypeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @GetMapping("/animals/types/{id}")
    public AnimalType getAnimalTypeById(@PathVariable Long id, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException {

        if (id == null || id <= 0) {
            throw new BadRequestException(); //400
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); //если в header есть поле Authorization

        Optional<AnimalType> animalType = animalTypeRepository.findById(id);

        if (animalType.isEmpty()) {
            throw new NotFoundException(); //404
        } else return animalType.get();
    }

    @PostMapping("/animals/types")
    @ResponseStatus(HttpStatus.CREATED)
    public AnimalType addAnimalType(@RequestBody Map<String,String> type, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, ConflictException {

        if (type.get("type") == null || type.get("type").isBlank()) {
            throw new BadRequestException(); //400
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //если в header есть поле Authorization

        if (!animalTypeRepository.findByType(type.get("type")).isEmpty()) { //такой тип животного уже существует
            throw new ConflictException(); //409
        }

        AnimalType newAnimalType = new AnimalType(type.get("type"));
        animalTypeRepository.save(newAnimalType);

        return animalTypeRepository.findById(newAnimalType.getId()).get();
    }

    @PutMapping("/animals/types/{id}")
    public AnimalType updateAnimalType(@PathVariable Long id, @RequestBody Map<String,String> type,
                                       @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException, ConflictException {

        if (id == null || id <=0 || type.get("type") == null || type.get("type").isBlank()) {
            throw new BadRequestException(); //400
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //если в header есть поле Authorization

        if (animalTypeRepository.findById(id).isEmpty()) { //тип животного с таким id не найден
            throw new NotFoundException(); //404
        }

        if (!animalTypeRepository.findByType(type.get("type")).isEmpty()) { //такой тип животного уже существует
            throw new ConflictException(); //409
        }

        AnimalType updatedAnimalType = animalTypeRepository.findById(id).get();

        updatedAnimalType.setType(type.get("type"));
        animalTypeRepository.save(updatedAnimalType);

        return updatedAnimalType;
    }

    @DeleteMapping("/animals/types/{id}")
    public void deleteAnimalType (@PathVariable Long id, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException {

        if (id == null || id <= 0){
            throw new BadRequestException();
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //если в header есть поле Authorization

        Optional<AnimalType> animalType = animalTypeRepository.findById(id);

        if (animalType.isEmpty()) { //тип с таким id не найден
            throw new NotFoundException();
        }

        if (!animalRepository.findByAnimalTypesContains(id).isEmpty()) { //имеются животные с таким типом
            throw new BadRequestException();
        }

        animalTypeRepository.delete(animalType.get());
    }
}