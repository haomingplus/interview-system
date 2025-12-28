package com.interviewkb.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PageResult() {}

    public PageResult(IPage<T> page) {
        this.records = page.getRecords();
        this.total = page.getTotal();
        this.size = page.getSize();
        this.current = page.getCurrent();
        this.pages = page.getPages();
        this.hasNext = page.getCurrent() < page.getPages();
        this.hasPrevious = page.getCurrent() > 1;
    }

    public PageResult(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = (total + size - 1) / size;
        this.hasNext = current < this.pages;
        this.hasPrevious = current > 1;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page);
    }

    public static <T> PageResult<T> of(List<T> records, long total, long size, long current) {
        return new PageResult<>(records, total, size, current);
    }
}
