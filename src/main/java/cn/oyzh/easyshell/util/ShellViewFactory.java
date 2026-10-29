package cn.oyzh.easyshell.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.controller.AboutController;
import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.controller.SettingController;
import cn.oyzh.easyshell.controller.connect.ShellAddConnectGuidController;
import cn.oyzh.easyshell.controller.connect.dameng.ShellAddDamengConnectController;
import cn.oyzh.easyshell.controller.connect.dameng.ShellUpdateDamengConnectController;
import cn.oyzh.easyshell.controller.connect.ftp.ShellAddFTPConnectController;
import cn.oyzh.easyshell.controller.connect.ftp.ShellUpdateFTPConnectController;
import cn.oyzh.easyshell.controller.connect.local.ShellAddLocalConnectController;
import cn.oyzh.easyshell.controller.connect.local.ShellUpdateLocalConnectController;
import cn.oyzh.easyshell.controller.connect.mariadb.ShellAddMariadbConnectController;
import cn.oyzh.easyshell.controller.connect.mariadb.ShellUpdateMariadbConnectController;
import cn.oyzh.easyshell.controller.connect.mongo.ShellAddMongoConnectController;
import cn.oyzh.easyshell.controller.connect.mongo.ShellUpdateMongoConnectController;
import cn.oyzh.easyshell.controller.connect.mosh.ShellAddMoshConnectController;
import cn.oyzh.easyshell.controller.connect.mosh.ShellUpdateMoshConnectController;
import cn.oyzh.easyshell.controller.connect.mysql.ShellAddMysqlConnectController;
import cn.oyzh.easyshell.controller.connect.mysql.ShellUpdateMysqlConnectController;
import cn.oyzh.easyshell.controller.connect.rdp.ShellAddRDPConnectController;
import cn.oyzh.easyshell.controller.connect.rdp.ShellUpdateRDPConnectController;
import cn.oyzh.easyshell.controller.connect.redis.ShellAddRedisConnectController;
import cn.oyzh.easyshell.controller.connect.redis.ShellUpdateRedisConnectController;
import cn.oyzh.easyshell.controller.connect.rlogin.ShellAddRLoginConnectController;
import cn.oyzh.easyshell.controller.connect.rlogin.ShellUpdateRLoginConnectController;
import cn.oyzh.easyshell.controller.connect.s3.ShellAddS3ConnectController;
import cn.oyzh.easyshell.controller.connect.s3.ShellUpdateS3ConnectController;
import cn.oyzh.easyshell.controller.connect.serial.ShellAddSerialConnectController;
import cn.oyzh.easyshell.controller.connect.serial.ShellUpdateSerialConnectController;
import cn.oyzh.easyshell.controller.connect.sftp.ShellAddSFTPConnectController;
import cn.oyzh.easyshell.controller.connect.sftp.ShellUpdateSFTPConnectController;
import cn.oyzh.easyshell.controller.connect.smb.ShellAddSMBConnectController;
import cn.oyzh.easyshell.controller.connect.smb.ShellUpdateSMBConnectController;
import cn.oyzh.easyshell.controller.connect.ssh.ShellAddSSHConnectController;
import cn.oyzh.easyshell.controller.connect.ssh.ShellUpdateSSHConnectController;
import cn.oyzh.easyshell.controller.connect.telnet.ShellAddTelnetConnectController;
import cn.oyzh.easyshell.controller.connect.telnet.ShellUpdateTelnetConnectController;
import cn.oyzh.easyshell.controller.connect.vnc.ShellAddVNCConnectController;
import cn.oyzh.easyshell.controller.connect.vnc.ShellUpdateVNCConnectController;
import cn.oyzh.easyshell.controller.connect.webdav.ShellAddWebdavConnectController;
import cn.oyzh.easyshell.controller.connect.webdav.ShellUpdateWebdavConnectController;
import cn.oyzh.easyshell.controller.connect.zk.ShellAddZKConnectController;
import cn.oyzh.easyshell.controller.connect.zk.ShellUpdateZKConnectController;
import cn.oyzh.easyshell.controller.data.ShellDataExportController;
import cn.oyzh.easyshell.controller.data.ShellDataImportController;
import cn.oyzh.easyshell.controller.docker.ShellDockerCommitController;
import cn.oyzh.easyshell.controller.docker.ShellDockerImageHistoryController;
import cn.oyzh.easyshell.controller.docker.ShellDockerInfoController;
import cn.oyzh.easyshell.controller.docker.ShellDockerInspectController;
import cn.oyzh.easyshell.controller.docker.ShellDockerLogsController;
import cn.oyzh.easyshell.controller.docker.ShellDockerPortController;
import cn.oyzh.easyshell.controller.docker.ShellDockerResourceController;
import cn.oyzh.easyshell.controller.docker.ShellDockerRunController;
import cn.oyzh.easyshell.controller.docker.ShellDockerSaveController;
import cn.oyzh.easyshell.controller.docker.ShellDockerTagController;
import cn.oyzh.easyshell.controller.docker.ShellDockerVersionController;
import cn.oyzh.easyshell.controller.file.ShellFileEditController;
import cn.oyzh.easyshell.controller.file.ShellFileErrorController;
import cn.oyzh.easyshell.controller.file.ShellFileInfoController;
import cn.oyzh.easyshell.controller.file.ShellFileManageController;
import cn.oyzh.easyshell.controller.file.ShellFilePermissionController;
import cn.oyzh.easyshell.controller.file.ShellFilePkgUploadController;
import cn.oyzh.easyshell.controller.file.ShellFileTransportController;
import cn.oyzh.easyshell.controller.file.ShellFileViewController;
import cn.oyzh.easyshell.controller.jump.ShellAddHostController;
import cn.oyzh.easyshell.controller.jump.ShellAddJumpController;
import cn.oyzh.easyshell.controller.jump.ShellUpdateJumpController;
import cn.oyzh.easyshell.controller.key.ShellAddKeyController;
import cn.oyzh.easyshell.controller.key.ShellCopyIdKeyController;
import cn.oyzh.easyshell.controller.key.ShellImportKeyController;
import cn.oyzh.easyshell.controller.key.ShellUpdateKeyController;
import cn.oyzh.easyshell.controller.s3.ShellS3AddBucketController;
import cn.oyzh.easyshell.controller.s3.ShellS3ShareFileController;
import cn.oyzh.easyshell.controller.s3.ShellS3UpdateBucketController;
import cn.oyzh.easyshell.controller.snippet.ShellSnippetController;
import cn.oyzh.easyshell.controller.split.ShellSplitGuidController;
import cn.oyzh.easyshell.controller.ssh.ShellSSHAuthController;
import cn.oyzh.easyshell.controller.tool.ShellToolController;
import cn.oyzh.easyshell.controller.tunneling.ShellAddTunnelingController;
import cn.oyzh.easyshell.controller.tunneling.ShellUpdateTunnelingController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileTask;
import cn.oyzh.easyshell.popups.snippet.ShellSnippetPopupController;
import cn.oyzh.easyshell.popups.term.ShellTermHistoryPopupController;
import cn.oyzh.easyshell.s3.ShellS3Bucket;
import cn.oyzh.easyshell.s3.ShellS3ClientV2;
import cn.oyzh.easyshell.s3.ShellS3File;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.docker.ShellDockerContainer;
import cn.oyzh.easyshell.ssh.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh.docker.ShellDockerImage;
import cn.oyzh.easyshell.ssh.docker.ShellDockerImageHistory;
import cn.oyzh.easyshell.ssh.docker.ShellDockerPort;
import cn.oyzh.easyshell.ssh.docker.ShellDockerResource;
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
     * 新增连接引导
     *
     */
    public static void addConnectGuid() {
        addConnectGuid(null);
    }

    /**
     * 新增连接引导
     *
     * @param group 分组
     */
    public static void addConnectGuid(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddConnectGuidController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增SSH连接
     *
     * @param group 分组
     */
    public static void addSSHConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddSSHConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增本地连接
     *
     * @param group 分组
     */
    public static void addLocalConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddLocalConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增telnet连接
     *
     * @param group 分组
     */
    public static void addTelnetConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddTelnetConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增串口连接
     *
     * @param group 分组
     */
    public static void addSerialConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddSerialConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增sftp连接
     *
     * @param group 分组
     */
    public static void addSFTPConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddSFTPConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增ftp连接
     *
     * @param group 分组
     */
    public static void addFTPConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddFTPConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增s3连接
     *
     * @param group  分组
     * @param s3Type s3类型
     */
    public static void addS3Connect(ShellGroup group, String s3Type) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddS3ConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.setProp("s3Type", s3Type);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增vnc连接
     *
     * @param group 分组
     */
    public static void addVNCConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddVNCConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增RLogin连接
     *
     * @param group 分组
     */
    public static void addRLoginConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddRLoginConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增smb连接
     *
     * @param group 分组
     */
    public static void addSMBConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddSMBConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增redis连接
     *
     * @param group 分组
     */
    public static void addRedisConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddRedisConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增zk连接
     *
     * @param group 分组
     */
    public static void addZKConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddZKConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增rdp连接
     *
     * @param group 分组
     */
    public static void addRDPConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddRDPConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增webdav连接
     *
     * @param group 分组
     */
    public static void addWebdavConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddWebdavConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增mysql连接
     *
     * @param group 分组
     */
    public static void addMysqlConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddMysqlConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增MariaDB连接
     *
     * @param group 分组
     */
    public static void addMariadbConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddMariadbConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增dameng连接
     *
     * @param group 分组
     */
    public static void addDamengConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddDamengConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增mongodb连接
     *
     * @param group 分组
     */
    public static void addMongoConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddMongoConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 新增mosh连接
     *
     * @param group 分组
     */
    public static void addMoshConnect(ShellGroup group) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddMoshConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("group", group);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改ssh连接
     *
     * @param connect 连接
     */
    public static void updateSSHConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateSSHConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改本地连接
     *
     * @param connect 连接
     */
    public static void updateLocalConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateLocalConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改telnet连接
     *
     * @param connect 连接
     */
    public static void updateTelnetConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateTelnetConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改串口连接
     *
     * @param connect 连接
     */
    public static void updateSerialConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateSerialConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改sftp连接
     *
     * @param connect 连接
     */
    public static void updateSFTPConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateSFTPConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改ftp连接
     *
     * @param connect 连接
     */
    public static void updateFTPConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateFTPConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改s3连接
     *
     * @param connect 连接
     */
    public static void updateS3Connect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateS3ConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改vnc连接
     *
     * @param connect 连接
     */
    public static void updateVNCConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateVNCConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改RLogin连接
     *
     * @param connect 连接
     */
    public static void updateRLoginConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateRLoginConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改smb连接
     *
     * @param connect 连接
     */
    public static void updateSMBConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateSMBConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改redis连接
     *
     * @param connect 连接
     */
    public static void updateRedisConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateRedisConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改zk连接
     *
     * @param connect 连接
     */
    public static void updateZKConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateZKConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改rdp连接
     *
     * @param connect 连接
     */
    public static void updateRDPConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateRDPConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改webdav连接
     *
     * @param connect 连接
     */
    public static void updateWebdavConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateWebdavConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改mysql连接
     *
     * @param connect 连接
     */
    public static void updateMysqlConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateMysqlConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改MariaDB连接
     *
     * @param connect 连接
     */
    public static void updateMariadbConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateMariadbConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改dameng连接
     *
     * @param connect 连接
     */
    public static void updateDamengConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateDamengConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改mongo连接
     *
     * @param connect 连接
     */
    public static void updateMongoConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateMongoConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 修改mosh连接
     *
     * @param connect 连接
     */
    public static void updateMoshConnect(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateMoshConnectController.class, StageManager.getPrimaryStage());
            adapter.setProp("shellConnect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 主页
     */
    public static void shellMain() {
        try {
            Class<?> clazz = MainController.class;
            if (StageManager.frontStage(clazz)) {
                return;
            }
            StageManager.showStage(clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 设置
     */
    public static void setting() {
        try {
            StageAdapter adapter = StageManager.getStage(SettingController.class);
            if (adapter != null) {
                if (JulLog.isInfoEnabled()) {
                    JulLog.info("front setting.");
                }
                adapter.front();
            } else {
                if (JulLog.isInfoEnabled()) {
                    JulLog.info("show setting.");
                }
                StageManager.showStage(SettingController.class, StageManager.getPrimaryStage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 数据导出
     */
    public static void dataExport() {
        try {
            StageManager.showStage(ShellDataExportController.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }


    /**
     * 数据导入
     *
     */
    public static void dataImport() {
        dataImport(null);
    }

    /**
     * 数据导入
     *
     * @param file 文件
     */
    public static void dataImport(File file) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDataImportController.class);
            adapter.setProp("file", file);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * ssh认证
     *
     * @param connect 连接
     * @return 页面
     */
    public static StageAdapter sshAuth(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSSHAuthController.class, StageManager.resolveDialogOwner());
            adapter.setProp("connect", connect);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 文件传输
     *
     * @param sourceConnect 来源连接
     */
    public static void fileTransport(ShellConnect sourceConnect) {
        Class<?> clazz = ShellFileTransportController.class;
        if (StageManager.frontStage(clazz, "sourceConnect", sourceConnect)) {
            return;
        }
        try {
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("sourceConnect", sourceConnect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    //    public static void ftpManage(ShellFTPClient client) {
    //        try {
    //            StageAdapter adapter = StageManager.parseStage(ShellFTPManageController.class);
    //            adapter.setProp("client", client);
    //            adapter.display();
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            MessageBox.exception(ex, StageManager.getFrontWindow());
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
    //            MessageBox.exception(ex, StageManager.getFrontWindow());
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
    //            MessageBox.exception(ex, StageManager.getFrontWindow());
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
    //            MessageBox.exception(ex, StageManager.getFrontWindow());
    //        }
    //    }

    //    public static void sftpManage(ShellSFTPClient client) {
    //        try {
    //            StageAdapter adapter = StageManager.parseStage(ShellSFTPManageController.class);
    //            adapter.setProp("client", client);
    //            adapter.display();
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            MessageBox.exception(ex, StageManager.getFrontWindow());
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
    //            MessageBox.exception(ex, StageManager.getFrontWindow());
    //        }
    //    }

    /**
     * 上传/下载管理
     *
     * @param client 文件客户端
     */
    public static void fileManage(ShellFileClient<?> client) {
        try {
            Class<?> clazz = ShellFileManageController.class;
            if (StageManager.frontStage(clazz, "client", client)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("client", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, owner);
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
            Class<?> clazz = ShellFileEditController.class;
            if (StageManager.frontStage(clazz, "file", file)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("file", file);
            adapter.setProp("client", client);
            adapter.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 文件查看
     *
     * @param file   文件
     * @param client 文件客户端
     * @param type   类型
     */
    public static void fileView(ShellFile file, ShellFileClient<?> client, String type) {
        try {
            Class<?> clazz = ShellFileViewController.class;
            if (StageManager.frontStage(clazz, "file", file)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("file", file);
            adapter.setProp("type", type);
            adapter.setProp("client", client);
            adapter.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, owner);
        }
    }

    /**
     * 打包上传
     *
     * @param dest   目标路径
     * @param files  文件
     * @param client 文件客户端
     * @return 窗口
     */
    public static StageAdapter filePkgUpload(String dest, List<File> files, ShellSFTPClient client) {
        try {
            Class<?> clazz = ShellFilePkgUploadController.class;
            if (StageManager.frontStage(clazz, "client", client)) {
                return StageManager.getStage(clazz, "client", client);
            }
            StageAdapter adapter = StageManager.parseStage(ShellFilePkgUploadController.class);
            adapter.setProp("dest", dest);
            adapter.setProp("files", files);
            adapter.setProp("client", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 复制密钥到主机
     *
     * @param keys  密钥
     * @param owner 父窗口
     */
    public static void copyKeysToHost(List<ShellKey> keys, Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellCopyIdKeyController.class, owner);
            adapter.setProp("keys", keys);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, owner);
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 添加隧道
     */
    public static StageAdapter addTunneling() {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddTunnelingController.class, StageManager.getPrimaryStage());
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
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
            StageAdapter adapter = StageManager.parseStage(ShellUpdateTunnelingController.class, StageManager.getPrimaryStage());
            adapter.setProp("config", config);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 分屏引导
     */
    public static void splitGuid() {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellSplitGuidController.class, StageManager.getPrimaryStage());
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 终端历史
     *
     * @param parent    父节点
     * @param client    ssh客户端
     * @param histories 历史
     * @param callback  回调函数
     */
    public static void termHistory(Node parent, ShellSSHClient client, List<String> histories, Consumer<String> callback) {
        try {
            PopupAdapter adapter = PopupManager.parsePopup(ShellTermHistoryPopupController.class);
            adapter.setProp("client", client);
            adapter.setProp("histories", histories);
            adapter.setSubmitHandler(callback);
            adapter.showPopup(parent);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 片段列表
     *
     * @param parent   历史
     * @param callback 回调函数
     */
    public static void snippetList(Node parent, Consumer<ShellSnippet> callback) {
        try {
            PopupAdapter adapter = PopupManager.parsePopup(ShellSnippetPopupController.class);
            adapter.setSubmitHandler(callback);
            adapter.showPopup(parent);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 片段
     */
    public static void snippet() {
        try {
            Class<?> clazz = ShellSnippetController.class;
            if (StageManager.frontStage(clazz)) {
                return;
            }
            StageManager.showStage(clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 添加bucket
     *
     * @param client 客户端
     */
    public static StageAdapter addS3Bucket(ShellS3ClientV2 client) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellS3AddBucketController.class, StageManager.getPrimaryStage());
            adapter.setProp("client", client);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 修改bucket
     *
     * @param client 客户端
     */
    public static StageAdapter updateS3Bucket(ShellS3ClientV2 client, ShellS3Bucket bucket) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellS3UpdateBucketController.class, StageManager.getPrimaryStage());
            adapter.setProp("client", client);
            adapter.setProp("bucket", bucket);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 文件错误信息
     *
     * @param task  任务
     * @param owner 父窗口
     */
    public static void fileError(ShellFileTask task, Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellFileErrorController.class, owner);
            adapter.setProp("task", task);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, owner);
        }
    }

    /**
     * 添加跳板机业务
     *
     * @param connect 连接
     * @param owner   父窗口
     * @return StageAdapter
     */
    public static StageAdapter addHost(ShellConnect connect, Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddHostController.class, owner);
            adapter.setProp("connect", connect);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, owner);
        }
        return null;
    }

    /**
     * 运行镜像业务
     *
     * @param exec  执行器
     * @param image 镜像
     */
    public static void runImage(ShellDockerExec exec, ShellDockerImage image) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerRunController.class, StageManager.getPrimaryStage());
            adapter.setProp("exec", exec);
            adapter.setProp("image", image);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 保存镜像业务
     *
     * @param exec  执行器
     * @param image 镜像
     */
    public static void saveImage(ShellDockerExec exec, ShellDockerImage image) {
        try {
            Class<?> clazz = ShellDockerSaveController.class;
            if (StageManager.frontStage(clazz, "image", image)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("exec", exec);
            adapter.setProp("image", image);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 镜像标签业务
     *
     * @param exec  执行器
     * @param image 镜像
     */
    public static void tagImage(ShellDockerExec exec, ShellDockerImage image) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellDockerTagController.class, StageManager.getPrimaryStage());
            adapter.setProp("exec", exec);
            adapter.setProp("image", image);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 保存容器业务
     *
     * @param exec      执行器
     * @param container 容器
     */
    public static void commitContainer(ShellDockerExec exec, ShellDockerContainer container) {
        try {
            Class<?> clazz = ShellDockerCommitController.class;
            if (StageManager.frontStage(clazz, "container", container)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(clazz);
            adapter.setProp("exec", exec);
            adapter.setProp("container", container);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 分享文件业务
     *
     * @param client 客户端
     * @param file   文件
     */
    public static void shareFile(ShellS3ClientV2 client, ShellS3File file) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellS3ShareFileController.class, StageManager.getPrimaryStage());
            adapter.setProp("s3File", file);
            adapter.setProp("client", client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
    }

    /**
     * 添加跳板机
     *
     * @param owner 父窗口
     * @return 页面
     */
    public static StageAdapter addJump(Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellAddJumpController.class, owner);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, owner);
        }
        return null;
    }

    /**
     * 修改跳板机
     *
     * @param owner  父窗口
     * @param config 配置
     * @return 页面
     */
    public static StageAdapter updateJump(ShellJumpConfig config, Window owner) {
        try {
            StageAdapter adapter = StageManager.parseStage(ShellUpdateJumpController.class, owner);
            adapter.setProp("config", config);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, owner);
        }
        return null;
    }
}
