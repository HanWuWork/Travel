package com.example.travelserver.service.user;

import com.example.travelserver.dto.user.LoginRequest;
import com.example.travelserver.dto.user.RegisterRequest;
import com.example.travelserver.entity.User;
import com.example.travelserver.repository.UserRepository;
import com.example.travelserver.vo.user.LoginVO;
import com.example.travelserver.vo.user.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务：注册、登录、Token 校验
 *
 * <p>Token 采用简单方案：登录成功后生成 UUID，在内存中维护 token -> userId 映射。
 * 适合单机演示；生产环境应替换为 JWT 或 Redis 存储。</p>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** token -> userId */
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 注册 */
    public UserVO register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            throw new com.example.travelserver.common.BusinessException(400, "用户名至少 3 个字符");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new com.example.travelserver.common.BusinessException(400, "密码至少 6 个字符");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new com.example.travelserver.common.BusinessException(400, "用户名已被注册");
        }

        String nickname = (request.getNickname() == null || request.getNickname().isBlank())
                ? request.getUsername() : request.getNickname();
        User user = new User(request.getUsername(),
                passwordEncoder.encode(request.getPassword()), nickname);
        user = userRepository.save(user);
        return toVO(user);
    }

    /** 登录，返回 token + 用户信息 */
    public LoginVO login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new com.example.travelserver.common.BusinessException(400, "用户名和密码不能为空");
        }
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new com.example.travelserver.common.BusinessException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new com.example.travelserver.common.BusinessException(401, "用户名或密码错误");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());
        return new LoginVO(token, toVO(user));
    }

    /** 根据 token 获取 userId（未登录返回 null） */
    public Long getUserIdByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return tokenStore.get(token);
    }

    /** 登出，移除 token */
    public void logout(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }

    public UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
