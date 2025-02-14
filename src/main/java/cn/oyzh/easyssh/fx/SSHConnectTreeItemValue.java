package cn.oyzh.easyssh.fx;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.easyfx.controls.FXText;
import cn.oyzh.easyfx.util.FXUtil;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


/**
 * SSH 连接树键值
 *
 * @author oyzh
 * @since 2023/08/10
 */
@Accessors(chain = true, fluent = true)
public class SSHConnectTreeItemValue extends SSHTreeItemValue {

    /**
     * 当前角色
     */
    private String role;

    /**
     * 是否cluster集群
     */
    @Setter
    private boolean cluster;

    /**
     * 是否master集群
     */
    @Setter
    private boolean master;

    /**
     * 是否只读
     */
    @Setter
    private boolean readOnly;

    public SSHConnectTreeItemValue(@NonNull String nodeName) {
        super(nodeName);
    }

    /**
     * 设置角色类型
     *
     * @param role 当前角色
     */
    public void role(String role) {
        if (StringUtil.equalsIgnoreCase("sentinel", role)) {
            this.role = "哨兵";
        } else if (StringUtil.equalsIgnoreCase("master", role)) {
            this.role = "主节点";
        } else if (StringUtil.equalsIgnoreCase("slave", role)) {
            this.role = "从节点";
        } else {
            this.role = null;
        }
    }

    /**
     * 清除角色组件
     */
    public void clearRole() {
        this.role = null;
        FXUtil.runLater(this::init);
    }

    /**
     * 初始化组件
     */
    public void init() {
        // 寻找组件
        FXText text = (FXText) this.getRootNode().lookup("#text");
        if (text != null && this.role == null) {
            this.getRootNode().getChildren().remove(text);
        } else if (text == null && this.role != null) {
            text = new FXText();
            text.setId("text");
            text.setFill(Color.valueOf("#228B22"));
            this.getRootNode().getChildren().add(text);
            HBox.setMargin(text, new Insets(0, 0, 0, 3));
            String str = "(" + this.role;
            if (this.cluster) {
                str += "-cluster集群";
            }
            if (this.master) {
                str += "-主从集群";
            }
            if (this.readOnly) {
                str += "-只读模式";
            }
            str += ")";
            text.setText(str);
        }
    }

    @Override
    public HBox create() {
        super.create();
        // 初始化组件
        this.init();
        return this.getRootNode();
    }

}
