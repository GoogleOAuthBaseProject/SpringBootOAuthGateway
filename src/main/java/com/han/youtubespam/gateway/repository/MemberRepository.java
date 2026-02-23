package com.han.youtubespam.gateway.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.han.youtubespam.gateway.entity.MemberEntity;

public interface MemberRepository extends JpaRepository<MemberEntity, UUID>, JpaSpecificationExecutor<MemberEntity> {
	Optional<MemberEntity> findByUuid(UUID uuid);

	Optional<MemberEntity> findBySub(String sub);
}
