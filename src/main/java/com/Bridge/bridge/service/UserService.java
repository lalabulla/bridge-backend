package com.Bridge.bridge.service;

import com.Bridge.bridge.domain.Field;
import com.Bridge.bridge.domain.Profile;
import com.Bridge.bridge.domain.User;
import com.Bridge.bridge.dto.request.UserFieldRequest;
import com.Bridge.bridge.dto.request.UserProfileRequest;
import com.Bridge.bridge.dto.request.UserRegisterRequest;
import com.Bridge.bridge.dto.request.UserSignUpRequest;
import com.Bridge.bridge.dto.response.UserSignUpResponse;
import com.Bridge.bridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 처음 로그인 시 이름 정보 등록
     */
    @Transactional
    public UserSignUpResponse signUpName(UserSignUpRequest request) {
        User findUser = userRepository.findByPlatformId(request.getPlatformId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));

        findUser.registerName(request.getName());
        return new UserSignUpResponse(findUser.getId());
    }

    public void signUpInfo(UserRegisterRequest request) {
        saveField(request.getUserId(), request.getUserField());
        saveProfile(request.getUserId(), request.getUserProfile());
    }

    /**
     * 처음 로그인 시 개인 관심분야 등록
     */
    @Transactional
    public boolean saveField(Long userId, UserFieldRequest request) {
        if(Objects.isNull(request)) {
            return false;
        }
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));

        List<Field> fields = request.toEntity();
        fields.stream()
                .forEach(f -> f.updateFieldUser(findUser));

        findUser.getFields().addAll(fields);
        return true;
    }

    /**
     * 처음 로그인 시 개인 프로필 등록
     */
    @Transactional
    public boolean saveProfile(Long userId, UserProfileRequest request) {
        if (Objects.isNull(request)) {
            return false;
        }

        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));

        Profile profile = request.toEntity();
        findUser.updateProfile(profile);
        return true;
    }
}