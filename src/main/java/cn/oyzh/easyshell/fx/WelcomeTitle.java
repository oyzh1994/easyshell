package cn.oyzh.easyshell.fx;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.fx.plus.controls.image.FXImageView;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 *
 * @author oyzh
 * @since 2025-11-17
 */
public class WelcomeTitle extends FXLabel {

    @Override
    public void initNode() {
        this.setFontSize(40);
        this.disableFontSize();
        Image image = new Image(ShellConst.ICON_PATH, 128, 128, true, true);
        FXImageView imageView = new FXImageView(image);
        imageView.setFitWidth(35);
        imageView.setFitHeight(35);
        this.setGraphic(imageView);
        Project project = Project.load();
        this.setText(project.getName() + "-v" + project.getVersion());
        super.initNode();
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
