package cn.oyzh.easyshell.smb;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileDownloadTask;
import cn.oyzh.easyshell.file.ShellFileProgressMonitor;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.internal.ShellClientActionUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * s3协议客户端
 *
 * @author oyzh
 * @since 2025-07-23
 */
public class ShellSMBClient implements ShellFileClient<ShellSMBFile> {

    /**
     * smb共享
     */
    private DiskShare smbShare;

    /**
     * smb回话
     */
    private Session smbSession;

    /**
     * smb连接
     */
    private Connection smbConn;

    /**
     * smb客户端
     */
    private SMBClient smbClient;

    /**
     * 连接
     */
    private final ShellConnect connect;

    /**
     * 延迟处理的文件
     */
    private final List<File> delayFiles = new ArrayList<>();

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellFileClient.super.onStateChanged(state3);

    public ShellSMBClient(ShellConnect connect) {
        this.connect = connect;
        this.state.set(ShellConnState.NOT_INITIALIZED);
        this.addStateListener(this.stateListener);
    }

    /**
     * 初始化客户端
     */
    private void initClient() {
        SmbConfig.Builder builder = SmbConfig.builder()
                .withTimeout(this.connectTimeout(), TimeUnit.MILLISECONDS)
                .withReadTimeout(this.connectTimeout(), TimeUnit.MILLISECONDS)
                .withWriteTimeout(this.connectTimeout(), TimeUnit.MILLISECONDS);
        if (this.connect.isEnableProxy()) {
            // Proxy proxy = ShellProxyUtil.initProxy1(this.connect.getProxyConfig());
            builder.withSocketFactory(new ShellSMBSocketFactory(this.connect.getProxyConfig(), this.connectTimeout()));
        }
        SmbConfig config = builder.build();
        this.smbClient = new SMBClient(config);
    }

    @Override
    public void start(int timeout) throws Exception {
        try {
            this.initClient();
            this.state.set(ShellConnState.CONNECTING);
            String host = this.connect.hostIp();
            int port = this.connect.hostPort();
            this.smbConn = this.smbClient.connect(host, port);
            if (this.isGuest()) {
                this.smbSession = this.smbConn.authenticate(AuthenticationContext.guest());
            } else if (this.isAnonymous()) {
                this.smbSession = this.smbConn.authenticate(AuthenticationContext.anonymous());
            } else {
                this.smbSession = this.smbConn.authenticate(new AuthenticationContext(this.connect.getUser(), this.connect.getPassword().toCharArray(), "DESKTOP-A1LGBH6"));
            }
            this.smbShare = (DiskShare) this.smbSession.connectShare(this.connect.getSmbShareName());
            // 列举文件，以测试连接是否可用
            this.lsFile("/");
            this.state.set(ShellConnState.CONNECTED);
        } catch (Throwable ex) {
            ex.printStackTrace();
            this.state.set(ShellConnState.FAILED);
            throw ex;
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    public boolean isGuest() {
        return "guest".equalsIgnoreCase(this.connect.getUser());
    }

    public boolean isAnonymous() {
        return "anonymous".equalsIgnoreCase(this.connect.getUser());
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.connect;
    }

    @Override
    public boolean isConnected() {
        return this.smbConn != null && this.smbConn.isConnected();
    }

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    @Override
    public void close() {
        try {
            if (this.smbShare != null) {
                IOUtil.close(this.smbShare);
                this.smbShare = null;
            }
            if (this.smbSession != null) {
                IOUtil.close(this.smbSession);
                this.smbSession = null;
            }
            if (this.smbConn != null) {
                IOUtil.close(this.smbConn);
                this.smbConn = null;
            }
            if (this.smbClient != null) {
                IOUtil.close(this.smbClient);
                this.smbClient = null;
            }
            this.closeDelayResources();
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void lsFileDynamic(String filePath, Consumer<ShellSMBFile> fileCallback) {
        List<FileIdBothDirectoryInformation> list = this.smbShare.list(filePath);
        for (FileIdBothDirectoryInformation information : list) {
            if (!ShellFileUtil.isNormal(information.getFileName())) {
                continue;
            }
            ShellSMBFile file = new ShellSMBFile(filePath, information);
            fileCallback.accept(file);
        }
    }

    @Override
    public void delete(String file) throws Exception {
        this.smbShare.rm(file);
    }

    @Override
    public void deleteDir(String dir) {
        this.smbShare.rmdir(dir, false);
    }

    @Override
    public void deleteDirRecursive(String dir) {
        this.smbShare.rmdir(dir, true);
    }

    @Override
    public boolean rename(ShellSMBFile file, String newName) throws Exception {
        if (file.isDirectory()) {
            Directory directory = this.openDir(file.getFilePath());
            directory.rename(newName, true);
            IOUtil.close(directory);
        } else {
            File smbFile = this.openFile(file.getFilePath());
            smbFile.rename(newName, false);
            IOUtil.close(smbFile);
        }
        return true;
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        // try {
        //     ShellSMBFile file = this.fileInfo(filePath);
        //     if (file != null) {
        //         return file.isFile() ? this.smbShare.fileExists(filePath) : this.smbShare.folderExists(filePath);
        //     }
        // } catch (Exception ignore) {
        // }
        boolean exist = false;
        try {
            exist = this.smbShare.fileExists(filePath);
        } catch (Exception ignore) {
        }
        if (!exist) {
            try {
                exist = this.smbShare.folderExists(filePath);
            } catch (Exception ignore) {
            }
        }
        return exist;
    }

    @Override
    public String realpath(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void touch(String filePath) throws Exception {
        // 打开远程文件进行写入
        try (File smbFile = this.smbShare.openFile(
                filePath,
                EnumSet.of(AccessMask.GENERIC_WRITE),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OVERWRITE_IF,  // 如果文件存在则覆盖
                null
        )) {
            smbFile.write(new byte[]{}, 0);
        }
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        this.smbShare.mkdir(filePath);
        return true;
    }

    // @Override
    // public void createDirRecursive(String filePath) throws Exception {
    //     String[] dirs = filePath.split("/");
    //     StringBuilder currentPath = new StringBuilder();
    //     for (String dir : dirs) {
    //         if (dir.isEmpty()) {
    //             continue;
    //         }
    //         currentPath.append("/").append(dir);
    //         try {
    //             // 创建缺失目录
    //             if (!this.exist(currentPath.toString())) {
    //                 this.createDir(currentPath.toString());
    //             }
    //         } catch (Exception ex) {
    //             // 创建缺失目录
    //             this.createDir(currentPath.toString());
    //         }
    //     }
    // }

    @Override
    public String workDir() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cd(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(ShellSMBFile remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        // 打开远程文件进行写入
        try (File smbFile = this.readFile(remoteFile.getFilePath())) {
            OutputStream out = new FileOutputStream(localFile);
            if (callback != null) {
                out = ShellFileProgressMonitor.of(out, callback);
            }
            IOUtil.saveToStream(smbFile.getInputStream(), out);
            IOUtil.close(out);
        }
    }

    @Override
    public InputStream getStream(ShellSMBFile remoteFile, Function<Long, Boolean> callback) throws IOException {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        File smbFile = this.readFile(remoteFile.getFilePath());
        this.delayFiles.add(smbFile);
        InputStream stream = smbFile.getInputStream();
        if (callback == null) {
            return stream;
        }
        return ShellFileProgressMonitor.of(stream, callback);
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        File smbFile = this.writeFile(remoteFile);
        OutputStream out = smbFile.getOutputStream();
        if (callback != null) {
            out = ShellFileProgressMonitor.of(out, callback);
        }
        IOUtil.saveToStream(localFile, out);
        IOUtil.close(localFile);
        IOUtil.close(out);
        IOUtil.close(smbFile);
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        File smbFile = this.writeFile(remoteFile);
        this.delayFiles.add(smbFile);
        return smbFile.getOutputStream();
    }

    /**
     * 删除竞争器
     */
    private final Competitor deleteCompetitor = new Competitor(5);

    @Override
    public Competitor deleteCompetitor() {
        return this.deleteCompetitor;
    }

    private final ObservableList<ShellFileDeleteTask> deleteTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileTransportTask> transportTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileDeleteTask> deleteTasks() {
        return this.deleteTasks;
    }

    /**
     * 上传竞争器
     */
    private final Competitor uploadCompetitor = new Competitor(2);

    @Override
    public Competitor uploadCompetitor() {
        return this.uploadCompetitor;
    }

    @Override
    public ObservableList<ShellFileUploadTask> uploadTasks() {
        return this.uploadTasks;
    }

    /**
     * 下载竞争器
     */
    private final Competitor downloadCompetitor = new Competitor(2);

    @Override
    public Competitor downloadCompetitor() {
        return this.downloadCompetitor;
    }

    @Override
    public ObservableList<ShellFileDownloadTask> downloadTasks() {
        return this.downloadTasks;
    }

    /**
     * 传输竞争器
     */
    private final Competitor transportCompetitor = new Competitor(2);

    @Override
    public Competitor transportCompetitor() {
        return transportCompetitor;
    }

    @Override
    public ObservableList<ShellFileTransportTask> transportTasks() {
        return this.transportTasks;
    }

    @Override
    public void closeDelayResources() {
        for (File file : this.delayFiles) {
            if (file != null) {
                IOUtil.close(file);
            }
        }
        this.delayFiles.clear();
    }

    @Override
    public boolean chmod(int permissions, String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public ShellSMBFile fileInfo(String filePath) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "fileInfo " + filePath);
        FileAllInformation allInformation = this.smbShare.getFileInformation(filePath);
        return new ShellSMBFile(filePath, allInformation);
    }

    @Override
    public boolean isCdSupport() {
        return false;
    }

    @Override
    public boolean isChmodSupport() {
        return false;
    }

    @Override
    public boolean isRealpathSupport() {
        return false;
    }

    @Override
    public boolean isWorkDirSupport() {
        return false;
    }

    /**
     * 读取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    private File readFile(String filePath) {
        return this.smbShare.openFile(
                filePath,
                EnumSet.of(AccessMask.GENERIC_READ),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN_IF,
                null
        );
    }

    /**
     * 写入文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    private File writeFile(String filePath) {
        return this.smbShare.openFile(
                filePath,
                EnumSet.of(AccessMask.GENERIC_WRITE),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                null
        );
    }

    /**
     * 打开文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    private File openFile(String filePath) {
        return this.smbShare.openFile(
                filePath,
                EnumSet.of(AccessMask.GENERIC_ALL),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN_IF,
                null
        );
    }

    /**
     * 打开目录
     *
     * @param filePath 文件路径
     * @return 目录
     */
    private Directory openDir(String filePath) {
        return this.smbShare.openDirectory(
                filePath,
                EnumSet.of(AccessMask.GENERIC_ALL),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN_IF,
                null
        );
    }

}
