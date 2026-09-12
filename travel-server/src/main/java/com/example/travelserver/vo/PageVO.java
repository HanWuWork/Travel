package com.example.travelserver.vo;

import lombok.Data;

import java.util.List;

/**
 * 通用分页结果封装
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageVO<T> {

    /** 当前页数据 */
    private List<T> list;

    /** 符合条件的总记录数 */
    private long total;

    /** 是否还有下一页 */
    private boolean hasMore;

    public PageVO() {
    }

    public PageVO(List<T> list, long total, boolean hasMore) {
        this.list = list;
        this.total = total;
        this.hasMore = hasMore;
    }
}