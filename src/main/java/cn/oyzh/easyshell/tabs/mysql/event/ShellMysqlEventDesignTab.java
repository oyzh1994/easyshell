package cn.oyzh.easyshell.tabs.mysql.event;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlBaseTab;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.EventSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * @author oyzh
 * @since 2024/09/09
 */
public class ShellMysqlEventDesignTab extends ShellMysqlBaseTab {

    {
        this.setClosable(true);
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/event/shellMysqlEventDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new EventSVGGlyph("12");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.eventName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedEvent();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    public MysqlEvent event() {
        return this.controller().getEvent();
    }

    public String eventName() {
        return this.event().getName();
    }

    @Override
    public MysqlDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param event 事件对象
     * @param item  db库树节点
     */
    public void init(MysqlEvent event, MysqlDatabaseTreeItem item) {
        this.controller().init(event, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellMysqlEventDesignTabController controller() {
        return (ShellMysqlEventDesignTabController) super.controller();
    }

    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
