package com.example.travelserver.service.user.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.dto.user.ProfileUpdateRequest;
import com.example.travelserver.entity.User;
import com.example.travelserver.repository.UserRepository;
import com.example.travelserver.service.user.AuthService;
import com.example.travelserver.service.user.ProfileService;
import com.example.travelserver.vo.user.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public ProfileServiceImpl(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public UserVO get(Long userId) {
        User user = mustGet(userId);
        return authService.toVO(user);
    }

    @Override
    @Transactional
    public UserVO update(Long userId, ProfileUpdateRequest request) {
        User user = mustGet(userId);
        if (request.getNickname() != null) {
            if (request.getNickname().isBlank()) {
                throw new BusinessException(400, "昵称不能为空");
            }
            if (request.getNickname().length() > 20) {
                throw new BusinessException(400, "昵称最多 20 个字符");
            }
            user.setNickname(request.getNickname().trim());
        }
        if (request.getBio() != null) {
            if (request.getBio().length() > 100) {
                throw new BusinessException(400, "签名最多 100 个字符");
            }
            user.setBio(request.getBio().trim());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity().trim());
        }
        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            user.setAvatar(request.getAvatar().trim());
        }
        return authService.toVO(userRepository.save(user));
    }

    private User mustGet(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }
}
