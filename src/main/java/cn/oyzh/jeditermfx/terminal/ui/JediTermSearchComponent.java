package cn.oyzh.jeditermfx.terminal.ui;

import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.jeditermfx.terminal.SubstringFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface JediTermSearchComponent {
  @NotNull JComponent getComponent();

  void addListener(@NotNull JediTermSearchComponentListener listener);

  void addKeyListener(@NotNull KeyListener listener);

  void onResultUpdated(@Nullable SubstringFinder.FindResult results);
}
