package com.example.travelserver.vo.user;

import lombok.Data;

import java.util.List;

/**
 * 打包清单汇总 VO（按分类分组 + 完成度）
 */
@Data
public class PackingSummaryVO {

    private long total;
    private long packed;

    /** 完成百分比 0-100 */
    private int percent;

    private List<Group> groups;

    @Data
    public static class Group {
        private String category;
        private List<PackingItemVO> items;

        public Group() {
        }

        public Group(String category, List<PackingItemVO> items) {
            this.category = category;
            this.items = items;
        }
    }
}
