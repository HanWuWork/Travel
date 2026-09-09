package com.example.travelserver.service.user;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.user.FavoriteRequest;
import com.example.travelserver.entity.Favorite;
import com.example.travelserver.repository.FavoriteRepository;
import com.example.travelserver.vo.user.FavoriteVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public FavoriteVO add(FavoriteRequest request) {
        Long userId = UserContext.getUserId();
        Long targetId = request.getTargetId() != null ? request.getTargetId() : 0L;
        // 防重复收藏
        if (favoriteRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, request.getTargetType())) {
            throw new BusinessException(400, "已收藏过该内容");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setTargetType(request.getTargetType());
        fav.setTargetId(targetId);
        fav.setTitle(request.getTitle());
        fav.setImage(request.getImage());
        fav.setDescription(request.getDescription());
        fav.setCreateTime(LocalDateTime.now());
        fav = favoriteRepository.save(fav);
        return toVO(fav);
    }

    public List<FavoriteVO> listMyFavorites() {
        Long userId = UserContext.getUserId();
        return favoriteRepository.findByUserIdOrderByCreateTimeDesc(userId)
                .stream().map(this::toVO).toList();
    }

    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        Favorite fav = favoriteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "收藏不存在"));
        if (!fav.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        favoriteRepository.delete(fav);
    }

    public boolean isFavorited(Long targetId, String targetType) {
        Long userId = UserContext.getUserId();
        if (userId == null) return false;
        Long tid = targetId != null ? targetId : 0L;
        return favoriteRepository.existsByUserIdAndTargetIdAndTargetType(userId, tid, targetType);
    }

    private FavoriteVO toVO(Favorite f) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(f.getId());
        vo.setTargetType(f.getTargetType());
        vo.setTargetId(f.getTargetId());
        vo.setTitle(f.getTitle());
        vo.setImage(f.getImage());
        vo.setDescription(f.getDescription());
        vo.setCreateTime(f.getCreateTime());
        return vo;
    }
}
