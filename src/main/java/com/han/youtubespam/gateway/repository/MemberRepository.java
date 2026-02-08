package com.han.youtubespam.gateway.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.han.youtubespam.gateway.entity.MemberEntity;

public interface MemberRepository extends JpaRepository<MemberEntity, UUID> {
	Optional<MemberEntity> findByUuid(String uuid);

	Optional<MemberEntity> findBySub(String sub);
}
