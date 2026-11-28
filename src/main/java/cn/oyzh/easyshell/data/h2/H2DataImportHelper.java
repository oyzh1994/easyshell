package cn.oyzh.easyshell.data.h2;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.util.h2.ShellH2DataUtil;

import java.util.List;
import java.util.Map;

/**
 * H2 数据导入转换工具。
 *
 * @since 2026-11-02
 */
public final class H2DataImportHelper {

    private H2DataImportHelper() {
    }

    public static List<String> toInsertSql(String tableName, List<H2Column> columns,
                                           List<Map<String, Object>> records) {
        return ShellH2DataUtil.toInsertSql(tableName, columns, records);
    }
}
