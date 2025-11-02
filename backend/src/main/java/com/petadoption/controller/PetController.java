package com.petadoption.controller;

import com.petadoption.model.Pet;
import com.petadoption.model.Pet.PetStatus;
import com.petadoption.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping
    public ResponseEntity<List<Pet>> getAllPets(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort) {
        
        List<Pet> pets;
        
        // Apply filters
        if (type != null && !type.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                try {
                    PetStatus petStatus = PetStatus.valueOf(status.toUpperCase());
                    pets = petService.getPetsByTypeAndStatus(type, petStatus);
                } catch (IllegalArgumentException e) {
                    pets = petService.getPetsByType(type);
                }
            } else {
                pets = petService.getPetsByType(type);
            }
        } else if (breed != null && !breed.isEmpty()) {
            pets = petService.getPetsByBreed(breed);
        } else if (minAge != null && maxAge != null) {
            pets = petService.getPetsByAgeRange(minAge, maxAge);
        } else if (status != null && !status.isEmpty()) {
            try {
                PetStatus petStatus = PetStatus.valueOf(status.toUpperCase());
                pets = petService.getPetsByStatus(petStatus);
            } catch (IllegalArgumentException e) {
                pets = petService.getAllPets();
            }
        } else {
            pets = petService.getAllPets();
        }
        
        // Apply sorting
        if ("oldest".equalsIgnoreCase(sort)) {
            pets = petService.getPetsSortedByOldest();
            // Filter again after sorting if filters were applied
            if ((type != null && !type.isEmpty()) || (status != null && !status.isEmpty())) {
                // Re-apply filters to sorted list (simplified - in production, use DB query)
                pets = pets.stream()
                    .filter(p -> type == null || type.isEmpty() || p.getType().equalsIgnoreCase(type))
                    .filter(p -> status == null || status.isEmpty() || p.getStatus().name().equalsIgnoreCase(status))
                    .toList();
            }
        } else if ("newest".equalsIgnoreCase(sort)) {
            pets = petService.getPetsSortedByNewest();
            // Filter again after sorting if filters were applied
            if ((type != null && !type.isEmpty()) || (status != null && !status.isEmpty())) {
                pets = pets.stream()
                    .filter(p -> type == null || type.isEmpty() || p.getType().equalsIgnoreCase(type))
                    .filter(p -> status == null || status.isEmpty() || p.getStatus().name().equalsIgnoreCase(status))
                    .toList();
            }
        }
        
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getPetTypes() {
        return ResponseEntity.ok(petService.getDistinctTypes());
    }

    @GetMapping("/breeds")
    public ResponseEntity<List<String>> getPetBreeds() {
        return ResponseEntity.ok(petService.getDistinctBreeds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPetById(@PathVariable Long id) {
        return petService.getPetById(id)
                .map(pet -> ResponseEntity.ok(pet))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Pet not found")));
    }
}

