package cn.oyzh.easyshell.sync;

import cn.oyzh.common.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * github gist操作器
 *
 * @author oyzh
 * @since 2025-10-13
 */
public class ShellGitHubGistOperator extends ShellGistOperator {

    private static final String GITHUB_API_BASE = "https://api.github.com/gists";

    public ShellGitHubGistOperator(String accessToken) {
        super(accessToken);
    }

    @Override
    public List<JSONObject> listGists() throws Exception {
        String url = GITHUB_API_BASE + "?page=1&per_page=100";
        HttpGet request = new HttpGet(url);
        this.setAuthHeader(request);
        try (CloseableHttpResponse response = this.httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONArray gists = JSON.parseArray(json);
            List<JSONObject> result = new ArrayList<>();
            for (int i = 0; i < gists.size(); i++) {
                JSONObject gist = gists.getJSONObject(i);
                result.add(gist);
            }
            return result;
        }
    }

    @Override
    public JSONObject getGist(String gistId) throws Exception {
        String url = GITHUB_API_BASE + "/" + gistId;
        HttpGet request = new HttpGet(url);
        this.setAuthHeader(request);
        try (CloseableHttpResponse response = this.httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return JSON.parseObject(json);
        }
    }

    @Override
    public String createGist(String description, Map<String, String> files, boolean isPublic) throws Exception {
        String url = GITHUB_API_BASE;
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
        this.setAuthHeader(request);
        request.setHeader("Content-Type", "application/json; charset=utf-8");
        StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
        request.setEntity(entity);
        try (CloseableHttpResponse response = this.httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject result = JSON.parseObject(json);
            return result.getString("id");
        }
    }

    @Override
    public boolean updateGist(String gistId, String description, Map<String, String> files) throws Exception {
        String url = GITHUB_API_BASE + "/" + gistId;
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
        this.setAuthHeader(request);
        request.setHeader("Content-Type", "application/json; charset=utf-8");
        StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
        request.setEntity(entity);
        try (CloseableHttpResponse response = this.httpClient.execute(request)) {
            return response.getStatusLine().getStatusCode() == 200;
        }
    }

    @Override
    public boolean deleteGist(String gistId) throws Exception {
        String url = GITHUB_API_BASE + "/" + gistId;
        HttpDelete request = new HttpDelete(url);
        this.setAuthHeader(request);
        try (CloseableHttpResponse response = this.httpClient.execute(request)) {
            return response.getStatusLine().getStatusCode() == 204;
        }
    }

    // 设置认证头 :cite[7]
    private void setAuthHeader(HttpUriRequest request) {
        if (StringUtil.isNotEmpty(this.accessToken)) {
            request.setHeader("Authorization", "token " + this.accessToken);
        }
        // 设置User-Agent，GitHub API要求
        request.setHeader("User-Agent", "Java-Gist-Client");
    }

}