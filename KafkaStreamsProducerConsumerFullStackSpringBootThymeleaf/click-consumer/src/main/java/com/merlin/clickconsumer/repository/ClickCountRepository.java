package com.merlin.clickconsumer.repository;

import com.merlin.clickconsumer.entity.ClickCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClickCountRepository extends JpaRepository<ClickCount, Long> {

    Optional<ClickCount> findByCountKey(String countKey);

    @Query("SELECT c FROM ClickCount c WHERE c.countKey != 'global' ORDER BY c.countValue DESC")
    List<ClickCount> findAllUserCounts();

    @Query("SELECT c FROM ClickCount c WHERE c.countKey != 'global' ORDER BY c.countValue DESC LIMIT :limit")
    List<ClickCount> findTopUsersByClickCount(int limit);

    boolean existsByCountKey(String countKey);

    @Query("SELECT c.countValue FROM ClickCount c WHERE c.countKey = 'global'")
    Optional<Long> findGlobalCount();
}
