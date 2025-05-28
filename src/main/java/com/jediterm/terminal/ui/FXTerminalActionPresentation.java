package com.jediterm.terminal.ui;

import javafx.scene.input.KeyCombination;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FXTerminalActionPresentation extends TerminalActionPresentation {

    private final List<KeyCombination> myKeyCombinations;

    public FXTerminalActionPresentation(@NotNull String name, @NotNull KeyCombination keyCombination) {
        this(name, List.of(keyCombination));
    }

    public FXTerminalActionPresentation(@NotNull String name, @NotNull List<KeyCombination> keyCombinations) {
        super(name, Collections.emptyList());
        myKeyCombinations = keyCombinations;
    }

    public @NotNull List<KeyCombination> getKeyCombinations() {
        return myKeyCombinations;
    }
}
