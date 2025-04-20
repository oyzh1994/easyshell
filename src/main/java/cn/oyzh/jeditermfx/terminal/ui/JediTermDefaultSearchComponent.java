package cn.oyzh.jeditermfx.terminal.ui;

import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.SubstringFinder;
import com.jediterm.terminal.ui.FXJediTermWidget;
import com.jediterm.terminal.ui.JediTermSearchComponentListener;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public final class JediTermDefaultSearchComponent extends FXHBox implements JediTermSearchComponent {

    private final ClearableTextField myTextField = new ClearableTextField();
    private final FXLabel label = new FXLabel();
    private final FXCheckBox ignoreCaseCheckBox = new FXCheckBox(I18nHelper.ignoreCase(), true);
    private final List<JediTermSearchComponentListener> myListeners = new CopyOnWriteArrayList<>();
    private final JediTermSearchComponentListener myMulticaster = createMulticaster();

    public JediTermDefaultSearchComponent(FXJediTermWidget jediTermWidget) {
        Button next = createNextButton();
        next.setOnAction(e -> this.myMulticaster.selectNextFindResult());

        Button prev = createPrevButton();
        prev.setOnAction(e -> this.myMulticaster.selectPrevFindResult());

        Button close = new Button("✕");
        close.setPrefHeight(24);
        close.setOnAction(e -> this.setVisible(false));

        label.setPrefHeight(24);
        this.setMaxSize(480, 30);
        myTextField.setPromptText(I18nHelper.pleaseInputContent());
        myTextField.setEditable(true);
        myTextField.setMaxHeight(24);
        myTextField.setMaxWidth(220);

        updateLabel(null);

        this.getChildren().add(myTextField);
        listenForChanges();
        this.getChildren().add(ignoreCaseCheckBox);
        this.getChildren().add(label);
        this.getChildren().add(next);
        this.getChildren().add(prev);
        this.getChildren().add(close);

        this.setOpaque(true);

        HBox.setHgrow(myTextField, Priority.ALWAYS);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle("-fx-background-color: -fx-background");

        Insets insets1 = new Insets(3, 0, 0, 3);
        FXHBox.setMargin(myTextField, new Insets(3, 0, 0, 0));
        FXHBox.setMargin(ignoreCaseCheckBox, insets1);
        FXHBox.setMargin(label, insets1);
        FXHBox.setMargin(next, insets1);
        FXHBox.setMargin(prev, insets1);
        FXHBox.setMargin(close, insets1);
        this.focusedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                myTextField.requestFocus();
            }
        });
        this.visibleProperty().addListener((ov, oldV, newV) -> {
            if (!newV) {
                this.myMulticaster.hideSearchComponent();
            }
        });
    }

    private void listenForChanges() {
        Runnable settingsChanged = () -> {
            myMulticaster.searchSettingsChanged(myTextField.getText(), ignoreCaseCheckBox.isSelected());
        };
        myTextField.addTextChangeListener((observableValue, s, t1) -> settingsChanged.run());
        ignoreCaseCheckBox.selectedChanged((o, n, t1) -> settingsChanged.run());
    }

    private Button createNextButton() {
        Button button = new Button("▼");
        button.setPrefHeight(24);
        return button;
    }

    private Button createPrevButton() {
        Button button = new Button("▲");
        button.setPrefHeight(24);
        return button;
    }

    private void updateLabel(@Nullable SubstringFinder.FindResult result) {
        if (result == null || result.getItems().isEmpty()) {
            label.text("");
        } else {
            SubstringFinder.FindResult.FindItem selectedItem = result.selectedItem();
            label.text(selectedItem.getIndex() + " of " + result.getItems().size());
        }
    }

    @Override
    public void onResultUpdated(SubstringFinder.FindResult results) {
        this.updateLabel(results);
    }

    @Override
    public @NotNull Pane getComponent() {
        return this;
    }

    @Override
    public void addListener(@NotNull JediTermSearchComponentListener listener) {
        myListeners.add(listener);
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        this.myTextField.requestFocus();
    }

    @Override
    public void addKeyListener(@NotNull BiConsumer<EventType<KeyEvent>, KeyEvent> listener) {
        this.myTextField.addEventFilter(KeyEvent.ANY, e -> listener.accept(e.getEventType(), e));
    }

    private @NotNull JediTermSearchComponentListener createMulticaster() {
        final Class<JediTermSearchComponentListener> listenerClass = JediTermSearchComponentListener.class;
        return (JediTermSearchComponentListener) Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class[]{listenerClass}, (object, method, params) -> {
            for (JediTermSearchComponentListener listener : myListeners) {
                method.invoke(listener, params);
            }
            //noinspection SuspiciousInvocationHandlerImplementation
            return null;
        });
    }
}
