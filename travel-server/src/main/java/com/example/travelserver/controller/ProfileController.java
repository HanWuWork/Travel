package com.example.travelserver.controller;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.user.ProfileUpdateRequest;
import com.example.travelserver.service.user.ProfileService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 个人资料接口（需登录）：资料查询/更新 + 头像上传
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final ProfileService profileService;

    @Value("${travel.upload.dir:uploads}")
    private String uploadDir;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** 当前用户资料 */
    @GetMapping
    public Result<UserVO> get() {
        return Result.ok(profileService.get(UserContext.getUserId()));
    }

    /** 更新资料 */
    @PutMapping
    public Result<UserVO> update(@RequestBody ProfileUpdateRequest request) {
        return Result.ok(profileService.update(UserContext.getUserId(), request));
    }

    /** 头像上传：返回可访问的相对 URL */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(400, "图片不能超过 5MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 jpg/png/gif/webp 图片");
        }
        try {
            Path dir = Paths.get(uploadDir, "avatar").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            String url = "/uploads/avatar/" + filename;
            log.info("头像上传成功: {}", url);
            return Result.ok(Map.of("url", url));
        } catch (IOException e) {
            log.error("头像保存失败", e);
            throw new BusinessException(500, "头像上传失败，请重试");
        }
    }
}
