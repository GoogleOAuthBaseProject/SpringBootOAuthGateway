package com.han.youtubespam.gateway.repository;

import org.springframework.data.jpa.domain.Specification;

import com.han.youtubespam.gateway.entity.MemberEntity;
import com.han.youtubespam.gateway.type.page.FindMemberRequestDto;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class MemberSpecification {
	public static Specification<MemberEntity> search(FindMemberRequestDto dto) {
		return (root, query, cb) -> {
			if (dto == null || dto.isEmpty())
				return cb.conjunction();

			String term = "%" + dto.term().toLowerCase() + "%";
			return switch (dto.type()) {
				case ALL -> cb.or(
					likeIgnoreCase(cb, root, "channelHandler", term),
					likeIgnoreCase(cb, root, "channelName", term)
				);
				case HANDLER -> likeIgnoreCase(cb, root, "channelHandler", term);
				case NAME -> likeIgnoreCase(cb, root, "channelName", term);
				case UUID -> likeIgnoreCase(cb, root, "uuid", term);
			};
		};
	}

	private static Predicate likeIgnoreCase(
		CriteriaBuilder cb,
		Root<MemberEntity> root,
		String field,
		String term
	) {
		return cb.like(
			cb.lower(root.get(field)),
			"%" + term.toLowerCase() + "%"
		);
	}
}
