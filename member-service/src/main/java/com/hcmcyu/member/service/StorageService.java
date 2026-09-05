package com.hcmcyu.member.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String storeAvatar(MultipartFile file);

    String storeBankQr(MultipartFile file);

    void delete(String storedUrl);
}
