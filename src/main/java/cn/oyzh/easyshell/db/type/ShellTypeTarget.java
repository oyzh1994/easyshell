package cn.oyzh.easyshell.db.type;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBTypeDefinition;

import java.util.List;

/**
 * 类型设计目标（模式节点）
 * <p>
 * 各支持自定义类型的数据库只需在模式树节点上实现本接口，即可复用类型树节点与类型设计页面。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public interface ShellTypeTarget {

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
     * 获取类型所属范围（模式名称）
     *
     * @return 范围名称
     */
    String typeScope();

    /**
     * 查询类型列表（仅包含名称与对象类型，不查询完整定义）
     *
     * @return 类型列表
     */
    List<? extends DBTypeDefinition> selectTypes();

    /**
     * 查询类型定义
     *
     * @param typeName 类型名称
     * @return 类型定义
     */
    String selectTypeDefinition(String typeName);

    /**
     * 执行类型相关SQL
     *
     * @param sqlList SQL列表
     */
    void executeTypeSql(List<String> sqlList);

    /**
     * 判断类型是否存在
     *
     * @param typeName 类型名称
     * @return 是否存在
     */
    boolean existType(String typeName);

    /**
     * 删除类型
     *
     * @param typeName    类型名称
     * @param objectType  对象类型（ENUM/DOMAIN/OBJECT 等，部分方言删除语句需要）
     */
    void dropType(String typeName, String objectType);

    /**
     * 创建一个新的类型对象
     *
     * @return 类型对象
     */
    DBTypeDefinition newType();

    /**
     * 刷新类型树节点
     */
    void reloadTypes();
}
