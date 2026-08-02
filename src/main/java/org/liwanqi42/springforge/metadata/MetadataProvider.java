package org.liwanqi42.springforge.metadata;

import org.liwanqi42.springforge.model.TableInfo;

import java.util.List;

/**
 * 表元数据提供者接口。
 *
 * <p>支持 JDBC 数据库直连和 JSON 配置文件两种输入方式。</p>
 */
public interface MetadataProvider {

    /**
     * 获取表元数据列表。
     *
     * @param tableNames 需要获取的表名列表（空表示全部）
     * @return 表信息列表
     */
    List<TableInfo> fetchMetadata(List<String> tableNames);
}
