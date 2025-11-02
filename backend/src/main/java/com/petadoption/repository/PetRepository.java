package com.petadoption.repository;

import com.petadoption.model.Pet;
import com.petadoption.model.Pet.PetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    List<Pet> findByStatus(PetStatus status);
    
    List<Pet> findByType(String type);
    
    List<Pet> findByBreed(String breed);
    
    @Query("SELECT p FROM Pet p WHERE p.age BETWEEN :minAge AND :maxAge")
    List<Pet> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
    
    @Query("SELECT p FROM Pet p WHERE p.type = :type AND p.status = :status")
    List<Pet> findByTypeAndStatus(@Param("type") String type, @Param("status") PetStatus status);
    
    @Query("SELECT DISTINCT p.type FROM Pet p")
    List<String> findDistinctTypes();
    
    @Query("SELECT DISTINCT p.breed FROM Pet p WHERE p.breed IS NOT NULL")
    List<String> findDistinctBreeds();
    
    List<Pet> findAllByOrderByCreatedAtDesc();
    
    List<Pet> findAllByOrderByCreatedAtAsc();
}
