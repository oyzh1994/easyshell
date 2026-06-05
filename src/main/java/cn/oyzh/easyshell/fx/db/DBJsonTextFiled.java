package cn.oyzh.easyshell.fx.db;


import cn.oyzh.fx.gui.text.field.LimitTextField;
import javafx.scene.control.Skin;

/**
 * @author oyzh
 * @since 2024/7/21
 */
@Deprecated
public class DBJsonTextFiled extends LimitTextField {

    public DBJsonTextFiled() {
        this.setSkin(new DBJsonTextFiledSkin(this));
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public DBJsonTextFiledSkin skin() {
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return (DBJsonTextFiledSkin) this.getSkin();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DBJsonTextFiledSkin(this);
    }

    public void setEnlargeWidth(double width) {
        this.skin().setEnlargeWidth(width);
    }

    public double getEnlargeWidth() {
        return this.skin().getEnlargeWidth();
    }

    public void setEnlargeHeight(double height) {
        this.skin().setEnlargeHeight(height);
    }

    public double getEnlargeHeight() {
        return this.skin().getEnlargeHeight();
    }
}
