package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.easyshell.query.ShellQueryInfoUtil;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.RichTabController;
import javafx.fxml.FXML;

/**
 * MySQL 查询信息标签页控制器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlQueryInfoTabController extends RichTabController {

    /**
     * 信息编辑区
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
