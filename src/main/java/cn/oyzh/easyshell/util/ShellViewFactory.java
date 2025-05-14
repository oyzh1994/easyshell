package cn.oyzh.easyshell.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.controller.AboutController;
import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.controller.SettingController;
import cn.oyzh.easyshell.controller.connect.ShellAddFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddGuidController;
import cn.oyzh.easyshell.controller.connect.ShellAddLocalConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddSFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddSSHConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddSerialConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddTelnetConnectController;
import cn.oyzh.easyshell.controller.connect.ShellExportConnectController;
import cn.oyzh.easyshell.controller.connect.ShellImportConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateLocalConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSSHConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSerialConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateTelnetConnectController;
import cn.oyzh.easyshell.controller.docker.ShellDockerInfoController;
import cn.oyzh.easyshell.controller.docker.ShellDockerVersionController;
import cn.oyzh.easyshell.controller.file.ShellFileEditController;
import cn.oyzh.easyshell.controller.file.ShellFileInfoController;
import cn.oyzh.easyshell.controller.file.ShellFileManageController;
import cn.oyzh.easyshell.controller.file.ShellFilePermissionController;
import cn.oyzh.easyshell.controller.file.ShellFileTransportController;
import cn.oyzh.easyshell.controller.tool.ShellToolController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

import java.io.File;

/**
 * shell页面工厂
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellViewFactory {

    /**
     * 新增引导
     *
     * @param group 分组
     */
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

    /**
     * 新增SSH连接
     *
     * @param group 分组
     */
    public static void addSSHConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddSSHConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 新增本地连接
     *
     * @param group 分组
     */
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

    /**
     * 新增telnet连接
     *
     * @param group 分组
     */
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

    /**
     * 新增串口连接
     *
     * @param group 分组
     */
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

    /**
     * 新增sftp连接
     *
     * @param group 分组
     */
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

    /**
     * 新增ftp连接
     *
     * @param group 分组
     */
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

    /**
     * 修改ssh连接
     *
     * @param connect 连接
     */
    public static void updateSSHConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateSSHConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 修改本地连接
     *
     * @param connect 连接
     */
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

    /**
     * 修改telnet连接
     *
     * @param connect 连接
     */
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

    /**
     * 修改串口连接
     *
     * @param connect 连接
     */
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

    /**
     * 修改sftp连接
     *
     * @param connect 连接
     */
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

    /**
     * 修改ftp连接
     *
     * @param connect 连接
     */
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
    public static void fileInfo(ShellFile file) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFileInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("file", file);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 文件传输
     *
     * @param sourceConnect 来源连接
     */
    public static void fileTransport(ShellConnect sourceConnect) {
//        // 判断窗口是否存在
//        List<StageAdapter> list = StageManager.listStage(ShellFileTransportController.class);
//        for (StageAdapter adapter : list) {
//            if (adapter.getProp("sourceConnect") == sourceConnect) {
//                adapter.toFront();
//                return;
//            }
//        }
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFileTransportController.class, null);
            adapter.setProp("sourceConnect", sourceConnect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

//    public static void ftpManage(ShellFTPClient client) {
//        try {
//            StageAdapter adapter = StageManager.parseStage(ShellFTPManageController.class);
//            adapter.setProp("client", client);
//            adapter.display();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    public static void ftpFilePermission(ShellFile file, ShellFTPClient client) {
//        try {
//            StageAdapter adapter = StageManager.parseStage(ShellFTPFilePermissionController.class);
//            adapter.setProp("file", file);
//            adapter.setProp("client", client);
//            adapter.display();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }
//
//    public static void sftpFilePermission(ShellFile file, ShellSFTPClient client) {
//        try {
//            StageAdapter adapter = StageManager.parseStage(ShellSFTPFilePermissionController.class);
//            adapter.setProp("file", file);
//            adapter.setProp("client", client);
//            adapter.display();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    public static void ftpFileEdit(ShellFile file, ShellFTPClient client) {
//        try {
//            StageAdapter adapter = StageManager.parseStage(ShellFTPFileEditController.class);
//            adapter.setProp("file", file);
//            adapter.setProp("client", client);
//            adapter.showAndWait();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    public static void sftpManage(ShellSFTPClient client) {
//        try {
//            StageAdapter adapter = StageManager.parseStage(ShellSFTPManageController.class);
//            adapter.setProp("client", client);
//            adapter.display();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    public static void sftpFileEdit(ShellFile file, ShellSFTPClient client) {
//        try {
//            StageAdapter adapter = StageManager.parseStage(ShellSFTPFileEditController.class);
//            adapter.setProp("file", file);
//            adapter.setProp("client", client);
//            adapter.showAndWait();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

    /**
     * 上传/下载管理
     *
     * @param client 文件客户端
     */
    public static void fileManage(ShellFileClient<?> client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFileManageController.class);
            adapter.setProp("client", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 文件编辑
     *
     * @param file   文件
     * @param client 文件客户端
     */
    public static void fileEdit(ShellFile file, ShellFileClient<?> client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFileEditController.class);
            adapter.setProp("file", file);
            adapter.setProp("client", client);
            adapter.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 文件权限
     *
     * @param file   文件
     * @param client 文件客户端
     */
    public static void filePermission(ShellFile file, ShellFileClient<?> client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFilePermissionController.class);
            adapter.setProp("file", file);
            adapter.setProp("client", client);
            adapter.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * docker信息
     *
     * @param info 信息
     */
    public static void dockerInfo(String info) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerInfoController.class, StageManager.getPrimaryStage());
            adapter.setProp("info", info);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * docker版本
     *
     * @param version 版本
     */
    public static void dockerVersion(String version) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerVersionController.class, StageManager.getPrimaryStage());
            adapter.setProp("version", version);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
