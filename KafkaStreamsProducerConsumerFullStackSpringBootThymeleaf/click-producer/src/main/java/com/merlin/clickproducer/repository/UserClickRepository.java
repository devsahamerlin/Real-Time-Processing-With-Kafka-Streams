package com.merlin.clickproducer.repository;

import com.merlin.clickproducer.entity.UserClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for UserClick entities.
 * Spring Data JPA automatically implements these methods for us.
 * This demonstrates the power of Spring's convention-over-configuration approach.
 */
@Repository
public interface UserClickRepository extends JpaRepository<UserClick, Long> {


    List<UserClick> findByUserIdOrderByClickTimestampDesc(String userId);
    long countByUserId(String userId);
    @Query("SELECT COUNT(u) FROM UserClick u")
    long countTotalClicks();
}
