package com.hcmcyu.member.repository;

import com.hcmcyu.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MemberRepository extends JpaRepository<Member, String>, JpaSpecificationExecutor<Member> {

    boolean existsByUserId(String userId);

    boolean existsByUserIdAndIdNot(String userId, String id);
}
