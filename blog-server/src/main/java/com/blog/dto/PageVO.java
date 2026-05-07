package com.blog.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageVO<T> {
    private List<T> records;
    private long total;
    private long page;
    private long size;

    public PageVO(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
