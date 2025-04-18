package com.jediterm.terminal.ui;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.jeditermfx.terminal.ui.BlinkingTextTracker;
import cn.oyzh.jeditermfx.terminal.ui.FXFontMetrics;
import cn.oyzh.jeditermfx.terminal.ui.FXScrollBarUtils;
import cn.oyzh.jeditermfx.terminal.ui.FXTransformers;
import cn.oyzh.jeditermfx.terminal.ui.TerminalAction;
import cn.oyzh.jeditermfx.terminal.ui.TerminalActionProvider;
import cn.oyzh.jeditermfx.terminal.ui.hyperlinks.LinkInfoEx;
import cn.oyzh.jeditermfx.terminal.ui.input.FXMouseEvent;
import cn.oyzh.jeditermfx.terminal.ui.input.FXMouseWheelEvent;
import cn.oyzh.jeditermfx.terminal.ui.settings.SettingsProvider;
import com.jediterm.core.TerminalCoordinates;
import com.jediterm.core.typeahead.TerminalTypeAheadManager;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.CursorShape;
import com.jediterm.terminal.DefaultTerminalCopyPasteHandler;
import com.jediterm.terminal.HyperlinkStyle;
import com.jediterm.terminal.RequestOrigin;
import com.jediterm.terminal.StyledTextConsumer;
import com.jediterm.terminal.SubstringFinder;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TerminalCopyPasteHandler;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalOutputStream;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.emulator.charset.CharacterSets;
import com.jediterm.terminal.emulator.mouse.MouseFormat;
import com.jediterm.terminal.emulator.mouse.MouseMode;
import com.jediterm.terminal.emulator.mouse.TerminalMouseListener;
import com.jediterm.terminal.model.CharBuffer;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.LinesStorage;
import com.jediterm.terminal.model.SelectionUtil;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalLine;
import com.jediterm.terminal.model.TerminalLineIntervalHighlighting;
import com.jediterm.terminal.model.TerminalModelListener;
import com.jediterm.terminal.model.TerminalSelection;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.hyperlinks.LinkInfo;
import com.jediterm.terminal.model.hyperlinks.TextProcessing;
import com.jediterm.terminal.util.CharUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.text.AttributedCharacterIterator;
import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


public class FXTerminalPanel extends FXHBox implements TerminalDisplay, TerminalActionProvider {

    private static final long serialVersionUID = -1048763516632093014L;

    public static final double SCROLL_SPEED = 0.05;

//    private static class CanvasPane extends Pane {
//
//        private final Canvas canvas;
//
//        public CanvasPane(Canvas canvas) {
//            this.canvas = canvas;
//            getChildren().addAll(canvas);
//            canvas.widthProperty().bind(this.widthProperty());
//            canvas.heightProperty().bind(this.heightProperty());
//        }
//    }

    private final Canvas canvas = new Canvas();

//    private final CanvasPane canvasPane = new CanvasPane(canvas);

    private final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

    //we scroll a window [0, terminal_height] in the range [-history_lines_count, terminal_height]
    private ScrollBar scrollBar;

    private boolean scrollBarThumbVisible = true;

//    private final HBox pane = new HBox(canvasPane, scrollBar);

    private ContextMenu popup;

    private final ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper(null);

    /**
     * The value of the selection property has two types: 1)The user selects text with the mouse. 2) The value is set
     * programmatically by a client of this class. In the first case, selectedText is updated only when the user
     * has finished selecting text with the mouse. In the second case, selectedText changes immediately after the
     * selection property is modified. The value of this variable is used to control the timing of the selectedText
     * update.
     */
    private boolean updateSelectedText = true;

    /*font related*/
    private Font myNormalFont;

    private Font myItalicFont;

    private Font myBoldFont;

    private Font myBoldItalicFont;

    private double myDescent = 0;

    private int mySpaceBetweenLines = 0;

    protected Dimension2D myCharSize;

    private TermSize myTermSize;

    private boolean myInitialSizeSyncDone = false;

    private TerminalStarter myTerminalStarter = null;

    private MouseMode myMouseMode = MouseMode.MOUSE_REPORTING_NONE;

    private com.jediterm.core.compatibility.Point mySelectionStartPoint = null;

    private final ObjectProperty<TerminalSelection> mySelection = new SimpleObjectProperty<>(null);

    private final TerminalCopyPasteHandler myCopyPasteHandler;

    private final SettingsProvider mySettingsProvider;

    private final TerminalTextBuffer myTerminalTextBuffer;

    final private StyleState myStyleState;

    /*scroll and cursor*/
    final private TerminalCursor myCursor = new TerminalCursor();

    private final BlinkingTextTracker myTextBlinkingTracker = new BlinkingTextTracker();

    private boolean myScrollingEnabled = true;

    protected int myClientScrollOrigin;

    private final List<BiConsumer<EventType<KeyEvent>, KeyEvent>> myCustomKeyListeners = new CopyOnWriteArrayList<>();

    private String myWindowTitle = "Terminal";

    private TerminalActionProvider myNextActionProvider;

    private String myInputMethodUncommittedChars;

    private Timeline myRepaintTimer;

    private final AtomicInteger scrollDy = new AtomicInteger(0);

    private final AtomicBoolean myHistoryBufferLineCountChanged = new AtomicBoolean(false);

    private final AtomicBoolean needRepaint = new AtomicBoolean(true);

    private int myMaxFPS = 50;

    private int myBlinkingPeriod = 500;

    private TerminalCoordinates myCoordsAccessor;

    private SubstringFinder.FindResult myFindResult;

    private LinkInfo myHoveredHyperlink = null;

    private Cursor myCursorType = Cursor.DEFAULT;

    private final TerminalKeyHandler myTerminalKeyHandler = new TerminalKeyHandler();

    private LinkInfoEx.HoverConsumer myLinkHoverConsumer;

    private TerminalTypeAheadManager myTypeAheadManager;

    private volatile boolean myBracketedPasteMode;

    private boolean myUsingAlternateBuffer = false;

    private boolean myFillCharacterBackgroundIncludingLineSpacing;

    private @Nullable TextStyle myCachedSelectionColor;

    private @Nullable TextStyle myCachedFoundPatternColor;

    private boolean myIgnoreNextKeyTypedEvent;

    public FXTerminalPanel(@NotNull SettingsProvider settingsProvider, @NotNull TerminalTextBuffer terminalTextBuffer,
                           @NotNull StyleState styleState) {
        mySettingsProvider = settingsProvider;
        myTerminalTextBuffer = terminalTextBuffer;
        myStyleState = styleState;
        myTermSize = new TermSize(terminalTextBuffer.getWidth(), terminalTextBuffer.getHeight());
        myMaxFPS = mySettingsProvider.maxRefreshRate();
        myCopyPasteHandler = createCopyPasteHandler();

        var css = FXTerminalPanel.class.getResource("/css/terminal-panel.css").toExternalForm();
        this.getStylesheets().add(css);
        setScrollBarRangeProperties(0, 80, 0, 80);
        mySelection.addListener((ov, oldV, newV) -> updateSelectedText());

        updateScrolling(true);

        terminalTextBuffer.addModelListener(this::repaint);
        terminalTextBuffer.addHistoryBufferListener(() -> myHistoryBufferLineCountChanged.set(true));
        TextProcessing textProcessing = terminalTextBuffer.getTextProcessing$core();
        if (textProcessing != null) {
            textProcessing.addHyperlinkListener(this::repaint);
        }
    }

    void setTypeAheadManager(@NotNull TerminalTypeAheadManager typeAheadManager) {
        myTypeAheadManager = typeAheadManager;
    }

    @NotNull
    protected TerminalCopyPasteHandler createCopyPasteHandler() {
        return new DefaultTerminalCopyPasteHandler();
    }

    public void repaint() {
        needRepaint.set(true);
    }

    private void doRepaint() {
        this.paintComponent(this.graphicsContext);
    }

    protected void reinitFontAndResize() {
        initFont();

        sizeTerminalFromComponent();
    }

    protected void initFont() {
        myNormalFont = createFont();
        myBoldFont = Font.font(myNormalFont.getFamily(), FontWeight.BOLD, myNormalFont.getSize());
        myItalicFont = Font.font(myNormalFont.getFamily(), FontPosture.ITALIC, myNormalFont.getSize());
        myBoldItalicFont = Font.font(myNormalFont.getFamily(), FontWeight.BOLD, FontPosture.ITALIC, myNormalFont.getSize());

        establishFontMetrics();
    }

    public void init(@NotNull ScrollBar scrollBar) {
        initFont();

        this.scrollBar = scrollBar;
        this.canvas.heightProperty().bind(this.heightProperty().subtract(2));
        this.canvas.widthProperty().bind(this.widthProperty().subtract(this.scrollBar.widthProperty()));
        this.setChild(this.canvas, scrollBar);

        this.setFocusTraversable(true);

        HBox.setHgrow(canvas, Priority.ALWAYS);
//        this.canvas.setCache(true);
        scrollBar.setOrientation(Orientation.VERTICAL);
        if (mySettingsProvider.useAntialiasing()) {
            //Important! FontSmoothingType.LCD is very slow
            graphicsContext.setFontSmoothingType(FontSmoothingType.GRAY);
            graphicsContext.setImageSmoothing(true);
        } else {
            graphicsContext.setImageSmoothing(false);
        }
        this.canvas.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isConsumed()) {
                return;
            }
            myIgnoreNextKeyTypedEvent = false;
            if (TerminalAction.processEvent(FXTerminalPanel.this, e) || processTerminalKeyPressed(e)) {
                e.consume();
                myIgnoreNextKeyTypedEvent = true;
            }
        });
        this.canvas.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (e.isConsumed()) {
                return;
            }
            if (myIgnoreNextKeyTypedEvent || processTerminalKeyTyped(e)) {
                e.consume();
            }
        });
        this.canvas.addEventFilter(MouseEvent.MOUSE_MOVED, (e) -> {
            handleHyperlinks(createPoint(e));
        });

        this.canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!isLocalMouseAction(e)) {
                return;
            }

            final com.jediterm.core.compatibility.Point charCoords = panelToCharCoords(createPoint(e));

            if (mySelection.get() == null) {
                // prevent unlikely case where drag started outside terminal panel
                if (mySelectionStartPoint == null) {
                    mySelectionStartPoint = charCoords;
                }
                updateSelection(new TerminalSelection(new com.jediterm.core.compatibility.Point(mySelectionStartPoint)), false);
            }
            repaint();
            mySelection.get().updateEnd(charCoords);
            if (mySettingsProvider.copyOnSelect()) {
                handleCopyOnSelect();
            }

            if (e.getY() < 0) {
                moveScrollBar((int) ((e.getY()) * SCROLL_SPEED));
            }
            if (e.getY() > getPixelHeight()) {
                moveScrollBar((int) ((e.getY() - getPixelHeight()) * SCROLL_SPEED));
            }
        });

        this.canvas.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (isLocalMouseAction(e)) {
                handleMouseWheelEvent(e, scrollBar);
            }
        });

        this.canvas.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            if (myLinkHoverConsumer != null) {
                myLinkHoverConsumer.onMouseExited();
                myLinkHoverConsumer = null;
            }
            updateHoveredHyperlink(null);
        });

        this.canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 1) {
                    mySelectionStartPoint = panelToCharCoords(createPoint(e));
                    updateSelection(null, true);
                    repaint();
                }
            }
        });

        this.canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            this.requestFocus();
            if (mySelection.get() != null) {
                updateSelectedText();
            }
            repaint();
        });

        this.canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            this.canvas.requestFocus();
            if (this.popup != null && e.getButton() == MouseButton.PRIMARY) {
                this.popup.hide();
                return;
            }
            var point = createPoint(e);
            HyperlinkStyle hyperlink = isFollowLinkEvent(e) ? findHyperlink(point) : null;
            if (hyperlink != null) {
                hyperlink.getLinkInfo().navigate();
            } else if (e.getButton() == MouseButton.PRIMARY && isLocalMouseAction(e)) {
                int count = e.getClickCount();
                if (count == 1) {
                    // do nothing
                } else if (count == 2) {
                    // select word
                    final com.jediterm.core.compatibility.Point charCoords = panelToCharCoords(point);
                    com.jediterm.core.compatibility.Point start = SelectionUtil.getPreviousSeparator(charCoords, myTerminalTextBuffer);
                    com.jediterm.core.compatibility.Point stop = SelectionUtil.getNextSeparator(charCoords, myTerminalTextBuffer);
                    var sel = new TerminalSelection(start);
                    updateSelection(sel, true);
                    sel.updateEnd(stop);

                    if (mySettingsProvider.copyOnSelect()) {
                        handleCopyOnSelect();
                    }
                } else if (count == 3) {
                    // select line
                    final com.jediterm.core.compatibility.Point charCoords = panelToCharCoords(point);
                    int startLine = charCoords.y;
                    while (startLine > -getScrollLinesStorage().getSize()
                            && myTerminalTextBuffer.getLine(startLine - 1).isWrapped()) {
                        startLine--;
                    }
                    int endLine = charCoords.y;
                    while (endLine < myTerminalTextBuffer.getHeight()
                            && myTerminalTextBuffer.getLine(endLine).isWrapped()) {
                        endLine++;
                    }
                    var sel = new TerminalSelection(new com.jediterm.core.compatibility.Point(0, startLine));
                    updateSelection(sel, true);
                    sel.updateEnd(new com.jediterm.core.compatibility.Point(myTermSize.getColumns(), endLine));

                    if (mySettingsProvider.copyOnSelect()) {
                        handleCopyOnSelect();
                    }
                }
            } else if (e.getButton() == MouseButton.MIDDLE && mySettingsProvider.pasteOnMiddleMouseClick() && isLocalMouseAction(e)) {
                handlePasteSelection();
            } else if (e.getButton() == MouseButton.SECONDARY) {
                HyperlinkStyle contextHyperlink = findHyperlink(point);
                TerminalActionProvider provider = getTerminalActionProvider(contextHyperlink != null ? contextHyperlink.getLinkInfo() : null, e);
                popup = createPopupMenu(provider);
                popup.setOnHidden(popupEvent -> {
                    popup = null;
                });
                popup.show(this.canvas, e.getScreenX(), e.getScreenY());
            }
            repaint();
        });

        this.canvas.widthProperty().addListener((ov, oldV, newV) -> {
            sizeTerminalFromComponent();
        });
        this.canvas.heightProperty().addListener((ov, oldV, newV) -> {
            sizeTerminalFromComponent();
        });

        this.canvas.inputMethodRequestsProperty().set(new MyInputMethodRequests());
        this.canvas.setOnInputMethodTextChanged(this::processInputMethodEvent);

        myFillCharacterBackgroundIncludingLineSpacing = mySettingsProvider.shouldFillCharacterBackgroundIncludingLineSpacing();
        this.canvas.focusedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                myFillCharacterBackgroundIncludingLineSpacing = mySettingsProvider.shouldFillCharacterBackgroundIncludingLineSpacing();
                myCursor.cursorChanged();
            } else {
                myCursor.cursorChanged();
                handleHyperlinks(this.canvas);
            }
        });

        this.scrollBar.valueProperty().addListener((ov, oldV, newV) -> {
            this.myClientScrollOrigin = resolveSwingScrollBarValue();
            repaint();
        });

        createRepaintTimer();

        // 事件处理
        this.addEventFilter(KeyEvent.ANY, this::handleKeyEvent);
    }

    /**
     * There is a difference between JavaFX ScrollBar.value Swing JScrollBar.value. In JavaFX value is calculated
     * in range [min, max], but in Swing in range [min, max - extent]
     */
    private int resolveSwingScrollBarValue() {
        var normalizedValue = (scrollBar.getValue() - scrollBar.getMin())
                / (scrollBar.getMax() - scrollBar.getMin());
        var swingValue = scrollBar.getMin()
                + normalizedValue * (scrollBar.getMax() - scrollBar.getVisibleAmount() - scrollBar.getMin());
        return (int) Math.round(swingValue);
    }

    private double resolveJavaFxScrollBarValue(int swingValue) {
        var normalizedValue = (swingValue - scrollBar.getMin())
                / ((scrollBar.getMax() - scrollBar.getVisibleAmount()) - scrollBar.getMin());
        var fxValue = scrollBar.getMin() + normalizedValue * (scrollBar.getMax() - scrollBar.getMin());
        return fxValue;
    }

    private boolean isFollowLinkEvent(@NotNull MouseEvent e) {
        return myCursorType == Cursor.HAND && e.getButton() == MouseButton.PRIMARY;
    }

    protected void handleMouseWheelEvent(@NotNull ScrollEvent e, @NotNull ScrollBar scrollBar) {
        var unitsToScroll = getUnitsToScroll(e);
        if (e.isShiftDown() || unitsToScroll == 0 || Math.abs(e.getDeltaY()) < 0.01) {
            return;
        }
        moveScrollBar(unitsToScroll);
        e.consume();
    }

    private void handleHyperlinks(@NotNull Point2D panelPoint) {
        Cell cell = panelPointToCell(panelPoint);
        HyperlinkStyle linkStyle = findHyperlink(cell);
        LinkInfo linkInfo = linkStyle != null ? linkStyle.getLinkInfo() : null;
        LinkInfoEx.HoverConsumer linkHoverConsumer = LinkInfoEx.getHoverConsumer(linkInfo);
        if (linkHoverConsumer != myLinkHoverConsumer) {
            if (myLinkHoverConsumer != null) {
                myLinkHoverConsumer.onMouseExited();
            }
            if (linkHoverConsumer != null) {
                LineCellInterval lineCellInterval = findIntervalWithStyle(cell, linkStyle);
                linkHoverConsumer.onMouseEntered(this.canvas, getBounds(lineCellInterval));
            }
        }
        myLinkHoverConsumer = linkHoverConsumer;
        if (linkStyle != null
                && linkStyle.getHighlightMode() != HyperlinkStyle.HighlightMode.NEVER) {
//                && linkStyle.getHighlightMode() != HyperlinkStyle.HighlightMode.NEVER_WITH_ORIGINAL_COLOR
//                && linkStyle.getHighlightMode() != HyperlinkStyle.HighlightMode.NEVER_WITH_CUSTOM_COLOR) {
            updateHoveredHyperlink(linkStyle.getLinkInfo());
        } else {
            updateHoveredHyperlink(null);
        }
    }

    private void updateHoveredHyperlink(@Nullable LinkInfo hoveredHyperlink) {
        if (myHoveredHyperlink != hoveredHyperlink) {
            updateCursor(hoveredHyperlink != null ? Cursor.HAND : Cursor.DEFAULT);
            myHoveredHyperlink = hoveredHyperlink;
            repaint();
        }
    }

    private @NotNull LineCellInterval findIntervalWithStyle(@NotNull Cell initialCell, @NotNull HyperlinkStyle style) {
        int startColumn = initialCell.getColumn();
        while (startColumn > 0 && style == myTerminalTextBuffer.getStyleAt(startColumn - 1, initialCell.getLine())) {
            startColumn--;
        }
        int endColumn = initialCell.getColumn();
        while (endColumn < myTerminalTextBuffer.getWidth() - 1 && style == myTerminalTextBuffer.getStyleAt(endColumn + 1, initialCell.getLine())) {
            endColumn++;
        }
        return new LineCellInterval(initialCell.getLine(), startColumn, endColumn);
    }

    private void handleHyperlinks(Node component) {
        handleHyperlinks(component.localToScreen(0, 0));
    }

    private @Nullable HyperlinkStyle findHyperlink(@NotNull Point2D p) {
        return findHyperlink(panelPointToCell(p));
    }

    private @Nullable HyperlinkStyle findHyperlink(@Nullable Cell cell) {
        if (cell != null && cell.getColumn() >= 0 && cell.getColumn() < myTerminalTextBuffer.getWidth() && cell.getLine() >= -myTerminalTextBuffer.getHistoryLinesCount() &&
                cell.getLine() <= myTerminalTextBuffer.getHeight()) {
            TextStyle style = myTerminalTextBuffer.getStyleAt(cell.getColumn(), cell.getLine());
            if (style instanceof HyperlinkStyle) {
                return (HyperlinkStyle) style;
            }
        }
        return null;
    }

    private void updateCursor(Cursor cursorType) {
        if (cursorType != myCursorType) {
            myCursorType = cursorType;
            this.canvas.setCursor(myCursorType);
        }
    }

    private void createRepaintTimer() {
        if (myRepaintTimer != null) {
            myRepaintTimer.stop();
        }
        myRepaintTimer = new Timeline(new KeyFrame(Duration.millis(1000 / myMaxFPS), new WeakRedrawTimer(this)));
        myRepaintTimer.setCycleCount(Timeline.INDEFINITE);
        myRepaintTimer.play();
    }

    public boolean isLocalMouseAction(MouseEvent e) {
        return mySettingsProvider.forceActionOnMouseReporting() || (isMouseReporting() == e.isShiftDown());
    }

    public boolean isLocalMouseAction(ScrollEvent e) {
        return mySettingsProvider.forceActionOnMouseReporting() || (isMouseReporting() == e.isShiftDown());
    }

    public boolean isRemoteMouseAction(MouseEvent e) {
        return isMouseReporting() && !e.isShiftDown();
    }

    public boolean isRemoteMouseAction(ScrollEvent e) {
        return isMouseReporting() && !e.isShiftDown();
    }

    public void setBlinkingPeriod(int blinkingPeriod) {
        myBlinkingPeriod = blinkingPeriod;
    }

    public void setCoordAccessor(TerminalCoordinates coordAccessor) {
        myCoordsAccessor = coordAccessor;
    }

    public void setFindResult(@Nullable SubstringFinder.FindResult findResult) {
        myFindResult = findResult;
        if (myFindResult != null && !myFindResult.getItems().isEmpty()) {
            selectFindResultItem(myFindResult.selectedItem());
        }
        repaint();
    }

    public SubstringFinder.FindResult getFindResult() {
        return myFindResult;
    }

    public @Nullable SubstringFinder.FindResult selectPrevFindResultItem() {
        return selectPrevOrNextFindResultItem(false);
    }

    public @Nullable SubstringFinder.FindResult selectNextFindResultItem() {
        return selectPrevOrNextFindResultItem(true);
    }

    protected @Nullable SubstringFinder.FindResult selectPrevOrNextFindResultItem(boolean next) {
        if (myFindResult != null && !myFindResult.getItems().isEmpty()) {
            SubstringFinder.FindResult.FindItem item = next ? myFindResult.nextFindItem() : myFindResult.prevFindItem();
            TerminalSelection sel = new TerminalSelection(new com.jediterm.core.compatibility.Point(item.getStart().x, item.getStart().y - myTerminalTextBuffer.getHistoryLinesCount()),
                    new com.jediterm.core.compatibility.Point(item.getEnd().x, item.getEnd().y - myTerminalTextBuffer.getHistoryLinesCount()));
            if (sel.getStart().y < getTerminalTextBuffer().getHeight() / 2) {
                this.scrollBar.setValue(sel.getStart().y - getTerminalTextBuffer().getHeight() / 2);
            } else {
                this.scrollBar.setValue(scrollBar.getMin());
            }
            this.updateSelection(sel, true);
            this.selectFindResultItem(item);
            repaint();
            return myFindResult;
        }
        return null;
    }

    protected void selectFindResultItem(SubstringFinder.FindResult.FindItem item) {
        int historyLineCount = getTerminalTextBuffer().getHistoryLinesCount();
        int screenLineCount = getTerminalTextBuffer().getScreenLinesCount();
        var selection = new TerminalSelection(new com.jediterm.core.compatibility.Point(item.getStart().x,
                item.getStart().y - myTerminalTextBuffer.getHistoryLinesCount()),
                new com.jediterm.core.compatibility.Point(item.getEnd().x, item.getEnd().y - myTerminalTextBuffer.getHistoryLinesCount()));
        updateSelection(selection, true);
        JulLog.debug("Find selection start: {} / {}, end: {} / {}", item.getStart().x, item.getStart().y,
                item.getEnd().x, item.getEnd().y);
        if (mySelection.get().getStart().y < getTerminalTextBuffer().getHeight() / 2) {
            var value = FXScrollBarUtils.getValueFor(item.getStart().y, historyLineCount + screenLineCount,
                    scrollBar.getMin(), scrollBar.getMax());
            this.scrollBar.setValue(value);
        } else {
            this.scrollBar.setValue(this.scrollBar.getMax());
        }
    }

    static class WeakRedrawTimer implements EventHandler<ActionEvent> {

        private WeakReference<FXTerminalPanel> ref;

        public WeakRedrawTimer(FXTerminalPanel terminalPanel) {
            this.ref = new WeakReference<>(terminalPanel);
        }

        @Override
        public void handle(ActionEvent e) {
            FXTerminalPanel terminalPanel = ref.get();
            if (terminalPanel != null) {
                terminalPanel.myCursor.changeStateIfNeeded();
                terminalPanel.myTextBlinkingTracker.updateState(terminalPanel.mySettingsProvider, terminalPanel);
                terminalPanel.updateScrolling(false);
                if (terminalPanel.needRepaint.getAndSet(false)) {
                    try {
                        terminalPanel.doRepaint();
                    } catch (Exception ex) {
                        JulLog.error("Error while terminal panel redraw", ex);
                    }
                }
            } else if (e.getSource() instanceof Timeline timeline) { // terminalPanel was garbage collected
//                Timeline timeline = (Timeline) e.getSource();
                //TODO???
//                timeline.removeActionListener(this);
                timeline.stop();
            }
        }
    }

    @Override
    public void terminalMouseModeSet(@NotNull MouseMode mouseMode) {
        myMouseMode = mouseMode;
    }

    @Override
    public void setMouseFormat(@NotNull MouseFormat mouseFormat) {
    }

    private boolean isMouseReporting() {
        return myMouseMode != MouseMode.MOUSE_REPORTING_NONE;
    }

    /**
     * Scroll to bottom to ensure the cursor will be visible.
     */
    private void scrollToBottom() {
        // Scroll to bottom even if the cursor is on the last line, i.e. it's currently visible.
        // This will address the cases when the scroll is fixed to show some history lines, Enter is hit and after
        // Enter processing, the cursor will be pushed out of visible area unless scroll is reset to screen buffer.
        int delta = 1;
        int zeroBasedCursorY = myCursor.myCursorCoordinates.y - 1;
        if (zeroBasedCursorY + delta >= myClientScrollOrigin + scrollBar.getVisibleAmount()) {
            scrollBar.setValue(scrollBar.getMax());
        }
    }

    private void pageUp() {
        moveScrollBar(-myTermSize.getRows());
    }

    private void pageDown() {
        moveScrollBar(myTermSize.getRows());
    }

    private void scrollUp() {
        moveScrollBar(-1);
    }

    private void scrollDown() {
        moveScrollBar(1);
    }

    private void moveScrollBar(int k) {
        var newValue = resolveJavaFxScrollBarValue(myClientScrollOrigin + k);
        if (newValue < scrollBar.getMin()) {
            scrollBar.setValue(scrollBar.getMin());
        } else if (newValue > scrollBar.getMax()) {
            scrollBar.setValue(scrollBar.getMax());
        } else {
            scrollBar.setValue(newValue);
        }
    }

    protected Font createFont() {
        return mySettingsProvider.getTerminalFont();
    }

    private @NotNull com.jediterm.core.compatibility.Point panelToCharCoords(final Point2D p) {
        Cell cell = panelPointToCell(p);
        return new com.jediterm.core.compatibility.Point(cell.getColumn(), cell.getLine());
    }

    private @NotNull Cell panelPointToCell(@NotNull Point2D p) {
        int xDiff = (int) Math.round(p.getX()) - getInsetX();
        int x = Math.min(xDiff / (int) Math.round(myCharSize.getWidth()), getColumnCount() - 1);
        x = Math.max(0, x);
        int y = Math.min((int) Math.round(p.getY()) / (int) Math.round(myCharSize.getHeight()), getRowCount() - 1) + myClientScrollOrigin;
        return new Cell(y, x);
    }

    private void copySelection(@Nullable com.jediterm.core.compatibility.Point selectionStart,
                               @Nullable com.jediterm.core.compatibility.Point selectionEnd,
                               boolean useSystemSelectionClipboardIfAvailable) {
        if (selectionStart == null || selectionEnd == null) {
            return;
        }
        String selectionText = SelectionUtil.getSelectionText(selectionStart, selectionEnd, myTerminalTextBuffer);
        if (selectionText.length() != 0) {
            myCopyPasteHandler.setContents(selectionText, useSystemSelectionClipboardIfAvailable);
        }
    }

    private void pasteFromClipboard(boolean useSystemSelectionClipboardIfAvailable) {
        String text = myCopyPasteHandler.getContents(useSystemSelectionClipboardIfAvailable);

        if (text == null) {
            return;
        }
        try {
            // Sanitize clipboard text to use CR as the line separator.
            // See https://github.com/JetBrains/jediterm/issues/136.
            if (!OSUtil.isWindows()) {
                // On Windows, Java automatically does this CRLF->LF sanitization, but
                // other terminals on Unix typically also do this sanitization, so
                // maybe JediTerm also should.
                text = text.replace("\r\n", "\n");
            }
            text = text.replace('\n', '\r');

            if (myBracketedPasteMode) {
                text = "\u001b[200~" + text + "\u001b[201~";
            }
            myTerminalStarter.sendString(text, true);
        } catch (RuntimeException e) {
            JulLog.info("", e);
        }
    }

    @Nullable
    private String getClipboardString() {
        return myCopyPasteHandler.getContents(false);
    }

    public @Nullable TermSize getTerminalSizeFromComponent() {
        int columns = ((int) Math.round(this.canvas.getWidth()) - getInsetX()) / (int) Math.round(myCharSize.getWidth());
        int rows = (int) Math.round(this.canvas.getHeight()) / (int) Math.round(myCharSize.getHeight());
        return rows > 0 && columns > 0 ? new TermSize(columns, rows) : null;
    }

    private void sizeTerminalFromComponent() {
        if (myTerminalStarter != null) {
            TermSize newSize = getTerminalSizeFromComponent();
            if (newSize != null) {
                newSize = JediTerminal.ensureTermMinimumSize(newSize);
                if (!myTermSize.equals(newSize) || !myInitialSizeSyncDone) {
                    myTermSize = newSize;
                    myInitialSizeSyncDone = true;
                    myTypeAheadManager.onResize();
                    myTerminalStarter.postResize(newSize, RequestOrigin.User);
                }
            }
        }
    }

    public void setTerminalStarter(final TerminalStarter terminalStarter) {
        myTerminalStarter = terminalStarter;
        sizeTerminalFromComponent();
    }

    public void addCustomKeyListener(@NotNull BiConsumer<EventType<KeyEvent>, KeyEvent> keyListener) {
        myCustomKeyListeners.add(keyListener);
    }

    public void removeCustomKeyListener(@NotNull BiConsumer<EventType<KeyEvent>, KeyEvent> keyListener) {
        myCustomKeyListeners.remove(keyListener);
    }

    @Override
    public void onResize(@NotNull TermSize newTermSize, @NotNull RequestOrigin origin) {
        myTermSize = newTermSize;
//        this.canvas.setWidth(getPixelHeight());
//        this.canvas.setHeight(getPixelWidth());
        Platform.runLater(() -> updateScrolling(true));
    }

    private void establishFontMetrics() {
        var fontMetrics = FXFontMetrics.create(myNormalFont, "W");
        final float lineSpacing = getLineSpacing();
        double fontMetricsHeight = fontMetrics.getHeight();

        myCharSize = new Dimension2D(Math.round(fontMetrics.getWidth()), Math.round(Math.ceil(fontMetricsHeight * lineSpacing)));
        mySpaceBetweenLines = Math.max(0, (int) Math.round(((myCharSize.getHeight() - fontMetricsHeight) / 2) * 2));
        fontMetrics = FXFontMetrics.create(myNormalFont, "qpjg");
        myDescent = fontMetrics.getDescent();
        if (JulLog.isDebugEnabled()) {
            // The magic +2 here is to give lines a tiny bit of extra height to avoid clipping when rendering some Apple
            // emoji, which are slightly higher than the font metrics reported character height :(
            double oldCharHeight = fontMetricsHeight + (int) (lineSpacing * 2) + 2;
            double oldDescent = fontMetrics.getDescent() + (int) lineSpacing;
            JulLog.debug("charHeight=" + oldCharHeight + "->" + myCharSize.getHeight() +
                    ", descent=" + oldDescent + "->" + myDescent);
        }
//TODO
//    var myMonospaced = isMonospaced(fo);
//    if (!myMonospaced) {
//      JulLog.info("WARNING: Font " + myNormalFont.getName() + " is non-monospaced");
//    }
    }

    private float getLineSpacing() {
        if (myTerminalTextBuffer.isUsingAlternateBuffer() && mySettingsProvider.shouldDisableLineSpacingForAlternateScreenBuffer()) {
            return 1.0f;
        }
        return mySettingsProvider.getLineSpacing();
    }

//TODO
//  private static boolean isMonospaced(FontMetrics fontMetrics) {
//    boolean isMonospaced = true;
//    int charWidth = -1;
//    for (int codePoint = 0; codePoint < 128; codePoint++) {
//      if (Character.isValidCodePoint(codePoint)) {
//        char character = (char) codePoint;
//        if (isWordCharacter(character)) {
//          int w = fontMetrics.charWidth(character);
//          if (charWidth != -1) {
//            if (w != charWidth) {
//              isMonospaced = false;
//              break;
//            }
//          } else {
//            charWidth = w;
//          }
//        }
//      }
//    }
//    return isMonospaced;
//  }

//    private static boolean isWordCharacter(char character) {
//        return Character.isLetterOrDigit(character);
//    }

    public @NotNull javafx.scene.paint.Color windowBackground() {
        return FXTransformers.toFxColor(getWindowBackground());
    }

    public @NotNull javafx.scene.paint.Color windowForeground() {
        return FXTransformers.toFxColor(getWindowForeground());
    }

    public void paintComponent(GraphicsContext gfx) {
        resetColorCache();

        gfx.setFill(this.windowBackground());

        gfx.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.fixScrollBarThumbVisibility();

        try {
            myTerminalTextBuffer.lock();
            // update myClientScrollOrigin as scrollArea might have been invoked after last WeakRedrawTimer action
            updateScrolling(false);
            myTerminalTextBuffer.processHistoryAndScreenLines(myClientScrollOrigin, myTermSize.getRows(), new StyledTextConsumer() {
                final int columnCount = getColumnCount();

                @Override
                public void consume(int x, int y, @NotNull TextStyle style, @NotNull CharBuffer characters, int startRow) {
                    int row = y - startRow;
                    drawCharacters(x, row, style, characters, gfx, myFillCharacterBackgroundIncludingLineSpacing);

                    if (myFindResult != null) {
                        List<Pair<Integer, Integer>> ranges = myFindResult.getRanges(characters);
                        if (ranges != null && !ranges.isEmpty()) {
                            TextStyle foundPatternStyle = getFoundPattern(style);
                            for (Pair<Integer, Integer> range : ranges) {
                                CharBuffer foundPatternChars = characters.subBuffer(range);
                                drawCharacters(x + range.getFirst(), row, foundPatternStyle, foundPatternChars, gfx);
                            }
                        }
                    }

                    if (mySelection.get() != null) {
                        Pair<Integer, Integer> interval = mySelection.get().intersect(x, row + myClientScrollOrigin, characters.length());
                        if (interval != null) {
                            TextStyle selectionStyle = getSelectionStyle(style);
                            CharBuffer selectionChars = characters.subBuffer(interval.getFirst() - x, interval.getSecond());

                            drawCharacters(interval.getFirst(), row, selectionStyle, selectionChars, gfx);
                        }
                    }
                }

                @Override
                public void consumeNul(int x, int y, int nulIndex, TextStyle style, CharBuffer characters, int startRow) {
                    int row = y - startRow;
                    if (mySelection.get() != null) {
                        // compute intersection with all NUL areas, non-breaking
                        Pair<Integer, Integer> interval = mySelection.get().intersect(nulIndex, row + myClientScrollOrigin, columnCount - nulIndex);
                        if (interval != null) {
                            TextStyle selectionStyle = getSelectionStyle(style);
                            drawCharacters(x, row, selectionStyle, characters, gfx);
                            return;
                        }
                    }
                    drawCharacters(x, row, style, characters, gfx);
                }

                @Override
                public void consumeQueue(int x, int y, int nulIndex, int startRow) {
                    if (x < columnCount) {
                        consumeNul(x, y, nulIndex, TextStyle.EMPTY, new CharBuffer(CharUtils.EMPTY_CHAR, columnCount - x), startRow);
                    }
                }
            });

            int cursorY = myCursor.getCoordY();
            if (cursorY < getRowCount() && !hasUncommittedChars()) {
                int cursorX = myCursor.getCoordX();
                Pair<Character, TextStyle> sc = myTerminalTextBuffer.getStyledCharAt(cursorX, cursorY);
                String cursorChar = "" + sc.getFirst();
                if (Character.isHighSurrogate(sc.getFirst())) {
                    cursorChar += myTerminalTextBuffer.getStyledCharAt(cursorX + 1, cursorY).getFirst();
                }
                TextStyle normalStyle = sc.getSecond() != null ? sc.getSecond() : myStyleState.getCurrent();
                TextStyle cursorStyle;
                if (inSelection(cursorX, cursorY)) {
                    cursorStyle = getSelectionStyle(normalStyle);
                } else {
                    cursorStyle = normalStyle;
                }
                myCursor.drawCursor(cursorChar, gfx, cursorStyle);
            }
        } finally {
            myTerminalTextBuffer.unlock();
        }
        resetColorCache();
        drawInputMethodUncommitedChars(gfx);

        drawMargins(gfx, this.canvas.getWidth(), this.canvas.getHeight());
    }

    /**
     * Hides/shows thumb in scroll bar.
     */
    private void fixScrollBarThumbVisibility() {
        if (scrollBarThumbVisible && myTerminalTextBuffer.getHistoryLinesCount() == 0) {
            this.scrollBar.getStyleClass().add("no-thumb");
            scrollBarThumbVisible = false;
        } else if (!scrollBarThumbVisible && myTerminalTextBuffer.getHistoryLinesCount() != 0) {
            this.scrollBar.getStyleClass().remove("no-thumb");
            scrollBarThumbVisible = true;
        }
    }

    private void resetColorCache() {
        myCachedSelectionColor = null;
        myCachedFoundPatternColor = null;
    }

    @NotNull
    private TextStyle getSelectionStyle(@NotNull TextStyle style) {
        if (mySettingsProvider.useInverseSelectionColor()) {
            return getInversedStyle(style);
        }
        TextStyle.Builder builder = style.toBuilder();
        TextStyle selectionStyle = getSelectionColor();
        builder.setBackground(selectionStyle.getBackground());
        builder.setForeground(selectionStyle.getForeground());
        if (builder instanceof HyperlinkStyle.Builder) {
            return ((HyperlinkStyle.Builder) builder).build(true);
        }
        return builder.build();
    }

    private @NotNull TextStyle getSelectionColor() {
        TextStyle selectionColor = myCachedSelectionColor;
        if (selectionColor == null) {
            selectionColor = mySettingsProvider.getSelectionColor();
            myCachedSelectionColor = selectionColor;
        }
        return selectionColor;
    }

    private @NotNull TextStyle getFoundPatternColor() {
        TextStyle foundPatternColor = myCachedFoundPatternColor;
        if (foundPatternColor == null) {
            foundPatternColor = mySettingsProvider.getFoundPatternColor();
            myCachedFoundPatternColor = foundPatternColor;
        }
        return foundPatternColor;
    }

    @NotNull
    private TextStyle getFoundPattern(@NotNull TextStyle style) {
        TextStyle.Builder builder = style.toBuilder();
        TextStyle foundPattern = getFoundPatternColor();
        builder.setBackground(foundPattern.getBackground());
        builder.setForeground(foundPattern.getForeground());
        return builder.build();
    }

    private void drawInputMethodUncommitedChars(GraphicsContext gfx) {
        if (hasUncommittedChars()) {
            double xCoord = (myCursor.getCoordX() + 1) * myCharSize.getWidth() + getInsetX();

            double y = myCursor.getCoordY() + 1;

            double yCoord = y * myCharSize.getHeight() - 3;

            double len = myInputMethodUncommittedChars.length() * myCharSize.getWidth();

            gfx.setFill(this.windowBackground());
            gfx.fillRect(xCoord, (y - 1) * myCharSize.getHeight() - 3, len, myCharSize.getHeight());

            gfx.setFill(this.windowForeground());
            gfx.setFont(myNormalFont);

            gfx.fillText(myInputMethodUncommittedChars, xCoord, yCoord);
            gfx.save();
            gfx.setLineWidth(1);
            gfx.setLineCap(StrokeLineCap.ROUND);
            gfx.setLineJoin(StrokeLineJoin.ROUND);
            gfx.setMiterLimit(0);
            gfx.setLineDashes(0, 2, 0, 2);
            gfx.setLineDashOffset(0);

            gfx.strokeLine(xCoord, yCoord, xCoord + len, yCoord);
            gfx.restore();
        }
    }

    private boolean hasUncommittedChars() {
        return myInputMethodUncommittedChars != null && !myInputMethodUncommittedChars.isEmpty();
    }

    private boolean inSelection(int x, int y) {
        return mySelection.get() != null && mySelection.get().contains(new com.jediterm.core.compatibility.Point(x, y));
    }

    // also called from com.intellij.terminal.JBTerminalPanel
    public void handleKeyEvent(@NotNull KeyEvent e) {
        if (e.getEventType() == KeyEvent.KEY_PRESSED) {
            for (BiConsumer<EventType<KeyEvent>, KeyEvent> keyListener : myCustomKeyListeners) {
                keyListener.accept(e.getEventType(), e);
            }
        } else if (e.getEventType() == KeyEvent.KEY_TYPED) {
            for (BiConsumer<EventType<KeyEvent>, KeyEvent> keyListener : myCustomKeyListeners) {
                keyListener.accept(e.getEventType(), e);
            }
        }
    }

    public double getPixelWidth() {
        return myCharSize.getWidth() * myTermSize.getColumns() + getInsetX();
    }

    public double getPixelHeight() {
        return myCharSize.getHeight() * myTermSize.getRows();
    }

    private int getColumnCount() {
        return myTermSize.getColumns();
    }

    private int getRowCount() {
        return myTermSize.getRows();
    }

    public String getWindowTitle() {
        return myWindowTitle;
    }

    @Override
    public @NotNull com.jediterm.core.Color getWindowForeground() {
        return toForeground(mySettingsProvider.getDefaultForeground());
    }

    @Override
    public @NotNull com.jediterm.core.Color getWindowBackground() {
        return toBackground(mySettingsProvider.getDefaultBackground());
    }

    private @NotNull javafx.scene.paint.Color getEffectiveForeground(@NotNull TextStyle style) {
        com.jediterm.core.Color color = style.hasOption(TextStyle.Option.INVERSE) ? getBackground(style) : getForeground(style);
        return FXTransformers.toFxColor(color);
    }

    private @NotNull javafx.scene.paint.Color getEffectiveBackground(@NotNull TextStyle style) {
        com.jediterm.core.Color color = style.hasOption(TextStyle.Option.INVERSE) ? getForeground(style) : getBackground(style);
        return FXTransformers.toFxColor(color);
    }

    private @NotNull com.jediterm.core.Color getForeground(@NotNull TextStyle style) {
        TerminalColor foreground = style.getForeground();
        return foreground != null ? toForeground(foreground) : getWindowForeground();
    }

    private com.jediterm.core.@NotNull Color toForeground(@NotNull TerminalColor terminalColor) {
        if (terminalColor.isIndexed()) {
            return getPalette().getForeground(terminalColor);
        }
        return terminalColor.toColor();
    }

    private @NotNull com.jediterm.core.Color getBackground(@NotNull TextStyle style) {
        TerminalColor background = style.getBackground();
        return background != null ? toBackground(background) : getWindowBackground();
    }

    private com.jediterm.core.@NotNull Color toBackground(@NotNull TerminalColor terminalColor) {
        if (terminalColor.isIndexed()) {
            return getPalette().getBackground(terminalColor);
        }
        return terminalColor.toColor();
    }

    protected int getInsetX() {
        return 4;
    }

    public void addTerminalMouseListener(final TerminalMouseListener listener) {
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
                com.jediterm.core.compatibility.Point p = panelToCharCoords(createPoint(e));
                listener.mousePressed(p.x, p.y, new FXMouseEvent(e));
            }
        });

        this.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
                com.jediterm.core.compatibility.Point p = panelToCharCoords(createPoint(e));
                listener.mouseReleased(p.x, p.y, new FXMouseEvent(e));
            }
        });

        this.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
                updateSelection(null, true);
                com.jediterm.core.compatibility.Point p = panelToCharCoords(createPoint(e));
                listener.mouseWheelMoved(p.x, p.y, new FXMouseWheelEvent(e));
            }
            if (myTerminalTextBuffer.isUsingAlternateBuffer() && mySettingsProvider.sendArrowKeysInAlternativeMode()) {
                //Send Arrow keys instead
                final byte[] arrowKeys;
                if (e.getDeltaY() > 0) {
                    arrowKeys = myTerminalStarter.getTerminal().getCodeForKey(KeyCode.UP.getCode(), 0);
                } else {
                    arrowKeys = myTerminalStarter.getTerminal().getCodeForKey(KeyCode.DOWN.getCode(), 0);
                }
                for (int i = 0; i < Math.abs(getUnitsToScroll(e)); i++) {
                    myTerminalStarter.sendBytes(arrowKeys, false);
                }
                e.consume();
            }
        });

        this.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
                com.jediterm.core.compatibility.Point p = panelToCharCoords(createPoint(e));
                listener.mouseMoved(p.x, p.y, new FXMouseEvent(e));
            }
        });

        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (mySettingsProvider.enableMouseReporting() && isRemoteMouseAction(e)) {
                com.jediterm.core.compatibility.Point p = panelToCharCoords(createPoint(e));
                listener.mouseDragged(p.x, p.y, new FXMouseEvent(e));
            }
        });
    }

    @NotNull
    TerminalKeyHandler getTerminalKeyListener() {
        return myTerminalKeyHandler;
    }

    private enum TerminalCursorState {
        SHOWING, HIDDEN, NO_FOCUS;
    }

    private class TerminalCursor {

        // cursor state
        private boolean myCursorIsShown; // blinking state

        private final Point myCursorCoordinates = new Point();

        private @NotNull CursorShape myDefaultCursorShape = CursorShape.BLINK_BLOCK;

        private @Nullable CursorShape myShape;

        // terminal modes
        private boolean myShouldDrawCursor = true;

        private long myLastCursorChange;

        private boolean myCursorHasChanged;

        public void setX(int x) {
            myCursorCoordinates.x = x;
            cursorChanged();
        }

        public void setY(int y) {
            myCursorCoordinates.y = y;
            cursorChanged();
        }

        public int getCoordX() {
            if (myTypeAheadManager != null) {
                return myTypeAheadManager.getCursorX() - 1;
            }
            return myCursorCoordinates.x;
        }

        public int getCoordY() {
            return myCursorCoordinates.y - 1 - myClientScrollOrigin;
        }

        public void setShouldDrawCursor(boolean shouldDrawCursor) {
            myShouldDrawCursor = shouldDrawCursor;
        }

        public boolean isBlinking() {
            return getEffectiveShape().isBlinking() && (getBlinkingPeriod() > 0);
        }

        public void cursorChanged() {
            myCursorHasChanged = true;
            myLastCursorChange = System.currentTimeMillis();
            repaint();
        }

        private boolean cursorShouldChangeBlinkState(long currentTime) {
            return currentTime - myLastCursorChange > getBlinkingPeriod();
        }

        public void changeStateIfNeeded() {
            if (!canvas.isFocused()) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (cursorShouldChangeBlinkState(currentTime)) {
                myCursorIsShown = !myCursorIsShown;
                myLastCursorChange = currentTime;
                myCursorHasChanged = false;
                repaint();
            }
        }

        private TerminalCursorState computeBlinkingState() {
            if (!isBlinking() || myCursorHasChanged || myCursorIsShown) {
                return TerminalCursorState.SHOWING;
            }
            return TerminalCursorState.HIDDEN;
        }

        private TerminalCursorState computeCursorState() {
            if (!myShouldDrawCursor) {
                return TerminalCursorState.HIDDEN;
            }
            if (!canvas.isFocused()) {
                return TerminalCursorState.NO_FOCUS;
            }
            return computeBlinkingState();
        }

        void drawCursor(String c, GraphicsContext gfx, TextStyle style) {
            TerminalCursorState state = computeCursorState();

            // hidden: do nothing
            if (state == TerminalCursorState.HIDDEN) {
                return;
            }

            final int x = getCoordX();
            final int y = getCoordY();
            // Outside bounds of window: do nothing
            if (y < 0 || y >= myTermSize.getRows()) {
                return;
            }

            CharBuffer buf = new CharBuffer(c);
            double xCoord = x * myCharSize.getWidth() + getInsetX();
            double yCoord = y * myCharSize.getHeight();
            double textLength = CharUtils.getTextLengthDoubleWidthAware(buf.getBuf(), buf.getStart(), buf.length(), mySettingsProvider.ambiguousCharsAreDoubleWidth());
            double height = Math.min(myCharSize.getHeight(), getHeight() - yCoord);
            double width = Math.min(textLength * FXTerminalPanel.this.myCharSize.getWidth(), FXTerminalPanel.this.getWidth() - xCoord);
            int lineStrokeSize = 2;

            javafx.scene.paint.Color fgColor = getEffectiveForeground(style);
            TextStyle inversedStyle = getInversedStyle(style);
            javafx.scene.paint.Color inverseBg = getEffectiveBackground(inversedStyle);

            switch (getEffectiveShape()) {
                case BLINK_BLOCK:
                case STEADY_BLOCK:
                    if (state == TerminalCursorState.SHOWING) {
                        gfx.setFill(inverseBg);
                        gfx.fillRect(xCoord, yCoord, width, height);
                        drawCharacters(x, y, inversedStyle, buf, gfx);
                    } else {
                        gfx.setFill(fgColor);
                        gfx.setLineWidth(1.0);
                        gfx.strokeRect(xCoord, yCoord, width, height);
                    }
                    break;

                case BLINK_UNDERLINE:
                case STEADY_UNDERLINE:
                    gfx.setFill(fgColor);
                    gfx.fillRect(xCoord, yCoord + height, width, lineStrokeSize);
                    break;

                case BLINK_VERTICAL_BAR:
                case STEADY_VERTICAL_BAR:
                    gfx.setFill(fgColor);
                    gfx.fillRect(xCoord, yCoord, lineStrokeSize, height);
                    break;
            }
        }

        void setShape(@Nullable CursorShape shape) {
            myShape = shape;
        }

        @NotNull CursorShape getEffectiveShape() {
            return Objects.requireNonNullElse(myShape, myDefaultCursorShape);
        }

        private void setDefaultShape(@NotNull CursorShape defaultShape) {
            myDefaultCursorShape = defaultShape;
        }
    }

    private int getBlinkingPeriod() {
        if (myBlinkingPeriod != mySettingsProvider.caretBlinkingMs()) {
            setBlinkingPeriod(mySettingsProvider.caretBlinkingMs());
        }
        return myBlinkingPeriod;
    }

    @NotNull
    private TextStyle getInversedStyle(@NotNull TextStyle style) {
        TextStyle.Builder builder = new TextStyle.Builder(style);
        builder.setOption(TextStyle.Option.INVERSE, !style.hasOption(TextStyle.Option.INVERSE));
        if (style.getForeground() == null) {
            builder.setForeground(myStyleState.getDefaultForeground());
        }
        if (style.getBackground() == null) {
            builder.setBackground(myStyleState.getDefaultBackground());
        }
        return builder.build();
    }

    private void drawCharacters(int x, int y, TextStyle style, CharBuffer buf, GraphicsContext gfx) {
        drawCharacters(x, y, style, buf, gfx, true);
    }

    private void drawCharacters(int x, int y, TextStyle style, CharBuffer buf, GraphicsContext gfx,
                                boolean includeSpaceBetweenLines) {
        if (myTextBlinkingTracker.shouldBlinkNow(style)) {
            style = getInversedStyle(style);
        }

        double xCoord = x * myCharSize.getWidth() + getInsetX();
        double yCoord = y * myCharSize.getHeight() + (includeSpaceBetweenLines ? 0 : mySpaceBetweenLines / 2.0);

        if (xCoord < 0 || xCoord > getWidth() || yCoord < 0 || yCoord > getHeight()) {
            return;
        }

        int textLength = CharUtils.getTextLengthDoubleWidthAware(buf.getBuf(), buf.getStart(), buf.length(), mySettingsProvider.ambiguousCharsAreDoubleWidth());
        double height = Math.min(myCharSize.getHeight() - (includeSpaceBetweenLines ? 0 : mySpaceBetweenLines), getHeight() - yCoord);
        double width = Math.min(textLength * this.myCharSize.getWidth(), FXTerminalPanel.this.getWidth() - xCoord);

        if (style instanceof HyperlinkStyle) {
            HyperlinkStyle hyperlinkStyle = (HyperlinkStyle) style;

            if (hyperlinkStyle.getHighlightMode() == HyperlinkStyle.HighlightMode.ALWAYS || (isHoveredHyperlink(hyperlinkStyle) && hyperlinkStyle.getHighlightMode() == HyperlinkStyle.HighlightMode.HOVER)) {

                // substitute text style with the hyperlink highlight style if applicable
                style = hyperlinkStyle.getHighlightStyle();
            }
        }

        javafx.scene.paint.Color backgroundColor = getEffectiveBackground(style);
        gfx.setFill(backgroundColor);
        gfx.fillRect(xCoord,
                yCoord,
                width,
                height);

        if (buf.isNul()) {
            return; // nothing more to do
        }

        gfx.setFill(getStyleForeground(style));

        drawChars(x, y, buf, style, gfx);

        if (style.hasOption(TextStyle.Option.UNDERLINED)) {
            double baseLine = (y + 1) * myCharSize.getHeight() - mySpaceBetweenLines / 2.0 - myDescent;
            double lineY = baseLine + 3;
            gfx.setLineWidth(1.0);
            gfx.strokeLine(xCoord, lineY, (x + textLength) * myCharSize.getWidth() + getInsetX(), lineY);
        }
    }

    private boolean isHoveredHyperlink(@NotNull HyperlinkStyle link) {
        return myHoveredHyperlink == link.getLinkInfo();
    }

    /**
     * Draw every char in separate terminal cell to guaranty equal width for different lines.
     * Nevertheless, to improve kerning we draw word characters as one block for monospaced fonts.
     */
    private void drawChars(int x, int y, @NotNull CharBuffer buf, @NotNull TextStyle style, @NotNull GraphicsContext gfx) {
        // workaround to fix Swing bad rendering of bold special chars on Linux
        // TODO required for italic?
        CharBuffer renderingBuffer;
        if (mySettingsProvider.DECCompatibilityMode() && style.hasOption(TextStyle.Option.BOLD)) {
            renderingBuffer = CharUtils.heavyDecCompatibleBuffer(buf);
        } else {
            renderingBuffer = buf;
        }

        BreakIterator iterator = BreakIterator.getCharacterInstance();
        char[] text = renderingBuffer.clone().getBuf();
        iterator.setText(new String(text));
        int endOffset;
        int startOffset = 0;
        while ((endOffset = iterator.next()) != BreakIterator.DONE) {
            endOffset = extendEndOffset(text, iterator, startOffset, endOffset);
            int effectiveEndOffset = shiftDwcToEnd(text, startOffset, endOffset);
            if (effectiveEndOffset == startOffset) {
                startOffset = endOffset;
                continue; // nothing to draw
            }
            Font font = getFontToDisplay(text, startOffset, effectiveEndOffset, style);
            gfx.setFont(font);
            double descent = myDescent;
            double baseLine = (y + 1) * myCharSize.getHeight() - mySpaceBetweenLines / 2.0 - descent;
            double charWidth = myCharSize.getWidth();
            double xCoord = (x + startOffset) * charWidth + getInsetX();
            double yCoord = y * myCharSize.getHeight() + mySpaceBetweenLines / 2.0;
            gfx.save();
            gfx.beginPath();
            gfx.rect(Math.round(xCoord), Math.round(yCoord), Math.round(this.getWidth() - xCoord), Math.round(this.getHeight() - yCoord));
            gfx.closePath();
            gfx.clip();

            int emptyCells = endOffset - startOffset;
            if (emptyCells >= 2) {
                double drawnWidth = myCharSize.getWidth();
                double emptySpace = Math.max(0, emptyCells * charWidth - drawnWidth);
                // paint a Unicode symbol closer to the center
                xCoord += emptySpace / 2;
            }
            xCoord = Math.round(xCoord);
            baseLine = Math.round(baseLine);
            //JulLog.debug("Drawing {} at {}:{}", str, xCoord, baseLine);
            var str = new String(text, startOffset, effectiveEndOffset - startOffset);
            gfx.fillText(str, xCoord, baseLine);
            gfx.restore();

            startOffset = endOffset;
        }
    }

    private static int shiftDwcToEnd(char[] text, int startOffset, int endOffset) {
        int ind = startOffset;
        for (int i = startOffset; i < endOffset; i++) {
            if (text[i] != CharUtils.DWC) {
                text[ind++] = text[i];
            }
        }
        Arrays.fill(text, ind, endOffset, CharUtils.DWC);
        return ind;
    }

    private static int extendEndOffset(char[] text, @NotNull BreakIterator iterator, int startOffset, int endOffset) {
        while (shouldExtend(text, startOffset, endOffset)) {
            int newEndOffset = iterator.next();
            if (newEndOffset == BreakIterator.DONE) {
                break;
            }
            if (newEndOffset - endOffset == 1 && !isUnicodePart(text, endOffset)) {
                iterator.previous(); // do not eat a plain char following Unicode symbol
                break;
            }
            startOffset = endOffset;
            endOffset = newEndOffset;
        }
        return endOffset;
    }

    private static boolean shouldExtend(char[] text, int startOffset, int endOffset) {
        if (endOffset - startOffset > 1) {
            return true;
        }
        if (isFormatChar(text, startOffset, endOffset)) {
            return true;
        }
        return endOffset < text.length && text[endOffset] == CharUtils.DWC;
    }

    private static boolean isUnicodePart(char[] text, int ind) {
        if (isFormatChar(text, ind, ind + 1)) {
            return true;
        }
        if (text[ind] == CharUtils.DWC) {
            return true;
        }
        return Character.UnicodeBlock.of(text[ind]) == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_ARROWS;
    }

    private static boolean isFormatChar(char[] text, int start, int end) {
        if (end - start == 1) {
            int charCode = text[start];
            // From CMap#getFormatCharGlyph
            if (charCode >= 0x200c) {
                //noinspection RedundantIfStatement
                if ((charCode <= 0x200f) ||
                        (charCode >= 0x2028 && charCode <= 0x202e) ||
                        (charCode >= 0x206a && charCode <= 0x206f)) {
                    return true;
                }
            }
        }
        return false;
    }

    private @NotNull javafx.scene.paint.Color getStyleForeground(@NotNull TextStyle style) {
        javafx.scene.paint.Color foreground = getEffectiveForeground(style);
        if (style.hasOption(TextStyle.Option.DIM)) {
            javafx.scene.paint.Color background = getEffectiveBackground(style);
            foreground = new javafx.scene.paint.Color((foreground.getRed() + background.getRed()) / 2,
                    (foreground.getGreen() + background.getGreen()) / 2,
                    (foreground.getBlue() + background.getBlue()) / 2,
                    foreground.getOpacity());
        }
        return foreground;
    }

    protected @NotNull Font getFontToDisplay(char[] text, int start, int end, @NotNull TextStyle style) {
        boolean bold = style.hasOption(TextStyle.Option.BOLD);
        boolean italic = style.hasOption(TextStyle.Option.ITALIC);
        // workaround to fix Swing bad rendering of bold special chars on Linux
        if (bold && mySettingsProvider.DECCompatibilityMode() && CharacterSets.isDecBoxChar(text[start])) {
            return myNormalFont;
        }
        return bold ? (italic ? myBoldItalicFont : myBoldFont)
                : (italic ? myItalicFont : myNormalFont);
    }

    private ColorPalette getPalette() {
        return mySettingsProvider.getTerminalColorPalette();
    }

    private void drawMargins(GraphicsContext graphicsContext, double width, double height) {
        graphicsContext.setFill(this.windowBackground());
        graphicsContext.fillRect(0, height, this.getWidth(), this.getHeight() - height);
        graphicsContext.fillRect(width, 0, this.getWidth() - width, this.getHeight());
    }

    // Called in a background thread with myTerminalTextBuffer.lock() acquired
    @Override
    public void scrollArea(final int scrollRegionTop, final int scrollRegionSize, int dy) {
        scrollDy.addAndGet(dy);
        updateSelection(null, true);
    }

    // should be called on EDT
    public void scrollToShowAllOutput() {
        myTerminalTextBuffer.lock();
        try {
            int historyLines = myTerminalTextBuffer.getHistoryLinesCount();
            if (historyLines > 0) {
                int termHeight = myTermSize.getRows();
                setScrollBarRangeProperties(-historyLines, historyLines + termHeight, -historyLines,
                        termHeight);
                TerminalModelListener modelListener = new TerminalModelListener() {
                    @Override
                    public void modelChanged() {
                        int zeroBasedCursorY = myCursor.myCursorCoordinates.y - 1;
                        if (zeroBasedCursorY + historyLines >= termHeight) {
                            myTerminalTextBuffer.removeModelListener(this);
                            Platform.runLater(() -> {
                                myTerminalTextBuffer.lock();
                                try {
                                    setScrollBarRangeProperties(0, myTermSize.getRows(),
                                            -myTerminalTextBuffer.getHistoryLinesCount(), myTermSize.getRows());
                                } finally {
                                    myTerminalTextBuffer.unlock();
                                }
                            });
                        }
                    }
                };
                myTerminalTextBuffer.addModelListener(modelListener);
                scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                        scrollBar.valueProperty().removeListener(this);
                        myTerminalTextBuffer.removeModelListener(modelListener);
                    }
                });
            }
        } finally {
            myTerminalTextBuffer.unlock();
        }
    }

    private void updateScrolling(boolean forceUpdate) {
        int dy = scrollDy.getAndSet(0);
        boolean historyBufferLineCountChanged = myHistoryBufferLineCountChanged.getAndSet(false);
        if (dy == 0 && !forceUpdate && !historyBufferLineCountChanged) {
            return;
        }
        if (myScrollingEnabled) {
            int value = myClientScrollOrigin;
            int historyLineCount = myTerminalTextBuffer.getHistoryLinesCount();
            if (value == 0) {
                setScrollBarRangeProperties(myTermSize.getRows(), myTermSize.getRows(), -historyLineCount, myTermSize.getRows());
            } else {
                // if scrolled to a specific area, update scroll to keep showing this area
                setScrollBarRangeProperties(
                        Math.min(Math.max(value + dy, -historyLineCount), myTermSize.getRows()),
                        myTermSize.getRows(),
                        -historyLineCount,
                        myTermSize.getRows());
            }
        } else {
            setScrollBarRangeProperties(myTermSize.getRows(), myTermSize.getRows(), 0, myTermSize.getRows());
        }
    }

    public void setCursor(final int x, final int y) {
        myCursor.setX(x);
        myCursor.setY(y);
    }

    @Override
    public void setCursorShape(@Nullable CursorShape cursorShape) {
        myCursor.setShape(cursorShape);
    }

    public void setDefaultCursorShape(@NotNull CursorShape defaultCursorShape) {
        myCursor.setDefaultShape(defaultCursorShape);
    }

    public void beep() {
        if (mySettingsProvider.audibleBell()) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public @Nullable Rectangle2D getBounds(@NotNull TerminalLineIntervalHighlighting highlighting) {
        TerminalLine line = highlighting.getLine();
        int index = myTerminalTextBuffer.findScreenLineIndex(line);
        if (index >= 0 && !highlighting.isDisposed()) {
            return getBounds(new LineCellInterval(index, highlighting.getStartOffset(), highlighting.getEndOffset() + 1));
        }
        return null;
    }

    private @NotNull Rectangle2D getBounds(@NotNull LineCellInterval cellInterval) {
        var x = cellInterval.getStartColumn() * myCharSize.getWidth() + getInsetX();
        var y = cellInterval.getLine() * myCharSize.getHeight();
        return new Rectangle2D(x, y, myCharSize.getWidth() * cellInterval.getCellCount(), myCharSize.getHeight());
    }

    public TerminalTextBuffer getTerminalTextBuffer() {
        return myTerminalTextBuffer;
    }

    @Override
    public @Nullable TerminalSelection getSelection() {
        return mySelection.get();
    }

    @Override
    public boolean ambiguousCharsAreDoubleWidth() {
        return mySettingsProvider.ambiguousCharsAreDoubleWidth();
    }

    @Override
    public void setBracketedPasteMode(boolean bracketedPasteModeEnabled) {
        myBracketedPasteMode = bracketedPasteModeEnabled;
    }

    // Use getScrollLinesStorage instead
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    public LinesBuffer getScrollBuffer() {
        return myTerminalTextBuffer.getHistoryBuffer();
    }

    public LinesStorage getScrollLinesStorage() {
        return myTerminalTextBuffer.getHistoryLinesStorage();
    }

    @Override
    public void setCursorVisible(boolean isCursorVisible) {
        myCursor.setShouldDrawCursor(isCursorVisible);
    }

    protected @NotNull ContextMenu createPopupMenu(@NotNull TerminalActionProvider actionProvider) {
        ContextMenu menu = new ContextMenu();
        TerminalAction.fillMenu(menu, actionProvider);
        return menu;
    }

    private @NotNull TerminalActionProvider getTerminalActionProvider(@Nullable LinkInfo linkInfo, @NotNull MouseEvent e) {
        LinkInfoEx.PopupMenuGroupProvider popupMenuGroupProvider = LinkInfoEx.getPopupMenuGroupProvider(linkInfo);
        if (popupMenuGroupProvider != null) {
            return new TerminalActionProvider() {

                @Override
                public List<TerminalAction> getActions() {
                    return popupMenuGroupProvider.getPopupMenuGroup(e);
                }

                @Override
                public TerminalActionProvider getNextProvider() {
                    return FXTerminalPanel.this;
                }

                @Override
                public void setNextProvider(TerminalActionProvider provider) {
                }
            };
        }
        return this;
    }

    @Override
    public void useAlternateScreenBuffer(boolean useAlternateScreenBuffer) {
        myScrollingEnabled = !useAlternateScreenBuffer;
        Platform.runLater(() -> {
            updateScrolling(true);
            if (myUsingAlternateBuffer != myTerminalTextBuffer.isUsingAlternateBuffer()) {
                myUsingAlternateBuffer = myTerminalTextBuffer.isUsingAlternateBuffer();
                if (mySettingsProvider.shouldDisableLineSpacingForAlternateScreenBuffer()) {
                    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), e -> {
                        reinitFontAndResize();
                    }));
                    timeline.setCycleCount(1);
                    timeline.play();
                }
            }
        });
    }

    public TerminalOutputStream getTerminalOutputStream() {
        return myTerminalStarter;
    }

    @Override
    public void setWindowTitle(@NotNull String windowTitle) {
        myWindowTitle = windowTitle;
    }

    @Override
    public List<TerminalAction> getActions() {
        return List.of(
                new TerminalAction(mySettingsProvider.getOpenUrlActionPresentation(), input -> {
                    return openSelectedTextAsURL();
                }).withEnabledSupplier(this::isSelectedTextUrl),
                new TerminalAction(mySettingsProvider.getCopyActionPresentation(), this::handleCopy) {
                    @Override
                    public boolean isEnabled(@Nullable KeyEvent e) {
                        return e != null || mySelection.get() != null;
                    }
                }.withMnemonicKey(KeyCode.C),
                new TerminalAction(mySettingsProvider.getPasteActionPresentation(), input -> {
                    handlePaste();
                    return true;
                }).withMnemonicKey(KeyCode.P).withEnabledSupplier(() -> getClipboardString() != null),
                new TerminalAction(mySettingsProvider.getSelectAllActionPresentation(), input -> {
                    selectAll();
                    return true;
                }),
                new TerminalAction(mySettingsProvider.getClearBufferActionPresentation(), input -> {
                    clearBuffer();
                    return true;
                }).withMnemonicKey(KeyCode.K).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()).separatorBefore(true),
                new TerminalAction(mySettingsProvider.getPageUpActionPresentation(), input -> {
                    pageUp();
                    return true;
                }).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()).separatorBefore(true),
                new TerminalAction(mySettingsProvider.getPageDownActionPresentation(), input -> {
                    pageDown();
                    return true;
                }).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()),
                new TerminalAction(mySettingsProvider.getLineUpActionPresentation(), input -> {
                    scrollUp();
                    return true;
                }).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()).separatorBefore(true),
                new TerminalAction(mySettingsProvider.getLineDownActionPresentation(), input -> {
                    scrollDown();
                    return true;
                }));
    }

    public void selectAll() {
        var selection = new TerminalSelection(new com.jediterm.core.compatibility.Point(0, -myTerminalTextBuffer.getHistoryLinesCount()),
                new com.jediterm.core.compatibility.Point(myTermSize.getColumns(), myTerminalTextBuffer.getScreenLinesCount()));
        updateSelection(selection, true);
    }

    @NotNull
    private boolean isSelectedTextUrl() {
        String selectionText = getSelectionText();
        if (selectionText != null) {
            try {
                URI uri = new URI(selectionText);
                //noinspection ResultOfMethodCallIgnored
                uri.toURL();
                return true;
            } catch (Exception e) {
                //pass
            }
        }
        return false;
    }

    @Nullable
    private String getSelectionText() {
        if (mySelection.get() != null) {
            Pair<com.jediterm.core.compatibility.Point, com.jediterm.core.compatibility.Point> points = mySelection.get().pointsForRun(myTermSize.getColumns());
            if (points.getFirst() != null || points.getSecond() != null) {
                return SelectionUtil
                        .getSelectionText(points.getFirst(), points.getSecond(), myTerminalTextBuffer);

            }
        }
        return null;
    }

    protected boolean openSelectedTextAsURL() {
        if (FXConst.getHostServices() != null) {
            try {
                String selectionText = getSelectionText();

                if (selectionText != null) {
                    FXConst.getHostServices().showDocument(selectionText);
                }
            } catch (Exception e) {
                //ok then
            }
        }
        return false;
    }

    public void clearBuffer() {
        clearBuffer(true);
    }

    /**
     * @param keepLastLine true to keep last line (e.g. to keep terminal prompt)
     *                     false to clear entire terminal panel (relevant for terminal console)
     */
    protected void clearBuffer(boolean keepLastLine) {
        if (!myTerminalTextBuffer.isUsingAlternateBuffer()) {
            myTerminalTextBuffer.clearHistory();

            if (myCoordsAccessor != null) {
                if (keepLastLine) {
                    if (myCoordsAccessor.getY() > 0) {
                        TerminalLine lastLine = myTerminalTextBuffer.getLine(myCoordsAccessor.getY() - 1);
                        myTerminalTextBuffer.clearScreenBuffer();
                        myCoordsAccessor.setY(0);
                        myCursor.setY(1);
                        myTerminalTextBuffer.addLine(lastLine);
                    }
                } else {
                    myTerminalTextBuffer.clearScreenBuffer();
                    myCoordsAccessor.setX(0);
                    myCoordsAccessor.setY(1);
                    myCursor.setX(0);
                    myCursor.setY(1);
                }
            }

            scrollBar.setValue(scrollBar.getMin());
            updateScrolling(true);

            myClientScrollOrigin = resolveSwingScrollBarValue();
        }
    }

    @Override
    public TerminalActionProvider getNextProvider() {
        return myNextActionProvider;
    }

    @Override
    public void setNextProvider(TerminalActionProvider provider) {
        myNextActionProvider = provider;
    }

    private static final byte ASCII_NUL = 0;

    private static final byte ASCII_ESC = 27;

    private static final byte ASCII_CTRL_C = 0x03;

    private boolean processTerminalKeyPressed(KeyEvent e) {
        if (hasUncommittedChars()) {
            return false;
        }
        try {
            final KeyCode keycode = e.getCode();
            final char keychar = KeyboardUtil.getKeyChar(e);

            // numLock does not change the code sent by keypad VK_DELETE
            // although it send the char '.'
            if (keycode == KeyCode.DELETE && keychar == '.') {
                myTerminalStarter.sendBytes(new byte[]{'.'}, true);
                return true;
            }
            // CTRL + Space is not handled in KeyEvent; handle it manually
            if (keychar == ' ' && e.isControlDown()) {
                myTerminalStarter.sendBytes(new byte[]{ASCII_NUL}, true);
                return true;
            }

            // ESCAPE is not handled in KeyEvent; handle it manually
            if (keycode == KeyCode.ESCAPE) {
                myTerminalStarter.sendBytes(new byte[]{ASCII_ESC}, false);
                return true;
            }

            // CTRL + C is not handled in KeyEvent; handle it manually
            if (keychar == 'c' && e.isControlDown()) {
                myTerminalStarter.sendBytes(new byte[]{ASCII_CTRL_C}, true);
                return true;
            }

            final byte[] code = myTerminalStarter.getTerminal().getCodeForKey(e.getCode().getCode(), getModifiersEx(e));
            if (code != null) {
                myTerminalStarter.sendBytes(code, true);
                if (mySettingsProvider.scrollToBottomOnTyping() && isCodeThatScrolls(keycode)) {
                    scrollToBottom();
                }
                return true;
            }
            if (isAltPressedOnly(e) && Character.isDefined(keychar) && mySettingsProvider.altSendsEscape()) {
                // Cannot use e.getKeyChar() on macOS:
                //  Option+f produces e.getKeyChar()='ƒ' (402), but 'f' (102) is needed.
                //  Option+b produces e.getKeyChar()='∫' (8747), but 'b' (98) is needed.
                myTerminalStarter.sendString(new String(new char[]{ASCII_ESC, simpleMapKeyCodeToChar(e)}), true);
                return true;
            }
            if (Character.isISOControl(keychar)) {// keys filtered out here will be processed in processTerminalKeyTyped
                return processCharacter(e, keychar);
            }
        } catch (Exception ex) {
            JulLog.error("Error sending pressed key to emulator", ex);
        }
        return false;
    }

    private static char simpleMapKeyCodeToChar(@NotNull KeyEvent e) {
        // zsh requires proper case of letter
        if (e.isShiftDown()) {
            return Character.toUpperCase(e.getText().charAt(0));
        }
        return Character.toLowerCase(e.getText().charAt(0));
    }

    private static boolean isAltPressedOnly(@NotNull KeyEvent e) {
        return e.isAltDown() && !e.isControlDown() && !e.isShiftDown();
    }

    private boolean processCharacter(@NotNull KeyEvent e, @NotNull char keyChar) {
        if (isAltPressedOnly(e) && mySettingsProvider.altSendsEscape()) {
            return false;
        }
        final char[] obuffer;
        obuffer = new char[]{keyChar};

        if (keyChar == '`' && e.isMetaDown()) {
            // Command + backtick is a short-cut on Mac OSX, so we shouldn't type anything
            return false;
        }

        myTerminalStarter.sendString(new String(obuffer), true);

        if (mySettingsProvider.scrollToBottomOnTyping()) {
            scrollToBottom();
        }
        return true;
    }

    private static boolean isCodeThatScrolls(KeyCode keycode) {
        return keycode == KeyCode.UP
                || keycode == KeyCode.DOWN
                || keycode == KeyCode.LEFT
                || keycode == KeyCode.RIGHT
                || keycode == KeyCode.BACK_SPACE
                || keycode == KeyCode.INSERT
                || keycode == KeyCode.DELETE
                || keycode == KeyCode.ENTER
                || keycode == KeyCode.HOME
                || keycode == KeyCode.END
                || keycode == KeyCode.PAGE_UP
                || keycode == KeyCode.PAGE_DOWN;
    }

    private boolean processTerminalKeyTyped(KeyEvent e) {
        if (hasUncommittedChars()) {
            return false;
        }
        String character = e.getCharacter();
        if (character == null || character.isEmpty()) {
            return false;
        }

        if (!Character.isISOControl(character.codePointAt(0))) {
            // keys filtered out here will be processed in processTerminalKeyPressed
            try {
                return processCharacter(e, character.charAt(0));
            } catch (Exception ex) {
                JulLog.error("Error sending typed key to emulator", ex);
            }
        }
        return false;
    }

    private class TerminalKeyHandler implements BiConsumer<EventType<KeyEvent>, KeyEvent> {

        private boolean myIgnoreNextKeyTypedEvent;

        public TerminalKeyHandler() {
        }

        public void keyPressed(KeyEvent e) {
            if (e.isConsumed()) {
                return;
            }
            myIgnoreNextKeyTypedEvent = false;
            if (TerminalAction.processEvent(FXTerminalPanel.this, e) || processTerminalKeyPressed(e)) {
                e.consume();
                myIgnoreNextKeyTypedEvent = true;
            }
        }

        public void keyTyped(KeyEvent e) {
            if (e.isConsumed()) {
                return;
            }
            if (myIgnoreNextKeyTypedEvent || processTerminalKeyTyped(e)) {
                e.consume();
            }
        }

        @Override
        public void accept(EventType<KeyEvent> type, KeyEvent keyEvent) {
            if (type == KeyEvent.KEY_PRESSED) {
                this.keyPressed(keyEvent);
            } else if (type == KeyEvent.KEY_TYPED) {
                this.keyTyped(keyEvent);
            }
        }
    }

    private void handlePaste() {
        pasteFromClipboard(false);
    }

    private void handlePasteSelection() {
        pasteFromClipboard(true);
    }

    /**
     * Copies selected text to clipboard.
     *
     * @param unselect                               true to unselect currently selected text
     * @param useSystemSelectionClipboardIfAvailable true to use {@link Toolkit#getSystemSelection()} if available
     */
    public void handleCopy(boolean unselect, boolean useSystemSelectionClipboardIfAvailable) {
        if (mySelection.get() != null) {
            Pair<com.jediterm.core.compatibility.Point, com.jediterm.core.compatibility.Point> points = mySelection.get().pointsForRun(myTermSize.getColumns());
            copySelection(points.getFirst(), points.getSecond(), useSystemSelectionClipboardIfAvailable);
            if (unselect) {
                updateSelection(null, true);
                repaint();
            }
        }
    }

    private boolean handleCopy(@Nullable KeyEvent e) {
        boolean ctrlC = e != null && e.getCode() == KeyCode.C && e.isControlDown() && !e.isAltDown() && !e.isMetaDown() && !e.isShiftDown();
        boolean sendCtrlC = ctrlC && mySelection.get() == null;
        handleCopy(ctrlC, false);
        return !sendCtrlC;
    }

    private void handleCopyOnSelect() {
        handleCopy(false, true);
    }

    /**
     * InputMethod implementation
     * For details read http://docs.oracle.com/javase/7/docs/technotes/guides/imf/api-tutorial.html
     */
    private void processInputMethodEvent(InputMethodEvent e) {
        if (e.getCommitted() == null) {
            return;
        }

        String committedText = e.getCommitted();
        if (!committedText.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (char c : committedText.toCharArray()) {
                if (c >= 0x20 && c != 0x7F) { // Filtering characters
                    sb.append(c);
                }
            }

            if (!sb.isEmpty()) {
                myTerminalStarter.sendString(sb.toString(), true);
            }
        }

        // Handling uncommitted text (in-progress input)
        ObservableList<InputMethodTextRun> composedTextRuns = e.getComposed();
        if (!composedTextRuns.isEmpty()) {
            StringBuilder uncommittedTextBuilder = new StringBuilder();
            for (InputMethodTextRun run : composedTextRuns) {
                uncommittedTextBuilder.append(run.getText()); // Extracting text from the run
            }
            myInputMethodUncommittedChars = uncommittedTextBuilder.toString();
        } else {
            myInputMethodUncommittedChars = null;
        }
    }

    private static String uncommittedChars(@Nullable AttributedCharacterIterator text) {
        if (text == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (char c = text.first(); c != CharacterIterator.DONE; c = text.next()) {
            if (c >= 0x20 && c != 0x7F) { // Hack just like in javax.swing.text.DefaultEditorKit.DefaultKeyTypedAction
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private class MyInputMethodRequests implements InputMethodRequests {

        @Override
        public Point2D getTextLocation(int i) {
            var x = myCursor.getCoordX() * myCharSize.getWidth() + getInsetX();
            var y = (myCursor.getCoordY() + 1) * myCharSize.getHeight();
            var screenBounds = canvas.localToScreen(canvas.getBoundsInLocal());
            double screenX = screenBounds.getMinX();
            double screenY = screenBounds.getMinY();
            //if user enables screen scaling in his operating system we must correct x and y
            Screen screen = resolveScreen();
            var point = new Point2D((x + screenX) * screen.getOutputScaleX(), (y + screenY) * screen.getOutputScaleY());
            return point;
        }

        @Override
        public int getLocationOffset(int i, int i1) {
            return 0;
        }

        @Override
        public void cancelLatestCommittedText() {

        }

        @Override
        public String getSelectedText() {
            return null;
        }

        /**
         * Returns the screen that shows the top left corner of the window.
         *
         * @return
         */
        private Screen resolveScreen() {
            Stage stage = (Stage) canvas.getScene().getWindow();
            Rectangle2D stageBounds = new Rectangle2D(stage.getX(), stage.getY(), 0, 0);
            var screens = Screen.getScreensForRectangle(stageBounds);
            if (!screens.isEmpty()) {
                return screens.get(0);
            } else {
                return Screen.getPrimary();
            }
        }
    }

    public void dispose() {
        myRepaintTimer.stop();
    }

    private static int getModifiersEx(KeyEvent event) {
        int modifiers = 0;
        if (event.isShiftDown()) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if (event.isControlDown()) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if (event.isAltDown()) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
        }
        if (event.isMetaDown()) {
            modifiers |= InputEvent.META_DOWN_MASK;
        }
        return modifiers;
    }

    private void updateSelection(TerminalSelection selection, boolean updateSelectedText) {
        this.updateSelectedText = updateSelectedText;
        //change listener -> updateSelectedText()
        mySelection.set(selection);
    }

    private void updateSelectedText() {
        if (this.updateSelectedText || mySelection.get() == null) {
            selectedText.set(getSelectionText());
        }
        this.updateSelectedText = true;
    }

    public Dimension2D getCharSize() {
        return myCharSize;
    }

    private Point2D createPoint(MouseEvent e) {
        return new Point2D(e.getX(), e.getY());
    }

    private Point2D createPoint(ScrollEvent e) {
        return new Point2D(e.getX(), e.getY());
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        this.canvas.requestFocus();
    }

    private int getUnitsToScroll(ScrollEvent event) {
        // Assume that each scroll unit corresponds to 40.0 pixels, which is a typical value.
        int unitsToScroll = (int) Math.round(event.getDeltaY() / 40.0);
        return unitsToScroll * -1;
    }

    private void setScrollBarRangeProperties(int value, int extent, int min, int max) {
        if (this.scrollBar == null) {
            return;
        }
        this.scrollBar.setVisibleAmount(extent);
        this.scrollBar.setMin(min);
        this.scrollBar.setMax(max);
        //value is updated in the end, because we have listener on value.
        this.scrollBar.setValue(value);
    }
}
