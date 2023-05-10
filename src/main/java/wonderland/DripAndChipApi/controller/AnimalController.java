package wonderland.DripAndChipApi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wonderland.DripAndChipApi.AnimalDTO;
import wonderland.DripAndChipApi.Auth;
import wonderland.DripAndChipApi.entity.Account;
import wonderland.DripAndChipApi.entity.Animal;
import wonderland.DripAndChipApi.entity.AnimalType;
import wonderland.DripAndChipApi.entity.LocationPoint;
import wonderland.DripAndChipApi.exceptions.*;
import wonderland.DripAndChipApi.repository.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class AnimalController {

    final String regex = "^[0-9]{4}-((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01])|(0[469]|11)-(0[1-9]|[12][0-9]|30)|(02)-(0[1-9]|[12][0-9]))T(0[0-9]|1[0-9]|2[0-3]):(0[0-9]|[1-5][0-9]):(0[0-9]|[1-5][0-9])\\.[0-9]{3}";

    @Autowired
    AnimalRepository animalRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AnimalTypeRepository animalTypeRepository;

    @Autowired
    LocationPointRepository locationPointRepository;

    @Autowired
    AnimalVisitedLocationRepository animVisLocRepository;

    public boolean findDuplicates(ArrayList<Long> arr) {
        if (arr.size() == 1) {
            return false;
        }

        Set<Long> set = new HashSet<>();

        for (Long val: arr) {
            if (set.contains(val)) return true; else set.add(val);
        }
        return false;
    }

    @GetMapping("/animals")
    public Iterable<Animal> getAnimals() {
        return animalRepository.findAll();
    }

    @GetMapping("/animals/{id}")
    public LinkedHashMap<String, Object> getAnimalById(@PathVariable Long id, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException { //просмотр информации о животном по id

        if (id == null || id <= 0) {
            throw new BadRequestException(); //400
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); //если в header есть поле Authorization

        Optional<Animal> foundAnimal = animalRepository.findById(id);

        if (foundAnimal.isEmpty()) {
            throw new NotFoundException();
        }

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", foundAnimal.get().getId());
        result.put("animalTypes", foundAnimal.get().getAnimTypes());
        result.put("weight", foundAnimal.get().getWeight());
        result.put("length", foundAnimal.get().getLength());
        result.put("height", foundAnimal.get().getHeight());
        result.put("gender", foundAnimal.get().getGender());
        result.put("lifeStatus", foundAnimal.get().getLifeStatus());
        result.put("chippingDateTime", foundAnimal.get().getChippingDateTime());
        result.put("chipperId", foundAnimal.get().getChipperId());
        result.put("chippingLocationId", foundAnimal.get().getChippingLocationId());
        result.put("visitedLocations", foundAnimal.get().getVisitedLocations());
        result.put("deathDateTime", foundAnimal.get().getDeathDateTime());

        return result;
    }

    @GetMapping("/animals/search")
    public ArrayList<LinkedHashMap<String, Object>> searchAnimal(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date startDateTime,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date endDateTime,
                               @RequestParam(required = false) Integer chipperId, @RequestParam(required = false) Long chippingLocationId,
                               @RequestParam(required = false) String lifeStatus, @RequestParam(required = false) String gender,
                               @RequestParam(required = false, defaultValue = "0") Integer from, @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                 @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException {


        if ((from != null && from < 0) || (size != null && size <= 0) || (startDateTime != null && startDateTime.toString().matches(regex)) ||
                (endDateTime != null && endDateTime.toString().matches(regex)) || (chipperId != null && chipperId <= 0) || (chippingLocationId != null && chippingLocationId <= 0)
                || (lifeStatus != null && (!lifeStatus.equals("ALIVE") && !lifeStatus.equals("DEAD"))) || (gender != null && (!gender.equals("MALE") && !gender.equals("FEMALE")
                && !gender.equals("OTHER")))) {
            throw new BadRequestException(); //400
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); //если в header есть поле Authorization

        //отбор животных, удовлетворяющих критериям поиска
        ArrayList<Animal> animalList = (ArrayList<Animal>) animalRepository.findAll();
        Stream<Animal> animalStream = animalList.stream();

        //TODO даты не работают
        if (startDateTime != null) {
            if (!startDateTime.toString().isEmpty()) {
                animalStream = animalStream.filter(animal -> animal.getChippingDateTime().getTime() > startDateTime.getTime());
            }
        }
        if (endDateTime != null) {
            if (!endDateTime.toString().isEmpty()) {
                animalStream = animalStream.filter(animal -> animal.getChippingDateTime().getTime() < endDateTime.getTime());
            }
        }

        if (chipperId != null) {
            animalStream = animalStream.filter(a -> a.getChipperId() == (chipperId));
        }

        if (chippingLocationId != null) {
                animalStream = animalStream.filter(a -> a.getChippingLocationId() == chippingLocationId);
        }

        if (lifeStatus != null) {
            if (!lifeStatus.isEmpty()) {
                animalStream = animalStream.filter(a -> a.getLifeStatus().equals(lifeStatus));
            }
        }

        if (gender != null) {
            if (!gender.isEmpty()) {
                animalStream = animalStream.filter(a -> a.getGender().equals(gender));
            }
        }

        animalList = animalStream.sorted(Comparator.comparingLong(Animal::getId)).skip(from).limit(size).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<LinkedHashMap<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < animalList.size(); i++) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();

            map.put("id", animalList.get(i).getId());
            map.put("animalTypes", animalList.get(i).getAnimTypes());
            map.put("weight", animalList.get(i).getWeight());
            map.put("length", animalList.get(i).getLength());
            map.put("height", animalList.get(i).getHeight());
            map.put("gender", animalList.get(i).getGender());
            map.put("lifeStatus", animalList.get(i).getLifeStatus());
            map.put("chippingDateTime", animalList.get(i).getChippingDateTime());
            map.put("chipperId", animalList.get(i).getChipperId());
            map.put("chippingLocationId", animalList.get(i).getChippingLocationId());
            map.put("visitedLocations", animalList.get(i).getVisitedLocations());
            map.put("deathDateTime", animalList.get(i).getDeathDateTime());

            result.add(map);
        }

        return result;
    }

    @PostMapping(value = "/animals")
    @ResponseStatus(HttpStatus.CREATED)
    public LinkedHashMap<String, Object> addAnimal (@RequestBody AnimalDTO animalData,
                                                    @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException, ConflictException {

        ArrayList<Long> animalTypes = animalData.getAnimalTypes();
        Float weight = animalData.getWeight();
        Float length = animalData.getLength();
        Float height = animalData.getHeight();
        String gender = animalData.getGender();
        Integer chipperId = animalData.getChipperId();
        Long chippingLocationId = animalData.getChippingLocationId();


        if (animalTypes.size() == 0 || !animalTypes.stream().filter(l -> l == null).toList().isEmpty() || !animalTypes.stream().filter(type -> type <= 0).toList().isEmpty()
                || weight == null || weight <= 0 || length == null || length <= 0 || height == null || height <= 0 || gender == null
                || !(gender.equals("MALE") || gender.equals("FEMALE") || gender.equals("OTHER")) || chipperId == null || chipperId <= 0 || chippingLocationId == null
                || chippingLocationId <= 0) {
            throw new BadRequestException();
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //если в header есть поле Authorization

        if (!animalTypeRepository.findAllById(animalTypes).iterator().hasNext() || accountRepository.findById(chipperId.longValue()).isEmpty()
                || locationPointRepository.findById(chippingLocationId).isEmpty()) {
            throw new NotFoundException();
        }

        if (findDuplicates(animalTypes)) {
            throw new ConflictException();
        }

        Animal newAnimal = new Animal(animalTypes.toArray(Long[]::new), weight, length, height,
                gender, chipperId, chippingLocationId, new Long[0]);

        animalRepository.save(newAnimal);

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", newAnimal.getId());
        result.put("animalTypes", animalTypes);
        result.put("weight", newAnimal.getWeight());
        result.put("length", newAnimal.getLength());
        result.put("height", newAnimal.getHeight());
        result.put("gender", newAnimal.getGender());
        result.put("lifeStatus", newAnimal.getLifeStatus());
        result.put("chippingDateTime", newAnimal.getChippingDateTime());
        result.put("chipperId", newAnimal.getChipperId());
        result.put("chippingLocationId", newAnimal.getChippingLocationId());
        result.put("visitedLocations", newAnimal.getVisitedLocations());
        result.put("deathDateTime", newAnimal.getDeathDateTime());

        return result;
    }

    @PutMapping("/animals/{id}")
    public LinkedHashMap<String, Object> updateAnimal (@PathVariable Long id, @RequestBody AnimalDTO animalDTO,
                                                       @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException {

        if (id == null || id <= 0 || animalDTO.getWeight() == null || animalDTO.getWeight() <= 0 || animalDTO.getLength() == null || animalDTO.getLength() <= 0
                || animalDTO.getHeight() == null || animalDTO.getHeight() <= 0 || animalDTO.getGender() == null
                || (!animalDTO.getGender().equals("MALE") && !animalDTO.getGender().equals("FEMALE") && !animalDTO.getGender().equals("OTHER"))
                || animalDTO.getChipperId() == null || animalDTO.getChipperId() <= 0 || animalDTO.getChippingLocationId() == null || animalDTO.getChippingLocationId() <= 0) {
            throw new BadRequestException(); //400
        }

        Optional<Animal> updatedAnimal = animalRepository.findById(id);
        Optional<Account> newChipper = accountRepository.findById(animalDTO.getChipperId().longValue());
        Optional<LocationPoint> newChippingLocation = locationPointRepository.findById(animalDTO.getChippingLocationId());

        if (updatedAnimal.isEmpty() || newChipper.isEmpty() || newChippingLocation.isEmpty()) {
            throw new NotFoundException();
        }

        if (animalDTO.getLifeStatus().equals("ALIVE") && animalRepository.findById(id).get().getLifeStatus().equals("DEAD")) {
            throw new BadRequestException();
        }

        //запрос от неавторизованного аккаунта, неверные авторизационные данные
        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException();//если в header есть поле Authorization

        if (updatedAnimal.get().getVisitedLocations().length != 0 //новая точка чипирования совпадает с первой посещенной точкой локации
                && newChippingLocation.get().getLatitude() == locationPointRepository.findById(animVisLocRepository.findById(updatedAnimal.get().getVisitedLocations()[0]).get().getLocationPointId()).get().getLatitude()
                && newChippingLocation.get().getLongitude() == locationPointRepository.findById(animVisLocRepository.findById(updatedAnimal.get().getVisitedLocations()[0]).get().getLocationPointId()).get().getLongitude()) {
            throw new BadRequestException();
        }

        updatedAnimal.get().setWeight(animalDTO.getWeight());
        updatedAnimal.get().setLength(animalDTO.getLength());
        updatedAnimal.get().setHeight(animalDTO.getHeight());
        updatedAnimal.get().setGender(animalDTO.getGender());
        updatedAnimal.get().setLifeStatus(animalDTO.getLifeStatus());
        updatedAnimal.get().setChipperId(animalDTO.getChipperId());
        updatedAnimal.get().setChippingLocationId(animalDTO.getChippingLocationId());

        if (animalDTO.getLifeStatus().equals("DEAD")) updatedAnimal.get().setDeathDateTime(new Date());

        animalRepository.save(updatedAnimal.get());

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", updatedAnimal.get().getId());
        result.put("animalTypes", updatedAnimal.get().getAnimTypes());
        result.put("weight", updatedAnimal.get().getWeight());
        result.put("length", updatedAnimal.get().getLength());
        result.put("height", updatedAnimal.get().getHeight());
        result.put("gender", updatedAnimal.get().getGender());
        result.put("lifeStatus", updatedAnimal.get().getLifeStatus());
        result.put("chippingDateTime", updatedAnimal.get().getChippingDateTime());
        result.put("chipperId", updatedAnimal.get().getChipperId());
        result.put("chippingLocationId", updatedAnimal.get().getChippingLocationId());
        result.put("visitedLocations", updatedAnimal.get().getVisitedLocations());
        result.put("deathDateTime", updatedAnimal.get().getDeathDateTime());

        return result;
    }

    @DeleteMapping("/animals/{id}")
    public void deleteAnimal(@PathVariable Long id, @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, ForbiddenException, UnauthorizedException {

        if (id == null || id <= 0) {
            throw new BadRequestException();
        }

        Optional<Animal> animal = animalRepository.findById(id);


        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException();//запрос от неавторизованного аккаунта, неверные авторизационные данные

        if (animal.isEmpty()) { //животное не найдено
            throw new NotFoundException();
        }

        if (animal.get().getVisitedLocations().length !=0) { //животное покинуло локацию чипирования, при этом есть другие посещённые точки
            throw new BadRequestException();
        }

        animalRepository.delete(animal.get());
    }

    @PostMapping("/animals/{animalId}/types/{typeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public LinkedHashMap<String, Object> addTypeToAnimal (@PathVariable Long animalId, @PathVariable Long typeId,
                                                          @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException, ConflictException {

        if (animalId == null || animalId <= 0 || typeId == null || typeId <= 0) {
            throw new BadRequestException(); //400
        }

        Animal animal = animalRepository.findById(animalId).orElseThrow(NotFoundException::new);
        AnimalType animalType = animalTypeRepository.findById(typeId).orElseThrow(NotFoundException::new);

        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException(); //запрос от неавторизованного аккаунта, неверные авторизационные данные

        if (Arrays.stream(animal.getAnimTypes()).toList().contains(typeId)) {
            throw new ConflictException(); //409
        }


        List<Long> animTypes = Arrays.stream(animal.getAnimTypes()).collect(Collectors.toCollection(ArrayList::new));
        animTypes.add(animalType.getId());
        animal.setAnimalTypes(animTypes.toArray(Long[]::new));
        animalRepository.save(animal);

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", animal.getId());
        result.put("animalTypes", animal.getAnimTypes());
        result.put("weight", animal.getWeight());
        result.put("length", animal.getLength());
        result.put("height", animal.getHeight());
        result.put("gender", animal.getGender());
        result.put("lifeStatus", animal.getLifeStatus());
        result.put("chippingDateTime", animal.getChippingDateTime());
        result.put("chipperId", animal.getChipperId());
        result.put("chippingLocationId", animal.getChippingLocationId());
        result.put("visitedLocations", animal.getVisitedLocations());
        result.put("deathDateTime", animal.getDeathDateTime());

        return result;
    }

    @PutMapping("/animals/{id}/types")
    public LinkedHashMap<String,Object> updateTypeOfAnimal (@PathVariable Long id, @RequestBody Map<String, Long> ids,
                                                            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, UnauthorizedException, NotFoundException, ConflictException {

        if (id == null || id <= 0 || ids.get("oldTypeId") == null || ids.get("oldTypeId") <= 0 || ids.get("newTypeId") == null || ids.get("newTypeId") <= 0) {
            throw new BadRequestException(); //400
        }

        Optional<Animal> animal = animalRepository.findById(id);
        Optional<AnimalType> animalType1 = animalTypeRepository.findById(ids.get("oldTypeId"));
        Optional<AnimalType> animalType2 = animalTypeRepository.findById(ids.get("newTypeId"));

        if (animal.isEmpty() || animalType1.isEmpty() || animalType2.isEmpty() || !Arrays.stream(animal.get().getAnimTypes()).toList().contains(ids.get("oldTypeId"))) {
            throw new NotFoundException();
        }

        if (
                Arrays.stream(animal.get().getAnimTypes()).toList().contains(ids.get("newTypeId"))) {
            throw new ConflictException();
        }

        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException();//запрос от неавторизованного аккаунта, неверные авторизационные данные

        List<Long> animTypes = Arrays.stream(animal.get().getAnimTypes()).collect(Collectors.toCollection(ArrayList::new));
        animTypes.remove(ids.get("oldTypeId"));
        animTypes.add(ids.get("newTypeId"));
        animal.get().setAnimalTypes(animTypes.toArray(Long[]::new));
        animalRepository.save(animal.get());

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", animal.get().getId());
        result.put("animalTypes", animal.get().getAnimTypes());
        result.put("weight", animal.get().getWeight());
        result.put("length", animal.get().getLength());
        result.put("height", animal.get().getHeight());
        result.put("gender", animal.get().getGender());
        result.put("lifeStatus", animal.get().getLifeStatus());
        result.put("chippingDateTime", animal.get().getChippingDateTime());
        result.put("chipperId", animal.get().getChipperId());
        result.put("chippingLocationId", animal.get().getChippingLocationId());
        result.put("visitedLocations", animal.get().getVisitedLocations());
        result.put("deathDateTime", animal.get().getDeathDateTime());

        return result;
    }

    @DeleteMapping("/animals/{animalId}/types/{typeId}")
    public LinkedHashMap<String, Object> deleteTypeofAnimal(@PathVariable Long animalId, @PathVariable Long typeId,
                                   @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false, defaultValue = "") String loginPass)
            throws BadRequestException, ForbiddenException, UnauthorizedException {

        Optional<Animal> animal = animalRepository.findById(animalId);
        Optional<AnimalType> animalType = animalTypeRepository.findById(typeId);

        if (animalId == null || animalId <= 0 || typeId == null || typeId <= 0) {
            throw new BadRequestException();
        }

        if (animal.isEmpty() || animalType.isEmpty() || !Arrays.stream(animal.get().getAnimTypes()).toList().contains(typeId)) {
            //животное не найдено, тип не найден, у данного животного нет такого типа
            throw new NotFoundException();
        }

        if ((animal.get().getAnimTypes().length == 1) && (animal.get().getAnimTypes()[0].equals(animalTypeRepository.findById(typeId).get().getId()))) throw new BadRequestException();

        if (!loginPass.isEmpty()) new Auth(accountRepository).testAuthorization(loginPass); else throw new UnauthorizedException();//запрос от неавторизованного аккаунта, неверные авторизационные данные

        List<Long> animTypes = Arrays.stream(animal.get().getAnimTypes()).collect(Collectors.toCollection(ArrayList::new));
        animTypes.remove(typeId);
        animal.get().setAnimalTypes(animTypes.toArray(Long[]::new));
        animalRepository.save(animal.get());

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", animal.get().getId());
        result.put("animalTypes", animal.get().getAnimTypes());
        result.put("weight", animal.get().getWeight());
        result.put("length", animal.get().getLength());
        result.put("height", animal.get().getHeight());
        result.put("gender", animal.get().getGender());
        result.put("lifeStatus", animal.get().getLifeStatus());
        result.put("chippingDateTime", animal.get().getChippingDateTime());
        result.put("chipperId", animal.get().getChipperId());
        result.put("chippingLocationId", animal.get().getChippingLocationId());
        result.put("visitedLocations", animal.get().getVisitedLocations());
        result.put("deathDateTime", animal.get().getDeathDateTime());

        return result;
    }
}