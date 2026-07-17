package cn.oyzh.easyshell.mongo.user;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.ShellMongoHelper;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author oyzh
 * @since 2026-07-02
 */
public class MongoUserRoleDb {

    private String db;

    private Set<String> roles;

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> roles() {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        return this.getRoles();
    }

    public boolean isEmpty() {
        return CollectionUtil.isEmpty(this.roles);
    }

    private FXHBox rolesControl;

    private static final Insets ROLES_MAIGIN = new Insets(10, 0, 0, 0);

    public FXHBox getRolesControl() {
        if (this.rolesControl == null) {
            this.rolesControl = new FXHBox();
            for (String role : ShellMongoHelper.ROLES) {
                FXCheckBox checkBox = new FXCheckBox(role);
                checkBox.selectedChanged((observable, oldValue, newValue) -> {
                    if (newValue) {
                        this.roles().add(role);
                    } else {
                        this.roles().remove(role);
                    }
                });
                this.rolesControl.addChild(checkBox);
                HBox.setMargin(checkBox, ROLES_MAIGIN);
            }
        }
        return this.rolesControl;
    }

    public String getRolesText() {
        return StringUtil.join(",", this.roles);
    }
}
