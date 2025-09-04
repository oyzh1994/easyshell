package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.easyshell.dto.zk.ZKACL;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * zk权限
 *
 * @author oyzh
 * @since 2022/6/7
 */

public class ZKACLControl extends ZKACL {

    /**
     * 是否已认证
     */
    private boolean authed;

    /**
     * 是否友好
     */
    private boolean friendly;

    public boolean isAuthed() {
        return authed;
    }

    public void setAuthed(boolean authed) {
        this.authed = authed;
    }

    public boolean isFriendly() {
        return friendly;
    }

    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }

    public String getIdControl() {
        return (String) super.idFriend().getValue(this.friendly);
    }

    public String getPermsControl() {
        return (String) super.permsFriend().getValue(this.friendly);
    }

    public String getSchemaControl() {
        return (String) super.schemeFriend().getValue(this.friendly);
    }

    public FXText getStatusControl() {
        if (this.authed) {
            FXText text = new FXText();
            text.setFill(Color.FORESTGREEN);
            text.setText(I18nHelper.authed());
            return text;
        }
        return null;
    }
}
