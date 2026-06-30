package cn.oyzh.easyshell.data.db.ui;

import cn.oyzh.easyshell.data.db.DBObjectStatus;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * @author oyzh
 * @since 2024/7/22
 */
public class DBStatusColumn<S extends DBObjectStatus> extends FXTableColumn<S, Object> {

    public DBStatusColumn() {
        this.setCellValueFactory(new PropertyValueFactory<>("status"));
        this.setMaxWidth(25);
        this.setRealWidth(25);
        this.setSortable(false);
        this.setResizable(false);
        this.setReorderable(false);
        this.text(I18nHelper.status());
    }

    @Override
    public void initNode() {
        // 仅显示图标
        this.showGraphicOnlyLater();
        super.initNode();
    }
}
