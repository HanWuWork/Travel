package com.example.travelserver.vo.user;

import lombok.Data;

import java.util.List;

/**
 * 记账统计 VO
 */
@Data
public class ExpenseStatsVO {

    /** 总支出 */
    private double total;

    /** 记账笔数 */
    private long count;

    /** 分类汇总 */
    private List<CategoryStat> byCategory;

    /** 按日期汇总 */
    private List<DailyStat> byDate;

    /** 关联行程预算（未关联行程时为 null） */
    private Integer budget;

    /** 剩余预算（budget - total，未关联行程时为 null） */
    private Double remaining;

    /** 是否超支 */
    private Boolean overspend;

    @Data
    public static class CategoryStat {
        private String category;
        private double amount;
        private int percent;
        private String color;

        public CategoryStat() {
        }

        public CategoryStat(String category, double amount, int percent, String color) {
            this.category = category;
            this.amount = amount;
            this.percent = percent;
            this.color = color;
        }
    }

    @Data
    public static class DailyStat {
        private String date;
        private double amount;

        public DailyStat() {
        }

        public DailyStat(String date, double amount) {
            this.date = date;
            this.amount = amount;
        }
    }
}
