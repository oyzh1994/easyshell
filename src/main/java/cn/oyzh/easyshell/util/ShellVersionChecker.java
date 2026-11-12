package cn.oyzh.easyshell.util;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.plus.util.FXUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * 新版本检测器。
 *
 * <p>只检测 GitHub 上标记为正式版本、且 tag 形如 {@code v1.2.4} 的 release，
 * beta、rc、preview 等版本会被忽略。</p>
 *
 * <p>检测本身不弹窗，结果缓存后由设置页的“检查更新”页面展示。</p>
 *
 * @author oyzh
 * @since 2026-11-11
 */
public final class ShellVersionChecker {

    /**
     * 稳定版本格式，仅接受三段数字，允许 v 前缀。
     */
    private static final Pattern STABLE_VERSION = Pattern.compile("^[vV]?(\\d+)\\.(\\d+)\\.(\\d+)$");

    /**
     * 启动后延迟检测时间，避免和主界面初始化争抢资源。
     */
    private static final long STARTUP_DELAY_MS = 2500;

    /**
     * 自动启动检测每次进程只执行一次。
     */
    private static final AtomicBoolean CHECKED = new AtomicBoolean();

    /**
     * 最近一次检测结果，设置页直接复用。
     */
    private static volatile CheckResult cachedResult;

    private ShellVersionChecker() {
    }

    /**
     * 获取最近一次检测结果。
     *
     * @return 检测结果，未检测时返回 null
     */
    public static CheckResult cachedResult() {
        return cachedResult;
    }

    /**
     * 启动时异步检测新版本，未开启更新提醒时不执行。
     */
    public static void checkOnStartup() {
        if (!isReminderEnabled(ShellSettingStore.SETTING)) {
            return;
        }
        checkAsync(false, null);
    }

    /**
     * 是否开启了更新提醒。
     *
     * @param setting 设置
     * @return 是否开启
     */
    static boolean isReminderEnabled(ShellSetting setting) {
        if (setting == null) {
            return false;
        }
        String reminder = setting.getVersionCheckReminder();
        return "always".equals(reminder)
                || "day".equals(reminder)
                || "week".equals(reminder)
                || "month".equals(reminder);
    }

    /**
     * 异步检测新版本。
     *
     * @param manual   是否手动触发，手动检测不会触发提醒
     * @param callback 检测完成回调，可为 null；回调在 JavaFX 线程执行
     */
    public static void checkAsync(boolean manual, Consumer<CheckResult> callback) {
        if (!manual && !CHECKED.compareAndSet(false, true)) {
            return;
        }
        ThreadUtil.start(() -> {
            if (!manual) {
                ThreadUtil.sleep(STARTUP_DELAY_MS);
            }
            CheckResult result = check(Project.load());
            cachedResult = result;
            if (!manual && result.success() && result.updateAvailable()) {
                FXUtil.runLater(() -> {
                    ShellSetting setting = ShellSettingStore.SETTING;
                    if (shouldRemind(setting)) {
                        // “每次启动提醒”不参与提醒日期记录，只有按周期提醒才记录
                        String reminder = setting.getVersionCheckReminder();
                        if (StringUtil.isNotBlank(reminder) && !"always".equals(reminder)) {
                            setting.setVersionCheckLastRemindTime(System.currentTimeMillis());
                            ShellSettingStore.INSTANCE.update(setting);
                        }
                        ShellViewFactory.settingUpdate();
                    }
                });
            }
            if (callback != null) {
                FXUtil.runLater(() -> callback.accept(result));
            }
        });
    }

    /**
     * 执行一次版本检测。
     *
     * @param project 项目信息
     * @return 检测结果
     */
    private static CheckResult check(Project project) {
        if (project == null) {
            return CheckResult.failure("", "project information is unavailable");
        }
        String currentVersion = project.getVersion();
        if (StringUtil.isBlank(project.getReleaseUrl())) {
            return CheckResult.failure(currentVersion, "release URL is unavailable");
        }
        try {
            Release release = fetchLatestStable(project.getReleaseUrl());
            if (release == null) {
                return CheckResult.failure(currentVersion, "no stable release found");
            }
            boolean updateAvailable = compareVersions(release.version(), currentVersion) > 0;
            return new CheckResult(true, currentVersion, release.version(), release.url(),
                    updateAvailable, null);
        } catch (Exception ex) {
            JulLog.debug("check EasyShell update failed", ex);
            return CheckResult.failure(currentVersion, ex.getMessage());
        }
    }

    /**
     * 判断当前是否应该自动提醒。
     *
     * @param setting 设置
     * @return 是否提醒
     */
    static boolean shouldRemind(ShellSetting setting) {
        if (setting == null) {
            return false;
        }
        String reminder = setting.getVersionCheckReminder();
        if (StringUtil.isBlank(reminder) || "off".equals(reminder) || "startup".equals(reminder)) {
            return false;
        }
        if ("always".equals(reminder)) {
            return true;
        }
        long interval;
        if ("day".equals(reminder)) {
            interval = TimeUnit.DAYS.toMillis(1);
        } else if ("week".equals(reminder)) {
            interval = TimeUnit.DAYS.toMillis(7);
        } else if ("month".equals(reminder)) {
            interval = TimeUnit.DAYS.toMillis(30);
        } else {
            return false;
        }
        Long lastRemindTime = setting.getVersionCheckLastRemindTime();
        return lastRemindTime == null
                || System.currentTimeMillis() - lastRemindTime >= interval;
    }

    /**
     * 从 GitHub Releases API 获取最新的正式版本。
     *
     * @param releaseUrl 项目发布地址
     * @return 最新正式版本，没有时返回 null
     * @throws Exception 请求异常
     */
    private static Release fetchLatestStable(String releaseUrl) throws Exception {
        String apiUrl = releasesApiUrl(releaseUrl);
        if (StringUtil.isBlank(apiUrl)) {
            return null;
        }
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        HttpGet request = new HttpGet(apiUrl);
        request.setConfig(config);
        request.setHeader("Accept", "application/vnd.github+json");
        request.setHeader("User-Agent", "EasyShell-Version-Checker");
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 200 || response.getEntity() == null) {
                return null;
            }
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONArray releases = JSON.parseArray(body);
            if (releases == null) {
                return null;
            }
            Release latest = null;
            for (int i = 0; i < releases.size(); i++) {
                JSONObject item = releases.getJSONObject(i);
                if (item == null || item.getBooleanValue("draft") || item.getBooleanValue("prerelease")) {
                    continue;
                }
                String tag = item.getString("tag_name");
                if (!isStableVersion(tag)) {
                    continue;
                }
                String version = normalizeVersion(tag);
                String url = item.getString("html_url");
                if (StringUtil.isBlank(url)) {
                    url = releaseUrl;
                }
                Release candidate = new Release(version, tag, url);
                if (latest == null || compareVersions(candidate.version(), latest.version()) > 0) {
                    latest = candidate;
                }
            }
            return latest;
        }
    }

    /**
     * 由发布地址推导 GitHub Releases API 地址。
     *
     * @param releaseUrl 发布地址
     * @return API 地址
     */
    static String releasesApiUrl(String releaseUrl) {
        if (StringUtil.isBlank(releaseUrl)) {
            return null;
        }
        try {
            URI uri = URI.create(releaseUrl.trim());
            String path = uri.getPath();
            if (StringUtil.isBlank(path)) {
                return null;
            }
            int index = path.lastIndexOf("/releases");
            if (index < 0) {
                return null;
            }
            return "https://api.github.com/repos" + path.substring(0, index) + "/releases?per_page=100";
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 是否为稳定的三段式版本号。
     *
     * @param value 版本号
     * @return 是否稳定
     */
    static boolean isStableVersion(String value) {
        return value != null && STABLE_VERSION.matcher(value.trim()).matches();
    }

    /**
     * 规范化版本号，去掉 v 前缀。
     *
     * @param value 版本号
     * @return 规范化版本号
     */
    static String normalizeVersion(String value) {
        String version = value == null ? "" : value.trim();
        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1);
        }
        return version;
    }

    /**
     * 比较两个版本号。
     *
     * @param left  左版本
     * @param right 右版本
     * @return 比较结果
     */
    static int compareVersions(String left, String right) {
        int[] leftParts = parseVersion(left);
        int[] rightParts = parseVersion(right);
        if (leftParts == null || rightParts == null) {
            return 0;
        }
        for (int i = 0; i < leftParts.length; i++) {
            int result = Integer.compare(leftParts[i], rightParts[i]);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    /**
     * 解析版本号。
     *
     * @param value 版本号
     * @return 版本数字，非法时返回 null
     */
    private static int[] parseVersion(String value) {
        if (!isStableVersion(value)) {
            return null;
        }
        String[] parts = normalizeVersion(value).split("\\.");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new int[]{
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            };
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 发布版本信息，仅用于检测结果。
     */
    private record Release(String version, String tag, String url) {
    }

    /**
     * 检测结果，供设置页展示。
     *
     * @param success         检测是否成功
     * @param currentVersion  当前版本
     * @param latestVersion   最新正式版本
     * @param releaseUrl      最新版本发布地址
     * @param updateAvailable 是否存在新版本
     * @param error           失败原因
     */
    public record CheckResult(
            boolean success,
            String currentVersion,
            String latestVersion,
            String releaseUrl,
            boolean updateAvailable,
            String error) {

        static CheckResult failure(String currentVersion, String error) {
            return new CheckResult(false, currentVersion, null, null, false, error);
        }
    }
}
