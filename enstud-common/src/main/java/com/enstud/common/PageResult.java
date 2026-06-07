package com.enstud.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 游标分页响应（基于时间游标，避免 Offset 深翻页性能问题）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    /** 数据列表 */
    private List<T> records;
    /** 下一页游标 */
    private String cursor;
    /** 是否还有更多数据 */
    private Boolean hasMore;

    public static <T> PageResult<T> of(List<T> records, String cursor, Boolean hasMore) {
        return new PageResult<>(records, cursor, hasMore);
    }

    public static <T> PageResult<T> of(List<T> records, String cursor) {
        return new PageResult<>(records, cursor, false);
    }
}
