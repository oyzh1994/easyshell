package cn.oyzh.easyshell.fx.ssh;

import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.easyshell.ssh.server.ShellServerMonitor;
import cn.oyzh.fx.plus.controls.FXProgressBar;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.pane.FXGridPane;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.DecimalFormat;

/**
 * 紧凑的 SSH 服务监控指标组件。
 *
 * @author oyzh
 * @since 2026-11-13
 */
public final class ShellServerMonitorPane extends FXGridPane {

    /**
     * 数值最多保留两位小数，并去掉无意义的尾零。
     */
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.##");

    /**
     * CPU 指标卡。
     */
    private final MetricCard cpuCard = new MetricCard("CPU", "cpu", MetricCard.GaugeType.SINGLE);

    /**
     * 内存指标卡。
     */
    private final MetricCard memoryCard = new MetricCard(I18nHelper.memory(), "memory", MetricCard.GaugeType.SINGLE);

    /**
     * 网络指标卡，合并进、出流量。
     */
    private final MetricCard networkCard = new MetricCard(I18nHelper.network(), "network", MetricCard.GaugeType.DUAL);

    /**
     * 磁盘指标卡，合并读、写速度。
     */
    private final MetricCard diskCard = new MetricCard(I18nHelper.disk(), "disk", MetricCard.GaugeType.DUAL);

    public ShellServerMonitorPane() {
        this.getStyleClass().add("server-monitor-pane");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setHgap(6);
        this.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(this, Priority.ALWAYS);

        this.cpuCard.setResponsiveCard();
        this.memoryCard.setResponsiveCard();
        this.networkCard.setResponsiveCard();
        this.diskCard.setResponsiveCard();
        this.add(this.cpuCard, 0, 0);
        this.add(this.memoryCard, 1, 0);
        this.add(this.networkCard, 2, 0);
        this.add(this.diskCard, 3, 0);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            this.getColumnConstraints().add(column);
        }

        this.clear();
        this.hideMetrics();
        URL css = ResourceUtil.getResource("/css/ssh/shell-server-monitor.css");
        if (css != null) {
            this.getStylesheets().add(css.toExternalForm());
        }
    }

    /**
     * 使用最新监控数据刷新组件。
     *
     * @param monitor 监控数据
     */
    public void update(ShellServerMonitor monitor) {
        if (monitor == null) {
            this.clear();
            return;
        }
        double cpu = positive(monitor.getCpuUsage());
        double memory = positive(monitor.getMemoryUsage());
        double receive = positive(monitor.getNetworkReceiveSpeed());
        double send = positive(monitor.getNetworkSendSpeed());
        double read = positive(monitor.getDiskReadSpeed());
        double write = positive(monitor.getDiskWriteSpeed());

        this.cpuCard.setPercent(cpu);
        this.memoryCard.setPercent(memory);
        this.networkCard.setPair("↓ " + format(receive) + "  ↑ " + format(send) + " KB/s", receive, send);
        this.diskCard.setPair("R " + format(read) + "  ·  W " + format(write) + " MB/s", read, write);
        this.networkCard.setTooltip(I18nHelper.networkInput() + ": " + format(receive) + " KB/s\n"
                + I18nHelper.networkOutput() + ": " + format(send) + " KB/s");
        this.diskCard.setTooltip(I18nHelper.diskRead() + ": " + format(read) + " MB/s\n"
                + I18nHelper.diskWrite() + ": " + format(write) + " MB/s");
    }

    /**
     * 清空为占位值。
     */
    public void clear() {
        this.cpuCard.setValue("—");
        this.cpuCard.setGauge(-1);
        this.memoryCard.setValue("—");
        this.memoryCard.setGauge(-1);
        this.networkCard.setPair("↓ —  ↑ —  KB/s", 0, 0);
        this.diskCard.setPair("R —  ·  W —  MB/s", 0, 0);
        this.networkCard.setTooltip(null);
        this.diskCard.setTooltip(null);
    }

    /**
     * 显示指标卡。
     */
    public void showMetrics() {
        this.setManaged(true);
        this.setVisible(true);
    }

    /**
     * 隐藏指标卡。
     */
    public void hideMetrics() {
        this.setManaged(false);
        this.setVisible(false);
    }

    private static double positive(double value) {
        return Double.isFinite(value) ? Math.max(0, value) : 0;
    }

    private static String formatPercent(double value) {
        return format(value) + "%";
    }

    private static String format(double value) {
        synchronized (NUMBER_FORMAT) {
            return NUMBER_FORMAT.format(value);
        }
    }

    /**
     * 单张指标卡。
     */
    private static final class MetricCard extends HBox {

        private final FXLabel valueLabel = new FXLabel();

        private final FXProgressBar gauge;

        private final FXProgressBar secondaryGauge;

        MetricCard(String title, String styleClass, GaugeType gaugeType) {
            this.getStyleClass().addAll("server-metric-card", "server-metric-" + styleClass);
            this.setAlignment(Pos.CENTER_LEFT);
            this.setSpacing(6);
            this.setMinHeight(26);
            this.setMaxHeight(26);
            this.setPrefHeight(26);
            this.setPadding(Insets.EMPTY);

            Region marker = new Region();
            marker.getStyleClass().add("server-metric-marker");
            FXLabel titleLabel = new FXLabel( title);
            titleLabel.getStyleClass().add("server-metric-title");
            titleLabel.setMinWidth(Region.USE_PREF_SIZE);
            this.valueLabel.getStyleClass().add("server-metric-value");
            this.valueLabel.setMinWidth(Region.USE_PREF_SIZE);
            VBox text;
            if (gaugeType != GaugeType.NONE) {
                this.gauge = new FXProgressBar();
                this.gauge.getStyleClass().add("server-metric-gauge");
                this.gauge.setFocusTraversable(false);
                this.gauge.setMinWidth(12);
                this.gauge.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(this.gauge, Priority.ALWAYS);
                if (gaugeType == GaugeType.DUAL) {
                    this.gauge.getStyleClass().add("server-metric-gauge-first");
                    this.secondaryGauge = new FXProgressBar();
                    this.secondaryGauge.getStyleClass().addAll("server-metric-gauge", "server-metric-gauge-second");
                    this.secondaryGauge.setFocusTraversable(false);
                    this.secondaryGauge.setMinWidth(12);
                    this.secondaryGauge.setMaxWidth(Double.MAX_VALUE);
                } else {
                    this.secondaryGauge = null;
                }
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox titleRow = new HBox(4, titleLabel, spacer, this.valueLabel);
                titleRow.setAlignment(Pos.CENTER_LEFT);
                titleRow.setMaxWidth(Double.MAX_VALUE);
                HBox gaugeRow;
                if (this.secondaryGauge == null) {
                    gaugeRow = new HBox(this.gauge);
                } else {
                    HBox.setHgrow(this.secondaryGauge, Priority.ALWAYS);
                    gaugeRow = new HBox(2, this.gauge, this.secondaryGauge);
                }
                gaugeRow.getStyleClass().add("server-metric-gauge-row");
                gaugeRow.setMaxWidth(Double.MAX_VALUE);
                text = new VBox(titleRow, gaugeRow);
                text.setSpacing(1);
            } else {
                this.gauge = null;
                this.secondaryGauge = null;
                text = new VBox(titleLabel, this.valueLabel);
                text.setSpacing(-2);
            }
            text.setAlignment(Pos.CENTER_LEFT);
            text.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(text, Priority.ALWAYS);
            this.getChildren().addAll(marker, text);
        }

        void setValue(String value) {
             this.valueLabel.text(value);
        }

        void setPercent(double value) {
            this.setValue(formatPercent(value));
            this.setGauge(value);
        }

        void setPair(String value, double first, double second) {
            this.setValue(value);
            if (this.gauge == null || this.secondaryGauge == null) {
                return;
            }
            double total = first + second;
            if (total <= 0 || !Double.isFinite(total)) {
                this.gauge.setProgress(0);
                this.secondaryGauge.setProgress(0);
                return;
            }
            this.gauge.setProgress(first / total);
            this.secondaryGauge.setProgress(second / total);
        }

        void setGauge(double value) {
            if (this.gauge == null) {
                return;
            }
            this.getStyleClass().removeAll("metric-warning", "metric-danger");
            if (value < 0) {
                this.gauge.setProgress(0);
            } else {
                this.gauge.setProgress(Math.min(1, value / 100));
                if (value >= 90) {
                    this.getStyleClass().add("metric-danger");
                } else if (value >= 75) {
                    this.getStyleClass().add("metric-warning");
                }
            }
        }

        void setTooltip(String text) {
            if (text == null || text.isBlank()) {
                this.valueLabel.setTooltip(null);
            } else {
                Tooltip tooltip = this.valueLabel.getTooltip();
                if (tooltip == null) {
                    tooltip = new Tooltip();
                    this.valueLabel.setTooltip(tooltip);
                }
                tooltip.setText(text);
            }
        }

        void setResponsiveCard() {
            this.setMinWidth(0);
            this.setPrefWidth(1);
            this.setMaxWidth(Double.MAX_VALUE);
        }

        enum GaugeType {
            NONE,
            SINGLE,
            DUAL
        }

    }
}
