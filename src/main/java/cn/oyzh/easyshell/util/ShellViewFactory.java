package cn.oyzh.easyshell.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.controller.AboutController;
import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.controller.SettingController;
import cn.oyzh.easyshell.controller.connect.ShellAddFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddGuidController;
import cn.oyzh.easyshell.controller.connect.ShellAddLocalConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddRLoginConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddSFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddSSHConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddSerialConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddTelnetConnectController;
import cn.oyzh.easyshell.controller.connect.ShellAddVNCConnectController;
import cn.oyzh.easyshell.controller.connect.ShellExportConnectController;
import cn.oyzh.easyshell.controller.connect.ShellImportConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateLocalConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateRLoginConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSSHConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateSerialConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateTelnetConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateVNCConnectController;
import cn.oyzh.easyshell.controller.docker.ShellDockerImageHistoryController;
import cn.oyzh.easyshell.controller.docker.ShellDockerInfoController;
import cn.oyzh.easyshell.controller.docker.ShellDockerInspectController;
import cn.oyzh.easyshell.controller.docker.ShellDockerLogsController;
import cn.oyzh.easyshell.controller.docker.ShellDockerPortController;
import cn.oyzh.easyshell.controller.docker.ShellDockerResourceController;
import cn.oyzh.easyshell.controller.docker.ShellDockerVersionController;
import cn.oyzh.easyshell.controller.file.ShellFileEditController;
import cn.oyzh.easyshell.controller.file.ShellFileInfoController;
import cn.oyzh.easyshell.controller.file.ShellFileManageController;
import cn.oyzh.easyshell.controller.file.ShellFilePermissionController;
import cn.oyzh.easyshell.controller.file.ShellFileTransportController;
import cn.oyzh.easyshell.controller.key.ShellAddKeyController;
import cn.oyzh.easyshell.controller.key.ShellCopyIdKeyController;
import cn.oyzh.easyshell.controller.key.ShellImportKeyController;
import cn.oyzh.easyshell.controller.key.ShellUpdateKeyController;
import cn.oyzh.easyshell.controller.split.ShellSplitGuidController;
import cn.oyzh.easyshell.controller.tool.ShellToolController;
import cn.oyzh.easyshell.controller.tunneling.ShellAddTunnelingController;
import cn.oyzh.easyshell.controller.tunneling.ShellUpdateTunnelingController;
import cn.oyzh.easyshell.docker.ShellDockerExec;
import cn.oyzh.easyshell.docker.ShellDockerImageHistory;
import cn.oyzh.easyshell.docker.ShellDockerPort;
import cn.oyzh.easyshell.docker.ShellDockerResource;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.popups.ShellTermHistoryPopupController;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.scene.Node;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

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

//    /**
//     * 新增ftps连接
//     *
//     * @param group 分组
//     */
//    public static void addFTPSConnect(ShellGroup group) {
//        try {
//            StageAdapter adapter = StageManager.parseStage(ShellAddFTPSConnectController.class);
//            adapter.setProp("group", group);
//            adapter.display();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

    /**
     * 新增vnc连接
     *
     * @param group 分组
     */
    public static void addVNCConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddVNCConnectController.class);
            adapter.setProp("group", group);
            adapter.display();
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 新增rlogin连接
     *
     * @param group 分组
     */
    public static void addRloginConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddRLoginConnectController.class);
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

//    /**
//     * 修改ftps连接
//     *
//     * @param connect 连接
//     */
//    public static void updateFTPSConnect(ShellConnect connect) {
//        try {
//            StageAdapter adapter = StageManager.parseStage(ShellUpdateFTPSConnectController.class);
//            adapter.setProp("shellConnect", connect);
//            adapter.display();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

    /**
     * 修改vnc连接
     *
     * @param connect 连接
     */
    public static void updateVNCConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateVNCConnectController.class);
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 修改rlogin连接
     *
     * @param connect 连接
     */
    public static void updateRloginConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateRLoginConnectController.class);
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
            StageAdapter adapter = StageManager.parseStage(ShellFileTransportController.class, StageManager.getPrimaryStage());
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
            StageAdapter adapter = StageManager.parseStage(ShellFileManageController.class, StageManager.getPrimaryStage());
            adapter.setProp("client", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 文件信息
     *
     * @param file  文件
     * @param owner 父窗口
     */
    public static void fileInfo(ShellFile file, Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFileInfoController.class, owner);
            adapter.setProp("file", file);
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
     * @param owner  父窗口
     */
    public static void fileEdit(ShellFile file, ShellFileClient<?> client, Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFileEditController.class, owner);
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
     * @param owner  父窗口
     */
    public static void filePermission(ShellFile file, ShellFileClient<?> client, Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFilePermissionController.class, owner);
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

    /**
     * docker历史
     *
     * @param histories 历史
     */
    public static void dockerHistory(List<ShellDockerImageHistory> histories) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerImageHistoryController.class, StageManager.getPrimaryStage());
            adapter.setProp("histories", histories);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * docker审查
     *
     * @param inspect 审查信息
     * @param image   是否镜像
     */
    public static void dockerInspect(String inspect, boolean image) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerInspectController.class, StageManager.getPrimaryStage());
            adapter.setProp("inspect", inspect);
            adapter.setProp("image", image);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * docker资源
     *
     * @param exec     执行器
     * @param resource 资源
     * @param id       id
     */
    public static void dockerResource(ShellDockerExec exec, ShellDockerResource resource, String id) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerResourceController.class, StageManager.getPrimaryStage());
            adapter.setProp("exec", exec);
            adapter.setProp("resource", resource);
            adapter.setProp("id", id);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * docker日志
     *
     * @param logs 日志
     */
    public static void dockerLogs(String logs) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerLogsController.class, StageManager.getPrimaryStage());
            adapter.setProp("logs", logs);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * docker端口
     *
     * @param ports 端口
     */
    public static void dockerPort(List<ShellDockerPort> ports) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerPortController.class, StageManager.getPrimaryStage());
            adapter.setProp("ports", ports);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 复制密钥到主机
     *
     * @param keys 密钥
     */
    public static void copyKeysToHost(List<ShellKey> keys) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellCopyIdKeyController.class, StageManager.getPrimaryStage());
            adapter.setProp("keys", keys);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 修改密钥
     *
     * @param key 密钥
     */
    public static void updateKey(ShellKey key) {
        try {
            if (key == null) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellUpdateKeyController.class, StageManager.getPrimaryStage());
            adapter.setProp("key", key);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加密钥
     */
    public static void addKey() {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddKeyController.class, StageManager.getPrimaryStage());
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 导入密钥
     */
    public static void importKey() {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellImportKeyController.class, StageManager.getPrimaryStage());
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加隧道
     */
    public static StageAdapter addTunneling() {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddTunnelingController.class, StageManager.getFrontWindow());
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 编辑隧道
     *
     * @param config 配置
     */
    public static StageAdapter updateTunneling(ShellTunnelingConfig config) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateTunnelingController.class, StageManager.getFrontWindow());
            adapter.setProp("config", config);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 分屏引导
     */
    public static void splitGuid() {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSplitGuidController.class, StageManager.getFrontWindow());
            adapter.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 终端历史
     *
     * @param parent   历史
     * @param client   ssh客户端
     * @param callback 回调函数
     */
    public static void termHistory(Node parent, ShellSSHClient client, Consumer<String> callback) {
        try {
            PopupAdapter adapter = PopupManager.parsePopup(ShellTermHistoryPopupController.class);
            adapter.setProp("client", client);
            adapter.setSubmitHandler(callback);
            adapter.showPopup(parent);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
