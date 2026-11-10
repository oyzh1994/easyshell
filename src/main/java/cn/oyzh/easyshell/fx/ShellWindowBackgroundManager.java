package cn.oyzh.easyshell.fx;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.tty.TtyTerminalCanvas;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 主窗口背景图片管理器，负责铺设全窗口背景并清理会遮挡背景的容器底色。
 * <p>
 * 背景由三层组成：底层主题色、图片层、雾化层，最上层是业务内容。
 * 图片只负责提供氛围，雾化层负责保证正文可读性；透明度由
 * {@link ShellSetting#getWindowBackgroundOpacity()} 控制（值越大，图片越清晰）。
 *
 * @author oyzh
 * @since 2026-11-10
 */
public final class ShellWindowBackgroundManager {

    /**
     * 背景启用样式类
     */
    private static final String STYLE_CLASS = "window-background-enabled";

    /**
     * 背景样式表
     */
    private static final String STYLE_SHEET = "/css/window-background.css";

    /**
     * 需要清理底色的节点样式类
     */
    private static final Set<String> TRANSPARENT_STYLE_CLASSES = Set.of(
            "window-connection-toolbar",
            "tab-content-area",
            "headers-region",
            "table-view",
            "tree-table-view",
            "list-view",
            "virtual-flow",
            "clipped-container",
            "sheet",
            "viewport",
            "scroll-pane",
            "virtualized-scroll-pane",
            "styled-text-area",
            "text-area",
            "rich-text-area",
            "chart",
            "chart-plot-background"
    );

    /**
     * 终端默认背景的不透明度，文本保持不透明
     */
    private static final double TERMINAL_BACKGROUND_OPACITY = 0;

    private final FXPane root;

    /**
     * 节点原始内联样式
     */
    private final Map<Node, String> originalStyles = new WeakHashMap<>();

    /**
     * 已处理的终端面板
     */
    private final Set<FXTerminalPanel> terminalPanels = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * 已监听子节点变化的父节点
     */
    private final Set<Parent> observedParents = Collections.newSetFromMap(new WeakHashMap<>());

    private final ListChangeListener<Node> childrenListener;

    private final ChangeListener<ThemeStyle> themeListener;

    private Rectangle baseLayer;

    private ImageView imageLayer;

    private Rectangle washLayer;

    private ChangeListener<Number> resizeListener;

    private ShellSetting setting;

    /**
     * 当前背景图片地址，用于避免重复加载
     */
    private String appliedUrl;

    private boolean active;

    private boolean refreshScheduled;

    private boolean disposed;

    public ShellWindowBackgroundManager(FXPane root) {
        this.root = root;
        if (!this.root.getStylesheets().contains(STYLE_SHEET)) {
            this.root.getStylesheets().add(STYLE_SHEET);
        }
        this.childrenListener = change -> this.scheduleSurfaceRefresh();
        this.themeListener = (observable, oldValue, newValue) -> this.updateThemeLayers();
        ThemeManager.currentThemeProperty().addListener(this.themeListener);
    }

    /**
     * 应用当前背景图片设置
     *
     * @param setting shell设置
     */
    public void apply(ShellSetting setting) {
        if (this.disposed) {
            return;
        }
        if (setting == null || !setting.isEnableBackgroundImage()) {
            this.clear();
            return;
        }
        String url = setting.getWindowBackgroundImageUrl();
        if (StringUtil.isBlank(url) || setting.isWindowBackgroundImageInvalid()
                || setting.getWindowBackgroundOpacity() <= 0) {
            this.clear();
            return;
        }
        // 相同图片只需刷新雾化层和透明规则，避免重复解码
        if (this.active && url.equals(this.appliedUrl)) {
            this.setting = setting;
            if (this.imageLayer != null) {
                this.imageLayer.setOpacity(Math.max(0, Math.min(100, setting.getWindowBackgroundOpacity())) / 100D);
            }
            this.updateThemeLayers();
            this.scheduleSurfaceRefresh();
            return;
        }
        Image image = this.loadImage(url);
        if (image == null) {
            this.clear();
            return;
        }
        this.setting = setting;
        this.appliedUrl = url;
        this.active = true;
        this.ensureBaseLayer();
        this.ensureImageLayer();
        this.imageLayer.setImage(image);
        this.imageLayer.setOpacity(Math.max(0, Math.min(100, setting.getWindowBackgroundOpacity())) / 100D);
        this.imageLayer.setVisible(true);
        this.ensureWashLayer();
        this.updateThemeLayers();
        this.resizeLayers();
        if (!this.root.getStyleClass().contains(STYLE_CLASS)) {
            this.root.getStyleClass().add(STYLE_CLASS);
        }
        this.scheduleSurfaceRefresh();
    }

    /**
     * 加载背景图片
     *
     * @param url 图片地址
     * @return 图片对象，加载失败返回null
     */
    private Image loadImage(String url) {
        try {
            boolean remote = StringUtil.startWithAnyIgnoreCase(url, "http", "https");
            Image image = new Image(url, remote);
            if (image.isError()) {
                return null;
            }
            image.errorProperty().addListener((observable, oldValue, newValue) -> {
                if (Boolean.TRUE.equals(newValue) && this.imageLayer != null && this.imageLayer.getImage() == image) {
                    Platform.runLater(this::clear);
                }
            });
            image.progressProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.doubleValue() >= 1) {
                    Platform.runLater(this::resizeLayers);
                }
            });
            return image;
        } catch (Exception ex) {
            return null;
        }
    }

    private void ensureBaseLayer() {
        if (this.baseLayer != null) {
            return;
        }
        this.baseLayer = new Rectangle();
        this.baseLayer.setManaged(false);
        this.baseLayer.setMouseTransparent(true);
        this.root.getChildren().add(0, this.baseLayer);
        this.resizeLayers();
    }

    private void ensureImageLayer() {
        if (this.imageLayer != null) {
            return;
        }
        this.imageLayer = new ImageView();
        this.imageLayer.setManaged(false);
        this.imageLayer.setMouseTransparent(true);
        this.imageLayer.setPickOnBounds(false);
        this.imageLayer.setPreserveRatio(false);
        this.imageLayer.setSmooth(true);
        this.root.getChildren().add(Math.min(1, this.root.getChildren().size()), this.imageLayer);

        // 轻微模糊+降饱和，让照片类背景更接近环境色而不是抢内容
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setSaturation(-0.12);
        GaussianBlur blur = new GaussianBlur(24);
        blur.setInput(colorAdjust);
        this.imageLayer.setEffect(blur);

        this.resizeListener = (observable, oldValue, newValue) -> this.resizeLayers();
        this.root.widthProperty().addListener(this.resizeListener);
        this.root.heightProperty().addListener(this.resizeListener);
        this.resizeLayers();
    }

    private void ensureWashLayer() {
        if (this.washLayer != null) {
            return;
        }
        this.washLayer = new Rectangle();
        this.washLayer.setManaged(false);
        this.washLayer.setMouseTransparent(true);
        this.root.getChildren().add(Math.min(2, this.root.getChildren().size()), this.washLayer);
        this.resizeLayers();
    }

    /**
     * 刷新主题底色层和雾化层。
     * <p>
     * 底色层保证图片透明时窗口仍然有稳定的主题背景；雾化层把图片压到较低的
     * 视觉权重，避免照片类背景影响正文对比度。
     */
    private void updateThemeLayers() {
        Color background = ThemeManager.currentBackgroundColor();
        if (this.baseLayer != null) {
            this.baseLayer.setFill(background);
        }
        if (this.washLayer != null) {
            double opacity = ThemeManager.isDarkMode() ? 0.62 : 0.55;
            this.washLayer.setFill(new Color(background.getRed(), background.getGreen(), background.getBlue(), opacity));
        }
    }

    private void resizeLayers() {
        double width = this.root.getWidth();
        double height = this.root.getHeight();
        if (this.imageLayer != null) {
            this.imageLayer.setFitWidth(width);
            this.imageLayer.setFitHeight(height);
            this.resizeImageLayer(width, height);
        }
        if (this.baseLayer != null) {
            this.baseLayer.setWidth(width);
            this.baseLayer.setHeight(height);
        }
        if (this.washLayer != null) {
            this.washLayer.setWidth(width);
            this.washLayer.setHeight(height);
        }
    }

    /**
     * 按窗口比例裁剪图片，保证铺满窗口且不变形
     */
    private void resizeImageLayer(double width, double height) {
        Image image = this.imageLayer.getImage();
        if (image == null || width <= 0 || height <= 0 || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return;
        }
        double targetRatio = width / height;
        double imageRatio = image.getWidth() / image.getHeight();
        if (imageRatio > targetRatio) {
            double viewportWidth = image.getHeight() * targetRatio;
            this.imageLayer.setViewport(new Rectangle2D((image.getWidth() - viewportWidth) / 2, 0, viewportWidth, image.getHeight()));
        } else {
            double viewportHeight = image.getWidth() / targetRatio;
            this.imageLayer.setViewport(new Rectangle2D(0, (image.getHeight() - viewportHeight) / 2, image.getWidth(), viewportHeight));
        }
        this.imageLayer.relocate(0, 0);
    }

    /**
     * 刷新透明规则并监听新加入的节点
     */
    private void applyTransparentSurfaces() {
        if (!this.active || this.disposed) {
            return;
        }
        this.refreshNode(this.root);
    }

    /**
     * 递归处理节点，同时监听动态创建的父节点
     */
    private void refreshNode(Node node) {
        if (this.isTransparentSurface(node)) {
            this.makeTransparent(node);
        }
        if (node instanceof TtyTerminalCanvas terminalCanvas) {
            this.applyTerminalBackground(terminalCanvas);
        }
        if (node instanceof Parent parent) {
            if (this.observedParents.add(parent)) {
                parent.getChildrenUnmodifiable().addListener(this.childrenListener);
            }
            for (Node child : List.copyOf(parent.getChildrenUnmodifiable())) {
                this.refreshNode(child);
            }
        }
    }

    private boolean isTransparentSurface(Node node) {
        for (String styleClass : node.getStyleClass()) {
            if (TRANSPARENT_STYLE_CLASSES.contains(styleClass)) {
                return true;
            }
        }
        return false;
    }

    private void makeTransparent(Node node) {
        String originalStyle = this.originalStyles.get(node);
        if (originalStyle == null) {
            originalStyle = node.getStyle() == null ? "" : node.getStyle();
            this.originalStyles.put(node, originalStyle);
        }
        String transparentStyle = "-fx-background-color: transparent;";
        String currentStyle = node.getStyle() == null ? "" : node.getStyle();
        if (!currentStyle.contains(transparentStyle)) {
            String separator = currentStyle.isBlank() || currentStyle.stripTrailing().endsWith(";") ? "" : ";";
            node.setStyle(currentStyle + separator + transparentStyle);
        }
    }

    private void applyTerminalBackground(TtyTerminalCanvas terminalCanvas) {
        if (this.setting == null || !(terminalCanvas.getParent() instanceof FXTerminalPanel terminalPanel)) {
            return;
        }
        this.terminalPanels.add(terminalPanel);
        // 旧版终端背景图功能已废弃并注释，统一使用主窗口背景图
        terminalPanel.setBackground(null);
        // 背景透明度由终端面板的默认底色控制，画布本身保持不透明，避免文字发灰
        terminalCanvas.setOpacity(1);
        terminalPanel.setBackgroundOpacity(TERMINAL_BACKGROUND_OPACITY);
    }

    private void scheduleSurfaceRefresh() {
        if (!this.active || this.refreshScheduled || this.disposed) {
            return;
        }
        this.refreshScheduled = true;
        Platform.runLater(() -> {
            this.refreshScheduled = false;
            this.applyTransparentSurfaces();
        });
    }

    /**
     * 移除背景图片并恢复容器原始样式
     */
    public void clear() {
        this.active = false;
        this.appliedUrl = null;
        this.root.getStyleClass().remove(STYLE_CLASS);
        if (this.baseLayer != null) {
            this.root.getChildren().remove(this.baseLayer);
            this.baseLayer = null;
        }
        if (this.imageLayer != null) {
            this.root.getChildren().remove(this.imageLayer);
            this.imageLayer = null;
        }
        if (this.washLayer != null) {
            this.root.getChildren().remove(this.washLayer);
            this.washLayer = null;
        }
        if (this.resizeListener != null) {
            this.root.widthProperty().removeListener(this.resizeListener);
            this.root.heightProperty().removeListener(this.resizeListener);
            this.resizeListener = null;
        }
        for (Map.Entry<Node, String> entry : this.originalStyles.entrySet()) {
            entry.getKey().setStyle(entry.getValue());
        }
        this.originalStyles.clear();
        for (FXTerminalPanel terminalPanel : this.terminalPanels) {
            terminalPanel.setBackgroundOpacity(1);
        }
        this.terminalPanels.clear();
        for (Parent parent : this.observedParents) {
            parent.getChildrenUnmodifiable().removeListener(this.childrenListener);
        }
        this.observedParents.clear();
        this.setting = null;
    }

    /**
     * 释放资源
     */
    public void dispose() {
        if (this.disposed) {
            return;
        }
        this.disposed = true;
        this.clear();
        ThemeManager.currentThemeProperty().removeListener(this.themeListener);
    }
}
