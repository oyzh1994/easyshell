package cn.oyzh.easyshell.http.script;

import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.http.ShellHttpResponse;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 请求脚本执行上下文。
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpScriptContext {

    private final ShellHttpWorkspace workspace;

    private final ShellHttpRequest request;

    private final ShellHttpEnvironment environment;

    private final Map<String, String> variables = new HashMap<>();

    private final List<TestResult> testResults = new ArrayList<>();

    private ShellHttpResponse response;

    public ShellHttpScriptContext(ShellHttpWorkspace workspace, ShellHttpRequest request,
                                  ShellHttpEnvironment environment) {
        this.workspace = workspace;
        this.request = request;
        this.environment = environment;
    }

    public ShellHttpWorkspace getWorkspace() {
        return workspace;
    }

    public ShellHttpRequest getRequest() {
        return request;
    }

    public ShellHttpEnvironment getEnvironment() {
        return environment;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public ShellHttpResponse getResponse() {
        return response;
    }

    public void setResponse(ShellHttpResponse response) {
        this.response = response;
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }

    public void addTestResult(String name, boolean passed, String message) {
        this.testResults.add(new TestResult(name, passed, message));
    }

    /**
     * 测试结果
     *
     * @param name    名称
     * @param passed  是否通过
     * @param message 信息
     */
    public record TestResult(String name, boolean passed, String message) {
    }
}
