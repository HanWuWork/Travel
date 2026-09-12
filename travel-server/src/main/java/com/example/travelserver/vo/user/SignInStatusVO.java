package com.example.travelserver.vo.user;

import lombok.Data;

import java.util.List;

/**
 * 签到状态 VO
 */
@Data
public class SignInStatusVO {

    /** 今日是否已签到 */
    private Boolean todaySigned;

    /** 当前连续签到天数 */
    private Integer consecutiveDays;

    /** 累计签到天数 */
    private Long totalDays;

    /** 当前积分 */
    private Integer points;

    /** 等级名称 */
    private String levelName;

    /** 当前等级所需起始积分 */
    private Integer levelMinPoints;

    /** 升级所需积分（-1 表示已达最高级） */
    private Integer nextLevelPoints;

    /** 今日签到可得积分（已签到时为实得） */
    private Integer todayPoints;

    /** 本月已签到日期（yyyy-MM-dd） */
    private List<String> monthSignedDates;

    /** 签到奖励规则说明 */
    private String rule;
}
