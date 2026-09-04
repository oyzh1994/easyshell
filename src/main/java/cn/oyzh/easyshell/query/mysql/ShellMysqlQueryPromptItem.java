package cn.oyzh.easyshell.query.mysql;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.query.DBQueryPromptItem;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * 查询提示内容
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class ShellMysqlQueryPromptItem extends DBQueryPromptItem {

    /**
     * 是否数据库类型
     *
     * @return 结果
     */
    public boolean isDatabaseType() {
        return 1 == this.getType();
    }

    /**
     * 是否表类型
     *
     * @return 结果
     */
    public boolean isTableType() {
        return 2 == this.getType();
    }

    /**
     * 是否字段类型
     *
     * @return 结果
     */
    public boolean isColumnType() {
        return 3 == this.getType();
    }

    /**
     * 是否关键字类型
     *
     * @return 结果
     */
    public boolean isKeywordType() {
        return 4 == this.getType();
    }

    /**
     * 是否视图类型
     *
     * @return 结果
     */
    public boolean isViewType() {
        return 5 == this.getType();
    }

    /**
     * 是否函数类型
     *
     * @return 结果
     */
    public boolean isFunctionType() {
        return 6 == this.getType();
    }

    /**
     * 是否过程类型
     *
     * @return 结果
     */
    public boolean isProcedureType() {
        return 7 == this.getType();
    }

    public String wrapContent( ) {
        if(this.isColumnType()){
            return DBUtil.wrap(this.getContent(), DBDialect.MYSQL);
        }
        return this.getContent();
    }
}
