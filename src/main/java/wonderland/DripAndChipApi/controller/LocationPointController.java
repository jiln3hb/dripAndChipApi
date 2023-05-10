package wonderland.DripAndChipApi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wonderland.DripAndChipApi.Auth;
import wonderland.DripAndChipApi.entity.AnimalVisitedLocation;
import wonderland.DripAndChipApi.entity.LocationPoint;
import wonderland.DripAndChipApi.exceptions.BadRequestException;
import wonderland.DripAndChipApi.exceptions.ConflictException;
import wonderland.DripAndChipApi.exceptions.NotFoundException;
import wonderland.DripAndChipApi.exceptions.UnauthorizedException;
import wonderland.DripAndChipApi.repository.AccountRepository;
import wonderland.DripAndChipApi.repository.AnimalRepository;
import wonderland.DripAndChipApi.repository.AnimalVisitedLocationRepository;
import wonderland.DripAndChipApi.repository.LocationPointRepository;

import java.util.Map;
import java.util.Optional;

@RestController
public class LocationPointController {

    @Autowired
    LocationPointRepository locPointRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AnimalRepository animalRepository;

    @Autowired
    AnimalVisitedLocationRepository animVisLocRepository;

    @GetMapping("/locations") //возвращает лист аккаунтов при переходе на /accounts
    public Iterable<LocationPoint> getLocPoints(){
        return locPointRepository.findAll();
    }

    @GetMapping("/locations/{id}")
    public LocationPoint getLocPointById(@PathVariable Long id, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException { //получение инфы о точке локации по id

        if (id == null || id <= 0) {
            throw new BadRequestException(); //400
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); //если в header есть поле Authorization

        Optional<LocationPoint> foundLoc = locPointRepository.findById(id);

        if (foundLoc.isPresent()) {
            return foundLoc.get();
        } else throw new NotFoundException(); //404
    }

    @PostMapping("/locations")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationPoint addLocationPoint(@RequestBody Map<String,Double> locPointData,
                                          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, ConflictException { //создание точки локации

        if (locPointData.get("latitude") == null || locPointData.get("latitude") < -90 || locPointData.get("latitude") > 90 || locPointData.get("longitude") == null
                || locPointData.get("longitude") < -180 || locPointData.get("longitude") > 180) {
            throw new BadRequestException(); //400
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //если в header есть поле Authorization

        if (locPointRepository.findByLatitudeAndLongitude(locPointData.get("latitude"), locPointData.get("longitude")) != null)
        { //точка с такими параметрами уже существует
            throw new ConflictException(); //409
        }

        LocationPoint newLocPoint = new LocationPoint(locPointData.get("latitude"), locPointData.get("longitude"));
        locPointRepository.save(newLocPoint);

        return locPointRepository.findById(newLocPoint.getId()).get();
    }

    @PutMapping("locations/{id}")
    public LocationPoint updateLocPoint(@PathVariable Long id, @RequestBody Map<String,Double> locPointData,
                                        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException, ConflictException { //изменение данных точки локации

        if (id == null || id <= 0 ||  locPointData.get("latitude") == null ||  locPointData.get("latitude") < -90 ||  locPointData.get("latitude") > 90 ||
                locPointData.get("longitude") == null ||  locPointData.get("longitude") < -180 || locPointData.get("longitude") > 180) {
            throw new BadRequestException(); //400
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //если в header есть поле Authorization

        if (locPointRepository.findById(id).isEmpty()) { //точка с таким id не найдена
            throw new NotFoundException(); //404
        }

        if (locPointRepository.findByLatitudeAndLongitude(locPointData.get("latitude"), locPointData.get("longitude")) != null) { //точка с такими параметрами уже существует
            throw new ConflictException(); //409
        }

        LocationPoint updatedLocPoint = locPointRepository.findById(id).get();

        updatedLocPoint.setLatitude(locPointData.get("latitude"));
        updatedLocPoint.setLongitude(locPointData.get("longitude"));

        return updatedLocPoint;
    }

    @DeleteMapping("/locations/{id}")
    public void deleteLocPoint(@PathVariable Long id, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException { //удаление точки локации

        if (id == null || id <= 0) {
            throw  new BadRequestException();
        }

        Optional<LocationPoint> locPoint = locPointRepository.findById(id);
        Optional<AnimalVisitedLocation> visLoc = animVisLocRepository.findByLocationPointId(id);

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //если в header есть поле Authorization

        if (locPoint.isEmpty()) {
            throw new NotFoundException();
        }

        if ((visLoc.isPresent() && !animalRepository.findByVisitedLocationsContains(visLoc.get().getId()).isEmpty()) || !animalRepository.findByChippingLocationId(locPoint.get().getId()).stream().toList().isEmpty()) { //точка связана с животным
            throw new BadRequestException();
        }

        locPointRepository.delete(locPoint.get());
    }
}