package cn.oyzh.easyshell.query;

import cn.oyzh.fx.db.query.DBQueryPromptItem;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontUtil;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询提示列表公共基类。
 *
 * <p>提示项默认使用固定宽度和固定行高，长表名、字段名、schema 路径会被裁剪。
 * 这里统一按提示内容和扩展内容调整宽度，并按字体调整行高。</p>
 *
 * @author oyzh
 * @since 2024/02/21
 */
public abstract class ShellQueryPromptListView<E extends DBQueryPromptItem> extends DBQueryPromptListView<E> {

    /**
     * 提示列表最小宽度
     */
    private static final double MIN_WIDTH = 360;

    /**
     * 提示项最小行高
     */
    private static final double MIN_ROW_HEIGHT = 20;

    @Override
    public void init(List<E> items) {
        super.init(items);
        this.fixRowHeight();
        this.fitContentWidth();
    }

    @Override
    public void initNode() {
        super.initNode();
        if (!this.getItems().isEmpty()) {
            this.fixRowHeight();
            this.fitContentWidth();
        }
    }

    /**
     * 按当前字体调整行高和文字控件高度，避免文字上下被裁剪。
     */
    private void fixRowHeight() {
        double rowHeight = Math.max(MIN_ROW_HEIGHT, FontManager.currentFont().getSize() + 7);
        for (FXHBox row : this.getItems()) {
            row.setRealHeight(rowHeight);
            for (Node child : row.getChildren()) {
                if (child instanceof Region region) {
                    region.setMinHeight(rowHeight);
                    region.setPrefHeight(rowHeight);
                    region.setMaxHeight(rowHeight);
                }
            }
        }
    }

    /**
     * 根据提示内容和扩展内容调整列表宽度，避免长名称被裁剪。
     */
    private void fitContentWidth() {
        javafx.scene.text.Font font = FontManager.currentFont();
        List<E> prompts = this.promptItems();
        double contentWidth = 0;
        for (int i = 0; i < prompts.size(); i++) {
            E item = prompts.get(i);
            FXHBox row = this.getItems().get(i);
            // 图标 + 图标与文字间距
            double promptWidth = 24 + FontUtil.textWidth(item.getContent(), font) + 6;
            this.setControlWidth(row.getChildren().get(0), promptWidth);
            double rowWidth = promptWidth;
            if (item.getExtContent() != null && row.getChildren().size() > 1) {
                // 主提示与扩展信息之间的间距
                double extWidth = FontUtil.textWidth(item.getExtContent(), font) + 6;
                this.setControlWidth(row.getChildren().get(1), extWidth);
                rowWidth += 10 + extWidth;
            }
            contentWidth = Math.max(contentWidth, rowWidth);
        }
        double maxWidth = Screen.getPrimary().getVisualBounds().getWidth() * 0.8;
        double width = Math.min(Math.max(MIN_WIDTH, contentWidth + 24), maxWidth);
        this.setRealWidth(Math.ceil(width));
    }

    private void setControlWidth(Node node, double width) {
        if (node instanceof Region region) {
            region.setMinWidth(width);
            region.setPrefWidth(width);
            region.setMaxWidth(width);
        }
    }

    /**
     * 获取列表中的提示项。
     *
     * @return 提示项
     */
    @SuppressWarnings("unchecked")
    private List<E> promptItems() {
        List<E> items = new ArrayList<>();
        for (FXHBox row : this.getItems()) {
            Object item = row.getProp("item");
            if (item != null) {
                items.add((E) item);
            }
        }
        return items;
    }
}
