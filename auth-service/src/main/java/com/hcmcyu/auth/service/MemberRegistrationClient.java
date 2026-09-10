package com.hcmcyu.auth.service;

import com.hcmcyu.auth.dto.MemberRegistrationResponse;
import com.hcmcyu.auth.dto.RegisterRequest;
import com.hcmcyu.auth.entity.UserAccount;

public interface MemberRegistrationClient {

    MemberRegistrationResponse createMemberProfile(UserAccount user, RegisterRequest request);
}
