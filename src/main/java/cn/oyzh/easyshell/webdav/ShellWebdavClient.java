package cn.oyzh.easyshell.webdav;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.common.util.HttpUtil;
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
import com.github.sardine.DavResource;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author oyzh
 * @since 2025-10-09
 */
public class ShellWebdavClient implements ShellFileClient<ShellWebdavFile> {

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellFileClient.super.onStateChanged(state3);

    /**
     * 延迟处理的文件
     */
    private final List<InputStream> delayInputStreams = new ArrayList<>();

    private ShellWebdavSardine sardine;

    /**
     * 连接
     */
    private final ShellConnect connect;

    public ShellWebdavClient(ShellConnect connect) {
        this.connect = connect;
        this.state.set(ShellConnState.NOT_INITIALIZED);
        this.addStateListener(this.stateListener);
    }

    /**
     * 初始化客户端
     */
    private void initClient() {
        if (this.connect.isEnableProxy()) {
        }
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(this.connectTimeout())
                        .setConnectTimeout(this.connectTimeout())
                        .setConnectionRequestTimeout(this.connectTimeout())
                        .build());
        this.sardine = new ShellWebdavSardine(builder, this.connect.getUser(), this.connect.getPassword());
        this.sardine.setAuthorization(this.getAuthorization());
    }

    @Override
    public void lsFileDynamic(String filePath, Consumer<ShellWebdavFile> fileCallback) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "ls " + filePath);
        String fullPath = this.getFullPath(filePath);
        List<DavResource> resources = this.sardine.list(fullPath);
        if (resources != null) {
            for (DavResource resource : resources) {
                if (ShellWebdavUtil.isRoot(resource) || ShellWebdavUtil.isSalf(resource, filePath)) {
                    continue;
                }
                ShellWebdavFile ftpFile = new ShellWebdavFile(filePath, resource);
                fileCallback.accept(ftpFile);
            }
        }
    }

    @Override
    public void delete(String file) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "rm " + file);
        String fullPath = this.getFullPath(file);
        this.sardine.delete(fullPath);
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "rm " + dir);
        String fullPath = this.getFullPath(dir);
        this.sardine.delete(fullPath);
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "rm " + dir);
        String fullPath = this.getFullPath(dir);
        this.sardine.delete(fullPath);
    }

    @Override
    public boolean rename(ShellWebdavFile file, String newName) throws Exception {
        return false;
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "exist " + filePath);
        String fullPath = this.getFullPath(filePath);
        return this.sardine.exists(fullPath);
    }

    @Override
    public String realpath(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRealpathSupport() {
        return false;
    }

    @Override
    public void touch(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "touch " + filePath);
        String fullPath = this.getFullPath(filePath);
        this.sardine.put(fullPath, new byte[0]);
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "mkdir " + filePath);
        String fullPath = this.getFullPath(filePath);
        this.sardine.createDirectory(fullPath);
        return true;
    }

    @Override
    public String workDir() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWorkDirSupport() {
        return false;
    }

    @Override
    public void cd(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCdSupport() {
        return false;
    }

    @Override
    public void get(ShellWebdavFile remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {
        String fPath = remoteFile.getFilePath();
        ShellClientActionUtil.forAction(this.connectName(), "get " + fPath);
        String fullPath = this.getFullPath(fPath);
        InputStream in = this.sardine.get(fullPath);
        FileOutputStream fOut = new FileOutputStream(localFile);
        if (callback != null) {
            OutputStream out = ShellFileProgressMonitor.of(fOut, callback);
            IOUtil.saveToStream(in, out);
            IOUtil.close(out);
        } else {
            IOUtil.saveToStream(in, fOut);
        }
        IOUtil.close(fOut);
        IOUtil.close(in);
    }

    @Override
    public InputStream getStream(ShellWebdavFile remoteFile, Function<Long, Boolean> callback) throws Exception {
        String fPath = remoteFile.getFilePath();
        InputStream stream = null;
        try {
            ShellClientActionUtil.forAction(this.connectName(), "get " + fPath);
            String fullPath = this.getFullPath(fPath);
            if (callback == null) {
                stream = this.sardine.get(fullPath);
            } else {
                stream = ShellFileProgressMonitor.of(this.sardine.get(fullPath), callback);
            }
        } finally {
            this.delayInputStreams.add(stream);
        }
        return stream;
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        String fullPath = this.getFullPath(remoteFile);
        InputStream stream = localFile;
        if (callback != null) {
            stream = ShellFileProgressMonitor.of(localFile, callback);
        }
        this.sardine.put(fullPath, stream, Map.of("Authorization", this.getAuthorization()));
        IOUtil.close(stream);
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPutStreamSupport() {
        return false;
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
        return uploadCompetitor;
    }

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

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
        return downloadCompetitor;
    }

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

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

    private final ObservableList<ShellFileTransportTask> transportTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileTransportTask> transportTasks() {
        return this.transportTasks;
    }

    @Override
    public void closeDelayResources() {
        for (InputStream stream : this.delayInputStreams) {
            IOUtil.close(stream);
        }
    }

    @Override
    public boolean chmod(int permissions, String filePath) throws Exception {
        return false;
    }

    @Override
    public boolean isChmodSupport() {
        return false;
    }

    @Override
    public ShellWebdavFile fileInfo(String filePath) throws Exception {
        String fullPath = this.getFullPath(filePath);
        List<DavResource> resources = this.sardine.list(fullPath);
        if (CollectionUtil.isNotEmpty(resources)) {
            String pPath = ShellFileUtil.parent(filePath);
            return new ShellWebdavFile(pPath, resources.getFirst());
        }
        return null;
    }

    @Override
    public void start(int timeout) throws Throwable {
        try {
            this.initClient();
            this.state.set(ShellConnState.CONNECTING);
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

    @Override
    public ShellConnect getShellConnect() {
        return this.connect;
    }

    @Override
    public boolean isConnected() {
        return this.state.get() == ShellConnState.CONNECTED;
    }

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    @Override
    public void close() throws Exception {
        if (this.sardine != null) {
            this.sardine.shutdown();
            this.sardine = null;
        }
        this.closeDelayResources();
        this.state.set(ShellConnState.CLOSED);
        this.removeStateListener(this.stateListener);
    }

    private String getFullPath(String path) {
        return ShellFileUtil.concat(this.connect.getHost(), path);
    }

    private String getAuthorization() {
        return HttpUtil.basic(this.connect.getUser(), this.connect.getPassword());
    }
}
