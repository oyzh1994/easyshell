package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.query.DBQueryPromptItem;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * SQLite 查询提示项。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryPromptItem extends DBQueryPromptItem {

    /**
     * 是否表或视图。
     *
     * @return 结果
     */
    public boolean isTableType() {
        return this.getType() == 2 || this.getType() == 5;
    }

    /**
     * 是否字段。
     *
     * @return 结果
     */
    public boolean isColumnType() {
        return this.getType() == 3;
    }

    /**
     * 获取补全内容。
     *
     * @return 内容
     */
    public String wrapContent() {
        return this.isColumnType() ? DBUtil.wrap(this.getContent(), DBDialect.SQLITE) : this.getContent();
    }
}

