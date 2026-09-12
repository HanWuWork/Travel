package com.example.travelserver.vo.user;

import lombok.Data;

/**
 * 记账条目 VO
 */
@Data
public class ExpenseVO {

    private Long id;
    private Long tripId;
    private String category;
    private Double amount;
    private String note;
    private String expenseDate;
}
