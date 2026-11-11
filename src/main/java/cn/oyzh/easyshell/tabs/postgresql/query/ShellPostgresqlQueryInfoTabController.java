package cn.oyzh.easyshell.tabs.postgresql.query;

import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.easyshell.query.ShellQueryInfoUtil;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

/**
 * PostgreSQL查询信息标签页控制器，负责将SQL执行结果信息输出到信息区域
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlQueryInfoTabController extends RichTabController {

    /**
     * 信息展示区域
     */
    @FXML
    private Editor infoArea;

    /**
     * 初始化
     *
     * @param results 结果集
     */
    public void init(DBQueryResults<?> results) {
        ShellQueryInfoUtil.initInfo(this.infoArea, results);
    }
}
