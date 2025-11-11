package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不在列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MysqlNotInListCondition extends MysqlCondition {

    public final static MysqlNotInListCondition INSTANCE = new MysqlNotInListCondition();

    public MysqlNotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }

    // @Override
    // public String wrapCondition(Object condition) {
    //     if (condition instanceof String str) {
    //         String[] arr = str.split(",");
    //         StringBuilder sb = new StringBuilder();
    //         for (String s : arr) {
    //             sb.append(",").append(ShellMysqlUtil.wrapData(s));
    //         }
    //         if (!sb.isEmpty()) {
    //             return this.getValue() + " (" + sb.substring(1) + ")";
    //         }
    //     }
    //     return super.wrapCondition(condition);
    // }
}
