package com.cms.contactmanagement.repository;

import com.cms.contactmanagement.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Page<Contact> findAllByUserId(Long userId, Pageable pageable);

    @Query("""
            select c
            from Contact c
            where c.userId = :userId
              and (:firstName is null or lower(c.firstName) like lower(concat('%', :firstName, '%')))
              and (:lastName is null or lower(c.lastName) like lower(concat('%', :lastName, '%')))
            """)
    Page<Contact> searchByUserIdAndName(
            @Param("userId") Long userId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            Pageable pageable
    );
}

