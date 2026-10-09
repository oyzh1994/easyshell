package cn.oyzh.easyshell.mariadb.view;

/**
 * MariaDB修改视图参数
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbAlertViewParam {

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 视图
     */
    private MariadbView view;

    /**
     * 获取库名称
     *
     * @return 库名称
     */
    public String getDbName() {
        return this.dbName;
    }

    /**
     * 设置库名称
     *
     * @param dbName 库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 获取视图
     *
     * @return 视图
     */
    public MariadbView getView() {
        return view;
    }

    /**
     * 设置视图
     *
     * @param view 视图
     */
    public void setView(MariadbView view) {
        this.view = view;
    }
}
