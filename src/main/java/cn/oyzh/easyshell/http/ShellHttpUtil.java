package cn.oyzh.easyshell.http;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.util.ShellI18nHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * HTTP 工作区工具方法。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public final class ShellHttpUtil {

    /**
     * 环境变量匹配表达式
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([^{}]+?)\\s*}}");

    /**
     * 字符集匹配表达式
     */
    private static final Pattern CHARSET_PATTERN = Pattern.compile(
            "charset\\s*=\\s*[\"']?([^;\"'\\s]+)", Pattern.CASE_INSENSITIVE);

    /**
     * 常见的文本内容类型
     */
    private static final String[] TEXT_CONTENT_TYPES = {
            "text/",
            "json",
            "xml",
            "javascript",
            "ecmascript",
            "x-www-form-urlencoded",
            "yaml",
            "yml",
            "csv",
            "graphql",
            "x-ndjson",
            "toml",
            "sql"
    };

    private ShellHttpUtil() {
    }

    /**
     * 使用当前环境替换 {{变量}}。
     *
     * @param text        原始文本
     * @param environment 当前环境
     * @return 替换结果
     */
    public static String resolveVariables(String text, ShellHttpEnvironment environment) {
        if (text == null || text.isEmpty() || environment == null) {
            return text;
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (ShellHttpKeyValue row : environment.getValues()) {
            if (row != null && row.isEnabled() && StringUtil.isNotBlank(row.getKey())) {
                values.put(row.getKey().trim(), row.getValue());
            }
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = values.getOrDefault(matcher.group(1).trim(), matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 拼接请求地址和查询参数。
     *
     * @param url         请求地址
     * @param rows        查询参数
     * @param environment 当前环境
     * @return 最终地址
     */
    public static URI resolveUri(String url, List<ShellHttpKeyValue> rows,
                                 ShellHttpEnvironment environment) {
        String resolved = resolveVariables(url == null ? "" : url.trim(), environment);
        if (StringUtil.isBlank(resolved)) {
            throw new IllegalArgumentException(ShellI18nHelper.httpUrlEmpty());
        }
        String query = encodeRows(rows, environment);
        if (StringUtil.isNotBlank(query)) {
            resolved = appendQuery(resolved, query);
        }
        URI uri;
        try {
            uri = URI.create(resolved);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ShellI18nHelper.httpUrlInvalid(), ex);
        }
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalArgumentException(ShellI18nHelper.httpUrlInvalid());
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException(ShellI18nHelper.httpUrlInvalid());
        }
        return uri;
    }

    /**
     * 在地址中追加查询字符串，保留原有查询参数和锚点。
     *
     * @param url   地址
     * @param query 查询字符串
     * @return 最终地址
     */
    public static String appendQuery(String url, String query) {
        if (StringUtil.isBlank(query)) {
            return url;
        }
        int fragmentIndex = url.indexOf('#');
        String fragment = fragmentIndex >= 0 ? url.substring(fragmentIndex) : "";
        String main = fragmentIndex >= 0 ? url.substring(0, fragmentIndex) : url;
        String separator = main.endsWith("?") || main.endsWith("&")
                ? "" : (main.contains("?") ? "&" : "?");
        return main + separator + query + fragment;
    }

    /**
     * 将启用的键值行编码为查询字符串。
     *
     * @param rows        键值行
     * @param environment 当前环境
     * @return 查询字符串
     */
    public static String encodeRows(List<ShellHttpKeyValue> rows, ShellHttpEnvironment environment) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (ShellHttpKeyValue row : rows) {
            if (row == null || !row.isEnabled() || StringUtil.isBlank(row.getKey())) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(encode(resolveVariables(row.getKey().trim(), environment)));
            builder.append('=');
            builder.append(encode(resolveVariables(row.getValue(), environment)));
        }
        return builder.toString();
    }

    /**
     * URL 编码。
     *
     * @param value 值
     * @return 编码结果
     */
    public static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /**
     * 根据 Content-Type 获取字符集。
     *
     * @param contentType 内容类型
     * @return 字符集
     */
    public static Charset charset(String contentType) {
        if (StringUtil.isNotBlank(contentType)) {
            Matcher matcher = CHARSET_PATTERN.matcher(contentType);
            if (matcher.find()) {
                try {
                    return Charset.forName(matcher.group(1));
                } catch (Exception ignored) {
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    /**
     * 判断响应是否适合按文本显示。
     *
     * @param contentType 内容类型
     * @return 结果
     */
    public static boolean isTextContent(String contentType) {
        if (StringUtil.isBlank(contentType)) {
            return true;
        }
        String value = contentType.toLowerCase(Locale.ROOT);
        for (String type : TEXT_CONTENT_TYPES) {
            if (value.contains(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 格式化响应正文。
     *
     * @param body        正文
     * @param contentType 内容类型
     * @param prettyJson  是否格式化 JSON
     * @return 展示正文
     */
    public static String formatResponseBody(String body, String contentType, boolean prettyJson) {
        body = stripZeroWidth(body);
        if (prettyJson && StringUtil.isNotBlank(body) && contentType != null
                && contentType.toLowerCase(Locale.ROOT).contains("json")) {
            try {
                if (JSONUtil.isJson(body)) {
                    return JSONUtil.toPretty(body);
                }
            } catch (Exception ignored) {
            }
        }
        return body;
    }

    /**
     * 为 HTML 响应创建本地预览文件。
     *
     * @param body    HTML 正文
     * @param baseUri 最终请求地址
     * @return 本地文件
     * @throws Exception 写入异常
     */
    public static Path createHtmlPreview(String body, URI baseUri) throws Exception {
        Path file = Files.createTempFile("easyshell-http-", ".html");
        String html = body == null ? "" : body;
        if (baseUri != null && !html.toLowerCase(Locale.ROOT).contains("<base ")) {
            String baseTag = "<base href=\"" + baseUri + "\">";
            Matcher head = Pattern.compile("<head(?:\\s[^>]*)?>", Pattern.CASE_INSENSITIVE).matcher(html);
            if (head.find()) {
                html = html.substring(0, head.end()) + baseTag + html.substring(head.end());
            } else {
                Matcher htmlTag = Pattern.compile("<html(?:\\s[^>]*)?>", Pattern.CASE_INSENSITIVE).matcher(html);
                if (htmlTag.find()) {
                    html = html.substring(0, htmlTag.end()) + "<head>" + baseTag + "</head>" + html.substring(htmlTag.end());
                } else {
                    html = "<!doctype html><html><head>" + baseTag + "</head><body>"
                            + html + "</body></html>";
                }
            }
        }
        Files.writeString(file, html, StandardCharsets.UTF_8);
        file.toFile().deleteOnExit();
        return file;
    }

    /**
     * 将字节数转换为易读文本。
     *
     * @param size 字节数
     * @return 文本
     */
    public static String humanSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KB", size / 1024.0);
        }
        if (size < 1024L * 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f MB", size / (1024.0 * 1024.0));
        }
        return String.format(Locale.ROOT, "%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * 常见 HTTP 状态描述。
     *
     * @param statusCode 状态码
     * @return 描述
     */
    public static String statusText(int statusCode) {
        return switch (statusCode) {
            case 100 -> "Continue";
            case 101 -> "Switching Protocols";
            case 102 -> "Processing";
            case 103 -> "Early Hints";
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 203 -> "Non-Authoritative Information";
            case 204 -> "No Content";
            case 205 -> "Reset Content";
            case 206 -> "Partial Content";
            case 207 -> "Multi-Status";
            case 208 -> "Already Reported";
            case 226 -> "IM Used";
            case 300 -> "Multiple Choices";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 303 -> "See Other";
            case 304 -> "Not Modified";
            case 305 -> "Use Proxy";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 402 -> "Payment Required";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 406 -> "Not Acceptable";
            case 407 -> "Proxy Authentication Required";
            case 408 -> "Request Timeout";
            case 409 -> "Conflict";
            case 410 -> "Gone";
            case 411 -> "Length Required";
            case 412 -> "Precondition Failed";
            case 413 -> "Content Too Large";
            case 414 -> "URI Too Long";
            case 415 -> "Unsupported Media Type";
            case 416 -> "Range Not Satisfiable";
            case 417 -> "Expectation Failed";
            case 418 -> "I'm a Teapot";
            case 421 -> "Misdirected Request";
            case 422 -> "Unprocessable Content";
            case 423 -> "Locked";
            case 424 -> "Failed Dependency";
            case 425 -> "Too Early";
            case 426 -> "Upgrade Required";
            case 428 -> "Precondition Required";
            case 429 -> "Too Many Requests";
            case 431 -> "Request Header Fields Too Large";
            case 451 -> "Unavailable For Legal Reasons";
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            case 505 -> "HTTP Version Not Supported";
            case 506 -> "Variant Also Negotiates";
            case 507 -> "Insufficient Storage";
            case 508 -> "Loop Detected";
            case 510 -> "Not Extended";
            case 511 -> "Network Authentication Required";
            default -> "";
        };
    }

    /**
     * 解压响应体。
     *
     * @param body     原始响应体
     * @param encoding Content-Encoding 响应头
     * @return 解压后的响应体
     * @throws IOException 解压异常
     */
    public static byte[] decompress(byte[] body, String encoding) throws IOException {
        if (body == null || body.length == 0) {
            return body;
        }
        if (StringUtil.isBlank(encoding)) {
            return isGzip(body) ? gunzip(body) : body;
        }
        String[] encodings = encoding.split(",");
        byte[] result = body;
        // Content-Encoding 按应用顺序排列，解码时需要倒序处理
        for (int i = encodings.length - 1; i >= 0; i--) {
            String value = encodings[i].trim().toLowerCase(Locale.ROOT);
            if (value.isEmpty() || "identity".equals(value)) {
                continue;
            }
            if ("gzip".equals(value) || "x-gzip".equals(value)) {
                if (isGzip(result)) {
                    result = gunzip(result);
                }
            } else if ("deflate".equals(value)) {
                result = inflate(result);
            } else {
                throw new IOException("Unsupported Content-Encoding: " + value);
            }
        }
        return result;
    }

    /**
     * 判断是否为 gzip 数据。
     *
     * @param body 数据
     * @return 结果
     */
    private static boolean isGzip(byte[] body) {
        return body.length >= 2 && (body[0] & 0xff) == 0x1f && (body[1] & 0xff) == 0x8b;
    }

    /**
     * 解压 gzip 数据。
     *
     * @param body 数据
     * @return 解压结果
     * @throws IOException 解压异常
     */
    private static byte[] gunzip(byte[] body) throws IOException {
        try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(body));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            input.transferTo(output);
            return output.toByteArray();
        }
    }

    /**
     * 解压 deflate 数据。
     *
     * @param body 数据
     * @return 解压结果
     * @throws IOException 解压异常
     */
    private static byte[] inflate(byte[] body) throws IOException {
        try (InflaterInputStream input = new InflaterInputStream(new ByteArrayInputStream(body));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            input.transferTo(output);
            return output.toByteArray();
        } catch (IOException ex) {
            // 部分服务端返回的是 raw deflate，而不是 zlib 包装格式
            try (InflaterInputStream input = new InflaterInputStream(
                    new ByteArrayInputStream(body), new Inflater(true));
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                input.transferTo(output);
                return output.toByteArray();
            }
        }
    }


    /**
     * 移除零宽字符，避免 JavaFX 文本控件在混合字体回退时把整行渲染成错误字形。
     *
     * @param text 文本
     * @return 处理后的文本
     */
    public static String stripZeroWidth(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.replace("\u200B", "")
                .replace("\u200C", "")
                .replace("\u200D", "")
                .replace("\u2060", "")
                .replace("\uFEFF", "");
    }

}
