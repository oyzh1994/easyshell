package com.jediterm.terminal.ui;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.controls.pane.FXStackPane;
import cn.oyzh.fx.plus.util.FXUtil;
import com.jediterm.core.typeahead.TerminalTypeAheadManager;
import com.jediterm.core.typeahead.TypeAheadTerminalModel;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.SubstringFinder;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalExecutorServiceManager;
import com.jediterm.terminal.TerminalMode;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TtyBasedArrayDataStream;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.JediTermDebouncerImpl;
import com.jediterm.terminal.model.JediTermTypeAheadModel;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.hyperlinks.AsyncHyperlinkFilter;
import com.jediterm.terminal.model.hyperlinks.HyperlinkFilter;
import com.jediterm.terminal.model.hyperlinks.TextProcessing;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * JediTermFX terminal widget with UI implemented in JavaFX.
 */
public class FXJediTermWidget extends FXStackPane implements TerminalSession, FXTerminalWidget, TerminalActionProvider {

    protected final FXTerminalPanel myTerminalPanel;

    private final ScrollBar myScrollBar;

    protected final JediTerminal myTerminal;

    private final AtomicReference<Session> myRunningSession = new AtomicReference<>();

    private final JediTermTypeAheadModel myTypeAheadTerminalModel;

    private final TerminalTypeAheadManager myTypeAheadManager;

    private FXJediTermSearchComponent myFindComponent;

    @SuppressWarnings("removal")
    private final FXPreConnectHandler myPreConnectHandler;

    private TtyConnector myTtyConnector;

    private TerminalStarter myTerminalStarter;

    private final CompletableFuture<TerminalStarter> myTerminalStarterFuture = new CompletableFuture<>();

    protected final SettingsProvider mySettingsProvider;

    private TerminalActionProvider myNextActionProvider;

//    private final StackPane myInnerPanel;

    private final TextProcessing myTextProcessing;

    private final List<FXTerminalWidgetListener> myListeners = new CopyOnWriteArrayList<>();

    private final Object myExecutorServiceManagerLock = new Object();

    private volatile TerminalExecutorServiceManager myExecutorServiceManager;

    public FXJediTermWidget(@NotNull SettingsProvider settingsProvider) {
        this(80, 24, settingsProvider);
    }

    public FXJediTermWidget(int columns, int lines, SettingsProvider settingsProvider) {

        mySettingsProvider = settingsProvider;

        StyleState styleState = createDefaultStyle();

        myTextProcessing = new TextProcessing(settingsProvider.getHyperlinkColor(),
                settingsProvider.getHyperlinkHighlightingMode());

        TerminalTextBuffer terminalTextBuffer = new TerminalTextBuffer(columns, lines, styleState, settingsProvider.getBufferMaxLinesCount(), myTextProcessing);
        myTextProcessing.setTerminalTextBuffer(terminalTextBuffer);

        myTerminalPanel = createTerminalPanel(mySettingsProvider, styleState, terminalTextBuffer);
        myTerminal = createTerminal(myTerminalPanel, terminalTextBuffer, styleState);

        myTypeAheadTerminalModel = new JediTermTypeAheadModel(myTerminal, terminalTextBuffer, settingsProvider);
        myTypeAheadManager = new TerminalTypeAheadManager(myTypeAheadTerminalModel);
        JediTermDebouncerImpl typeAheadDebouncer =
                new JediTermDebouncerImpl(myTypeAheadManager::debounce, TerminalTypeAheadManager.MAX_TERMINAL_DELAY, getExecutorServiceManager());
        myTypeAheadManager.setClearPredictionsDebouncer(typeAheadDebouncer);
        myTerminalPanel.setTypeAheadManager(myTypeAheadManager);
        myTypeAheadTerminalModel.addTypeAheadModelListener(myTerminalPanel::repaint);

        myTerminal.setModeEnabled(TerminalMode.AltSendsEscape, mySettingsProvider.altSendsEscape());

        myTerminalPanel.addTerminalMouseListener(myTerminal);
        myTerminalPanel.setNextProvider(this);
        myTerminalPanel.setCoordAccessor(myTerminal);

        myPreConnectHandler = createPreConnectHandler(myTerminal);
        myTerminalPanel.addCustomKeyListener(myPreConnectHandler);
        myScrollBar = createScrollBar();

        this.setFocusTraversable(false);
        VBox.setVgrow(myTerminalPanel, Priority.ALWAYS);
        this.addChild(myTerminalPanel);

        myScrollBar.setOrientation(Orientation.VERTICAL);

        myTerminalPanel.init(myScrollBar);
    }

    @Override
    public Node getPreferredFocusableNode() {
        return this.myTerminalPanel;
    }

    protected ScrollBar createScrollBar() {
        ScrollBar scrollBar = new ScrollBar();
        return scrollBar;
    }

    protected StyleState createDefaultStyle() {
        StyleState styleState = new StyleState();
        styleState.setDefaultStyle(mySettingsProvider.getDefaultStyle());
        return styleState;
    }

    protected FXTerminalPanel createTerminalPanel(@NotNull SettingsProvider settingsProvider, @NotNull StyleState styleState, @NotNull TerminalTextBuffer terminalTextBuffer) {
        return new FXTerminalPanel(settingsProvider, terminalTextBuffer, styleState);
    }

    protected @NotNull JediTerminal createTerminal(@NotNull TerminalDisplay display,
                                                   @NotNull TerminalTextBuffer textBuffer,
                                                   @NotNull StyleState initialStyleState) {
        return new JediTerminal(display, textBuffer, initialStyleState);
    }

    @SuppressWarnings({"removal", "DeprecatedIsStillUsed"})
    @Deprecated(forRemoval = true)
    private FXPreConnectHandler createPreConnectHandler(JediTerminal terminal) {
        return new FXPreConnectHandler(terminal);
    }

    public TerminalDisplay getTerminalDisplay() {
        return getTerminalPanel();
    }

    public FXTerminalPanel getTerminalPanel() {
        return myTerminalPanel;
    }

    public final @NotNull TerminalExecutorServiceManager getExecutorServiceManager() {
        TerminalExecutorServiceManager manager = myExecutorServiceManager;
        if (manager != null) return manager;
        synchronized (myExecutorServiceManagerLock) {
            manager = myExecutorServiceManager;
            if (manager == null) {
                manager = createExecutorServiceManager();
                myExecutorServiceManager = manager;
            }
            return manager;
        }
    }

    protected @NotNull TerminalExecutorServiceManager createExecutorServiceManager() {
        return new JediTermExecutorServiceManager();
    }

    @SuppressWarnings("unused")
    public TerminalTypeAheadManager getTypeAheadManager() {
        return myTypeAheadManager;
    }

    public void setTtyConnector(@NotNull TtyConnector ttyConnector) {
        myTtyConnector = ttyConnector;

        TypeAheadTerminalModel.ShellType shellType;
        if (ttyConnector instanceof ProcessTtyConnector) {
            List<String> commandLine = ((ProcessTtyConnector) myTtyConnector).getCommandLine();
            shellType = TypeAheadTerminalModel.commandLineToShellType(commandLine);
        } else {
            shellType = TypeAheadTerminalModel.ShellType.Unknown;
        }
        myTypeAheadTerminalModel.setShellType(shellType);
        myTerminalStarter = createTerminalStarter(myTerminal, myTtyConnector);
        myTerminalStarterFuture.complete(myTerminalStarter);
        myTerminalPanel.setTerminalStarter(myTerminalStarter);
    }

    protected TerminalStarter createTerminalStarter(@NotNull JediTerminal terminal, @NotNull TtyConnector connector) {
        return new TerminalStarter(terminal, connector,
                new TtyBasedArrayDataStream(connector, myTypeAheadManager::onTerminalStateChanged), myTypeAheadManager, getExecutorServiceManager());
    }

    @Override
    public TtyConnector getTtyConnector() {
        return myTtyConnector;
    }

    @Override
    public Terminal getTerminal() {
        return myTerminal;
    }

    public void start() {
        synchronized (myRunningSession) {
            if (myRunningSession.get() == null) {
                EmulatorTask task = new EmulatorTask(() -> {
                    synchronized (myRunningSession) {
                        myRunningSession.set(null);
                    }
                });
                Future<?> future = getExecutorServiceManager().getUnboundedExecutorService().submit(task);
                myRunningSession.set(new Session(task, future));
            } else {
                JulLog.error("Should not try to start session again at this point... ");
            }
        }
    }

    /**
     * @deprecated use {@link #close()} instead
     */
    @Deprecated
    public void stop() {
        stopRunningSession();
    }

    private void stopRunningSession() {
        Session session = myRunningSession.get();
        if (session != null) {
            session.stop();
        }
    }

    public boolean isSessionRunning() {
        return myRunningSession.get() != null;
    }

    @Override
    public TerminalTextBuffer getTerminalTextBuffer() {
        return myTerminalPanel.getTerminalTextBuffer();
    }

    @Override
    public boolean requestFocusInWindow() {
        myTerminalPanel.requestFocus();
        return true;
    }

    @Override
    public void requestFocus() {
        myTerminalPanel.requestFocus();
    }

    public boolean canOpenSession() {
        return !isSessionRunning();
    }

    @Override
    public FXJediTermWidget createTerminalSession(TtyConnector ttyConnector) {
        setTtyConnector(ttyConnector);
        return this;
    }

    @Override
    public Pane getComponent() {
        return this;
    }

    @Override
    public void close() {
        try {
            stopRunningSession();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (myTerminalStarter != null) {
            try {
                myTerminalStarter.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            myTerminalPanel.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            getExecutorServiceManager().shutdownWhenAllExecuted();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 额外代码，清除监听器
        this.myListeners.clear();
    }

    @Override
    public List<TerminalAction> getActions() {
        return List.of(new FXTerminalAction((FXTerminalActionPresentation) mySettingsProvider.getFindActionPresentation(),
                keyEvent -> {
                    showFindText();
                    return true;
                }).withMnemonicKey(KeyCode.F));
    }

    private void showFindText() {
        if (myFindComponent == null) {
            myFindComponent = createSearchComponent();

            final Pane component = myFindComponent.getComponent();
            this.addChild(component);
            StackPane.setAlignment(component, Pos.TOP_RIGHT);
            ScrollBar scrollBar = (ScrollBar) myTerminalPanel.getChildren().get(1);
            StackPane.setMargin(component, new Insets(0, scrollBar.getWidth(), 0, 0));
            component.requestFocus();

            JediTermSearchComponentListener listener = new JediTermSearchComponentListener() {
                @Override
                public void searchSettingsChanged(@NotNull String textToFind, boolean ignoreCase) {
                    findText(textToFind, ignoreCase);
                }

                @Override
                public void hideSearchComponent() {
                    component.getChildren().clear();
                    removeChild(component);
                    myFindComponent = null;
                    myTerminalPanel.setFindResult(null);
                    myTerminalPanel.requestFocus();
                }

                @Override
                public void selectNextFindResult() {
                    myFindComponent.onResultUpdated(myTerminalPanel.selectNextFindResultItem());
                }

                @Override
                public void selectPrevFindResult() {
                    myFindComponent.onResultUpdated(myTerminalPanel.selectPrevFindResultItem());
                }
            };
            myFindComponent.addListener(listener);

            myFindComponent.addKeyListener((t, e) -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    listener.hideSearchComponent();
                } else if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.DOWN) {
                    listener.selectNextFindResult();
                } else if (e.getCode() == KeyCode.UP) {
                    listener.selectPrevFindResult();
                }
            });
        } else {
            myFindComponent.getComponent().requestFocus();
        }
    }

    protected @NotNull FXJediTermSearchComponent createSearchComponent() {
        return new FXJediTermDefaultSearchComponent(this);
    }

    private void findText(String text, boolean ignoreCase) {
        SubstringFinder.FindResult results = TerminalSearchUtil.searchInTerminalTextBuffer(getTerminalTextBuffer(), text, ignoreCase);
        myTerminalPanel.setFindResult(results);
        myFindComponent.onResultUpdated(results);
        myScrollBar.requestLayout();
    }

    @Override
    public TerminalActionProvider getNextProvider() {
        return myNextActionProvider;
    }

    public void setNextProvider(TerminalActionProvider actionProvider) {
        this.myNextActionProvider = actionProvider;
    }

    private static class Session {

        private final EmulatorTask myEmulatorTask;

        private final Future<?> mySessionFuture;

        public Session(@NotNull EmulatorTask emulatorTask, @NotNull Future<?> sessionFuture) {
            myEmulatorTask = emulatorTask;
            mySessionFuture = sessionFuture;
        }

        void stop() {
            myEmulatorTask.requestStop();
            mySessionFuture.cancel(true);
        }
    }

    private class EmulatorTask implements Runnable {

        private final TerminalStarter myStarter;

        private final Runnable myOnDone;

        public EmulatorTask(@NotNull Runnable onDone) {
            myStarter = Objects.requireNonNull(myTerminalStarter);
            myOnDone = onDone;
        }

        @SuppressWarnings("removal")
        public void run() {
            TtyConnector ttyConnector = myStarter.getTtyConnector();
            try {
                if (ttyConnector.init(myPreConnectHandler)) {
                    myTerminalPanel.addCustomKeyListener(myTerminalPanel.getTerminalKeyListener());
                    myTerminalPanel.removeCustomKeyListener(myPreConnectHandler);
                    myStarter.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JulLog.error("Exception running terminal", e);
            } finally {
                try {
                    ttyConnector.close();
                } catch (Exception ignored) {
                }
                try {
                    for (FXTerminalWidgetListener listener : myListeners) {
                        listener.allSessionsClosed(FXJediTermWidget.this);
                    }
                } catch (Exception e) {
                    JulLog.error("Unhandled exception when closing terminal", e);
                }
                try {
                    myOnDone.run();
                } catch (Exception e) {
                    JulLog.error("Unhandled exception when closing terminal", e);
                }
            }
        }

        void requestStop() {
            myStarter.requestEmulatorStop();
        }
    }

    /**
     * @deprecated use {@link #getTtyConnector()} to figure out if session started
     */
    @Deprecated
    public @Nullable TerminalStarter getTerminalStarter() {
        return myTerminalStarter;
    }

    protected void doWithTerminalStarter(@NotNull Consumer<TerminalStarter> consumer) {
        myTerminalStarterFuture.thenAccept(consumer);
    }

    public void addHyperlinkFilter(HyperlinkFilter filter) {
        myTextProcessing.addHyperlinkFilter(filter);
    }

    public void addAsyncHyperlinkFilter(@NotNull AsyncHyperlinkFilter filter) {
        myTextProcessing.addAsyncHyperlinkFilter(filter);
    }

    @Override
    public void addListener(FXTerminalWidgetListener listener) {
        myListeners.add(listener);
    }

    @Override
    public void removeListener(FXTerminalWidgetListener listener) {
        myListeners.remove(listener);
    }
}
