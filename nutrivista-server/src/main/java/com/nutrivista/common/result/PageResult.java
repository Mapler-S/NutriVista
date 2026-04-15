package com.nutrivista.common.result;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应封装
 *
 * @param <T> 列表元素类型
 */
@Getter
public class PageResult<T> {

    private final List<T> records;
    private final long total;
    private final int page;
    private final int pageSize;
    private final int totalPages;

    private PageResult(List<T> records, long total, int page, int pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    /**
     * 从 Spring Data Page 对象构建分页结果
     *
     * @param page Spring Data 分页对象
     * @param <T>  元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,  // 转为 1-based
                page.getSize()
        );
    }

    /**
     * 手动构建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, int page, int pageSize) {
        return new PageResult<>(records, total, page, pageSize);
    }

    /**
     * 包装为统一响应格式
     */
    public Result<PageResult<T>> toResult() {
        return Result.success(this);
    }
}
