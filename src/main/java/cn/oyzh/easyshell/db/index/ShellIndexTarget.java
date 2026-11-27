package cn.oyzh.easyshell.db.index;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBIndex;

import java.util.List;

/**
 * 索引设计目标（模式节点或数据库节点）
 * <p>
 * 各数据库只需在模式/数据库树节点上实现本接口，即可复用索引树节点与索引设计页面。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public interface ShellIndexTarget {

    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    DBDialect dialect();

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    String connectName();

    /**
     * 获取索引所属范围（模式名称或数据库名称）
     *
     * @return 范围名称
     */
    String indexScope();

    /**
     * 查询索引列表（包含所属表名称）
     *
     * @return 索引列表
     */
    List<? extends DBIndex> selectIndexes();

    /**
     * 查询索引定义
     *
     * @param indexName 索引名称
     * @return 索引定义
     */
    String selectIndexDefinition(String indexName);

    /**
     * 执行索引相关SQL
     *
     * @param sqlList SQL列表
     */
    void executeIndexSql(List<String> sqlList);

    /**
     * 判断索引是否存在
     *
     * @param indexName 索引名称
     * @return 是否存在
     */
    boolean existIndex(String indexName);

    /**
     * 删除索引
     *
     * @param indexName 索引名称
     * @param tableName 表名称
     */
    void dropIndex(String indexName, String tableName);

    /**
     * 创建一个新的索引对象
     *
     * @return 索引对象
     */
    DBIndex newIndex();

    /**
     * 刷新索引树节点
     */
    void reloadIndexes();
}
