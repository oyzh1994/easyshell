package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMessage;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;

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

    /**
     * 只重新测量指定单元格并重新布局当前可视区域。
     *
     * @param cell 单元格
     */
    public void refreshCellLayout(ListCell<LLMMessage> cell) {
        if (cell != null) {
            cell.setPrefHeight(Region.USE_COMPUTED_SIZE);
            cell.requestLayout();
        }
        this.requestLayout();
    }
}
