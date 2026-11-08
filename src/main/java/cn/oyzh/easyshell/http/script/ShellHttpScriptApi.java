package cn.oyzh.easyshell.http.script;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.easyshell.http.ShellHttpResponse;
import cn.oyzh.easyshell.store.http.ShellHttpEnvironmentStore;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Postman 风格请求脚本 API。
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpScriptApi {

    private final ShellHttpScriptContext context;

    private final Environment environment;

    private final Variables variables;

    private final Request request;

    private final Response response;

    public ShellHttpScriptApi(ShellHttpScriptContext context) {
        this.context = context;
        this.environment = new Environment(context);
        this.variables = new Variables(context);
        this.request = new Request(context);
        this.response = new Response(context);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Variables getVariables() {
        return variables;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    /**
     * 注册测试
     *
     * @param name     测试名称
     * @param callback 测试函数
     */
    public void test(String name, Object callback) {
        try {
            if (callback instanceof ScriptObjectMirror mirror && mirror.isFunction()) {
                mirror.call(null);
            } else {
                throw new IllegalArgumentException("pm.test callback is not a function");
            }
            context.addTestResult(name, true, null);
        } catch (Throwable ex) {
            context.addTestResult(name, false, ex.getMessage() == null ? ex.toString() : ex.getMessage());
        }
    }

    /**
     * 断言
     *
     * @param actual 实际值
     * @return 断言
     */
    public Expect expect(Object actual) {
        return new Expect(actual);
    }

    /**
     * 简单断言对象
     */
    public static class Expect {

        private final Object actual;

        private Expect(Object actual) {
            this.actual = actual;
        }

        public Expect getTo() {
            return this;
        }

        public void equal(Object expected) {
            if (!Objects.equals(this.actual, expected)) {
                throw new AssertionError("Expected " + expected + " but got " + this.actual);
            }
        }

        public void eql(Object expected) {
            this.equal(expected);
        }
    }

    /**
     * 环境变量 API
     */
    public static class Environment {

        private final ShellHttpScriptContext context;

        private Environment(ShellHttpScriptContext context) {
            this.context = context;
        }

        public String get(String key) {
            ShellHttpEnvironment environment = this.context.getEnvironment();
            if (environment != null) {
                for (ShellHttpKeyValue row : environment.getValues()) {
                    if (row != null && row.isEnabled() && StringUtil.equals(row.getKey(), key)) {
                        return row.getValue();
                    }
                }
            }
            return this.context.getVariables().get(key);
        }

        public void set(String key, Object value) {
            String text = value == null ? "" : String.valueOf(value);
            this.context.getVariables().put(key, text);
            ShellHttpEnvironment environment = this.context.getEnvironment();
            if (environment == null) {
                return;
            }
            ShellHttpKeyValue target = null;
            for (ShellHttpKeyValue row : environment.getValues()) {
                if (row != null && StringUtil.equals(row.getKey(), key)) {
                    target = row;
                    break;
                }
            }
            if (target == null) {
                target = new ShellHttpKeyValue(key, text);
                environment.getValues().add(target);
            } else {
                target.setValue(text);
                target.setEnabled(true);
            }
            ShellHttpEnvironmentStore.INSTANCE.update(environment);
        }

        public void unset(String key) {
            this.context.getVariables().remove(key);
            ShellHttpEnvironment environment = this.context.getEnvironment();
            if (environment == null) {
                return;
            }
            environment.getValues().removeIf(row -> row != null && StringUtil.equals(row.getKey(), key));
            ShellHttpEnvironmentStore.INSTANCE.update(environment);
        }
    }

    /**
     * 临时变量 API
     */
    public static class Variables {

        private final ShellHttpScriptContext context;

        private Variables(ShellHttpScriptContext context) {
            this.context = context;
        }

        public String get(String key) {
            return this.context.getVariables().get(key);
        }

        public void set(String key, Object value) {
            this.context.getVariables().put(key, value == null ? "" : String.valueOf(value));
        }

        public void unset(String key) {
            this.context.getVariables().remove(key);
        }
    }

    /**
     * 请求 API
     */
    public static class Request {

        private final ShellHttpScriptContext context;

        private Request(ShellHttpScriptContext context) {
            this.context = context;
        }

        public String getUrl() {
            return this.context.getRequest().getUrl();
        }

        public void setUrl(String url) {
            this.context.getRequest().setUrl(url);
        }

        public String getMethod() {
            return this.context.getRequest().getMethod();
        }

        public void setMethod(String method) {
            this.context.getRequest().setMethod(method);
        }

        public RequestHeaders getHeaders() {
            return new RequestHeaders(this.context.getRequest());
        }
    }

    /**
     * 请求头 API
     */
    public static class RequestHeaders {

        private final ShellHttpRequest request;

        private RequestHeaders(ShellHttpRequest request) {
            this.request = request;
        }

        public String get(String key) {
            for (ShellHttpKeyValue row : this.request.getHeaders()) {
                if (row != null && row.isEnabled() && StringUtil.equals(row.getKey(), key)) {
                    return row.getValue();
                }
            }
            return null;
        }

        public void add(String key, String value) {
            this.request.getHeaders().add(new ShellHttpKeyValue(key, value));
        }

        public void add(Object object) {
            String key = property(object, "key");
            String value = property(object, "value");
            if (StringUtil.isNotBlank(key)) {
                this.add(key, value);
            }
        }

        public void upsert(String key, String value) {
            for (ShellHttpKeyValue row : this.request.getHeaders()) {
                if (row != null && StringUtil.equals(row.getKey(), key)) {
                    row.setValue(value);
                    row.setEnabled(true);
                    return;
                }
            }
            this.add(key, value);
        }

        public void remove(String key) {
            this.request.getHeaders().removeIf(row -> row != null && StringUtil.equals(row.getKey(), key));
        }
    }

    /**
     * 响应 API
     */
    public static class Response {

        private final ShellHttpScriptContext context;

        private Response(ShellHttpScriptContext context) {
            this.context = context;
        }

        public int getCode() {
            ShellHttpResponse response = this.context.getResponse();
            return response == null ? 0 : response.getStatusCode();
        }

        public String getStatus() {
            ShellHttpResponse response = this.context.getResponse();
            return response == null ? null : response.getStatusText();
        }

        public long getResponseTime() {
            ShellHttpResponse response = this.context.getResponse();
            return response == null ? 0 : response.getDurationMs();
        }

        public String text() {
            ShellHttpResponse response = this.context.getResponse();
            return response == null ? null : response.getBody();
        }

        public Object json() {
            String text = this.text();
            if (StringUtil.isBlank(text)) {
                return null;
            }
            return JSONUtil.parseObject(text);
        }

        public ResponseHeaders headers() {
            return new ResponseHeaders(this.context.getResponse());
        }

        public To getTo() {
            return new To(this.context);
        }
    }

    /**
     * 响应断言入口
     */
    public static class To {

        private final Have have;

        private To(ShellHttpScriptContext context) {
            this.have = new Have(context);
        }

        public Have getHave() {
            return this.have;
        }
    }

    /**
     * 响应断言
     */
    public static class Have {

        private final ShellHttpScriptContext context;

        private Have(ShellHttpScriptContext context) {
            this.context = context;
        }

        public void status(int code) {
            ShellHttpResponse response = this.context.getResponse();
            int actual = response == null ? 0 : response.getStatusCode();
            if (actual != code) {
                throw new AssertionError("Expected status " + code + " but got " + actual);
            }
        }

        public void header(String name) {
            ShellHttpResponse response = this.context.getResponse();
            if (response == null || response.getHeaders() == null
                    || response.getHeaders().keySet().stream().noneMatch(key -> StringUtil.equals(key, name))) {
                throw new AssertionError("Missing header " + name);
            }
        }
    }

    /**
     * 响应头 API
     */
    public static class ResponseHeaders {

        private final ShellHttpResponse response;

        private ResponseHeaders(ShellHttpResponse response) {
            this.response = response;
        }

        public String get(String key) {
            if (this.response == null || this.response.getHeaders() == null) {
                return null;
            }
            List<String> values = this.response.getHeaders().get(key);
            if (values == null) {
                values = this.response.getHeaders().entrySet().stream()
                        .filter(entry -> StringUtil.equals(entry.getKey(), key))
                        .findFirst().map(Map.Entry::getValue).orElse(null);
            }
            return values == null || values.isEmpty() ? null : values.getFirst();
        }
    }

    private static String property(Object object, String name) {
        if (object instanceof ScriptObjectMirror mirror) {
            Object value = mirror.get(name);
            return value == null ? null : String.valueOf(value);
        }
        return null;
    }
}
