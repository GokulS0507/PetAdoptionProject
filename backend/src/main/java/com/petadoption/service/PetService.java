package com.petadoption.service;

import com.petadoption.model.Pet;
import com.petadoption.model.Pet.PetStatus;
import com.petadoption.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PetService {

    @Autowired
    private PetRepository petRepository;

    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    public Optional<Pet> getPetById(Long id) {
        return petRepository.findById(id);
    }

    public List<Pet> getPetsByStatus(PetStatus status) {
        return petRepository.findByStatus(status);
    }

    public List<Pet> getPetsByType(String type) {
        return petRepository.findByType(type);
    }

    public List<Pet> getPetsByBreed(String breed) {
        return petRepository.findByBreed(breed);
    }

    public List<Pet> getPetsByAgeRange(Integer minAge, Integer maxAge) {
        return petRepository.findByAgeRange(minAge, maxAge);
    }

    public List<Pet> getPetsByTypeAndStatus(String type, PetStatus status) {
        return petRepository.findByTypeAndStatus(type, status);
    }

    public List<String> getDistinctTypes() {
        return petRepository.findDistinctTypes();
    }

    public List<String> getDistinctBreeds() {
        return petRepository.findDistinctBreeds();
    }

    public List<Pet> getPetsSortedByNewest() {
        return petRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Pet> getPetsSortedByOldest() {
        return petRepository.findAllByOrderByCreatedAtAsc();
    }

    public Pet createPet(Pet pet) {
        return petRepository.save(pet);
    }

    public Pet updatePet(Long id, Pet petDetails) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        
        pet.setName(petDetails.getName());
        pet.setType(petDetails.getType());
        pet.setBreed(petDetails.getBreed());
        pet.setAge(petDetails.getAge());
        pet.setDescription(petDetails.getDescription());
        if (petDetails.getImagePath() != null) {
            pet.setImagePath(petDetails.getImagePath());
        }
        pet.setStatus(petDetails.getStatus());
        
        return petRepository.save(pet);
    }

    public void deletePet(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        petRepository.delete(pet);
    }
}
