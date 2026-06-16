package cn.oyzh.easyshell.fx.file;

import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.fx.plus.controls.table.FXTableCell;
import cn.oyzh.fx.plus.controls.table.FakerResizeTableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author oyzh
 * @since 2026-05-27
 */
public class ShellFileSizeTableColumn extends FakerResizeTableColumn<ShellFile, Long> {

    @Override
    protected Callback<TableColumn<ShellFile, Long>, TableCell<ShellFile, Long>> cellFactory() {
        return col -> new FXTableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    super.setText(null);
                    super.setGraphic(null);
                } else {
                    ShellFile file = this.getTableItem();
                    super.setText(file.getFileSizeDisplay());
                }
            }
        };
    }

    @Override
    public void initNode() {
        super.setFakerMode(false);
        this.setCellFactory(this.cellFactory());
        super.initNode();
    }
}
