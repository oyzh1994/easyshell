package cn.oyzh.easyshell.fx.h2;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.beans.property.SimpleObjectProperty;

/**
 * H2 字段表格。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2ColumnTableView extends FXTableView<H2Column> {

    @Override
    public void initNode() {
        this.getColumns().clear();
        this.getColumns().add(this.column(I18nHelper.name(), column -> column.getName()));
        this.getColumns().add(this.column(I18nHelper.type(), column -> column.getType()));
        this.getColumns().add(this.column(I18nResourceBundle.i18nString("shell.h2.notNull"),
                column -> column.isNotNull()));
        this.getColumns().add(this.column(I18nHelper.primaryKey(), column -> column.isPrimaryKey()));
        this.getColumns().add(this.column(I18nHelper.autoIncrement(), column -> column.isAutoIncrement()));
        this.getColumns().add(this.column(I18nHelper.defaultValue(), column -> column.getDefaultValue()));
        this.getColumns().add(this.column(I18nResourceBundle.i18nString("base.hidden"), column -> column.isHidden()));
        super.initNode();
    }

    private FXTableColumn<H2Column, Object> column(String title, java.util.function.Function<H2Column, Object> getter) {
        FXTableColumn<H2Column, Object> column = new FXTableColumn<>(title);
        column.setCellValueFactory(param -> new SimpleObjectProperty<>(getter.apply(param.getValue())));
        return column;
    }
}
