package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.sync.ShellGiteeGistOperator;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiteeGistTest {

    private String accessToken = ""; // 在 Gitee 设置中生成

    @Test
    public void test1() throws Exception {
        // 1. 列出所有代码片段
        System.out.println("=== 代码片段列表 ===");
        ShellGiteeGistOperator operator = new ShellGiteeGistOperator(accessToken);
        List<JSONObject> gists = operator.listGists();
        for (JSONObject gist : gists) {
            System.out.println("ID: " + gist.get("id") +
                    ", 描述: " + gist.get("description"));
        }
    }

    @Test
    public void test2() throws Exception {
        ShellGiteeGistOperator operator = new ShellGiteeGistOperator(accessToken);
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
        ShellGiteeGistOperator operator = new ShellGiteeGistOperator(accessToken);
        String gistId = "09uzksnmrf6ylptgh2owq43";
        // 3. 获取代码片段详情
        System.out.println("\n=== 代码片段详情 ===");
        Map<String, Object> gistDetail = operator.getGist(gistId);
        System.out.println("描述: " + gistDetail.get("description"));
        System.out.println("URL: " + gistDetail.get("html_url"));
    }

    @Test
    public void test4() throws Exception {
        ShellGiteeGistOperator operator = new ShellGiteeGistOperator(accessToken);
        String gistId = "09uzksnmrf6ylptgh2owq43";
        // 4. 获取特定文件内容
        System.out.println("\n=== 文件内容 ===");
        JSONObject javaContent = operator.getFileContent(gistId);
        if (javaContent != null) {
            System.out.println("files:");
            System.out.println(javaContent);
            System.out.println(javaContent.getString("test"));
        }
    }

    @Test
    public void test5() throws Exception {
        ShellGiteeGistOperator operator = new ShellGiteeGistOperator(accessToken);
        String gistId = "09uzksnmrf6ylptgh2owq43";
        // 5. 更新代码片段
        System.out.println("\n=== 更新代码片段 ===");
        Map<String, String> updatedFiles = new HashMap<>();
        updatedFiles.put("test", "1");
        boolean updated = operator.updateGist(gistId, "测试更新", updatedFiles);
        System.out.println("更新结果: " + (updated ? "成功" : "失败"));
    }

    @Test
    public void test6() throws Exception {
        ShellGiteeGistOperator operator = new ShellGiteeGistOperator(accessToken);
        String gistId = "09uzksnmrf6ylptgh2owq43";
        // 6. 检查代码片段是否存在
        System.out.println("\n=== 检查代码片段 ===");
        boolean exists = operator.gistExists(gistId);
        System.out.println("代码片段存在: " + exists);
    }

    @Test
    public void test7() throws Exception {
        ShellGiteeGistOperator operator = new ShellGiteeGistOperator(accessToken);
        String gistId = "09uzksnmrf6ylptgh2owq43";
        // 5. 更新代码片段
        System.out.println("\n=== 删除代码片段字段 ===");
        // JSONObject object = operator.getFileContent(gistId);
        // object.getJSONObject("test").put("content", null);
        // boolean updated = operator.updateGist(gistId, "测试删除", object.toJavaObject(Map.class));
        Map<String, String> updatedFiles = new HashMap<>();
        updatedFiles.put("test", "null");
        boolean updated = operator.updateGist(gistId, "测试删除", updatedFiles);
        System.out.println("删除字段结果: " + (updated ? "成功" : "失败"));
    }
}