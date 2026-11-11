package cn.oyzh.easyshell.fx;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.tty.TtyTermWidget;
import cn.oyzh.fx.tty.TtyTerminalCanvas;
import cn.oyzh.fx.tty.ghostty.TerminalTheme;
import cn.oyzh.fx.tty.ghostty.TerminalView;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

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

    /**
     * 背景图预处理的最大边长
     */
    private static final int IMAGE_RENDER_LIMIT = 768;

    /**
     * 背景图模糊半径
     */
    private static final double IMAGE_BLUR_RADIUS = 24;

    /**
     * 图片比例比较误差
     */
    private static final double IMAGE_RATIO_EPSILON = 0.001;

    /**
     * 图片重绘延迟
     */
    private static final Duration IMAGE_RENDER_DELAY = Duration.millis(120);

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
     * Ghostty 终端原始主题，用于恢复背景
     */
    private final Map<TerminalView, TerminalTheme> ghosttyThemes = new WeakHashMap<>();

    /**
     * 已监听子节点变化的父节点
     */
    private final Set<Parent> observedParents = Collections.newSetFromMap(new WeakHashMap<>());

    private final ListChangeListener<Node> childrenListener;

    private final ChangeListener<ThemeStyle> themeListener;

    private Rectangle baseLayer;

    private ImageView imageLayer;

    private Rectangle washLayer;

    /**
     * 原始背景图片
     */
    private Image sourceImage;

    /**
     * 已经完成效果处理的背景图片
     */
    private Image renderedImage;

    /**
     * 处理图片对应的目标比例
     */
    private double renderedImageRatio;

    /**
     * 处理图片对应的目标尺寸
     */
    private double renderedImageSize;

    private ChangeListener<Number> resizeListener;

    private PauseTransition imageRenderDelay;

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
        this.sourceImage = image;
        this.renderedImage = null;
        this.renderedImageRatio = 0;
        this.renderedImageSize = 0;
        this.ensureBaseLayer();
        this.ensureImageLayer();
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
                if (Boolean.TRUE.equals(newValue)) {
                    FXUtil.runLater(() -> {
                        if (this.sourceImage == image) {
                            this.clear();
                        }
                    });
                }
            });
            image.progressProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.doubleValue() >= 1) {
                    FXUtil.runLater(() -> {
                        if (this.sourceImage == image) {
                            this.resizeLayers();
                        }
                    });
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
        this.imageLayer.setCache(true);
        this.imageLayer.setCacheHint(CacheHint.QUALITY);
        this.root.getChildren().add(Math.min(1, this.root.getChildren().size()), this.imageLayer);

        this.resizeListener = (observable, oldValue, newValue) -> {
            this.resizeLayers();
            this.scheduleImageRender();
        };
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
            if (this.renderedImage == null) {
                this.renderBackgroundImage(width, height);
            } else {
                this.imageLayer.setImage(this.renderedImage);
                this.updateImageViewport(this.renderedImage, width, height);
            }
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
     * 在离线图片上执行一次降饱和和模糊，避免运行时对全窗口持续执行 GaussianBlur。
     */
    private void renderBackgroundImage(double width, double height) {
        if (this.imageLayer == null || this.sourceImage == null || width <= 0 || height <= 0
                || this.sourceImage.getWidth() <= 0 || this.sourceImage.getHeight() <= 0) {
            return;
        }
        double targetRatio = width / height;
        if (this.isRenderedImageCurrent(width, height)) {
            return;
        }
        double renderScale = Math.min(1, IMAGE_RENDER_LIMIT / Math.max(width, height));
        int renderWidth = (int) Math.max(1, Math.round(width * renderScale));
        int renderHeight = (int) Math.max(1, Math.round(height * renderScale));

        ImageView preview = new ImageView(this.sourceImage);
        preview.setViewport(this.coverViewport(this.sourceImage, targetRatio));
        preview.setFitWidth(renderWidth);
        preview.setFitHeight(renderHeight);
        preview.setPreserveRatio(false);
        preview.setSmooth(true);

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setSaturation(-0.12);
        GaussianBlur blur = new GaussianBlur(Math.max(0.1, IMAGE_BLUR_RADIUS * renderScale));
        blur.setInput(colorAdjust);
        preview.setEffect(blur);

        WritableImage rendered = new WritableImage(renderWidth, renderHeight);
        try {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            preview.snapshot(parameters, rendered);
        } catch (RuntimeException ex) {
            this.imageLayer.setImage(this.sourceImage);
            this.updateImageViewport(this.sourceImage, width, height);
            return;
        }
        this.renderedImage = rendered;
        this.renderedImageRatio = targetRatio;
        this.renderedImageSize = Math.max(width, height);
        this.imageLayer.setImage(rendered);
        this.imageLayer.setViewport(null);
        this.imageLayer.relocate(0, 0);
    }

    /**
     * 按窗口比例裁剪图片，保证铺满窗口且不变形。
     */
    private void updateImageViewport(Image image, double width, double height) {
        if (image == null || width <= 0 || height <= 0 || image.getWidth() <= 0 || image.getHeight() <= 0) {
            this.imageLayer.setViewport(null);
            return;
        }
        this.imageLayer.setViewport(this.coverViewport(image, width / height));
        this.imageLayer.relocate(0, 0);
    }

    private Rectangle2D coverViewport(Image image, double targetRatio) {
        double imageRatio = image.getWidth() / image.getHeight();
        if (imageRatio > targetRatio) {
            double viewportWidth = image.getHeight() * targetRatio;
            return new Rectangle2D((image.getWidth() - viewportWidth) / 2, 0, viewportWidth, image.getHeight());
        }
        double viewportHeight = image.getWidth() / targetRatio;
        return new Rectangle2D(0, (image.getHeight() - viewportHeight) / 2, image.getWidth(), viewportHeight);
    }

    /**
     * 延迟重绘背景图，避免拖动窗口时连续执行离线效果。
     */
    private void scheduleImageRender() {
        if (!this.active || this.disposed || this.sourceImage == null) {
            return;
        }
        double width = this.root.getWidth();
        double height = this.root.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        if (this.isRenderedImageCurrent(width, height)) {
            return;
        }
        if (this.imageRenderDelay == null) {
            this.imageRenderDelay = new PauseTransition(IMAGE_RENDER_DELAY);
            this.imageRenderDelay.setOnFinished(event -> this.renderBackgroundImage(this.root.getWidth(), this.root.getHeight()));
        }
        this.imageRenderDelay.playFromStart();
    }

    private boolean isRenderedImageCurrent(double width, double height) {
        if (this.renderedImage == null) {
            return false;
        }
        double targetRatio = width / height;
        double targetSize = Math.max(width, height);
        return Math.abs(this.renderedImageRatio - targetRatio) <= IMAGE_RATIO_EPSILON
                && Math.abs(this.renderedImageSize - targetSize) <= Math.max(1, targetSize * 0.05);
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
        if (node instanceof TerminalView terminalView) {
            this.makeTransparent(terminalView);
            this.applyGhosttyBackground(terminalView);
        } else if (node instanceof TtyTermWidget terminalWidget) {
            // Ghostty 视图嵌在 TtyTermWidget 中；宿主本身也保持透明，避免遮挡背景图
            this.makeTransparent(terminalWidget);
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

    /**
     * 让 Ghostty 终端默认背景透明，以透出主窗口背景图。
     * <p>
     * ANSI 颜色、选中背景和光标颜色保持不变；只把默认背景的 alpha 置 0。
     * 原始主题会被缓存，关闭背景图时恢复。
     */
    private void applyGhosttyBackground(TerminalView terminalView) {
        TerminalTheme current = terminalView.getTheme();
        TerminalTheme original = this.ghosttyThemes.get(terminalView);
        // 当前主题是不透明的，说明是新的基础主题（首次应用或外部切换主题），重新记录。
        if (original == null || current != null && current.background().getOpacity() > 0) {
            original = current;
            if (original != null) {
                this.ghosttyThemes.put(terminalView, original);
            }
        }
        if (original == null) {
            return;
        }
        Color background = original.background();
        if (background.getOpacity() <= 0) {
            return;
        }
        if (current != null && current.background().getOpacity() <= 0) {
            return;
        }
        Color transparent = new Color(
                background.getRed(),
                background.getGreen(),
                background.getBlue(),
                0);
        terminalView.setTheme(original.withBackground(transparent));
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
        if (this.imageRenderDelay != null) {
            this.imageRenderDelay.stop();
            this.imageRenderDelay = null;
        }
        this.root.removeClass(STYLE_CLASS);
        if (this.baseLayer != null) {
            this.root.removeChild(this.baseLayer);
            this.baseLayer = null;
        }
        if (this.imageLayer != null) {
            this.root.removeChild(this.imageLayer);
            this.imageLayer = null;
        }
        this.sourceImage = null;
        this.renderedImage = null;
        this.renderedImageRatio = 0;
        this.renderedImageSize = 0;
        if (this.washLayer != null) {
            this.root.removeChild(this.washLayer);
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
        for (Map.Entry<TerminalView, TerminalTheme> entry : this.ghosttyThemes.entrySet()) {
            entry.getKey().setTheme(entry.getValue());
        }
        this.ghosttyThemes.clear();
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
