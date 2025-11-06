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
import cn.oyzh.easyshell.fx.svg.glyph.protocol.RDPSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.RLoginSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.S3SVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.SFTPSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.SMBSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.SerialPortSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.TelnetSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.VNCSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.protocol.WebdavSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.redis.RedisSVGGlyph;
import cn.oyzh.easyshell.fx.svg.glyph.zk.ZookeeperSVGGlyph;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.fx.gui.svg.glyph.database.MysqlSVGGlyph;
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
        this.addItem(ShellPrototype.SFTP);
        this.addItem(ShellPrototype.FTP);
        this.addItem(ShellPrototype.S3);
        this.addItem(ShellPrototype.VNC);
        this.addItem(ShellPrototype.RDP);
        this.addItem(ShellPrototype.SMB);
        this.addItem(ShellPrototype.MINIO);
        this.addItem(ShellPrototype.REDIS);
        this.addItem(ShellPrototype.RLOGIN);
        this.addItem(ShellPrototype.WEBDAV);
        this.addItem(ShellPrototype.TELNET);
        this.addItem(ShellPrototype.ZOOKEEPER);
        this.addItem(ShellPrototype.SERIAL_PORT);
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
        return switch (name.toLowerCase()) {
            case "macos" -> new AppleSVGGlyph();
            case "ubuntu" -> new UbuntuSVGGlyph();
            case "centos" -> new CentosSVGGlyph();
            case "windows" -> new WindowsSVGGlyph();
            case "arch" -> new ArchSVGGlyph();
            case "mint" -> new MintSVGGlyph();
            case "raspberrypi" -> new RaspberrypiSVGGlyph();
            case "redhat" -> new RedhatSVGGlyph();
            case "debian" -> new DebianSVGGlyph();
            case "deepin" -> new DeepinSVGGlyph();
            case "freebsd" -> new FreebsdSVGGlyph();
            case "fedora" -> new FedoraSVGGlyph();
            case "sftp" -> new SFTPSVGGlyph();
            case "ftp" -> new FTPSVGGlyph();
            case "smb" -> new SMBSVGGlyph();
            case "vnc" -> new VNCSVGGlyph();
            case "s3" -> new S3SVGGlyph();
            case "telnet" -> new TelnetSVGGlyph();
            case "serial" -> new SerialPortSVGGlyph();
            case "rlogin" -> new RLoginSVGGlyph();
            case "minio" -> new MinioSVGGlyph();
            case "alibaba cloud" -> new AlibabaCloudSVGGlyph();
            case "tencent cloud" -> new TencentCloudSVGGlyph();
            case "huawei cloud" -> new HuaweiCloudSVGGlyph();
            case "redis" -> new RedisSVGGlyph();
            case "zk", "zookeeper" -> new ZookeeperSVGGlyph();
            case "rdp" -> new RDPSVGGlyph();
            case "webdav" -> new WebdavSVGGlyph();
            case "mysql" -> new MysqlSVGGlyph();
            case "ssh" -> new LinuxSVGGlyph();
            default -> new LinuxSVGGlyph();
        };
    }

    public void selectType(String type) {
        if (StringUtil.equalsIgnoreCase(type, ShellPrototype.SFTP)) {
            super.select(ShellPrototype.SFTP);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.FTP)) {
            super.select(ShellPrototype.FTP);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.SMB)) {
            super.select(ShellPrototype.SMB);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.VNC)) {
            super.select(ShellPrototype.VNC);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.S3)) {
            super.select(ShellPrototype.S3);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.TELNET)) {
            super.select(ShellPrototype.TELNET);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.SERIAL_PORT)) {
            super.select(ShellPrototype.SERIAL_PORT);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.RLOGIN)) {
            super.select(ShellPrototype.RLOGIN);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.MINIO)) {
            super.select(ShellPrototype.MINIO);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.REDIS)) {
            super.select(ShellPrototype.REDIS);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.ZOOKEEPER)) {
            super.select(ShellPrototype.ZOOKEEPER);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.RDP)) {
            super.select(ShellPrototype.RDP);
        } else if (StringUtil.equalsIgnoreCase(type, ShellPrototype.WEBDAV)) {
            super.select(ShellPrototype.WEBDAV);
        } else {
            super.select("Linux");
        }
    }
}
