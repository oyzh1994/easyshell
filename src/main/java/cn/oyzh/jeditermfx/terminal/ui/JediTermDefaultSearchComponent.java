package cn.oyzh.jeditermfx.terminal.ui;

import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.SubstringFinder;
import com.jediterm.terminal.ui.FXJediTermWidget;
import com.jediterm.terminal.ui.JediTermSearchComponentListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class JediTermDefaultSearchComponent extends FXHBox implements JediTermSearchComponent {

    private JediTermSearchComponentListener listener;

    public JediTermDefaultSearchComponent(FXJediTermWidget jediTermWidget) {
        ClearableTextField textField = new ClearableTextField();
        textField.setId("text");
        Label label = new Label();
        label.setId("label");
        CheckBox ignoreCase = new CheckBox(I18nHelper.ignoreCase());
        ignoreCase.setId("ignoreCase");
        ignoreCase.setSelected(true);
        Button next = new Button("▼");
        next.setOnAction(e -> {
//            onResultUpdated(jediTermWidget.getTerminalPanel().selectNextFindResultItem());
            if (this.listener != null) {
                this.listener.selectNextFindResult();
            }
        });
        Button prev = new Button("▲");
        prev.setOnAction(e -> {
//            onResultUpdated(jediTermWidget.getTerminalPanel().selectPrevFindResultItem());
            if (this.listener != null) {
                this.listener.selectPrevFindResult();
            }
        });
        Button close = new Button("✕");
        close.setOnAction(e -> this.setVisible(false));
        var charSize = jediTermWidget.getTerminalPanel().getCharSize();
        textField.setPromptText(I18nHelper.pleaseInputContent());
        HBox.setHgrow(textField, Priority.ALWAYS);
        this.setMaxSize(charSize.getWidth() * 60, 30);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle("-fx-background-color: -fx-background");
        updateLabel(null);
        HBox.setMargin(ignoreCase, new Insets(0, 5, 0, 5));
        HBox.setMargin(label, new Insets(0, 5, 0, 5));
        HBox.setMargin(next, new Insets(0, 0, 0, 5));
        HBox.setMargin(prev, new Insets(0, 0, 0, 3));
        HBox.setMargin(close, new Insets(0, 5, 0, 3));
        this.focusedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                textField.requestFocus();
            }
        });
        this.addChild(textField);
        this.addChild(ignoreCase);
        this.addChild(label);
        this.addChild(next);
        this.addChild(prev);
        this.addChild(close);
    }

    public TextField getTextField() {
        return (TextField) this.lookup("#text");
    }

    public Label getLabel() {
        return (Label) this.lookup("#label");
    }

    public CheckBox getIgnoreCase() {
        return (CheckBox) this.lookup("#ignoreCase");
    }

    private void updateLabel(@Nullable SubstringFinder.FindResult result) {
        Label label = this.getLabel();
        if (label == null) {
            return;
        }
        if (result == null) {
            label.setText("");
        } else if (!result.getItems().isEmpty()) {
            SubstringFinder.FindResult.FindItem selectedItem = result.selectedItem();
            label.setText(selectedItem.getIndex() + " of " + result.getItems().size());
        }
    }

    @Override
    public @NotNull HBox getComponent() {
        return this;
    }

    @Override
    public void addListener(@NotNull JediTermSearchComponentListener listener) {
        this.listener = listener;
        this.visibleProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                listener.hideSearchComponent();
            }
        });
        this.getTextField().textProperty().addListener((observableValue, s, t1) -> {
            listener.searchSettingsChanged(t1, this.getIgnoreCase().isSelected());
        });
        this.getIgnoreCase().selectedProperty().addListener((observableValue, s, t1) -> {
            listener.searchSettingsChanged(this.getTextField().getText(), t1);
        });
    }

    @Override
    public void addKeyListener(@NotNull BiConsumer<EventType<KeyEvent>, KeyEvent> listener) {
        this.getTextField().addEventFilter(KeyEvent.ANY, e -> {
            listener.accept(e.getEventType(), e);
        });
    }

    public void onResultUpdated(SubstringFinder.FindResult results) {
        this.updateLabel(results);
    }

    @Override
    public void requestFocus() {
        this.getTextField().requestFocus();
    }
}
