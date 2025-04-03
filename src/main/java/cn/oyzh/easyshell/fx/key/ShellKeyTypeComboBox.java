package cn.oyzh.easyshell.fx.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.svg.glyph.LinuxSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.AppleSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.ArchSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.CentosSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.DebianSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.DeepinSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.FedoraSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.FreebsdSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.MintSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.RaspberrypiSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.RedhatSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.UbuntuSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.os.WindowsSVGGlyph;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * shell密钥类型选择框
 *
 * @author oyzh
 * @since 23/03/09
 */
public class ShellKeyTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("RSA");
        this.addItem("ED25519");
    }
}
