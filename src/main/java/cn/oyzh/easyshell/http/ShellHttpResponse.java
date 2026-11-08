package cn.oyzh.easyshell.http;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP 响应结果。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpResponse {

    /**
     * 状态码
     */
    private int statusCode;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 最终请求地址
     */
    private URI uri;

    /**
     * 响应头
     */
    private Map<String, List<String>> headers;

    /**
     * 响应正文原始字节
     */
    private byte[] data;

    /**
     * 响应正文
     */
    private String body;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 字符集
     */
    private Charset charset;

    /**
     * 耗时，毫秒
     */
    private long durationMs;

    /**
     * 正文大小，字节
     */
    private long size;

    /**
     * 是否二进制响应
     */
    private boolean binary;

    /**
     * 临时文件
     */
    private String tempFile;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public String getTempFile() {
        return tempFile;
    }

    public void setTempFile(String tempFile) {
        this.tempFile = tempFile;
    }

    /**
     * 是否 HTML 响应
     *
     * @return 结果
     */
    public boolean isHtml() {
        return this.contentType != null
                && (this.contentType.toLowerCase(Locale.ROOT).contains("text/html")
                || this.contentType.toLowerCase(Locale.ROOT).contains("application/xhtml"));
    }

    /**
     * 是否 JSON 响应
     *
     * @return 结果
     */
    public boolean isJson() {
        if (this.contentType == null) {
            return false;
        }
        String type = this.contentType.toLowerCase(Locale.ROOT);
        return type.contains("application/json") || type.contains("+json");
    }

    /**
     * 是否 CSS 响应
     *
     * @return 结果
     */
    public boolean isCss() {
        return this.contentType != null
                && this.contentType.toLowerCase(Locale.ROOT).contains("text/css");
    }

    /**
     * 是否 JavaScript 响应
     *
     * @return 结果
     */
    public boolean isJavaScript() {
        if (this.contentType == null) {
            return false;
        }
        String type = this.contentType.toLowerCase(Locale.ROOT);
        return type.contains("javascript") || type.contains("ecmascript");
    }

    /**
     * 是否 Markdown 响应
     *
     * @return 结果
     */
    public boolean isMarkdown() {
        return this.contentType != null
                && this.contentType.toLowerCase(Locale.ROOT).contains("markdown");
    }

    /**
     * 是否成功状态码
     *
     * @return 结果
     */
    public boolean isSuccessful() {
        return this.statusCode >= 200 && this.statusCode < 300;
    }
}
