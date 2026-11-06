package cn.oyzh.easyshell.tabs.postgresql.sequence;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlSequence;
import cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlBaseTab;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.SequenceSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * PostgreSQL序列设计标签页，负责序列信息的展示与设计维护
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlSequenceDesignTab extends ShellPostgresqlBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "postgresql/sequence/shellPostgresqlSequenceDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SequenceSVGGlyph graphic = (SequenceSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new SequenceSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.sequenceName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedProcedure();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.schema() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.schema() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取序列对象
     *
     * @return 序列对象
     */
    public PostgresqlSequence sequence() {
        return this.controller().getSequence();
    }

    /**
     * 获取序列名称
     *
     * @return 序列名称
     */
    public String sequenceName() {
        return this.sequence().getName();
    }

    @Override
    public ShellPostgresqlSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param sequence 查询对象
     * @param item      db库树节点
     */
    public void init(PostgresqlSequence sequence, ShellPostgresqlSchemaTreeItem item) {
        this.controller().init(sequence, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellPostgresqlSequenceDesignTabController controller() {
        return (ShellPostgresqlSequenceDesignTabController) super.controller();
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
