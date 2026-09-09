package com.example.travelserver.controller;

import com.example.travelserver.dto.user.FavoriteRequest;
import com.example.travelserver.service.user.FavoriteService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.FavoriteVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 收藏接口（需登录）
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/add")
    public Result<FavoriteVO> add(@RequestBody FavoriteRequest request) {
        return Result.ok(favoriteService.add(request));
    }

    @GetMapping("/list")
    public Result<List<FavoriteVO>> list() {
        return Result.ok(favoriteService.listMyFavorites());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        favoriteService.delete(id);
        return Result.ok();
    }

    @GetMapping("/check")
    public Result<Map<String, Boolean>> check(@RequestParam Long targetId,
                                              @RequestParam String targetType) {
        boolean favorited = favoriteService.isFavorited(targetId, targetType);
        return Result.ok(Map.of("favorited", favorited));
    }
}
