package cn.oyzh.easyshell.fx.mysql.record;


import cn.oyzh.easyshell.fx.mysql.record.DBJsonTextFiledSkin;
import cn.oyzh.fx.gui.text.field.LimitTextField;

/**
 * @author oyzh
 * @since 2024/7/21
 */
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
        return (DBJsonTextFiledSkin) this.getSkin();
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
