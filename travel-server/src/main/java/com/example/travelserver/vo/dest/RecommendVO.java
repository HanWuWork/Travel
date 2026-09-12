package com.example.travelserver.vo.dest;

import lombok.Data;

/**
 * 推荐结果 VO
 */
@Data
public class RecommendVO {

    /** 推荐城市（含推荐理由） */
    private java.util.List<CityReco> cities;

    /** 推荐景点（基于收藏城市） */
    private java.util.List<AttractionVO> attractions;

    /** 推荐依据说明，如"根据你的收藏与足迹推荐"或"热门推荐" */
    private String basis;

    /** 是否基于个人偏好（false 表示无个人数据，走热门兜底） */
    private Boolean personalized;

    @Data
    public static class CityReco {
        private Long id;
        private String name;
        private String province;
        private String description;
        private Boolean hot;

        /** 推荐理由 */
        private String reason;

        /** 内部排序分（不对外展示也用不上，但便于调试） */
        private Double score;
    }
}
