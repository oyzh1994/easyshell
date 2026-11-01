package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Objects;

/**
 * 可选择、可复制的只读消息文本控件
 *
 * @author oyzh
 * @since 2026-11-01
 */
public final class LLMMessageTextArea extends FXTextArea {

    private Text measurement = new Text();

    private final DoubleProperty measurementWidth = new SimpleDoubleProperty(-1);

    private String measuredText;

    private Font measuredFont;

    private double measuredWrappingWidth = Double.NaN;

    private double measuredWidth;

    private double measuredHeight;

    private double lastLayoutHeight = -1;

    private ScrollPane internalScrollPane;

    private boolean measurementResetScheduled;

    public LLMMessageTextArea() {
        this.getStyleClass().add("llm-message-text");
        this.setEditable(false);
        this.setWrapText(true);
        this.setPrefColumnCount(1);
        this.setPrefRowCount(1);
        this.setMinWidth(0);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setFocusTraversable(true);
        this.setCursor(Cursor.TEXT);
        this.setContextMenu(this.createContextMenu());
        this.textProperty().addListener((observable, oldValue, newValue) -> this.requestLayoutIfHeightChanged());
        this.measurementWidth.addListener((observable, oldValue, newValue) -> {
            this.measuredWrappingWidth = Double.NaN;
            this.lastLayoutHeight = -1;
            this.requestLayout();
        });
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                this.scheduleMeasurementReset();
            }
        });
        this.fontProperty().addListener((observable, oldValue, newValue) -> {
            this.lastLayoutHeight = -1;
            this.requestLayout();
        });
        this.skinProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.internalScrollPane = null;
                this.disableInternalScrollBars();
                this.scheduleMeasurementReset();
            }
        });
        this.addEventFilter(ScrollEvent.SCROLL, this::forwardScroll);
    }

    @Override
    protected void layoutChildren() {
        this.disableInternalScrollBars();
        super.layoutChildren();
    }

    @Override
    protected double computePrefWidth(double height) {
        String text = this.getText();
        if (text == null || text.isEmpty()) {
            return 0;
        }
        this.prepareMeasurement(text, 0);
        return Math.ceil(this.measuredWidth + this.getInsets().getLeft() + this.getInsets().getRight());
    }

    @Override
    protected double computePrefHeight(double width) {
        String text = this.getText();
        double insetsHeight = this.getInsets().getTop() + this.getInsets().getBottom();
        if (text == null || text.isEmpty()) {
            return insetsHeight;
        }
        double measurementWidth = this.getMeasurementWidth();
        if (measurementWidth > 0) {
            width = measurementWidth;
        } else if (width <= 0) {
            this.prepareMeasurement(text, 0);
            width = this.measurement.getLayoutBounds().getWidth()
                    + this.getInsets().getLeft() + this.getInsets().getRight();
        }
        double contentWidth = Math.max(1,
                width - this.getInsets().getLeft() - this.getInsets().getRight());
        this.prepareMeasurement(text, contentWidth);
        return Math.ceil(this.measuredHeight + insetsHeight);
    }

    public double getMeasurementWidth() {
        return this.measurementWidth.get();
    }

    public DoubleProperty measurementWidthProperty() {
        return this.measurementWidth;
    }

    private void prepareMeasurement(String text, double wrappingWidth) {
        Font font = this.getFont();
        if (Objects.equals(text, this.measuredText)
                && Objects.equals(font, this.measuredFont)
                && Double.compare(wrappingWidth, this.measuredWrappingWidth) == 0) {
            return;
        }
        this.measurement.setFont(font);
        this.measurement.setText(text);
        this.measurement.setWrappingWidth(wrappingWidth);
        this.measuredText = text;
        this.measuredFont = font;
        this.measuredWrappingWidth = wrappingWidth;
        this.measuredWidth = this.measurement.getLayoutBounds().getWidth();
        this.measuredHeight = this.measurement.getLayoutBounds().getHeight();
    }

    private void requestLayoutIfHeightChanged() {
        double width = this.getMeasurementWidth();
        if (width <= 0) {
            width = this.getWidth();
        }
        if (width <= 0) {
            this.lastLayoutHeight = -1;
            this.requestLayout();
            return;
        }
        double height = this.computePrefHeight(width);
        if (Math.abs(height - this.lastLayoutHeight) > 0.5) {
            this.lastLayoutHeight = height;
            this.requestLayout();
        }
    }

    private void resetMeasurement() {
        this.measurement = new Text();
        this.measuredText = null;
        this.measuredFont = null;
        this.measuredWrappingWidth = Double.NaN;
        this.measuredWidth = 0;
        this.measuredHeight = 0;
        this.lastLayoutHeight = -1;
        this.requestLayout();
    }

    private void scheduleMeasurementReset() {
        if (this.measurementResetScheduled) {
            return;
        }
        this.measurementResetScheduled = true;
        Platform.runLater(() -> {
            this.measurementResetScheduled = false;
            this.disableInternalScrollBars();
            this.resetMeasurement();
        });
    }

    private ContextMenu createContextMenu() {
        MenuItem copy = new MenuItem(I18nHelper.copy());
        copy.setOnAction(event -> this.copyMessage());
        MenuItem selectAll = new MenuItem(I18nHelper.selectAll());
        selectAll.setOnAction(event -> this.selectAll());
        return new ContextMenu(copy, selectAll);
    }

    private void copyMessage() {
        String text = this.getSelectedText();
        if (text == null || text.isEmpty()) {
            text = this.getText();
        }
        if (StringUtil.isNotBlank(text)) {
            ClipboardUtil.setString(text);
        }
    }

    private void forwardScroll(ScrollEvent event) {
        Parent parent = this.getParent();
        if (parent != null) {
            parent.fireEvent(event.copyFor(parent, parent));
            event.consume();
        }
    }

    private void disableInternalScrollBars() {
        if (this.internalScrollPane == null && this.lookup(".scroll-pane") instanceof ScrollPane scrollPane) {
            this.internalScrollPane = scrollPane;
        }
        if (this.internalScrollPane != null) {
            this.internalScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
            this.internalScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
            this.internalScrollPane.setFitToWidth(true);
        }
    }
}
