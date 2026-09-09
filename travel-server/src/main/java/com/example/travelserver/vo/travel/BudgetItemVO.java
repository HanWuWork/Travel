package com.example.travelserver.vo.travel;

/**
 * 预算分配项
 */
public class BudgetItemVO {

    /** 类别名称（交通/住宿/餐饮/门票/购物等） */
    private String label;

    /** 占比百分比（0-100） */
    private Integer percent;

    /** 金额（元） */
    private Integer amount;

    /** 进度条颜色（hex） */
    private String color;

    public BudgetItemVO() {
    }

    public BudgetItemVO(String label, Integer percent, Integer amount, String color) {
        this.label = label;
        this.percent = percent;
        this.amount = amount;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
