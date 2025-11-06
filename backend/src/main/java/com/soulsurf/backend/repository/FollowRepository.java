package com.soulsurf.backend.repository;

import com.soulsurf.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<User, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM User u
        JOIN u.seguindo f
        WHERE u.id = :followerId AND f.id = :followingId
    """)
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
