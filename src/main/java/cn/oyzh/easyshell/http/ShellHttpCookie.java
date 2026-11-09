package cn.oyzh.easyshell.http;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.util.StringUtil;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 响应 Cookie。
 *
 * @author oyzh
 * @since 2026-11-09
 */
public class ShellHttpCookie {

    private String name;

    private String value;

    private String domain;

    private String path;

    private String expires;

    private boolean httpOnly;

    private boolean secure;

    private String sameSite;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExpires() {
        return expires;
    }

    /**
     * 获取格式化后的到期时间。
     *
     * @return 到期时间
     */
    public String getExpiresText() {
        if (StringUtil.isBlank(this.expires)) {
            return "";
        }
        if (this.expires.startsWith("Max-Age=")) {
            try {
                long seconds = Long.parseLong(this.expires.substring("Max-Age=".length()));
                Date date = new Date(System.currentTimeMillis() + seconds * 1000);
                return DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            } catch (Exception ignored) {
                return this.expires;
            }
        }
        Date date = parseExpires(this.expires);
        return date == null ? this.expires : DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    /**
     * 解析响应中的 Set-Cookie。
     *
     * @param response 响应
     * @return Cookie 列表
     */
    public static List<ShellHttpCookie> parse(ShellHttpResponse response) {
        List<ShellHttpCookie> cookies = new ArrayList<>();
        if (response == null || response.getHeaders() == null) {
            return cookies;
        }
        for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
            if (!"Set-Cookie".equalsIgnoreCase(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            for (String header : entry.getValue()) {
                ShellHttpCookie cookie = parse(header, response.getUri());
                if (cookie != null) {
                    cookies.add(cookie);
                }
            }
        }
        return cookies;
    }

    /**
     * 解析单个 Set-Cookie 值。
     *
     * @param header Set-Cookie
     * @param uri    响应地址
     * @return Cookie
     */
    private static Date parseExpires(String value) {
        String text = value.trim().replaceAll("\\s+", " ");
        try {
            return Date.from(ZonedDateTime.parse(text,
                    DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.ENGLISH)).toInstant());
        } catch (Exception ignored) {
        }
        String[] patterns = {
                "EEE, dd MMM yyyy HH:mm:ss zzz",
                "EEE, d MMM yyyy HH:mm:ss zzz",
                "EEE, dd-MMM-yyyy HH:mm:ss zzz",
                "EEE, d-MMM-yyyy HH:mm:ss zzz",
                "EEE MMM d HH:mm:ss yyyy",
                "EEE, dd MMM yyyy HH:mm:ss Z"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
                format.setLenient(false);
                return format.parse(text);
            } catch (Exception ignored) {
            }
        }
        try {
            return DateUtil.parse(text, "EEE, dd MMM yyyy HH:mm:ss zzz");
        } catch (Exception ignored) {
        }
        return null;
    }

    private static ShellHttpCookie parse(String header, URI uri) {
        if (StringUtil.isBlank(header)) {
            return null;
        }
        String[] parts = header.split(";");
        int index = parts[0].indexOf('=');
        if (index <= 0) {
            return null;
        }
        ShellHttpCookie cookie = new ShellHttpCookie();
        cookie.setName(parts[0].substring(0, index).trim());
        cookie.setValue(parts[0].substring(index + 1).trim());
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            int attrIndex = part.indexOf('=');
            String attrName = attrIndex >= 0 ? part.substring(0, attrIndex).trim() : part;
            String attrValue = attrIndex >= 0 ? part.substring(attrIndex + 1).trim() : "";
            switch (attrName.toLowerCase(Locale.ROOT)) {
                case "domain" -> cookie.setDomain(attrValue);
                case "path" -> cookie.setPath(attrValue);
                case "expires" -> cookie.setExpires(attrValue);
                case "max-age" -> {
                    if (StringUtil.isBlank(cookie.getExpires())) {
                        cookie.setExpires("Max-Age=" + attrValue);
                    }
                }
                case "httponly" -> cookie.setHttpOnly(true);
                case "secure" -> cookie.setSecure(true);
                case "samesite" -> cookie.setSameSite(attrValue);
                default -> {
                }
            }
        }
        if (StringUtil.isBlank(cookie.getDomain()) && uri != null && uri.getHost() != null) {
            cookie.setDomain(uri.getHost());
        }
        if (StringUtil.isBlank(cookie.getPath())) {
            cookie.setPath("/");
        }
        return cookie;
    }
}
