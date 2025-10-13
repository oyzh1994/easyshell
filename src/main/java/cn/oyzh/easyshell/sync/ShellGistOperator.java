package cn.oyzh.easyshell.sync;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.List;
import java.util.Map;

/**
 * gist操作器
 *
 * @author oyzh
 * @since 2025-10-11
 */
public abstract class ShellGistOperator implements AutoCloseable {

    /**
     * 访问令牌
     */
    protected String accessToken;

    /**
     * http客户端
     */
    protected CloseableHttpClient httpClient;

    public ShellGistOperator(String accessToken) {
        this.accessToken = accessToken;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * 获取所有代码片段列表
     *
     * @return 片段列表
     * @throws Exception 异常
     */
    public abstract List<JSONObject> listGists() throws Exception;

    /**
     * 获取特定代码片段详情
     *
     * @param gistId 片段id
     * @return 片段代码详情
     * @throws Exception 异常
     */
    public abstract JSONObject getGist(String gistId) throws Exception;

    /**
     * 创建代码片段
     *
     * @param description 描述
     * @param files       文件列表
     * @param isPublic    是否公开
     * @return 片段id
     * @throws Exception 异常
     */
    public abstract String createGist(String description, Map<String, String> files, boolean isPublic) throws Exception;

    /**
     * 更新代码片段
     *
     * @param gistId      片段id
     * @param description 描述
     * @param files       文件列表
     * @return 结果
     * @throws Exception 异常
     */
    public abstract boolean updateGist(String gistId, String description, Map<String, String> files) throws Exception;

    /**
     * 删除代码片段
     *
     * @param gistId 片段id
     * @return 结果
     * @throws Exception 异常
     */
    public abstract boolean deleteGist(String gistId) throws Exception;

    /**
     * 获取代码片段的特定文件
     *
     * @param gistId 片段id
     * @return 文件内容
     * @throws Exception 异常
     */
    public JSONObject getFileContent(String gistId) throws Exception {
        JSONObject object = this.getGist(gistId);
        return object.getJSONObject("files");
    }

    /**
     * 检查代码片段是否存在
     *
     * @param gistId 片段id
     * @return 异常
     */
    public boolean gistExists(String gistId) {
        try {
            JSONObject object = this.getGist(gistId);
            return !object.isEmpty();
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        if (this.httpClient != null) {
            this.httpClient.close();
            this.httpClient = null;
        }
        this.accessToken = null;
    }
}