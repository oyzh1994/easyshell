package cn.oyzh.jeditermfx.terminal.ui;

import com.jediterm.terminal.SubstringFinder;
import com.jediterm.terminal.ui.JediTermSearchComponentListener;
import javafx.event.EventType;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;


public interface JediTermSearchComponent {
    @NotNull Pane getComponent();

    void addListener(@NotNull JediTermSearchComponentListener listener);

    void addKeyListener(@NotNull BiConsumer<EventType<KeyEvent>, KeyEvent> listener);

    void onResultUpdated(@Nullable SubstringFinder.FindResult results);
}
