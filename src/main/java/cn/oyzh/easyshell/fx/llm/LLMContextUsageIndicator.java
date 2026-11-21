package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMDurationUtil;
import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.easyshell.llm.LLMTokenUsageStats;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

/**
 * Compact circular context-window usage indicator.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMContextUsageIndicator extends StackPane {

    private static final double SIZE = 28;

    private static final double RADIUS = 10;

    private final Circle track = new Circle(SIZE / 2, SIZE / 2, RADIUS);

    private final Arc progress = new Arc(SIZE / 2, SIZE / 2, RADIUS, RADIUS, 90, 0);

    private final Tooltip tooltip = new Tooltip();

    private LLMTokenUsageStats stats = LLMTokenUsageStats.EMPTY;

    private LLMModelContextWindowUtil.ContextWindow contextWindow =
            LLMModelContextWindowUtil.ContextWindow.UNKNOWN;

    public LLMContextUsageIndicator() {
        this.getStyleClass().add("llm-context-usage");
        this.setPrefSize(SIZE, SIZE);
        this.setMinSize(SIZE, SIZE);
        this.setMaxSize(SIZE, SIZE);

        this.track.setFill(null);
        this.track.setStrokeWidth(2.0);
        this.track.getStyleClass().add("llm-context-usage-track");

        this.progress.setType(ArcType.OPEN);
        this.progress.setFill(null);
        this.progress.setStrokeWidth(2.5);
        this.progress.getStyleClass().add("llm-context-usage-progress");

        this.getChildren().addAll(this.track, this.progress);
        Tooltip.install(this, this.tooltip);
        this.update(LLMTokenUsageStats.EMPTY,
                LLMModelContextWindowUtil.ContextWindow.UNKNOWN);
    }

    /**
     * Updates the displayed statistics and model context capacity.
     *
     * @param stats         token statistics
     * @param contextWindow context capacity
     */
    public void update(LLMTokenUsageStats stats,
                       LLMModelContextWindowUtil.ContextWindow contextWindow) {
        this.stats = stats == null ? LLMTokenUsageStats.EMPTY : stats;
        this.contextWindow = contextWindow == null
                ? LLMModelContextWindowUtil.ContextWindow.UNKNOWN
                : contextWindow;
        double ratio = this.ratio();
        boolean known = ratio >= 0;
        this.progress.setVisible(known && ratio > 0);
        this.progress.setLength(known ? -360 * ratio : 0);
        this.getStyleClass().removeAll(
                "llm-context-usage-unknown",
                "llm-context-usage-warning",
                "llm-context-usage-danger");
        if (!known) {
            this.getStyleClass().add("llm-context-usage-unknown");
        } else if (ratio >= 0.9) {
            this.getStyleClass().add("llm-context-usage-danger");
        } else if (ratio >= 0.7) {
            this.getStyleClass().add("llm-context-usage-warning");
        }
        this.tooltip.setText(this.buildTooltip());
        this.setAccessibleText(this.tooltip.getText());
    }

    /**
     * Returns the visual ratio, or {@code -1} when usage or capacity is unknown.
     *
     * @return ratio in the range 0..1 or -1
     */
    public double ratio() {
        long used = this.stats.contextTokens();
        if (used < 0 || !this.contextWindow.known()) {
            return -1;
        }
        return Math.min(1, used / (double) this.contextWindow.tokens());
    }

    public String tooltipText() {
        return this.tooltip.getText();
    }

    boolean isProgressVisible() {
        return this.progress.isVisible();
    }

    private String buildTooltip() {
        long used = this.stats.contextTokens();
        StringBuilder text = new StringBuilder();
        if (used >= 0 && this.contextWindow.known()) {
            text.append(String.format(i18n("shell.llm.contextUsage.usage"),
                    Math.round(this.ratio() * 100),
                    LLMModelContextWindowUtil.formatTokens(used),
                    LLMModelContextWindowUtil.formatTokens(this.contextWindow.tokens())));
            if (this.contextWindow.estimated()) {
                text.append(" · ").append(i18n("shell.llm.contextUsage.estimated"));
            }
        } else if (used >= 0) {
            text.append(String.format(i18n("shell.llm.contextUsage.used"),
                    LLMModelContextWindowUtil.formatTokens(used)));
            text.append('\n').append(i18n("shell.llm.contextUsage.capacityUnknown"));
        } else {
            text.append(i18n("shell.llm.contextUsage.unknown"));
        }
        if (this.stats.hasLastUsage()) {
            text.append('\n').append(String.format(
                    i18n("shell.llm.contextUsage.lastRequest"),
                    LLMModelContextWindowUtil.formatTokens(this.stats.lastInputTokens()),
                    LLMModelContextWindowUtil.formatTokens(this.stats.lastOutputTokens()),
                    LLMModelContextWindowUtil.formatTokens(this.stats.lastTotalTokens())));
        }
        if (this.stats.hasSessionUsage()) {
            String key = this.stats.cumulativeUsageComplete()
                    ? "shell.llm.contextUsage.sessionTotal"
                    : "shell.llm.contextUsage.sessionTotalPartial";
            text.append('\n').append(String.format(i18n(key),
                    LLMModelContextWindowUtil.formatTokens(
                            this.stats.sessionTotalTokens())));
        }
        if (this.stats.lastDurationMillis() >= 0) {
            text.append('\n').append(String.format(
                    i18n("shell.llm.contextUsage.duration"),
                    LLMDurationUtil.format(this.stats.lastDurationMillis())));
        }
        return text.toString();
    }

    private static String i18n(String key) {
        return I18nResourceBundle.i18nString(key);
    }
}
