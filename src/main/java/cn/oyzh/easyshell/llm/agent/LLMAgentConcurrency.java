package cn.oyzh.easyshell.llm.agent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Agent concurrency values and effective-limit resolution.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMAgentConcurrency {

    public static final int PHYSICAL_CORES = 0;

    public static final int UNLIMITED = -1;

    public static final List<Integer> FIXED_OPTIONS = List.of(2, 4, 8, 12, 20);

    private LLMAgentConcurrency() {
    }

    private static final class PhysicalProcessorHolder {
        private static final int COUNT = probePhysicalProcessorCount();
    }

    public static int normalize(int value) {
        return value == PHYSICAL_CORES || value == UNLIMITED || FIXED_OPTIONS.contains(value)
                ? value
                : PHYSICAL_CORES;
    }

    public static int resolve(int configured, int scheduledSteps) {
        int steps = Math.max(1, scheduledSteps);
        int normalized = normalize(configured);
        if (normalized == UNLIMITED) {
            return steps;
        }
        int limit = normalized;
        if (normalized == PHYSICAL_CORES) {
            limit = physicalProcessorCount();
        }
        return Math.max(1, Math.min(limit, steps));
    }

    public static int physicalProcessorCount() {
        return PhysicalProcessorHolder.COUNT;
    }

    private static int probePhysicalProcessorCount() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            int count;
            if (os.contains("mac")) {
                count = parseCount(runCommand("sysctl", "-n", "hw.physicalcpu"));
            } else if (os.contains("win")) {
                count = parseCount(runCommand("powershell", "-NoProfile", "-Command",
                        "(Get-CimInstance Win32_Processor | Measure-Object -Property NumberOfCores -Sum).Sum"));
            } else {
                count = linuxPhysicalProcessorCount();
            }
            if (count > 0) {
                return count;
            }
        } catch (Exception ignored) {
        }
        return Math.max(1, Runtime.getRuntime().availableProcessors());
    }

    private static int linuxPhysicalProcessorCount() throws IOException {
        Path cpuInfo = Path.of("/proc/cpuinfo");
        if (!Files.isRegularFile(cpuInfo)) {
            return -1;
        }
        Set<String> cores = new HashSet<>();
        Integer fallback = null;
        String physicalId = null;
        String coreId = null;
        for (String line : Files.readAllLines(cpuInfo, StandardCharsets.UTF_8)) {
            int separator = line.indexOf(':');
            if (separator <= 0) {
                if (physicalId != null && coreId != null) {
                    cores.add(physicalId + ":" + coreId);
                }
                physicalId = null;
                coreId = null;
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if (key.equals("physical id")) {
                physicalId = value;
            } else if (key.equals("core id")) {
                coreId = value;
            } else if (key.equals("cpu cores") && fallback == null) {
                fallback = parseCount(value);
            }
        }
        if (physicalId != null && coreId != null) {
            cores.add(physicalId + ":" + coreId);
        }
        return !cores.isEmpty() ? cores.size() : fallback == null ? -1 : fallback;
    }

    private static String runCommand(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!process.waitFor(2, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            return "";
        }
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    private static int parseCount(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return -1;
        }
    }
}
