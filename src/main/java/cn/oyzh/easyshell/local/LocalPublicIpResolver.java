package cn.oyzh.easyshell.local;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Resolves the current public egress IP by querying external IP echo services.
 *
 * <p>There is no reliable way to obtain a NAT public address without contacting
 * an external service. Local interface addresses are intentionally rejected.</p>
 *
 * @author oyzh
 * @since 2026-11-21
 */
final class LocalPublicIpResolver {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private static final int MAX_RESPONSE_LENGTH = 4096;

    private static final Pattern IPV4 = Pattern.compile(
            "(?<![0-9.])(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
                    + "(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}(?![0-9.])");

    private static final Pattern IPV6_CANDIDATE = Pattern.compile(
            "(?i)(?<![0-9a-f:])(?:[0-9a-f]{0,4}:){2,}[0-9a-f:.]*[0-9a-f]"
                    + "(?![0-9a-f:])");

    private static final List<URI> DEFAULT_ENDPOINTS = List.of(
            URI.create("https://api.ipify.org"),
            URI.create("https://ifconfig.me/ip"),
            URI.create("https://icanhazip.com"),
            URI.create("https://ipinfo.io/ip"),
            URI.create("https://myip.ipip.net")
    );

    private final List<URI> endpoints;

    private final Duration timeout;

    private final HttpClient client;

    LocalPublicIpResolver() {
        this(DEFAULT_ENDPOINTS, TIMEOUT);
    }

    LocalPublicIpResolver(List<URI> endpoints, Duration timeout) {
        this(endpoints, timeout, ProxySelector.getDefault());
    }

    LocalPublicIpResolver(List<URI> endpoints, Duration timeout, ProxySelector proxySelector) {
        this.endpoints = List.copyOf(endpoints);
        this.timeout = timeout;
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL);
        if (proxySelector != null) {
            builder.proxy(proxySelector);
        }
        this.client = builder.build();
    }

    Resolution resolve() {
        List<String> errors = new ArrayList<>();
        for (URI endpoint : this.endpoints) {
            try {
                HttpRequest request = HttpRequest.newBuilder(endpoint)
                        .timeout(this.timeout)
                        .header("Accept", "text/plain")
                        .header("User-Agent", "EasyShell public-ip probe")
                        .GET()
                        .build();
                HttpResponse<String> response = this.client.send(
                        request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    errors.add(endpoint + ": HTTP " + response.statusCode());
                    continue;
                }
                String body = limit(response.body());
                String ip = extractPublicIp(body);
                if (ip != null) {
                    return new Resolution(ip, endpoint.toString(), List.copyOf(errors));
                }
                errors.add(endpoint + ": response did not contain a public IP literal");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                errors.add(endpoint + ": interrupted");
                return new Resolution(null, null, List.copyOf(errors));
            } catch (Exception ex) {
                errors.add(endpoint + ": " + errorMessage(ex));
            }
        }
        return new Resolution(null, null, List.copyOf(errors));
    }

    static String extractPublicIp(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        var ipv4 = IPV4.matcher(text);
        while (ipv4.find()) {
            String candidate = ipv4.group();
            if (isPublicIp(candidate)) {
                return candidate;
            }
        }
        var ipv6 = IPV6_CANDIDATE.matcher(text);
        while (ipv6.find()) {
            String candidate = trimAddress(ipv6.group());
            if (isPublicIp(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    static boolean isPublicIp(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String candidate = trimAddress(value);
        boolean ipv4 = IPV4.matcher(candidate).matches();
        boolean ipv6 = candidate.contains(":") && IPV6_CANDIDATE.matcher(candidate).matches();
        if (!ipv4 && !ipv6) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(candidate);
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress()
                    || address.isMulticastAddress()) {
                return false;
            }
            if (address instanceof Inet4Address) {
                return isPublicIpv4(address.getAddress());
            }
            return isPublicIpv6(address.getAddress());
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean isPublicIpv4(byte[] address) {
        int first = address[0] & 0xff;
        int second = address[1] & 0xff;
        int third = address[2] & 0xff;
        return first != 0
                && first < 224
                && !(first == 10)
                && !(first == 100 && second >= 64 && second <= 127)
                && !(first == 127)
                && !(first == 169 && second == 254)
                && !(first == 172 && second >= 16 && second <= 31)
                && !(first == 192 && second == 0 && third == 0)
                && !(first == 192 && second == 0 && third == 2)
                && !(first == 192 && second == 168)
                && !(first == 198 && (second == 18 || second == 19))
                && !(first == 198 && second == 51 && third == 100)
                && !(first == 203 && second == 0 && third == 113)
                && !(first == 255);
    }

    private static boolean isPublicIpv6(byte[] address) {
        int first = address[0] & 0xff;
        int second = address[1] & 0xff;
        // Unique local addresses fc00::/7.
        if ((first & 0xfe) == 0xfc) {
            return false;
        }
        // Documentation range 2001:db8::/32.
        return !(first == 0x20 && second == 0x01
                && (address[2] & 0xff) == 0x0d && (address[3] & 0xff) == 0xb8);
    }

    private static String trimAddress(String value) {
        String result = value == null ? "" : value.trim();
        while (result.startsWith("[")) {
            result = result.substring(1);
        }
        while (result.endsWith("]")) {
            result = result.substring(0, result.length() - 1);
        }
        int zone = result.indexOf('%');
        return zone < 0 ? result : result.substring(0, zone);
    }

    private static String limit(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= MAX_RESPONSE_LENGTH
                ? value
                : value.substring(0, MAX_RESPONSE_LENGTH);
    }

    private static String errorMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank()
                ? ex.getClass().getSimpleName()
                : message;
    }

    record Resolution(String ip, String source, List<String> errors) {
    }
}
