package com.example.travelserver.service.user;

import com.example.travelserver.vo.user.PackingItemVO;
import com.example.travelserver.vo.user.PackingSummaryVO;

/**
 * 打包清单服务
 */
public interface PackingService {

    /** 清单（按分类分组 + 完成度）；tripId 为空表示通用清单 */
    PackingSummaryVO summary(Long userId, Long tripId);

    /** 新增条目 */
    PackingItemVO add(Long userId, Long tripId, String name, String category);

    /** 勾选/取消勾选 */
    PackingItemVO toggle(Long userId, Long itemId);

    /** 删除条目 */
    void delete(Long userId, Long itemId);

    /**
     * 从模板生成清单（证件/衣物/电子/洗漱/药品/其他），已存在的同名条目会跳过
     *
     * @return 新增条目数
     */
    int applyTemplate(Long userId, Long tripId);

    /** 清空清单 */
    void clear(Long userId, Long tripId);
}
