package cn.oyzh.easyshell.http.script;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.http.ShellHttpCookie;
import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.easyshell.http.ShellHttpUtil;
import cn.oyzh.easyshell.http.ShellHttpResponse;
import cn.oyzh.easyshell.store.http.ShellHttpEnvironmentStore;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

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

        private final boolean negate;

        private Expect(Object actual) {
            this(actual, false);
        }

        private Expect(Object actual, boolean negate) {
            this.actual = actual;
            this.negate = negate;
        }

        public Expect getTo() {
            return this;
        }

        public Expect getBe() {
            return this;
        }

        public Expect getHave() {
            return this;
        }

        public Expect getAn() {
            return this;
        }

        public Expect getNot() {
            return new Expect(this.actual, !this.negate);
        }

        public void equal(Object expected) {
            this.assertion(Objects.equals(this.actual, expected),
                    "Expected " + expected + " but got " + this.actual);
        }

        public void eql(Object expected) {
            this.equal(expected);
        }

        public void a(Object type) {
            this.assertion(this.isType(type), "Expected " + type + " but got " + this.actual);
        }

        public void an(Object type) {
            this.a(type);
        }

        public void include(Object expected) {
            this.assertion(this.contains(expected), "Expected value to include " + expected);
        }

        public void contain(Object expected) {
            this.include(expected);
        }

        public void property(String name) {
            this.assertion(this.hasProperty(name), "Expected property " + name);
        }

        public void lengthOf(int length) {
            this.assertion(this.length() == length,
                    "Expected length " + length + " but got " + this.length());
        }

        public void above(Object expected) {
            this.assertion(this.compare(expected) > 0, "Expected value above " + expected);
        }

        public void below(Object expected) {
            this.assertion(this.compare(expected) < 0, "Expected value below " + expected);
        }

        public void match(String regex) {
            this.assertion(this.actual != null
                            && Pattern.compile(regex).matcher(String.valueOf(this.actual)).find(),
                    "Expected value to match " + regex);
        }

        public boolean getTrue() {
            this.assertion(Boolean.TRUE.equals(this.actual), "Expected true but got " + this.actual);
            return true;
        }

        public boolean getFalse() {
            this.assertion(Boolean.FALSE.equals(this.actual), "Expected false but got " + this.actual);
            return true;
        }

        public boolean getNull() {
            this.assertion(this.actual == null, "Expected null but got " + this.actual);
            return true;
        }

        public boolean getOk() {
            this.assertion(this.actual != null && !Boolean.FALSE.equals(this.actual),
                    "Expected truthy value");
            return true;
        }

        private void assertion(boolean condition, String message) {
            if (this.negate ? condition : !condition) {
                throw new AssertionError(message);
            }
        }

        private boolean isType(Object type) {
            if (this.actual == null) {
                return "null".equalsIgnoreCase(String.valueOf(type));
            }
            String expected = String.valueOf(type).toLowerCase();
            return switch (expected) {
                case "string" -> this.actual instanceof CharSequence;
                case "number" -> this.actual instanceof Number;
                case "boolean" -> this.actual instanceof Boolean;
                case "array", "list" -> this.actual instanceof Collection<?>
                        || this.actual.getClass().isArray();
                case "object", "map" -> this.actual instanceof Map<?, ?>;
                default -> false;
            };
        }

        private boolean contains(Object expected) {
            if (this.actual instanceof CharSequence text) {
                return expected != null && text.toString().contains(String.valueOf(expected));
            }
            if (this.actual instanceof Collection<?> collection) {
                return collection.contains(expected);
            }
            if (this.actual instanceof Map<?, ?> map) {
                return map.containsKey(expected);
            }
            if (this.actual != null && this.actual.getClass().isArray()) {
                int length = Array.getLength(this.actual);
                for (int i = 0; i < length; i++) {
                    if (Objects.equals(Array.get(this.actual, i), expected)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean hasProperty(String name) {
            return this.actual instanceof Map<?, ?> map && map.containsKey(name);
        }

        private int length() {
            if (this.actual instanceof CharSequence text) {
                return text.length();
            }
            if (this.actual instanceof Collection<?> collection) {
                return collection.size();
            }
            if (this.actual != null && this.actual.getClass().isArray()) {
                return Array.getLength(this.actual);
            }
            return -1;
        }

        private int compare(Object expected) {
            if (!(this.actual instanceof Number left) || !(expected instanceof Number right)) {
                return 0;
            }
            return Double.compare(left.doubleValue(), right.doubleValue());
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

        public boolean has(String key) {
            return this.get(key) != null;
        }

        public void clear() {
            this.context.getVariables().clear();
            ShellHttpEnvironment environment = this.context.getEnvironment();
            if (environment == null) {
                return;
            }
            environment.getValues().removeIf(row -> row != null && row.isEnabled());
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

        public boolean has(String key) {
            return this.context.getVariables().containsKey(key);
        }

        public void clear() {
            this.context.getVariables().clear();
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

        public RequestUrl getUrl() {
            return new RequestUrl(this.context);
        }

        public void setUrl(String url) {
            this.context.getRequest().setUrl(url);
        }

        public RequestBody getBody() {
            return new RequestBody(this.context);
        }

        public void addQueryParams(String query) {
            this.getUrl().addQueryParams(query);
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
     * 请求地址 API
     */
    public static class RequestUrl {

        private final ShellHttpScriptContext context;

        private RequestUrl(ShellHttpScriptContext context) {
            this.context = context;
        }

        public RequestQuery getQuery() {
            return new RequestQuery(this.context);
        }

        public void addQueryParams(String query) {
            if (StringUtil.isBlank(query)) {
                return;
            }
            String value = query.startsWith("?") ? query.substring(1) : query;
            for (ShellHttpKeyValue row : ShellHttpUtil.parseQuery("?" + value)) {
                this.context.getRequest().getParams().add(row);
            }
        }

        public void addQueryParams(Object object) {
            String key = property(object, "key");
            String value = property(object, "value");
            if (StringUtil.isNotBlank(key)) {
                this.context.getRequest().getParams().add(new ShellHttpKeyValue(key, value));
            }
        }

        @Override
        public String toString() {
            return this.context.getRequest().getUrl();
        }
    }

    /**
     * 请求查询参数 API
     */
    public static class RequestQuery {

        private final ShellHttpScriptContext context;

        private RequestQuery(ShellHttpScriptContext context) {
            this.context = context;
        }

        public String get(String key) {
            for (ShellHttpKeyValue row : this.context.getRequest().getParams()) {
                if (row != null && row.isEnabled() && StringUtil.equals(row.getKey(), key)) {
                    return row.getValue();
                }
            }
            return null;
        }

        public boolean has(String key) {
            return this.get(key) != null;
        }

        public void add(String key, String value) {
            this.context.getRequest().getParams().add(new ShellHttpKeyValue(key, value));
        }

        public void add(Object object) {
            String key = property(object, "key");
            String value = property(object, "value");
            if (StringUtil.isNotBlank(key)) {
                this.add(key, value);
            }
        }

        public void remove(String key) {
            this.context.getRequest().getParams().removeIf(
                    row -> row != null && StringUtil.equals(row.getKey(), key));
        }

        public Map<String, String> toObject() {
            Map<String, String> values = new java.util.LinkedHashMap<>();
            for (ShellHttpKeyValue row : this.context.getRequest().getParams()) {
                if (row != null && row.isEnabled() && StringUtil.isNotBlank(row.getKey())) {
                    values.put(row.getKey(), row.getValue());
                }
            }
            return values;
        }
    }

    /**
     * 请求体 API
     */
    public static class RequestBody {

        private final ShellHttpScriptContext context;

        private RequestBody(ShellHttpScriptContext context) {
            this.context = context;
        }

        public String getMode() {
            String type = this.context.getRequest().getBodyType();
            if (ShellHttpRequest.BODY_RAW.equalsIgnoreCase(type)) {
                return "raw";
            }
            if (ShellHttpRequest.BODY_FORM.equalsIgnoreCase(type)) {
                return "urlencoded";
            }
            if (ShellHttpRequest.BODY_FORM_DATA.equalsIgnoreCase(type)) {
                return "formdata";
            }
            if (ShellHttpRequest.BODY_BINARY.equalsIgnoreCase(type)) {
                return "file";
            }
            return "none";
        }

        public String getRaw() {
            return this.context.getRequest().getBody();
        }

        public void setRaw(String raw) {
            this.context.getRequest().setBody(raw);
            this.context.getRequest().setBodyType(ShellHttpRequest.BODY_RAW);
        }

        public RequestFormRows getUrlencoded() {
            return new RequestFormRows(this.context);
        }

        public RequestFormRows getFormdata() {
            return new RequestFormRows(this.context);
        }
    }

    /**
     * 请求表单字段 API
     */
    public static class RequestFormRows {

        private final ShellHttpScriptContext context;

        private RequestFormRows(ShellHttpScriptContext context) {
            this.context = context;
        }

        public String get(String key) {
            for (ShellHttpKeyValue row : this.context.getRequest().getFormBody()) {
                if (row != null && row.isEnabled() && StringUtil.equals(row.getKey(), key)) {
                    return row.getValue();
                }
            }
            return null;
        }

        public void add(String key, String value) {
            this.context.getRequest().getFormBody().add(new ShellHttpKeyValue(key, value));
        }

        public void add(Object object) {
            String key = property(object, "key");
            String value = property(object, "value");
            if (StringUtil.isNotBlank(key)) {
                this.add(key, value);
            }
        }

        public void remove(String key) {
            this.context.getRequest().getFormBody().removeIf(
                    row -> row != null && StringUtil.equals(row.getKey(), key));
        }

        public Map<String, String> toObject() {
            Map<String, String> values = new java.util.LinkedHashMap<>();
            for (ShellHttpKeyValue row : this.context.getRequest().getFormBody()) {
                if (row != null && row.isEnabled() && StringUtil.isNotBlank(row.getKey())) {
                    values.put(row.getKey(), row.getValue());
                }
            }
            return values;
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

        public boolean has(String key) {
            return this.get(key) != null;
        }

        public Map<String, String> toObject() {
            Map<String, String> values = new java.util.LinkedHashMap<>();
            for (ShellHttpKeyValue row : this.request.getHeaders()) {
                if (row != null && row.isEnabled() && StringUtil.isNotBlank(row.getKey())) {
                    values.put(row.getKey(), row.getValue());
                }
            }
            return values;
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

        public long getResponseSize() {
            ShellHttpResponse response = this.context.getResponse();
            return response == null ? 0 : response.getSize();
        }

        public String getContentType() {
            ShellHttpResponse response = this.context.getResponse();
            return response == null ? null : response.getContentType();
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

        public ResponseHeaders getHeaders() {
            return new ResponseHeaders(this.context.getResponse());
        }

        public ResponseHeaders headers() {
            return this.getHeaders();
        }

        public ResponseCookies getCookies() {
            return new ResponseCookies(this.context.getResponse());
        }

        public To getTo() {
            return new To(this.context);
        }
    }

    /**
     * 响应断言入口
     */
    public static class To {

        private final ShellHttpScriptContext context;

        private final Have have;

        private To(ShellHttpScriptContext context) {
            this.context = context;
            this.have = new Have(context);
        }

        public Have getHave() {
            return this.have;
        }

        public Be getBe() {
            return new Be(this.context);
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

        public void header(String name, String value) {
            ShellHttpResponse response = this.context.getResponse();
            String actual = response == null ? null : response.getHeaders().entrySet().stream()
                    .filter(entry -> StringUtil.equals(entry.getKey(), name))
                    .map(entry -> entry.getValue().isEmpty() ? null : entry.getValue().getFirst())
                    .findFirst().orElse(null);
            if (!StringUtil.equals(actual, value)) {
                throw new AssertionError("Expected header " + name + "=" + value
                        + " but got " + actual);
            }
        }

        public void body(String expected) {
            ShellHttpResponse response = this.context.getResponse();
            String actual = response == null ? null : response.getBody();
            if (!StringUtil.equals(actual, expected)) {
                throw new AssertionError("Response body does not match");
            }
        }

        public void jsonBody() {
            ShellHttpResponse response = this.context.getResponse();
            if (response == null || StringUtil.isBlank(response.getBody())
                    || JSONUtil.parseObject(response.getBody()) == null) {
                throw new AssertionError("Response body is not valid JSON");
            }
        }
    }

    /**
     * 响应状态断言。
     */
    public static class Be {

        private final ShellHttpScriptContext context;

        private Be(ShellHttpScriptContext context) {
            this.context = context;
        }

        public boolean getOk() {
            return this.ok();
        }

        public boolean ok() {
            this.check(this.status() >= 200 && this.status() < 300, "ok");
            return true;
        }

        public boolean getSuccess() {
            return this.success();
        }

        public boolean success() {
            this.check(this.status() >= 200 && this.status() < 300, "success");
            return true;
        }

        public boolean getClientError() {
            return this.clientError();
        }

        public boolean clientError() {
            this.check(this.status() >= 400 && this.status() < 500, "client error");
            return true;
        }

        public boolean getServerError() {
            return this.serverError();
        }

        public boolean serverError() {
            this.check(this.status() >= 500 && this.status() < 600, "server error");
            return true;
        }

        public boolean getError() {
            return this.error();
        }

        public boolean error() {
            this.check(this.status() >= 400, "error");
            return true;
        }

        public boolean getRedirect() {
            return this.redirect();
        }

        public boolean redirect() {
            this.check(this.status() >= 300 && this.status() < 400, "redirect");
            return true;
        }

        private int status() {
            ShellHttpResponse response = this.context.getResponse();
            return response == null ? 0 : response.getStatusCode();
        }

        private void check(boolean condition, String type) {
            if (!condition) {
                throw new AssertionError("Expected response to be " + type
                        + " but got " + this.status());
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

        public boolean has(String key) {
            return this.get(key) != null;
        }

        public Map<String, String> toObject() {
            Map<String, String> values = new java.util.LinkedHashMap<>();
            if (this.response != null && this.response.getHeaders() != null) {
                this.response.getHeaders().forEach((key, headerValues) -> {
                    if (headerValues != null && !headerValues.isEmpty()) {
                        values.put(key, headerValues.getFirst());
                    }
                });
            }
            return values;
        }
    }

    /**
     * 响应 Cookie API。
     */
    public static class ResponseCookies {

        private final ShellHttpResponse response;

        private ResponseCookies(ShellHttpResponse response) {
            this.response = response;
        }

        public ShellHttpCookie get(String name) {
            for (ShellHttpCookie cookie : ShellHttpCookie.parse(this.response)) {
                if (StringUtil.equals(cookie.getName(), name)) {
                    return cookie;
                }
            }
            return null;
        }

        public boolean has(String name) {
            return this.get(name) != null;
        }

        public Map<String, String> toObject() {
            Map<String, String> values = new java.util.LinkedHashMap<>();
            for (ShellHttpCookie cookie : ShellHttpCookie.parse(this.response)) {
                values.put(cookie.getName(), cookie.getValue());
            }
            return values;
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
