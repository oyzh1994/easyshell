//package cn.oyzh.jeditermfx.terminal.model;
//
//import cn.oyzh.jeditermfx.terminal.ui.settings.SettingsProvider;
//import com.jediterm.terminal.Terminal;
//import com.jediterm.terminal.model.JediTermTypeAheadModel;
//import com.jediterm.terminal.model.TerminalTextBuffer;
//import com.jediterm.terminal.model.TerminalTypeAheadSettings;
//import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
//import org.jetbrains.annotations.NotNull;
//
//
//public class FXJediTermTypeAheadModel extends JediTermTypeAheadModel {
//
//    public FXJediTermTypeAheadModel(@NotNull Terminal terminal, @NotNull TerminalTextBuffer textBuffer,
//                                    @NotNull SettingsProvider settingsProvider) {
//        super(terminal, textBuffer, new DefaultSettingsProvider() {
//            @Override
//            public @NotNull TerminalTypeAheadSettings getTypeAheadSettings() {
//                return settingsProvider.getTypeAheadSettings();
//            }
//        });
//    }
//}
