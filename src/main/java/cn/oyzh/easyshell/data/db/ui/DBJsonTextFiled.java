package cn.oyzh.easyshell.data.db.ui;


import cn.oyzh.fx.gui.text.field.LimitTextField;

/**
 * @author oyzh
 * @since 2024/7/21
 */
@Deprecated
public class DBJsonTextFiled extends LimitTextField {

    public DBJsonTextFiled() {
        this.setSkin(new DBJsonTextFiledSkin(this));
    }

    @Override
    public DBJsonTextFiledSkin skin() {
        return (DBJsonTextFiledSkin) super.skin();
    }

    @Override
    protected DBJsonTextFiledSkin createDefaultSkin() {
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
