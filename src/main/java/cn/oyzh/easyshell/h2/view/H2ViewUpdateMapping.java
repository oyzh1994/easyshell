package cn.oyzh.easyshell.h2.view;

import cn.oyzh.common.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * H2 简单视图到基表的更新映射。
 *
 * @since 2026-11-28
 */
public final class H2ViewUpdateMapping {

    private final String tableName;

    /**
     * 视图字段名 -> 基表字段名
     */
    private final Map<String, String> columns;

    /**
     * 视图虚拟字段名 -> 表达式
     */
    private final Map<String, String> virtualColumns;

    public H2ViewUpdateMapping(String tableName, Map<String, String> columns,
                               Map<String, String> virtualColumns) {
        this.tableName = tableName;
        this.columns = Map.copyOf(new LinkedHashMap<>(columns));
        this.virtualColumns = Map.copyOf(new LinkedHashMap<>(virtualColumns));
    }

    public String getTableName() {
        return tableName;
    }

    public Map<String, String> getColumns() {
        return columns;
    }

    public Map<String, String> getVirtualColumns() {
        return virtualColumns;
    }

    public boolean isVirtualColumn(String viewColumn) {
        return this.virtualColumns.keySet().stream()
                .anyMatch(item -> StringUtil.equalsIgnoreCase(item, viewColumn));
    }

    public String virtualExpression(String viewColumn) {
        if (StringUtil.isBlank(viewColumn)) {
            return null;
        }
        for (Map.Entry<String, String> entry : this.virtualColumns.entrySet()) {
            if (StringUtil.equalsIgnoreCase(entry.getKey(), viewColumn)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String sourceColumn(String viewColumn) {
        if (StringUtil.isBlank(viewColumn)) {
            return null;
        }
        for (Map.Entry<String, String> entry : this.columns.entrySet()) {
            if (StringUtil.equalsIgnoreCase(entry.getKey(), viewColumn)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
