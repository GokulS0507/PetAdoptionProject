package com.petadoption.service;

import com.petadoption.dto.AdoptionRequestDTO;
import com.petadoption.model.AdoptionRequest;
import com.petadoption.model.AdoptionRequest.RequestStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.repository.AdoptionRequestRepository;
import com.petadoption.repository.PetRepository;
import com.petadoption.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdoptionService {

    @Autowired
    private AdoptionRequestRepository adoptionRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PetRepository petRepository;

    public AdoptionRequest createAdoptionRequest(Long userId, Long petId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + petId));
        
        if (adoptionRequestRepository.existsByUserAndPet(user, pet)) {
            throw new RuntimeException("Adoption request already exists for this pet");
        }
        
        AdoptionRequest request = new AdoptionRequest();
        request.setUser(user);
        request.setPet(pet);
        request.setStatus(RequestStatus.PENDING);
        
        return adoptionRequestRepository.save(request);
    }

    public List<AdoptionRequestDTO> getAdoptionRequestsByUserId(Long userId) {
        List<AdoptionRequest> requests = adoptionRequestRepository.findByUserId(userId);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<AdoptionRequestDTO> getAllAdoptionRequests() {
        List<AdoptionRequest> requests = adoptionRequestRepository.findAll();
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public AdoptionRequest approveAdoptionRequest(Long requestId) {
        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Adoption request not found with id: " + requestId));
        
        request.setStatus(RequestStatus.APPROVED);
        
        // Update pet status to ADOPTED
        Pet pet = request.getPet();
        pet.setStatus(Pet.PetStatus.ADOPTED);
        petRepository.save(pet);
        
        return adoptionRequestRepository.save(request);
    }

    public AdoptionRequest rejectAdoptionRequest(Long requestId) {
        AdoptionRequest request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Adoption request not found with id: " + requestId));
        
        request.setStatus(RequestStatus.REJECTED);
        
        // Update pet status back to AVAILABLE if it was PENDING
        Pet pet = request.getPet();
        if (pet.getStatus() == Pet.PetStatus.PENDING) {
            pet.setStatus(Pet.PetStatus.AVAILABLE);
            petRepository.save(pet);
        }
        
        return adoptionRequestRepository.save(request);
    }

    private AdoptionRequestDTO convertToDTO(AdoptionRequest request) {
        AdoptionRequestDTO dto = new AdoptionRequestDTO();
        dto.setId(request.getId());
        dto.setUserId(request.getUser().getId());
        dto.setUserName(request.getUser().getName());
        dto.setPetId(request.getPet().getId());
        dto.setPetName(request.getPet().getName());
        dto.setPetType(request.getPet().getType());
        dto.setStatus(request.getStatus().name());
        dto.setRequestDate(request.getRequestDate());
        return dto;
    }
}
