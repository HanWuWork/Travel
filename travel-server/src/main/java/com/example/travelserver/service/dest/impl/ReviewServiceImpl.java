package com.example.travelserver.service.dest.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.dto.dest.ReviewRequest;
import com.example.travelserver.entity.AttractionReview;
import com.example.travelserver.entity.User;
import com.example.travelserver.repository.AttractionRepository;
import com.example.travelserver.repository.AttractionReviewRepository;
import com.example.travelserver.repository.UserRepository;
import com.example.travelserver.service.dest.ReviewService;
import com.example.travelserver.vo.dest.ReviewSummaryVO;
import com.example.travelserver.vo.dest.ReviewVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AttractionReviewRepository reviewRepository;
    private final AttractionRepository attractionRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(AttractionReviewRepository reviewRepository,
                             AttractionRepository attractionRepository,
                             UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.attractionRepository = attractionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ReviewVO> list(Long userId, Long attractionId) {
        return reviewRepository.findByAttractionIdOrderByUpdateTimeDesc(attractionId).stream()
                .map(r -> toVO(r, userId))
                .collect(Collectors.toList());
    }

    @Override
    public ReviewSummaryVO summary(Long attractionId) {
        List<AttractionReview> reviews = reviewRepository.findByAttractionIdOrderByUpdateTimeDesc(attractionId);
        ReviewSummaryVO vo = new ReviewSummaryVO();
        vo.setReviewCount(reviews.size());

        long[] dist = new long[5];
        double sum = 0;
        Map<String, Long> tagCount = new LinkedHashMap<>();
        for (AttractionReview r : reviews) {
            int rating = r.getRating() == null ? 0 : r.getRating();
            sum += rating;
            if (rating >= 1 && rating <= 5) {
                dist[5 - rating]++;
            }
            if (r.getTags() != null && !r.getTags().isBlank()) {
                for (String t : r.getTags().split("[,，\\s]+")) {
                    if (!t.isBlank()) {
                        tagCount.merge(t.trim(), 1L, Long::sum);
                    }
                }
            }
        }
        vo.setAvgRating(reviews.isEmpty() ? null : Math.round(sum / reviews.size() * 10) / 10.0);
        vo.setDistribution(Arrays.asList(dist[0], dist[1], dist[2], dist[3], dist[4]));
        vo.setTopTags(tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(8)
                .map(e -> new ReviewSummaryVO.TagCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public ReviewVO submit(Long userId, ReviewRequest request) {
        if (request == null || request.getAttractionId() == null) {
            throw new BusinessException(400, "缺少地点 ID");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException(400, "请给出 1-5 星评分");
        }
        if (request.getContent() != null && request.getContent().length() > 500) {
            throw new BusinessException(400, "评价最多 500 字");
        }
        attractionRepository.findById(request.getAttractionId())
                .orElseThrow(() -> new BusinessException(404, "地点不存在"));

        AttractionReview review = reviewRepository
                .findByAttractionIdAndUserId(request.getAttractionId(), userId)
                .orElseGet(() -> {
                    AttractionReview r = new AttractionReview();
                    r.setAttractionId(request.getAttractionId());
                    r.setUserId(userId);
                    r.setCreateTime(LocalDateTime.now());
                    return r;
                });
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setTags(request.getTags());
        review.setUpdateTime(LocalDateTime.now());
        review = reviewRepository.save(review);
        return toVO(review, userId);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long reviewId) {
        AttractionReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(404, "评价不存在"));
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的评价");
        }
        reviewRepository.delete(review);
    }

    @Override
    public List<ReviewVO> myReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByUpdateTimeDesc(userId).stream()
                .map(r -> toVO(r, userId))
                .collect(Collectors.toList());
    }

    private ReviewVO toVO(AttractionReview r, Long currentUserId) {
        ReviewVO vo = new ReviewVO();
        vo.setId(r.getId());
        vo.setAttractionId(r.getAttractionId());
        vo.setUserId(r.getUserId());
        User u = userRepository.findById(r.getUserId()).orElse(null);
        if (u != null) {
            vo.setAuthorName(u.getNickname() == null ? u.getUsername() : u.getNickname());
            vo.setAuthorAvatar(u.getAvatar());
        }
        vo.setRating(r.getRating());
        vo.setContent(r.getContent());
        if (r.getTags() != null && !r.getTags().isBlank()) {
            List<String> tags = new ArrayList<>();
            for (String t : r.getTags().split("[,，\\s]+")) {
                if (!t.isBlank()) {
                    tags.add(t.trim());
                }
            }
            vo.setTagList(tags);
        } else {
            vo.setTagList(List.of());
        }
        LocalDateTime time = r.getUpdateTime() != null ? r.getUpdateTime() : r.getCreateTime();
        vo.setCreateTime(time == null ? null : time.format(FMT));
        vo.setMine(currentUserId != null && currentUserId.equals(r.getUserId()));
        return vo;
    }
}
