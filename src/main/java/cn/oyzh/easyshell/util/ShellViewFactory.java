package cn.oyzh.easyshell.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.controller.AboutController;
import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.controller.SettingController;
import cn.oyzh.easyshell.controller.connect.ShellAddConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddGuidController;
import cn.oyzh.easyshell.controller.connect.ShellAddLocalConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddSFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddSerialConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddTelnetConnectController;
import cn.oyzh.easyshell.controller.connect.ShellExportConnectController;
import cn.oyzh.easyshell.controller.connect.ShellImportConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateLocalConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSerialConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateTelnetConnectController;
import cn.oyzh.easyshell.controller.ftp.ShellFTPManageController;
import cn.oyzh.easyshell.controller.sftp.ShellSftpFileInfoController;
import cn.oyzh.easyshell.controller.sftp.ShellSftpTransportController;
import cn.oyzh.easyshell.controller.tool.ShellToolController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

import java.io.File;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellViewFactory {

    public static void addGuid(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddGuidController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void addConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void addLocalConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddLocalConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void addTelnetConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddTelnetConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void addSerialConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddSerialConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void addSFTPConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddSFTPConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void addFTPConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddFTPConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void updateConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void updateLocalConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateLocalConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void updateTelnetConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateTelnetConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void updateSerialConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateSerialConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void updateSFTPConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateSFTPConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void updateFTPConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateFTPConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 主页
     */
    public static void main() {
        try {
            StageAdapter adapter = StageManager.getStage(MainController.class);
            if (adapter != null) {
                JulLog.info("front main.");
                adapter.toFront();
            } else {
                JulLog.info("show main.");
                StageManager.showStage(MainController.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 设置
     */
    public static void setting() {
        try {
            StageAdapter adapter = StageManager.getStage(SettingController.class);
            if (adapter != null) {
                JulLog.info("front setting.");
                adapter.toFront();
            } else {
                JulLog.info("show setting.");
                StageManager.showStage(SettingController.class, StageManager.getPrimaryStage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 工具
     */
    public static void tool() {
        try {
            StageManager.showStage(ShellToolController.class, StageManager.getPrimaryStage());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 关于
     */
    public static void about() {
        try {
            StageManager.showStage(AboutController.class, StageManager.getPrimaryStage());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 导出连接
     */
    public static void exportConnect() {
        try {
            StageManager.showStage(ShellExportConnectController.class, StageManager.getPrimaryStage());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 导入连接
     */
    public static void importConnect(File file) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellImportConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("file", file);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 文件信息
     */
    public static void fileInfo(ShellSftpFile file) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSftpFileInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("file", file);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 传输数据
     *
     * @param sourceConnect 来源连接
     */
    public static void transportData(ShellConnect sourceConnect) {
        // 判断窗口是否存在
        List<StageAdapter> list = StageManager.listStage(ShellSftpTransportController.class);
        for (StageAdapter adapter : list) {
            if (adapter.getProp("sourceConnect") == sourceConnect) {
                adapter.toFront();
                return;
            }
        }
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSftpTransportController.class, null);
            adapter.setProp("sourceConnect", sourceConnect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public static void ftpManage(ShellFTPClient client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFTPManageController.class);
            adapter.setProp("client", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
