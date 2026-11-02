package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;

public final class LLMListViewVirtualFlow extends VirtualFlow<ListCell<LLMMessage>> {

    public void invalidateSizeEstimates() {
        int cellCount = this.getCellCount();
        if (cellCount == 0) {
            return;
        }
        this.setCellCount(cellCount + 1);
        this.setCellCount(cellCount);
    }

    public ScrollBar getVerticalScrollBar() {
        return this.getVbar();
    }
}