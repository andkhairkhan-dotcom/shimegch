package com.example.application.repository;

import com.example.application.domain.Apartment;
import com.example.application.domain.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {

    @Query("SELECT h FROM Household h " +
           "JOIN FETCH h.apartment a " +
           "JOIN FETCH a.entrance e " +
           "JOIN FETCH e.building b " +
           "WHERE a = :apartment")
    Optional<Household> findByApartment(@Param("apartment") Apartment apartment);

    @Query("SELECT h FROM Household h " +
           "JOIN FETCH h.apartment a " +
           "JOIN FETCH a.entrance e " +
           "JOIN FETCH e.building b " +
           "WHERE LOWER(h.householdName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Household> findByHouseholdNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT h FROM Household h " +
           "JOIN FETCH h.apartment a " +
           "JOIN FETCH a.entrance e " +
           "JOIN FETCH e.building b " +
           "WHERE b.buildingNumber = :buildingNumber")
    List<Household> findByBuildingNumber(@Param("buildingNumber") String buildingNumber);

    @Query("SELECT h FROM Household h " +
           "JOIN FETCH h.apartment a " +
           "JOIN FETCH a.entrance e " +
           "JOIN FETCH e.building b " +
           "WHERE b.buildingNumber = :buildingNumber AND e.entranceNumber = :entranceNumber")
    List<Household> findByBuildingAndEntrance(@Param("buildingNumber") String buildingNumber,
                                            @Param("entranceNumber") Integer entranceNumber);

    @Query("SELECT h FROM Household h " +
           "JOIN FETCH h.apartment a " +
           "JOIN FETCH a.entrance e " +
           "JOIN FETCH e.building b")
    List<Household> findAll();
}
