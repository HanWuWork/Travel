package com.example.travelserver.service.user;

import com.example.travelserver.vo.user.ExchangeVO;

/**
 * 汇率换算服务（数据源：open.er-api.com 免费接口，无需 Key）
 */
public interface ExchangeService {

    /**
     * 换算
     *
     * @param from   源货币代码（如 CNY）
     * @param to     目标货币代码（如 USD）
     * @param amount 金额
     */
    ExchangeVO convert(String from, String to, double amount);
}
