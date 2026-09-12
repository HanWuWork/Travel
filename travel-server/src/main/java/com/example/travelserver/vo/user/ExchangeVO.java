package com.example.travelserver.vo.user;

import lombok.Data;

import java.util.List;

/**
 * 汇率 VO
 */
@Data
public class ExchangeVO {

    private String from;
    private String to;
    private Double amount;

    /** 1 单位 from 兑换 to 的汇率 */
    private Double rate;

    /** 换算结果 */
    private Double result;

    /** 汇率更新时间 */
    private String updateTime;

    /** 常用货币对人民币的参考汇率（仅 from=CNY 时返回） */
    private List<RateItem> commonRates;

    @Data
    public static class RateItem {
        private String code;
        private String name;
        private Double rate;

        public RateItem() {
        }

        public RateItem(String code, String name, Double rate) {
            this.code = code;
            this.name = name;
            this.rate = rate;
        }
    }
}
