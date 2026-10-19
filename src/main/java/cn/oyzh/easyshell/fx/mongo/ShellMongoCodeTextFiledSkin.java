package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.LongTextFiledSkin;
import javafx.scene.control.TextField;

/**
 * MongoDB代码文本输入框皮肤
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoCodeTextFiledSkin extends LongTextFiledSkin {

    /**
     * 构造器
     *
     * @param textField 文本输入框
     */
    public ShellMongoCodeTextFiledSkin(TextField textField) {
        super(textField);
    }

    @Override
    protected EditorFormatType getFormatType() {
        return EditorFormatType.SQL;
    }
}
