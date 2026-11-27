package cn.oyzh.easyshell.popups.mongo;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.util.mongo.ShellMongoNodeUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * MongoDB 字段信息弹窗业务。
 * <p>
 * MongoDB 的字段没有注释、长度等信息，这里展示字段名、BSON 类型、标签以及所属的库和集合。
 *
 * @author oyzh
 * @since 2026-11-27
 */
@PopupAttribute(
        cssUrls = FXConst.CSS_PATH + "db/db.css",
        anchorLocation = PopupWindow.AnchorLocation.CONTENT_TOP_LEFT,
        value = FXConst.POPUP_PATH + "mongo/shellMongoFieldInfoPopup.fxml"
)
public class ShellMongoFieldInfoPopupController extends PopupController {

    /**
     * 标签行高度
     */
    private static final double TAG_ROW_HEIGHT = 30;

    /**
     * 弹窗初始高度，不含标签行
     */
    private double baseHeight;

    /**
     * 名称
     */
    @FXML
    private TextField name;

    /**
     * 类型
     */
    @FXML
    private TextField type;

    /**
     * 标签组件
     */
    @FXML
    private FXHBox tagsBox;

    /**
     * 标签容器
     */
    @FXML
    private FXHBox tagsContainer;

    /**
     * 库名称
     */
    @FXML
    private TextField database;

    /**
     * 集合名称
     */
    @FXML
    private TextField collection;

    /**
     * 关闭
     */
    @FXML
    private void close() {
        this.closeWindow();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        MongoColumn column = this.getProp("column");
        this.fillColumn(column);
    }

    /**
     * 填充字段信息
     *
     * @param column 字段信息
     */
    public void fillColumn(MongoColumn column) {
        if (column == null) {
            return;
        }
        this.name.setText(column.getName());
        this.type.setText(column.getType());
        this.database.setText(column.getDbName());
        this.collection.setText(column.getCollectionName());
        this.fillTags(column);
    }

    /**
     * 填充字段标签
     *
     * @param column 字段信息
     */
    private void fillTags(MongoColumn column) {
        this.tagsContainer.getChildren().clear();
        List<FXLabel> tags = ShellMongoNodeUtil.generateTags(column);
        boolean showTags = CollectionUtil.isNotEmpty(tags);
        if (showTags) {
            this.tagsContainer.addChild(tags);
            this.tagsBox.display();
            boolean first = true;
            for (FXLabel tag : tags) {
                HBox.setMargin(tag, new Insets(5, 0, 0, first ? 10 : 5));
                first = false;
            }
        } else {
            this.tagsBox.setVisible(false);
        }
        // 标签行展示后容器高度需要同步增加，否则五行内容会被压缩
        if (this.baseHeight > 0 && this.tagsBox.getParent() instanceof FXVBox box) {
            box.setRealHeight(this.baseHeight + (showTags ? TAG_ROW_HEIGHT : 0));
        }
    }

    @Override
    public void onPopupInitialize(PopupAdapter window) {
        super.onPopupInitialize(window);
        this.tagsBox.managedBindVisible();
        // 记录初始高度，作为是否显示标签行的基准
        if (this.tagsBox.getParent() instanceof FXVBox box) {
            this.baseHeight = Math.max(box.getRealHeight(), box.getPrefHeight());
        }
    }
}
