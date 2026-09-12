package com.example.travelserver.vo.dest;

import lombok.Data;

import java.util.List;

/**
 * 评价汇总 VO
 */
@Data
public class ReviewSummaryVO {

    /** 用户评价平均分（无评价时为 null） */
    private Double avgRating;

    /** 评价条数 */
    private long reviewCount;

    /** 各星级数量，下标 0 对应 5 星，依次递减 */
    private List<Long> distribution;

    /** 热门标签及计数 */
    private List<TagCount> topTags;

    @Data
    public static class TagCount {
        private String tag;
        private long count;

        public TagCount() {
        }

        public TagCount(String tag, long count) {
            this.tag = tag;
            this.count = count;
        }
    }
}
