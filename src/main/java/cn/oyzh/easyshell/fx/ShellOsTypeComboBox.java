package cn.oyzh.easyshell.fx;

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
import cn.oyzh.easyshell.fx.svg.glyph.other.AlibabaCloudSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.other.HuaweiCloudSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.other.MinioSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.other.TencentCloudSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.FTPSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.RLoginSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.S3SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.SFTPSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.SerialPortSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.TelnetSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.VNCSVGGlyph;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * shell 系统、协议、应用类型选择框
 *
 * @author oyzh
 * @since 2025/03/09
 */
public class ShellOsTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("Ubuntu");
        this.addItem("Centos");
        this.addItem("Debian");
        this.addItem("Arch");
        this.addItem("Raspberrypi");
        this.addItem("Deepin");
        this.addItem("Freebsd");
        this.addItem("Fedora");
        this.addItem("Redhat");
        this.addItem("Mint");
        this.addItem("Linux");
        this.addItem("Macos");
        this.addItem("Windows");
        this.addItem("SFTP");
        this.addItem("FTP");
        this.addItem("VNC");
        this.addItem("S3");
        this.addItem("Telnet");
        this.addItem("Serial");
        this.addItem("RLogin");
        this.addItem("Minio");
        this.addItem("Alibaba Cloud");
        this.addItem("Tencent Cloud");
        this.addItem("Huawei Cloud");
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
            case "Macos" -> new AppleSVGGlyph();
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
            case "SFTP" -> new SFTPSVGGlyph();
            case "FTP" -> new FTPSVGGlyph();
            case "VNC" -> new VNCSVGGlyph();
            case "S3" -> new S3SVGGlyph();
            case "Telnet" -> new TelnetSVGGlyph();
            case "Serial" -> new SerialPortSVGGlyph();
            case "RLogin" -> new RLoginSVGGlyph();
            case "Minio" -> new MinioSVGGlyph();
            case "Alibaba Cloud" -> new AlibabaCloudSVGGlyph();
            case "Tencent Cloud" -> new TencentCloudSVGGlyph();
            case "Huawei Cloud" -> new HuaweiCloudSVGGlyph();
            default -> new LinuxSVGGlyph();
        };
    }
}
