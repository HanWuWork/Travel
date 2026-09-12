package com.example.travelserver.service.user;

import com.example.travelserver.vo.travel.TravelPlanVO;
import com.example.travelserver.vo.user.CollabVO;

import java.util.List;

/**
 * 行程协作服务：分享码、加入、共同编辑、成员管理
 */
public interface CollabService {

    /** 创建（或复用）分享码，仅行程所有者可操作 */
    CollabVO share(Long userId, Long tripId);

    /** 查看分享信息（凭分享码；未加入时 role=none） */
    CollabVO info(Long userId, String shareCode);

    /** 凭分享码加入协作 */
    CollabVO join(Long userId, String shareCode);

    /** 我参与协作的行程列表 */
    List<CollabVO> myCollabTrips(Long userId);

    /** 获取协作行程详情（含 plan 与 version，需为成员） */
    CollabVO detail(Long userId, Long tripId);

    /** 协作编辑保存（成员 + 允许编辑），版本号 +1 */
    CollabVO update(Long userId, Long tripId, TravelPlanVO plan);

    /** 成员列表 */
    List<CollabVO.Member> members(Long userId, Long tripId);

    /** 退出协作 / 移除成员（所有者可移除他人，成员可移除自己） */
    void removeMember(Long userId, Long tripId, Long memberId);
}
