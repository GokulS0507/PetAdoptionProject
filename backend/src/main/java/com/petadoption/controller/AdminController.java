package com.petadoption.controller;

import com.petadoption.dto.AdoptionRequestDTO;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.FileUploadService;
import com.petadoption.service.PetService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class AdminController {

    @Autowired
    private PetService petService;
    
    @Autowired
    private AdoptionService adoptionService;
    
    @Autowired
    private FileUploadService fileUploadService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    @GetMapping("/pets")
    public ResponseEntity<?> getAllPets(HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized - Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        
        return ResponseEntity.ok(petService.getAllPets());
    }

    @PostMapping("/pets")
    public ResponseEntity<?> createPet(
            @RequestParam String name,
            @RequestParam String type,
            @RequestParam(required = false) String breed,
            @RequestParam Integer age,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false, defaultValue = "AVAILABLE") String status,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized - Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        
        try {
            Pet pet = new Pet();
            pet.setName(name);
            pet.setType(type);
            pet.setBreed(breed);
            pet.setAge(age);
            pet.setDescription(description);
            pet.setStatus(Pet.PetStatus.valueOf(status.toUpperCase()));
            
            if (image != null && !image.isEmpty()) {
                String imagePath = fileUploadService.uploadFile(image);
                pet.setImagePath(imagePath);
            }
            
            Pet savedPet = petService.createPet(pet);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pet created successfully");
            response.put("pet", savedPet);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/pets/{id}")
    public ResponseEntity<?> updatePet(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String status,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized - Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        
        try {
            Pet existingPet = petService.getPetById(id)
                    .orElseThrow(() -> new RuntimeException("Pet not found"));
            
            Pet petDetails = new Pet();
            petDetails.setName(name != null ? name : existingPet.getName());
            petDetails.setType(type != null ? type : existingPet.getType());
            petDetails.setBreed(breed != null ? breed : existingPet.getBreed());
            petDetails.setAge(age != null ? age : existingPet.getAge());
            petDetails.setDescription(description != null ? description : existingPet.getDescription());
            petDetails.setStatus(status != null ? Pet.PetStatus.valueOf(status.toUpperCase()) : existingPet.getStatus());
            
            if (image != null && !image.isEmpty()) {
                // Delete old image if exists
                if (existingPet.getImagePath() != null) {
                    try {
                        fileUploadService.deleteFile(existingPet.getImagePath());
                    } catch (Exception e) {
                        // Log error but continue
                    }
                }
                String imagePath = fileUploadService.uploadFile(image);
                petDetails.setImagePath(imagePath);
            } else {
                petDetails.setImagePath(existingPet.getImagePath());
            }
            
            Pet updatedPet = petService.updatePet(id, petDetails);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pet updated successfully");
            response.put("pet", updatedPet);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/pets/{id}")
    public ResponseEntity<?> deletePet(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized - Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        
        try {
            Pet pet = petService.getPetById(id)
                    .orElseThrow(() -> new RuntimeException("Pet not found"));
            
            // Delete image file if exists
            if (pet.getImagePath() != null) {
                try {
                    fileUploadService.deleteFile(pet.getImagePath());
                } catch (Exception e) {
                    // Log error but continue with pet deletion
                }
            }
            
            petService.deletePet(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Pet deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getAllAdoptionRequests(HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized - Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        
        List<AdoptionRequestDTO> requests = adoptionService.getAllAdoptionRequests();
        return ResponseEntity.ok(requests);
    }
}
