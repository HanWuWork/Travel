package com.example.travelserver.service.user.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.entity.SignInRecord;
import com.example.travelserver.entity.User;
import com.example.travelserver.repository.SignInRecordRepository;
import com.example.travelserver.repository.UserRepository;
import com.example.travelserver.service.user.SignInService;
import com.example.travelserver.vo.user.SignInStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SignInServiceImpl implements SignInService {

    /** 等级门槛：积分 → 名称 */
    private static final int[] LEVEL_POINTS = {0, 100, 300, 600, 1200};
    private static final String[] LEVEL_NAMES = {"旅行新手", "旅行爱好者", "旅行达人", "资深旅人", "环球旅行家"};

    private final SignInRecordRepository signInRepository;
    private final UserRepository userRepository;

    public SignInServiceImpl(SignInRecordRepository signInRepository, UserRepository userRepository) {
        this.signInRepository = signInRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SignInStatusVO status(Long userId) {
        String today = LocalDate.now().toString();
        SignInRecord todayRecord = signInRepository.findByUserIdAndSignDate(userId, today).orElse(null);

        List<SignInRecord> all = signInRepository.findByUserIdOrderBySignDateDesc(userId);
        int consecutive = computeConsecutive(all);

        SignInStatusVO vo = new SignInStatusVO();
        vo.setTodaySigned(todayRecord != null);
        vo.setConsecutiveDays(consecutive);
        vo.setTotalDays(signInRepository.countByUserId(userId));
        vo.setTodayPoints(todayRecord == null ? pointsFor(consecutive + 1) : todayRecord.getPoints());

        String month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String start = month + "-01";
        String end = YearMonth.now().atEndOfMonth().toString();
        vo.setMonthSignedDates(signInRepository
                .findByUserIdAndSignDateBetweenOrderBySignDateAsc(userId, start, end)
                .stream().map(SignInRecord::getSignDate).collect(Collectors.toList()));

        fillLevel(vo, userId);
        vo.setRule("每日签到 +5 积分，连续签到每天额外 +1（最多 +7），断签后连续天数重新计算");
        return vo;
    }

    @Override
    @Transactional
    public SignInStatusVO signIn(Long userId) {
        String today = LocalDate.now().toString();
        if (signInRepository.findByUserIdAndSignDate(userId, today).isPresent()) {
            throw new BusinessException(400, "今天已经签到过啦，明天再来");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        List<SignInRecord> all = signInRepository.findByUserIdOrderBySignDateDesc(userId);
        int consecutive = computeConsecutive(all) + 1;
        int points = pointsFor(consecutive);

        SignInRecord record = new SignInRecord();
        record.setUserId(userId);
        record.setSignDate(today);
        record.setPoints(points);
        record.setConsecutiveDays(consecutive);
        record.setCreateTime(java.time.LocalDateTime.now());
        signInRepository.save(record);

        user.setPoints((user.getPoints() == null ? 0 : user.getPoints()) + points);
        userRepository.save(user);

        SignInStatusVO vo = status(userId);
        vo.setTodayPoints(points);
        return vo;
    }

    /** 连续签到天数：从今天或昨天起向前连续 */
    private int computeConsecutive(List<SignInRecord> recordsDesc) {
        if (recordsDesc.isEmpty()) {
            return 0;
        }
        LocalDate cursor = LocalDate.now();
        // 若今天没签，允许从昨天起算连续天数
        if (!recordsDesc.get(0).getSignDate().equals(cursor.toString())) {
            cursor = cursor.minusDays(1);
        }
        int count = 0;
        for (SignInRecord r : recordsDesc) {
            if (r.getSignDate().equals(cursor.toString())) {
                count++;
                cursor = cursor.minusDays(1);
            } else if (r.getSignDate().compareTo(cursor.toString()) < 0) {
                break;
            }
        }
        return count;
    }

    /** 积分规则：基础 5 分 + 连续加成（最多 +7） */
    private int pointsFor(int consecutiveDays) {
        return 5 + Math.min(Math.max(consecutiveDays - 1, 0), 7);
    }

    private void fillLevel(SignInStatusVO vo, Long userId) {
        int points = userRepository.findById(userId).map(u -> u.getPoints() == null ? 0 : u.getPoints()).orElse(0);
        vo.setPoints(points);
        int idx = 0;
        for (int i = 0; i < LEVEL_POINTS.length; i++) {
            if (points >= LEVEL_POINTS[i]) {
                idx = i;
            }
        }
        vo.setLevelName(LEVEL_NAMES[idx]);
        vo.setLevelMinPoints(LEVEL_POINTS[idx]);
        vo.setNextLevelPoints(idx + 1 < LEVEL_POINTS.length ? LEVEL_POINTS[idx + 1] : -1);
    }
}
