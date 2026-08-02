package ${basePackage}.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
<#if useLombok>
    import lombok.Data;
</#if>

/**
 * 统一分页结果封装。
 *
 * @param <T> 数据类型
 */
<#if useLombok>
    @Data
</#if>
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private long pages;
    private long current;
    private long size;
    private List<T> records;

    private PageResult() {}

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> r = new PageResult<>();
        r.total = page.getTotal();
        r.pages = page.getPages();
        r.current = page.getCurrent();
        r.size = page.getSize();
        r.records = page.getRecords();
        return r;
    }

    public static <T> PageResult<T> empty() {
        PageResult<T> r = new PageResult<>();
        r.total = 0;
        r.pages = 0;
        r.current = 1;
        r.size = 10;
        r.records = Collections.emptyList();
        return r;
    }

<#if !useLombok>
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getPages() { return pages; }
    public void setPages(long pages) { this.pages = pages; }
    public long getCurrent() { return current; }
    public void setCurrent(long current) { this.current = current; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
</#if>
}
