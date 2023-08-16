package cn.oyzh.easyssh.fx;

import cn.oyzh.easyfx.controls.FXHBox;
import cn.oyzh.easyfx.controls.FXText;
import cn.oyzh.easyfx.svg.SVGGlyph;
import cn.oyzh.easyfx.util.FXUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


/**
 * ssh树键值
 *
 * @author oyzh
 * @since 2023/07/7
 */
@Slf4j
@Accessors(chain = true, fluent = true)
public class SSHTreeItemValue {

    /**
     * 图标
     */
    @Getter
    protected Node graphic;

    private SimpleObjectProperty<FXHBox> rootNodeProperty;

    public SimpleObjectProperty<FXHBox> rootNodeProperty() {
        if (this.rootNodeProperty == null) {
            this.rootNodeProperty = new SimpleObjectProperty<>();
        }
        return this.rootNodeProperty;
    }

    public FXHBox getRootNode() {
        return this.rootNodeProperty == null ? null : this.rootNodeProperty().get();
    }

    public void setRootNode(FXHBox rootNode) {
        this.rootNodeProperty().set(rootNode);
    }

    /**
     * 键名称
     */
    @Getter
    @Accessors(fluent = true,chain = true)
    protected String nodeName;

    public SSHTreeItemValue(@NonNull String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * 移除键图标组件
     */
    protected void removeGraphic() {
        if (this.getRootNode() == null) {
            return;
        }
        // 移除键图标组件
        if (!(this.getRootNode().getChildren().get(0) instanceof FXText)) {
            this.getRootNode().getChildren().remove(0);
            if (log.isDebugEnabled()) {
                log.debug("remove graphic.");
            }
        }
    }

    /**
     * 初始化图标组件
     */
    protected void initGraphic() {
        if (this.getRootNode() == null) {
            return;
        }

        Node node = this.getRootNode().getChildren().get(0);
        // 移除图标
        if (this.graphic == null && !(node instanceof FXText)) {
            this.getRootNode().getChildren().remove(0);
            if (log.isDebugEnabled()) {
                log.debug("remove graphic.");
            }
            return;
        }

        if (this.graphic == null) {
            return;
        }

        boolean updateGraphic = false;
        // 添加图标
        if (node instanceof FXText) {
            this.getRootNode().getChildren().add(0, this.graphic);
            updateGraphic = true;
        } else if (this.graphic != node) { // 更新图标
            this.getRootNode().getChildren().set(0, this.graphic);
            updateGraphic = true;
        }

        // 更新图标
        if (updateGraphic) {
            HBox.setMargin(this.graphic, new Insets(0, 3, 0, 0));
            if (this.graphic instanceof SVGGlyph glyph) {
                glyph.setSize(13);
            }
        }
    }

    /**
     * 初始化图标组件
     */
    public void graphic(Node graphic) {
        if (this.graphic != graphic) {
            this.graphic = graphic;
            FXUtil.runLater(() -> {
                this.removeGraphic();
                this.initGraphic();
            });
        }
    }

    /**
     * 初始化键名称组件
     */
    protected void initNodeName() {
        FXText text = null;
        if (this.getRootNode().getChildren().size() == 1 && this.getRootNode().getChildren().get(0) instanceof FXText text1) {
            text = text1;
        } else if (this.getRootNode().getChildren().size() >= 2 && this.getRootNode().getChildren().get(1) instanceof FXText text1) {
            text = text1;
        }

        // 添加名称
        if (text == null) {
            text = new FXText(this.nodeName());
            this.getRootNode().getChildren().add(text);
        }
    }

    /**
     * 获取键名称组件
     *
     * @return 键名称组件
     */
    public Text nodeNameText() {
        if (this.getRootNode() == null) {
            return null;
        }
        if (this.getRootNode().getChildren().get(1) instanceof FXText text) {
            return text;
        }
        if (this.getRootNode().getChildren().get(0) instanceof FXText text) {
            return text;
        }
        return null;
    }

    /**
     * 创建组件
     *
     * @return 组件
     */
    public HBox create() {
        try {
            if (this.getRootNode() == null) {
                // 初始化根键
                this.setRootNode(new FXHBox());
                ;
                this.getRootNode().setCursor(Cursor.HAND);
                if (log.isDebugEnabled()) {
                    log.debug("create rootNode:{}", this.nodeName());
                }
            }
            // 初始化键名称组件
            this.initNodeName();
            // 初始化图标组件
            this.initGraphic();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.getRootNode();
    }

    /**
     * 销毁组件
     */
    public void destroy() {
        try {
            if (this.getRootNode() != null) {
                this.setRootNode(null);
                if (log.isDebugEnabled()) {
                    log.debug("destroy rootNode:{}", this.nodeName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
