package wonderland.DripAndChipApi.controller;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wonderland.DripAndChipApi.Auth;
import wonderland.DripAndChipApi.entity.Animal;
import wonderland.DripAndChipApi.entity.AnimalVisitedLocation;
import wonderland.DripAndChipApi.entity.LocationPoint;
import wonderland.DripAndChipApi.exceptions.*;
import wonderland.DripAndChipApi.repository.AccountRepository;
import wonderland.DripAndChipApi.repository.AnimalRepository;
import wonderland.DripAndChipApi.repository.AnimalVisitedLocationRepository;
import wonderland.DripAndChipApi.repository.LocationPointRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class AnimalVisitedLocationController {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AnimalRepository animalRepository;

    @Autowired
    LocationPointRepository locPointRepository;

    @Autowired
    AnimalVisitedLocationRepository animVisLocRepository;

    @GetMapping("/animals/{id}/locations") //поиск
    public ArrayList<AnimalVisitedLocation> getLocPointsVisitedByAnimal (@PathVariable Long id,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDateTime,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDateTime,
                                                                         @RequestParam(required = false, defaultValue = "0") Integer from,
                                                                         @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                         @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException {

        if (id == null || id <= 0 || (from != null && from < 0) || (size != null && size <= 0)) {
            throw new BadRequestException();
        }

        //неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); //если в header есть поле Authorization

        if (animalRepository.findById(id).isEmpty()) { //животное с таким id не найдено
            throw new NotFoundException();
        }

        ArrayList<Long> animVisLocsIds = Arrays.stream(animalRepository.findById(id).get().getVisitedLocations()).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<AnimalVisitedLocation> animVisLocsList = new ArrayList<>();
        animVisLocRepository.findAllById(animVisLocsIds).iterator().forEachRemaining(animVisLocsList::add);
        Stream<AnimalVisitedLocation> animalVisitedLocationStream = animVisLocsList.stream();

        if (startDateTime != null) {
            if (!startDateTime.toString().isEmpty()) {
                animalVisitedLocationStream = animalVisitedLocationStream.filter(visLoc -> visLoc.getDateTimeOfVisitLocationPoint().after(startDateTime));
            }
        }
        if (endDateTime != null) {
            if (!endDateTime.toString().isEmpty()) {
                animalVisitedLocationStream = animalVisitedLocationStream.filter(animal -> animal.getDateTimeOfVisitLocationPoint().before(endDateTime));
            }
        }

        animVisLocsList = animalVisitedLocationStream.sorted(Comparator.comparing(AnimalVisitedLocation::getDateTimeOfVisitLocationPoint)).skip(from).limit(size).collect(Collectors.toCollection(ArrayList::new));

        return animVisLocsList;
    }

    @PostMapping("/animals/{animalId}/locations/{pointId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AnimalVisitedLocation addAnimalVisitedLocationPoint (@PathVariable Long animalId, @PathVariable Long pointId,
                                                                @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException  {

        Optional<Animal> animal = animalRepository.findById(animalId);
        Optional<LocationPoint> locPoint = locPointRepository.findById(pointId);

        if (animalId == null || animalId <= 0 || pointId == null || pointId <= 0) {
            throw new BadRequestException();
        }

        if (animal.isEmpty() || locPoint.isEmpty()) {
            throw new NotFoundException();
        }

        if (animal.get().getLifeStatus().equals("DEAD") || locPoint.get().equals(animal.get().getChippingLocationId())) throw new BadRequestException();

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException();//если в header есть поле Authorization

        if (animal.get().getVisitedLocations().length != 0) {
            if (animVisLocRepository.findByLocationPointId(locPoint.get().getId()).isPresent() && animal.get().getVisitedLocations()[animal.get().getVisitedLocations().length - 1] == animVisLocRepository.findByLocationPointId(locPoint.get().getId()).get().getId()) {
                //попытка добавить т. локации, в которой уже находится животное
                throw new BadRequestException();
            }
        }

        if (animal.get().getChippingLocationId() == locPoint.get().getId()) { //животное находится в т. локации и никуда не перемещалось
            throw new BadRequestException();
        }

        AnimalVisitedLocation newAnimalVisitedLocation = new AnimalVisitedLocation(new Date(), pointId);
        animVisLocRepository.save(newAnimalVisitedLocation);

        List<Long> visLocations = Arrays.stream(animal.get().getVisitedLocations()).collect(Collectors.toCollection(ArrayList::new));
        visLocations.add(newAnimalVisitedLocation.getId());
        animal.get().setVisitedLocations(visLocations.toArray(Long[]::new));
        animalRepository.save(animal.get());

        return newAnimalVisitedLocation;
    }

    @PutMapping("/animals/{animalId}/locations")
    public AnimalVisitedLocation updateAnimalVisitedLocationPoint (@PathVariable Long animalId, @RequestBody Map<String, Long> ids,
                                             @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException {

        Optional<Animal> animal = animalRepository.findById(animalId);
        Optional<AnimalVisitedLocation> animVisLoc = animVisLocRepository.findById(ids.get("visitedLocationPointId"));
        Optional<LocationPoint> locPoint = locPointRepository.findById(ids.get("locationPointId"));



        if (animalId == null || animalId <= 0 || ids.get("visitedLocationPointId") == null || ids.get("visitedLocationPointId") <= 0
                || ids.get("locationPointId") == null || ids.get("locationPointId") <= 0) {
            throw new BadRequestException();
        }

        if (animal.isEmpty()
                || locPoint.isEmpty()) {
            throw new NotFoundException();
        }

        if (animVisLoc.isEmpty()
        ) {
            throw new ConflictException();
        }

        if (!Arrays.asList(animal.get().getVisitedLocations()).contains(ids.get("visitedLocationPointId"))) {
            throw new ForbiddenException();
        }

        if (animVisLoc.get().getLocationPointId() == locPoint.get().getId()) throw new BadRequestException();

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException();//если в header есть поле Authorization

        if (animal.get().getVisitedLocations().length != 0) {
            if ((animal.get().getVisitedLocations()[0] == animVisLoc.get().getId()) && (animal.get().getChippingLocationId() == locPoint.get().getId())) { //обновление первой посещенной точки на точку чипирования
                throw new BadRequestException();
            }
        }

        int index = ArrayUtils.indexOf(animal.get().getVisitedLocations(), animVisLoc.get().getId());

        if (animal.get().getVisitedLocations().length > 1) { //TODO доделать

            if (locPoint.get().getId() == animal.get().getVisitedLocations()[index - 1]) {
                throw new BadRequestException(); //обновление точки локации на точку, совпадающую с предыдущей точкой
            }

            if (animal.get().getVisitedLocations().length > 2) {
                if (locPoint.get().getId() == animal.get().getVisitedLocations()[index + 1]) {
                    throw new BadRequestException(); //обновление точки локации на точку, совпадающую со следующей точкой
                }
            }
        }

        AnimalVisitedLocation updatedAnimVisLoc = new AnimalVisitedLocation(new Date(), ids.get("locationPointId"));
        animVisLocRepository.save(updatedAnimVisLoc);

        List<Long> visLocations = Arrays.stream(animal.get().getVisitedLocations()).collect(Collectors.toCollection(ArrayList::new));
        visLocations.remove(ids.get("visitedLocationId"));
        visLocations.add(ids.get("locationPointId"));
        animal.get().setVisitedLocations(visLocations.toArray(Long[]::new));
        animalRepository.save(animal.get());

        return updatedAnimVisLoc;
    }

    @DeleteMapping("/animals/{animalId}/locations/{visitedPointId}")
    public void deleteAnimalType (@PathVariable Long animalId, @PathVariable Long visitedPointId,
                                  @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException {

        if (animalId == null || animalId <= 0 || visitedPointId == null || visitedPointId <= 0){
            throw new BadRequestException();
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException();//если в header есть поле Authorization

        Optional<Animal> animal = animalRepository.findById(animalId);
        Optional<AnimalVisitedLocation> animVisLoc = animVisLocRepository.findById(visitedPointId);

        if (animal.isEmpty() || animVisLoc.isEmpty()
                || Arrays.stream(animalRepository.findById(animalId).get().getVisitedLocations()).filter(a -> a.equals(visitedPointId)).toArray().length == 0) {
            //животное с таким animalId не найдено, такой visitedLocationPoint не найден, у животного нет такого visitedLocationPoint
            throw new NotFoundException();
        }

        //TODO если удаляется первая посещенная т. локации, а вторая т. совпадает с т. чипирования, то она удаляется автоматически

        List<Long> visLocations = Arrays.stream(animal.get().getVisitedLocations()).collect(Collectors.toCollection(ArrayList::new));
        visLocations.remove(animVisLocRepository.findById(animVisLoc.get().getId()).get().getId());
        animal.get().setVisitedLocations(visLocations.toArray(Long[]::new));
        animalRepository.save(animal.get());
        animVisLocRepository.delete(animVisLoc.get());
    }
}