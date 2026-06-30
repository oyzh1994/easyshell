package cn.oyzh.easyshell.fx.mongo;


import cn.oyzh.fx.editor.incubator.control.JsonTextFiled;
import org.bson.types.Code;

/**
 * @author oyzh
 * @since 2024/7/21
 */
public class ShellMongoCodeTextFiled extends JsonTextFiled {

    @Override
    public ShellMongoCodeTextFiledSkin skin() {
        return (ShellMongoCodeTextFiledSkin) super.skin();
    }

    @Override
    protected ShellMongoCodeTextFiledSkin createDefaultSkin() {
        return new ShellMongoCodeTextFiledSkin(this);
    }

    @Override
    public void setArray(boolean array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue() {
        String text = this.getText();
        return new Code(text);
    }

    @Override
    public void formatValue() {
        this.setText(format(super.getValue()));
    }

    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return sequence.toString();
        }
        if (val instanceof Code code) {
            return code.getCode();
        }
        return val == null ? null : val.toString();
    }

}
