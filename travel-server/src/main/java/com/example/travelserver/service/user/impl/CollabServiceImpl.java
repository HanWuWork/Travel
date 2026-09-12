package com.example.travelserver.service.user.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.entity.Trip;
import com.example.travelserver.entity.TripCollaborator;
import com.example.travelserver.entity.TripShare;
import com.example.travelserver.entity.User;
import com.example.travelserver.repository.TripCollaboratorRepository;
import com.example.travelserver.repository.TripRepository;
import com.example.travelserver.repository.TripShareRepository;
import com.example.travelserver.repository.UserRepository;
import com.example.travelserver.service.user.CollabService;
import com.example.travelserver.vo.travel.TravelPlanVO;
import com.example.travelserver.vo.user.CollabVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollabServiceImpl implements CollabService {

    private static final Logger log = LoggerFactory.getLogger(CollabServiceImpl.class);
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TripRepository tripRepository;
    private final TripShareRepository shareRepository;
    private final TripCollaboratorRepository collaboratorRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final com.example.travelserver.service.user.NotificationService notificationService;

    public CollabServiceImpl(TripRepository tripRepository,
                             TripShareRepository shareRepository,
                             TripCollaboratorRepository collaboratorRepository,
                             UserRepository userRepository,
                             ObjectMapper objectMapper,
                             com.example.travelserver.service.user.NotificationService notificationService) {
        this.tripRepository = tripRepository;
        this.shareRepository = shareRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public CollabVO share(Long userId, Long tripId) {
        Trip trip = mustGetTrip(tripId);
        if (!trip.getUserId().equals(userId)) {
            throw new BusinessException(403, "只有行程创建者可以分享");
        }
        TripShare share = shareRepository.findByTripId(tripId).orElseGet(() -> {
            TripShare s = new TripShare();
            s.setTripId(tripId);
            s.setOwnerId(userId);
            s.setShareCode(generateCode());
            s.setAllowEdit(true);
            s.setCreateTime(LocalDateTime.now());
            return shareRepository.save(s);
        });
        // 确保所有者作为成员存在
        if (!collaboratorRepository.existsByTripIdAndUserId(tripId, userId)) {
            collaboratorRepository.save(new TripCollaborator(tripId, userId, TripCollaborator.OWNER));
        }
        return buildVO(trip, share, userId, false);
    }

    @Override
    public CollabVO info(Long userId, String shareCode) {
        TripShare share = mustGetShare(shareCode);
        Trip trip = mustGetTrip(share.getTripId());
        return buildVO(trip, share, userId, false);
    }

    @Override
    @Transactional
    public CollabVO join(Long userId, String shareCode) {
        TripShare share = mustGetShare(shareCode);
        Trip trip = mustGetTrip(share.getTripId());
        if (!collaboratorRepository.existsByTripIdAndUserId(trip.getId(), userId)) {
            collaboratorRepository.save(new TripCollaborator(trip.getId(), userId, TripCollaborator.EDITOR));
            notificationService.notify(trip.getUserId(), userId,
                    com.example.travelserver.entity.Notification.COLLAB,
                    "有好友加入了你的行程协作",
                    trip.getDestination() + " · " + trip.getDays() + "天行程",
                    "/trip-detail?id=" + trip.getId() + "&shared=1");
        }
        return buildVO(trip, share, userId, true);
    }

    @Override
    public List<CollabVO> myCollabTrips(Long userId) {
        List<CollabVO> result = new ArrayList<>();
        for (TripCollaborator c : collaboratorRepository.findByUserIdOrderByJoinedAtDesc(userId)) {
            tripRepository.findById(c.getTripId()).ifPresent(trip -> {
                // 只展示"别人分享给我"的行程（自己创建的已在"我的行程"里）
                if (!trip.getUserId().equals(userId)) {
                    shareRepository.findByTripId(trip.getId())
                            .ifPresent(share -> result.add(buildVO(trip, share, userId, false)));
                }
            });
        }
        return result;
    }

    @Override
    public CollabVO detail(Long userId, Long tripId) {
        Trip trip = mustGetTrip(tripId);
        TripShare share = shareRepository.findByTripId(tripId).orElse(null);
        if (!canView(userId, trip)) {
            throw new BusinessException(403, "你不是该行程的成员");
        }
        return buildVO(trip, share, userId, true);
    }

    @Override
    @Transactional
    public CollabVO update(Long userId, Long tripId, TravelPlanVO plan) {
        Trip trip = mustGetTrip(tripId);
        if (!canEdit(userId, trip)) {
            throw new BusinessException(403, "无权编辑该行程");
        }
        if (plan == null || plan.getDestination() == null || plan.getDestination().isBlank()) {
            throw new BusinessException(400, "行程数据不完整");
        }
        trip.setDestination(plan.getDestination());
        trip.setBudget(plan.getBudget());
        trip.setDays(plan.getDays());
        try {
            trip.setPlanJson(objectMapper.writeValueAsString(plan));
        } catch (Exception e) {
            log.error("协作行程序列化失败", e);
            throw new BusinessException(500, "保存失败");
        }
        trip.setUpdateTime(LocalDateTime.now());
        trip.setVersion((trip.getVersion() == null ? 1 : trip.getVersion()) + 1);
        trip = tripRepository.save(trip);
        TripShare share = shareRepository.findByTripId(tripId).orElse(null);
        return buildVO(trip, share, userId, true);
    }

    @Override
    public List<CollabVO.Member> members(Long userId, Long tripId) {
        Trip trip = mustGetTrip(tripId);
        if (!canView(userId, trip)) {
            throw new BusinessException(403, "你不是该行程的成员");
        }
        return buildMembers(tripId);
    }

    @Override
    @Transactional
    public void removeMember(Long userId, Long tripId, Long memberId) {
        Trip trip = mustGetTrip(tripId);
        TripCollaborator member = collaboratorRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(404, "成员不存在"));
        if (!member.getTripId().equals(tripId)) {
            throw new BusinessException(400, "成员不属于该行程");
        }
        boolean isOwner = trip.getUserId().equals(userId);
        boolean isSelf = member.getUserId().equals(userId);
        if (!isOwner && !isSelf) {
            throw new BusinessException(403, "无权移除该成员");
        }
        if (TripCollaborator.OWNER.equals(member.getRole())) {
            throw new BusinessException(400, "行程创建者不能退出协作");
        }
        collaboratorRepository.delete(member);
    }

    // ===== 权限与装配 =====

    private boolean canView(Long userId, Trip trip) {
        if (userId == null) {
            return false;
        }
        return trip.getUserId().equals(userId) || collaboratorRepository.existsByTripIdAndUserId(trip.getId(), userId);
    }

    private boolean canEdit(Long userId, Trip trip) {
        if (userId == null) {
            return false;
        }
        if (trip.getUserId().equals(userId)) {
            return true;
        }
        TripShare share = shareRepository.findByTripId(trip.getId()).orElse(null);
        if (share != null && Boolean.FALSE.equals(share.getAllowEdit())) {
            return false;
        }
        return collaboratorRepository.existsByTripIdAndUserId(trip.getId(), userId);
    }

    private CollabVO buildVO(Trip trip, TripShare share, Long userId, boolean withPlan) {
        CollabVO vo = new CollabVO();
        vo.setTripId(trip.getId());
        vo.setShareCode(share == null ? null : share.getShareCode());
        vo.setDestination(trip.getDestination());
        vo.setDays(trip.getDays());
        vo.setBudget(trip.getBudget());
        vo.setStartDate(trip.getStartDate());
        vo.setOwnerId(trip.getUserId());
        vo.setVersion(trip.getVersion());
        userRepository.findById(trip.getUserId()).ifPresent(u ->
                vo.setOwnerName(u.getNickname() == null ? u.getUsername() : u.getNickname()));

        String role = "none";
        if (userId != null) {
            if (trip.getUserId().equals(userId)) {
                role = TripCollaborator.OWNER;
            } else if (collaboratorRepository.existsByTripIdAndUserId(trip.getId(), userId)) {
                role = TripCollaborator.EDITOR;
            }
        }
        vo.setRole(role);
        vo.setMemberCount(collaboratorRepository.countByTripId(trip.getId()));
        if (withPlan) {
            vo.setPlan(readPlan(trip));
        }
        return vo;
    }

    private List<CollabVO.Member> buildMembers(Long tripId) {
        List<CollabVO.Member> list = new ArrayList<>();
        for (TripCollaborator c : collaboratorRepository.findByTripIdOrderByJoinedAtAsc(tripId)) {
            CollabVO.Member m = new CollabVO.Member();
            m.setId(c.getId());
            m.setUserId(c.getUserId());
            m.setRole(c.getRole());
            m.setJoinedAt(c.getJoinedAt() == null ? null : c.getJoinedAt().format(TIME_FMT));
            userRepository.findById(c.getUserId()).ifPresent((User u) -> {
                m.setName(u.getNickname() == null ? u.getUsername() : u.getNickname());
                m.setAvatar(u.getAvatar());
            });
            list.add(m);
        }
        return list;
    }

    private TravelPlanVO readPlan(Trip trip) {
        try {
            TravelPlanVO vo = objectMapper.readValue(trip.getPlanJson(), TravelPlanVO.class);
            vo.setId(trip.getId());
            vo.setStartDate(trip.getStartDate());
            return vo;
        } catch (Exception e) {
            TravelPlanVO vo = new TravelPlanVO();
            vo.setId(trip.getId());
            vo.setDestination(trip.getDestination());
            vo.setBudget(trip.getBudget());
            vo.setDays(trip.getDays());
            vo.setStartDate(trip.getStartDate());
            vo.setItinerary(List.of());
            vo.setBudgetBreakdown(List.of());
            vo.setTips(List.of());
            return vo;
        }
    }

    private Trip mustGetTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(404, "行程不存在"));
    }

    private TripShare mustGetShare(String shareCode) {
        if (shareCode == null || shareCode.isBlank()) {
            throw new BusinessException(400, "请提供分享码");
        }
        return shareRepository.findByShareCode(shareCode.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException(404, "分享码无效或已失效"));
    }

    private String generateCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (shareRepository.findByShareCode(code).isEmpty()) {
                return code;
            }
        }
        throw new BusinessException(500, "分享码生成失败，请重试");
    }
}
