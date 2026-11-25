package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.fx.editor.incubator.control.LongTextFiled;
import org.bson.types.Code;

/**
 * MongoDB代码文本输入框
 *
 * @author oyzh
 * @since 2024/7/21
 */
public class ShellMongoCodeTextFiled extends LongTextFiled {

    @Override
    public ShellMongoCodeTextFiledSkin skin() {
        return (ShellMongoCodeTextFiledSkin) super.skin();
    }

    @Override
    protected ShellMongoCodeTextFiledSkin createDefaultSkin() {
        return new ShellMongoCodeTextFiledSkin(this);
    }

    @Override
    public Object getValue() {
        String text = this.getText();
        return new Code(text == null ? "" : text);
    }

    @Override
    public void formatValue() {
        this.setText(format(super.value()));
    }

    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return sequence.toString();
        }
        if (val instanceof Code code) {
            return code.getCode() == null ? "" : code.getCode();
        }
        return val == null ? "" : val.toString();
    }
}
