package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;

public final class LLMListViewSkin extends ListViewSkin<LLMMessage> {

    private LLMListViewVirtualFlow messageFlow;

    public LLMListViewSkin(ListView<LLMMessage> control) {
        super(control);
    }

    @Override
    protected VirtualFlow<ListCell<LLMMessage>> createVirtualFlow() {
        this.messageFlow = new LLMListViewVirtualFlow();
        return this.messageFlow;
    }

    public void resetCellSizeEstimates() {
        if (this.messageFlow != null) {
            this.messageFlow.invalidateSizeEstimates();
        }
    }

    /**
     * 重新测量指定单元格。
     *
     * @param cell 单元格
     */
    public void refreshCellLayout(ListCell<LLMMessage> cell) {
        if (this.messageFlow != null) {
            this.messageFlow.refreshCellLayout(cell);
        }
    }

    public ScrollBar getVerticalScrollBar() {
        return this.messageFlow == null ? null : this.messageFlow.getVerticalScrollBar();
    }
}
