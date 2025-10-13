package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.sync.ShellGitHubGistOperator;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GithubGistTest {

    private String accessToken = ""; // 在 Gitee 设置中生成

    @Test
    public void test1() throws Exception {
        // 1. 列出所有代码片段
        System.out.println("=== 代码片段列表 ===");
        ShellGitHubGistOperator operator = new ShellGitHubGistOperator(accessToken);
        List<JSONObject> gists = operator.listGists();
        for (JSONObject gist : gists) {
            System.out.println("ID: " + gist.get("id") +
                    ", 描述: " + gist.get("description"));
        }
    }

    @Test
    public void test2() throws Exception {
        ShellGitHubGistOperator operator = new ShellGitHubGistOperator(accessToken);
        // 2. 创建新的代码片段
        System.out.println("\n=== 创建代码片段 ===");
        Map<String, String> files = new HashMap<>();
        files.put("name","test");
        files.put("age","11");
        String newGistId = operator.createGist("测试代码片段", files, false);
        System.out.println("创建成功，ID: " + newGistId);
    }

    @Test
    public void test3() throws Exception {
        ShellGitHubGistOperator operator = new ShellGitHubGistOperator(accessToken);
        String gistId = "313cef1f3060beb8f0aa29829494c110";
        // 3. 获取代码片段详情
        System.out.println("\n=== 代码片段详情 ===");
        Map<String, Object> gistDetail = operator.getGist(gistId);
        System.out.println("描述: " + gistDetail.get("description"));
        System.out.println("URL: " + gistDetail.get("html_url"));
    }

    @Test
    public void test4() throws Exception {
        ShellGitHubGistOperator operator = new ShellGitHubGistOperator(accessToken);
        String gistId = "313cef1f3060beb8f0aa29829494c110";
        // 4. 获取特定文件内容
        System.out.println("\n=== 文件内容 ===");
        JSONObject javaContent = operator.getFileContent(gistId);
        if (javaContent != null) {
            System.out.println("files:");
            System.out.println(javaContent);
        }
    }

    @Test
    public void test5() throws Exception {
        ShellGitHubGistOperator operator = new ShellGitHubGistOperator(accessToken);
        String gistId = "313cef1f3060beb8f0aa29829494c110";
        // 5. 更新代码片段
        System.out.println("\n=== 更新代码片段 ===");
        Map<String, String> updatedFiles = new HashMap<>();
        updatedFiles.put("test", "test data");
        boolean updated = operator.updateGist(gistId, "测试更新", updatedFiles);
        System.out.println("更新结果: " + (updated ? "成功" : "失败"));
    }

    @Test
    public void test6() throws Exception {
        ShellGitHubGistOperator operator = new ShellGitHubGistOperator(accessToken);
        String gistId = "313cef1f3060beb8f0aa29829494c110";
        // 6. 检查代码片段是否存在
        System.out.println("\n=== 检查代码片段 ===");
        boolean exists = operator.gistExists(gistId);
        System.out.println("代码片段存在: " + exists);
    }
}