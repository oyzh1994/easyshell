package cn.oyzh.easyshell.http;

import cn.oyzh.common.util.StringUtil;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP Digest 认证工具。
 *
 * @author oyzh
 * @since 2026-11-09
 */
public final class ShellHttpDigest {

    private ShellHttpDigest() {
    }

    /**
     * 根据服务端 WWW-Authenticate 质询生成 Authorization 头。
     *
     * @param user      用户名
     * @param password  密码
     * @param uri       请求地址
     * @param method    请求方法
     * @param challenge 质询值
     * @return Authorization 头
     */
    public static String authorization(String user, String password, URI uri,
                                       String method, String challenge) {
        Map<String, String> params = parse(challenge);
        String realm = params.get("realm");
        String nonce = params.get("nonce");
        if (StringUtil.isBlank(realm) || StringUtil.isBlank(nonce)) {
            throw new IllegalArgumentException("Digest challenge missing realm or nonce");
        }
        String algorithm = StringUtil.blankToDefault(params.get("algorithm"), "MD5").toUpperCase(Locale.ROOT);
        String hashName = algorithm.startsWith("SHA-256") ? "SHA-256" : "MD5";
        boolean session = algorithm.endsWith("-SESS");
        String qop = params.get("qop");
        String selectedQop = null;
        if (StringUtil.isNotBlank(qop)) {
            for (String value : qop.split(",")) {
                if ("auth".equalsIgnoreCase(value.trim())) {
                    selectedQop = "auth";
                    break;
                }
            }
            if (selectedQop == null) {
                throw new IllegalArgumentException("Only Digest qop=auth is supported");
            }
        }
        String digestUri = uri.getRawPath() == null ? "/" : uri.getRawPath();
        if (uri.getRawQuery() != null) {
            digestUri += "?" + uri.getRawQuery();
        }
        String nc = "00000001";
        String cnonce = UUID.randomUUID().toString().replace("-", "");
        String ha1 = hash(hashName, user + ":" + realm + ":" + password);
        if (session) {
            ha1 = hash(hashName, ha1 + ":" + nonce + ":" + cnonce);
        }
        String ha2 = hash(hashName, method + ":" + digestUri);
        String response = selectedQop == null
                ? hash(hashName, ha1 + ":" + nonce + ":" + ha2)
                : hash(hashName, ha1 + ":" + nonce + ":" + nc + ":" + cnonce
                        + ":" + selectedQop + ":" + ha2);
        StringBuilder builder = new StringBuilder("Digest ");
        builder.append("username=\"").append(escape(user)).append("\", ")
                .append("realm=\"").append(escape(realm)).append("\", ")
                .append("nonce=\"").append(escape(nonce)).append("\", ")
                .append("uri=\"").append(escape(digestUri)).append("\", ")
                .append("response=\"").append(response).append("\", ")
                .append("algorithm=").append(algorithm);
        if (selectedQop != null) {
            builder.append(", qop=").append(selectedQop)
                    .append(", nc=").append(nc)
                    .append(", cnonce=\"").append(cnonce).append("\"");
        }
        String opaque = params.get("opaque");
        if (opaque != null) {
            builder.append(", opaque=\"").append(escape(opaque)).append("\"");
        }
        return builder.toString();
    }

    /**
     * 解析质询参数。
     *
     * @param challenge 质询值
     * @return 参数
     */
    private static Map<String, String> parse(String challenge) {
        Map<String, String> params = new HashMap<>();
        if (challenge == null) {
            return params;
        }
        String text = challenge.trim();
        if (text.regionMatches(true, 0, "Digest", 0, 6)) {
            text = text.substring(6).trim();
        }
        int index = 0;
        while (index < text.length()) {
            while (index < text.length()
                    && (text.charAt(index) == ',' || Character.isWhitespace(text.charAt(index)))) {
                index++;
            }
            int keyStart = index;
            while (index < text.length() && text.charAt(index) != '=') {
                index++;
            }
            if (index >= text.length()) {
                break;
            }
            String key = text.substring(keyStart, index).trim().toLowerCase(Locale.ROOT);
            index++;
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
            StringBuilder value = new StringBuilder();
            if (index < text.length() && text.charAt(index) == '"') {
                index++;
                while (index < text.length()) {
                    char c = text.charAt(index++);
                    if (c == '\\' && index < text.length()) {
                        value.append(text.charAt(index++));
                    } else if (c == '"') {
                        break;
                    } else {
                        value.append(c);
                    }
                }
            } else {
                while (index < text.length() && text.charAt(index) != ',') {
                    value.append(text.charAt(index++));
                }
            }
            params.put(key, value.toString().trim());
        }
        return params;
    }

    private static String hash(String algorithm, String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(algorithm);
            byte[] bytes = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unsupported Digest algorithm: " + algorithm, ex);
        }
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
