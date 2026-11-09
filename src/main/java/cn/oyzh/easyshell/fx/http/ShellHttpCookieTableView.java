package cn.oyzh.easyshell.fx.http;

import cn.oyzh.easyshell.http.ShellHttpCookie;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * HTTP 响应 Cookie 表格。
 *
 * @author oyzh
 * @since 2026-11-09
 */
public class ShellHttpCookieTableView extends FXTableView<ShellHttpCookie> {

    @Override
    public void initNode() {
        super.initNode();
        this.setEditable(false);
        this.setFixedCellSize(28);
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        FXTableColumn<ShellHttpCookie, String> name = column(I18nHelper.name(), 140,
                cookie -> new ReadOnlyStringWrapper(cookie.getName()));
        FXTableColumn<ShellHttpCookie, String> value = column(I18nHelper.value(), 240,
                cookie -> new ReadOnlyStringWrapper(cookie.getValue()));
        FXTableColumn<ShellHttpCookie, String> domain = column(I18nHelper.domain(), 170,
                cookie -> new ReadOnlyStringWrapper(cookie.getDomain()));
        FXTableColumn<ShellHttpCookie, String> path = column(I18nHelper.path(), 100,
                cookie -> new ReadOnlyStringWrapper(cookie.getPath()));
        FXTableColumn<ShellHttpCookie, String> expires = column(
                ShellI18nHelper.httpCookieExpires(), 180,
                cookie -> new ReadOnlyStringWrapper(cookie.getExpiresText()));
        FXTableColumn<ShellHttpCookie, Boolean> httpOnly = booleanColumn(
                ShellI18nHelper.httpCookieHttpOnly(), 80,
                cookie -> new ReadOnlyBooleanWrapper(cookie.isHttpOnly()));
        FXTableColumn<ShellHttpCookie, Boolean> secure = booleanColumn(
                ShellI18nHelper.httpCookieSecure(), 70,
                cookie -> new ReadOnlyBooleanWrapper(cookie.isSecure()));
        FXTableColumn<ShellHttpCookie, String> sameSite = column(
                ShellI18nHelper.httpCookieSameSite(), 90,
                cookie -> new ReadOnlyStringWrapper(cookie.getSameSite()));

        this.getColumns().setAll(List.of(name, value, domain, path, expires, httpOnly, secure, sameSite));
    }

    private FXTableColumn<ShellHttpCookie, String> column(String title, double width,
                                                          java.util.function.Function<ShellHttpCookie,
                                                                  ReadOnlyStringWrapper> valueFactory) {
        FXTableColumn<ShellHttpCookie, String> column = new FXTableColumn<>();
        column.setText(title);
        column.setPrefWidth(width);
        column.setSortable(false);
        column.setResizable(true);
        column.setCellValueFactory(cell -> valueFactory.apply(cell.getValue()));
        return column;
    }

    private FXTableColumn<ShellHttpCookie, Boolean> booleanColumn(String title, double width,
                                                                  java.util.function.Function<ShellHttpCookie,
                                                                          ReadOnlyBooleanWrapper> valueFactory) {
        FXTableColumn<ShellHttpCookie, Boolean> column = new FXTableColumn<>();
        column.setText(title);
        column.setPrefWidth(width);
        column.setSortable(false);
        column.setResizable(true);
        column.setCellValueFactory(cell -> valueFactory.apply(cell.getValue()));
        column.setCellFactory(column1 -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                this.checkBox.setDisable(true);
                this.checkBox.setMouseTransparent(true);
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    this.setGraphic(null);
                    return;
                }
                this.checkBox.setSelected(item);
                this.setGraphic(this.checkBox);
            }
        });
        return column;
    }
}
