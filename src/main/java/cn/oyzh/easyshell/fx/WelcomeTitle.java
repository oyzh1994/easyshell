package cn.oyzh.easyshell.fx;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.tabs.home.ShellHomeEasterEggManager;
import cn.oyzh.fx.plus.controls.image.FXImageView;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 *
 * @author oyzh
 * @since 2025-11-17
 */
public class WelcomeTitle extends FXLabel {

    /**
     * Logo图片
     */
    private FXImageView logo;

    /**
     * 彩蛋管理器
     */
    private ShellHomeEasterEggManager easterEggManager;

    /**
     * 彩蛋动画层
     */
    private Pane easterEggLayer;

    @Override
    public void initNode() {
        this.setFontSize(40);
        this.disableFontSize();
        Image image = new Image(ShellConst.ICON_PATH, 128, 128, true, true);
        this.logo = new FXImageView(image);
        this.logo.setFitWidth(35);
        this.logo.setFitHeight(35);
        this.logo.setCursor(Cursor.HAND);
        this.logo.setPickOnBounds(true);
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, this::clickEasterEgg);
        this.setGraphic(this.logo);
        Project project = Project.load();
        this.setText(project.getName() + "-v" + project.getVersion());
        super.initNode();
    }

    /**
     * 获取Logo图片
     *
     * @return Logo图片
     */
    public FXImageView getLogo() {
        return this.logo;
    }

    private void clickEasterEgg(MouseEvent event) {
        Bounds logoBounds = this.logo.localToScene(this.logo.getBoundsInLocal());
        if (!logoBounds.contains(event.getSceneX(), event.getSceneY())) {
            return;
        }
        Pane layer = this.findEasterEggLayer();
        if (layer == null) {
            return;
        }
        if (this.easterEggManager == null || this.easterEggLayer != layer) {
            this.easterEggLayer = layer;
            this.easterEggManager = new ShellHomeEasterEggManager(this.logo, layer);
        }
        this.easterEggManager.click(event);
    }

    private Pane findEasterEggLayer() {
        Node parent = this.logo.getParent();
        while (parent != null) {
            if (parent instanceof Pane pane) {
                return pane;
            }
            parent = parent.getParent();
        }
        return null;
    }

//    @Override
//    public void changeTheme(ThemeStyle style) {
//        super.changeTheme(style);
//        // 颜色
//        RadialGradient radialGradient = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.REFLECT, new Stop(0, ThemeManager.currentForegroundColor()), new Stop(1, ThemeManager.currentAccentColor()));
//        this.setTextFill(radialGradient);
//        // 发光
//        this.setEffect(new Glow(0.6));
//    }
}
