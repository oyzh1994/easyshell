package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.JsonTextFiledSkin;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author oyzh
 * @since 2026-06-11
 */
public class ShellMongoCodeTextFiledSkin extends JsonTextFiledSkin {

    public ShellMongoCodeTextFiledSkin(TextField textField) {
        super(textField);
    }

    @Override
    protected void onButtonClick(MouseEvent e) {
        super.onButtonClick(e);
        super.editor.setFormatType(EditorFormatType.SQL);
    }
}
