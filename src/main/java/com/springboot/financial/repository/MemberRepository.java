package com.springboot.financial.repository;

import com.springboot.financial.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {


    Optional<MemberEntity> findByUsername(String username);

    boolean existsByUsername(String username);


}
