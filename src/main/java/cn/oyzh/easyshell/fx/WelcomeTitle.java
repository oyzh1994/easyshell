package cn.oyzh.easyshell.fx;

import cn.oyzh.common.dto.Project;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.effect.Glow;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

/**
 *
 * @author oyzh
 * @since 2025-11-17
 */
public class WelcomeTitle extends FXText {

    @Override
    public void initNode() {
        super.initNode();
        this.setEnableFont(false);
        Project project = Project.load();
        this.setText(project.getName() + "-v" + project.getVersion());
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        super.changeTheme(style);
        this.setFont(Font.font(FontManager.currentFont().getFamily(), 40));
        // 颜色
        RadialGradient radialGradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5,
                true, CycleMethod.REFLECT,
                new Stop(0, ThemeManager.currentForegroundColor()),
                new Stop(1, ThemeManager.currentAccentColor())
        );
        this.setFill(radialGradient);
        // 发光
        this.setEffect(new Glow(0.6));
    }
}
