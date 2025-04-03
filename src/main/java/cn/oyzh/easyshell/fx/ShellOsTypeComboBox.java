package cn.oyzh.easyshell.fx;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.RuntimeUtil;
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

import java.util.List;

/**
 * shell终端类型选择框
 *
 * @author oyzh
 * @since 23/03/09
 */
public class ShellOsTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("Ubuntu");
        this.addItem("Centos");
        this.addItem("Windows");
        this.addItem("Debian");
        this.addItem("Apple");
        this.addItem("Arch");
        this.addItem("Raspberrypi");
        this.addItem("Deepin");
        this.addItem("Freebsd");
        this.addItem("Fedora");
        this.addItem("Redhat");
        this.addItem("Mint");
        this.addItem("Linux");
        // 设置单元格工厂
        this.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setGraphic(getGlyph(item));
                        }
                    }
                };
            }
        });
        // 设置显示在下拉框中的单元格
        this.setButtonCell(this.getCellFactory().call(null));
    }

    public static SVGGlyph getGlyph(String name) {
        if (StringUtil.isBlank(name)) {
            return new LinuxSVGGlyph();
        }
        return switch (name) {
            case "Apple" -> new AppleSVGGlyph();
            case "Ubuntu" -> new UbuntuSVGGlyph();
            case "Centos" -> new CentosSVGGlyph();
            case "Windows" -> new WindowsSVGGlyph();
            case "Arch" -> new ArchSVGGlyph();
            case "Mint" -> new MintSVGGlyph();
            case "Raspberrypi" -> new RaspberrypiSVGGlyph();
            case "Redhat" -> new RedhatSVGGlyph();
            case "Debian" -> new DebianSVGGlyph();
            case "Deepin" -> new DeepinSVGGlyph();
            case "Freebsd" -> new FreebsdSVGGlyph();
            case "Fedora" -> new FedoraSVGGlyph();
            default -> new LinuxSVGGlyph();
        };
    }
}
