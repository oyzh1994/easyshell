package cn.oyzh.easyshell.tabs.h2.sequence;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;
import cn.oyzh.easyshell.tabs.h2.ShellH2BaseTab;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.sequence.ShellH2SequencesTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.SequenceSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * H2 序列设计页。
 *
 * @since 2026-11-28
 */
public class ShellH2SequenceDesignTab extends ShellH2BaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/sequence/shellH2SequenceDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
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
            name = I18nHelper.unnamedSequence();
        }
        String title = name + "@" + this.schema() + "(" + this.connectName() + ")";
        this.text(this.isUnsaved() ? "* " + title : title);
    }

    public void init(H2Sequence sequence, ShellH2SequencesTreeItem group) {
        this.controller().init(sequence, group);
        this.flush();
    }

    public String sequenceName() {
        return this.controller().sequenceName();
    }

    public String originalName() {
        return this.controller().originalName();
    }

    public boolean isNewData() {
        return this.controller().isNewData();
    }

    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    public String schema() {
        return this.controller().schema();
    }

    @Override
    public ShellH2RootTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    @Override
    public ShellH2SequenceDesignTabController controller() {
        return (ShellH2SequenceDesignTabController) super.controller();
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
