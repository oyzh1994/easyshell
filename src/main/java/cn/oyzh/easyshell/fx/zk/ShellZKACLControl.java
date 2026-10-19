package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.easyshell.dto.zk.ShellZKACL;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * ZooKeeper 权限控件
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKACLControl extends ShellZKACL {

    /**
     * 是否已认证
     */
    private boolean authed;

    /**
     * 是否友好
     */
    private boolean friendly;

    /**
     * 是否已认证
     *
     * @return 结果
     */
    public boolean isAuthed() {
        return authed;
    }

    /**
     * 设置是否已认证
     *
     * @param authed 是否已认证
     */
    public void setAuthed(boolean authed) {
        this.authed = authed;
    }

    /**
     * 是否友好显示
     *
     * @return 结果
     */
    public boolean isFriendly() {
        return friendly;
    }

    /**
     * 设置是否友好显示
     *
     * @param friendly 是否友好显示
     */
    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }

    /**
     * 获取id显示值
     *
     * @return id显示值
     */
    public String getIdControl() {
        return (String) super.idFriend().getValue(this.friendly);
    }

    /**
     * 获取权限显示值
     *
     * @return 权限显示值
     */
    public String getPermsControl() {
        return (String) super.permsFriend().getValue(this.friendly);
    }

    /**
     * 获取协议显示值
     *
     * @return 协议显示值
     */
    public String getSchemaControl() {
        return (String) super.schemeFriend().getValue(this.friendly);
    }

    /**
     * 获取状态控件
     *
     * @return 状态控件
     */
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
