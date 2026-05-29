package cn.oyzh.easyshell.fx.db;

import cn.oyzh.easyshell.db.DBObjectStatus;
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
        // 设置了文字，但是仅显示图标
        this.text(I18nHelper.status());
        this.showGraphicOnly();
    }
}
