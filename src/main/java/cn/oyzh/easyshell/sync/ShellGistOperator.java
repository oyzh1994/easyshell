package cn.oyzh.easyshell.sync;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShellGistOperator implements AutoCloseable {
    private static final String GITEE_API_BASE = "https://gitee.com/api/v5/gists";
    private String accessToken;
    private CloseableHttpClient httpClient;

    public ShellGistOperator(String accessToken) {
        this.accessToken = accessToken;
        this.httpClient = HttpClients.createDefault();
    }

    // 获取所有代码片段列表
    public List<Map<String, Object>> listGists() throws Exception {
        String url = GITEE_API_BASE + "?access_token=" + accessToken + "&page=1&per_page=100";

        HttpGet request = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            JSONArray gists = JSON.parseArray(json);
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < gists.size(); i++) {
                JSONObject gist = gists.getJSONObject(i);
                Map<String, Object> gistInfo = new HashMap<>();
                gistInfo.put("id", gist.getString("id"));
                gistInfo.put("description", gist.getString("description"));
                gistInfo.put("created_at", gist.getString("created_at"));
                gistInfo.put("updated_at", gist.getString("updated_at"));
                gistInfo.put("public", gist.getBoolean("public"));
                result.add(gistInfo);
            }
            return result;
        }
    }

    // 获取特定代码片段详情
    public Map<String, Object> getGist(String gistId) throws Exception {
        String url = GITEE_API_BASE + "/" + gistId + "?access_token=" + accessToken;

        HttpGet request = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            JSONObject gist = JSON.parseObject(json);

            Map<String, Object> gistDetail = new HashMap<>();
            gistDetail.put("id", gist.getString("id"));
            gistDetail.put("description", gist.getString("description"));
            gistDetail.put("html_url", gist.getString("html_url"));
            gistDetail.put("files", gist.getJSONObject("files"));
            gistDetail.put("created_at", gist.getString("created_at"));
            gistDetail.put("updated_at", gist.getString("updated_at"));

            return gistDetail;
        }
    }

    // 创建代码片段
    public String createGist(String description, Map<String, String> files, boolean isPublic) throws Exception {
        String url = GITEE_API_BASE + "?access_token=" + accessToken;

        JSONObject requestBody = new JSONObject();
        requestBody.put("description", description);
        requestBody.put("public", isPublic);

        JSONObject filesNode = new JSONObject();
        for (Map.Entry<String, String> file : files.entrySet()) {
            JSONObject fileNode = new JSONObject();
            fileNode.put("content", file.getValue());
            filesNode.put(file.getKey(), fileNode);
        }
        requestBody.put("files", filesNode);

        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json;charset=UTF-8");
        StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
        request.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            JSONObject result = JSON.parseObject(json);
            return result.getString("id");
        }
    }

    // 更新代码片段
    public boolean updateGist(String gistId, String description,
                              Map<String, String> files) throws Exception {
        String url = GITEE_API_BASE + "/" + gistId + "?access_token=" + accessToken;

        JSONObject requestBody = new JSONObject();
        if (description != null) {
            requestBody.put("description", description);
        }
        if (files != null && !files.isEmpty()) {
            JSONObject filesNode = new JSONObject();
            for (Map.Entry<String, String> file : files.entrySet()) {
                JSONObject fileNode = new JSONObject();
                fileNode.put("content", file.getValue());
                filesNode.put(file.getKey(), fileNode);
            }
            requestBody.put("files", filesNode);
        }

        HttpPatch request = new HttpPatch(url);
        request.setHeader("Content-Type", "application/json;charset=utf-8");
        StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
        request.setEntity(entity);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return response.getStatusLine().getStatusCode() == 200;
        }
    }

    // 删除代码片段
    public boolean deleteGist(String gistId) throws Exception {
        String url = GITEE_API_BASE + "/" + gistId + "?access_token=" + accessToken;

        HttpDelete request = new HttpDelete(url);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return response.getStatusLine().getStatusCode() == 204;
        }
    }

    // 获取代码片段的特定文件内容
    public Map<String, String> getFileContent(String gistId) throws Exception {
        Map<String, Object> gistDetail = getGist(gistId);
        return (Map<String, String>) gistDetail.get("files");
    }

    // 检查代码片段是否存在
    public boolean gistExists(String gistId) {
        try {
            getGist(gistId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void close() throws Exception {
        httpClient.close();
    }
}