package cn.oyzh.easyshell.serialPort;

import cn.oyzh.common.log.JulLog;
import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Iammm
 * 2025/4/6 10:48
 * 监听可用串口列表
 */
public class FXPortMonitor {

    private final ObservableList<SerialPort> portList = FXCollections.observableArrayList();
    private final ComboBox<SerialPort> comboBox;
    private ScheduledExecutorService executor;

    public FXPortMonitor(ComboBox<SerialPort> comboBox) {
        this.comboBox = comboBox;
        comboBox.setItems(portList);
        // 添加显示格式转换
        comboBox.setConverter(new StringConverter<SerialPort>() {
            @Override
            public String toString(SerialPort port) {
                return port != null ? port.getSystemPortName() : "";
            }

            @Override
            public SerialPort fromString(String string) {
                return portList.stream()
                        .filter(p -> p.getSystemPortName().equals(string))
                        .findFirst().orElse(null);
            }
        });
    }

    public void startMonitoring(int intervalMillis) {
        JulLog.info("开始可用串口列表检测");
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                checkPortChangesAsync();
                JulLog.debug("检测可用串口列表");
            } catch (Exception e) {
                JulLog.error("检测任务异常: " + e);
            }
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public void stopMonitoring() {
        JulLog.info("停止可用串口列表检测");
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void checkPortChangesAsync() {
        try {
            Set<String> currentPorts = new HashSet<>();
            SerialPort[] ports = SerialPort.getCommPorts();
            if (ports == null) {
                JulLog.warn("获取串口列表返回null，可能驱动异常");
                return;
            }

            JulLog.debug("可用串口数量：" + ports.length);
            for (SerialPort port : ports) {
                String portId = port.getSystemPortName(); // 获取系统级唯一标识（如"COM3"）
                currentPorts.add(portId);
                JulLog.debug("可用串口：" + portId);
            }

            Platform.runLater(() -> {
                try {
                    // 添加新端口（通过标识符判断）
                    Arrays.stream(ports)
                            .filter(port -> portList.stream().noneMatch(p ->
                                    p.getSystemPortName().equals(port.getSystemPortName())))
                            .forEach(portList::add);

                    // 移除消失的端口
                    new ArrayList<>(portList).stream()
                            .filter(existingPort -> !currentPorts.contains(
                                    existingPort.getSystemPortName()))
                            .forEach(portList::remove);

                    // 自动选择逻辑
                    if (comboBox.getValue() == null && !portList.isEmpty()) {
                        SerialPort newSelection = portList.getFirst();
                        comboBox.setValue(newSelection);
                        JulLog.debug("自动选中端口: " + newSelection.getSystemPortName());
                    }
                } catch (Exception e) {
                    JulLog.error("UI更新异常: " + e);
                }
            });
        } catch (Exception e) {
            JulLog.error("串口检测发生异常: " + e);
        } catch (Throwable t) { // 捕获Error级异常
            JulLog.error("致命错误: " + t);
            t.printStackTrace();
        }
    }

}
