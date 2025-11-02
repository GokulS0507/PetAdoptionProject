package com.petadoption.repository;

import com.petadoption.model.AdoptionRequest;
import com.petadoption.model.AdoptionRequest.RequestStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {
    
    List<AdoptionRequest> findByUser(User user);
    
    List<AdoptionRequest> findByUserId(Long userId);
    
    List<AdoptionRequest> findByPet(Pet pet);
    
    List<AdoptionRequest> findByPetId(Long petId);
    
    List<AdoptionRequest> findByStatus(RequestStatus status);
    
    Optional<AdoptionRequest> findByUserAndPet(User user, Pet pet);
    
    boolean existsByUserAndPet(User user, Pet pet);
}
