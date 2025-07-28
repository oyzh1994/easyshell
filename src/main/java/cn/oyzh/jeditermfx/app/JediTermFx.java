// package cn.oyzh.jeditermfx.app;
//
// import cn.oyzh.common.log.JulLog;
// import cn.oyzh.common.system.OSUtil;
// import cn.oyzh.jeditermfx.app.debug.TerminalDebugUtil;
// import cn.oyzh.jeditermfx.app.pty.LoggingTtyConnector;
// import cn.oyzh.jeditermfx.app.pty.PtyProcessTtyConnector;
// import com.jediterm.terminal.ui.FXHyperlinkFilter;
// import com.jediterm.terminal.TtyConnector;
// import com.jediterm.terminal.model.TerminalTextBuffer;
// import com.jediterm.terminal.ui.FXJediTermWidget;
// import com.jediterm.terminal.ui.settings.SettingsProvider;
// import com.pty4j.PtyProcess;
// import com.pty4j.PtyProcessBuilder;
// import kotlin.collections.ArraysKt;
// import kotlin.jvm.internal.Intrinsics;
// import kotlin.text.Charsets;
// import org.jetbrains.annotations.NotNull;
// import org.jetbrains.annotations.Nullable;
//
// import java.io.IOException;
// import java.nio.charset.Charset;
// import java.nio.charset.StandardCharsets;
// import java.nio.file.Path;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
//
// public final class JediTermFx extends AbstractTerminalApplication {
//
//     @NotNull
//     @Override
//     public TtyConnector createTtyConnector() {
//         try {
//             var envs = configureEnvironmentVariables();
//             String[] command;
//             if (OSUtil.isWindows()) {
// //                command = new String[]{"cmd.exe"};
//                 command = new String[]{"powershell.exe"};
//             } else {
//                 String shell = (String) envs.get("SHELL");
//                 if (shell == null) {
//                     shell = "/bin/bash";
//                 }
//                 if (OSUtil.isMacOS()) {
//                     command = new String[]{shell, "--login"};
//                 } else {
//                     command = new String[]{shell};
//                 }
//             }
//             var workingDirectory = Path.of(".").toAbsolutePath().normalize().toString();
//             if (JulLog.isInfoEnabled()) {
//                 JulLog.info("Starting {} in {}", String.join(" ", command), workingDirectory);
//             }
//             var process = new PtyProcessBuilder()
//                     .setDirectory(workingDirectory)
//                     .setInitialColumns(120)
//                     .setInitialRows(20)
//                     .setCommand(command)
//                     .setEnvironment(envs)
//                     .setConsole(false)
//                     .setUseWinConPty(true)
//                     .start();
//             return new LoggingPtyProcessTtyConnector(process, StandardCharsets.UTF_8, Arrays.asList(command));
//         } catch (Exception e) {
//             throw new IllegalStateException(e);
//         }
//     }
//
//     private final Map<String, String> configureEnvironmentVariables() {
//         HashMap envs = new HashMap<String, String>(System.getenv());
//         if (OSUtil.isMacOS()) {
//             envs.put("LC_CTYPE", Charsets.UTF_8.name());
//         }
//         if (!OSUtil.isWindows()) {
//             envs.put("TERM", "xterm-256color");
//         }
//         return envs;
//     }
//
//     @NotNull
//     protected FXJediTermWidget createTerminalWidget(@NotNull SettingsProvider settingsProvider) {
//         Intrinsics.checkNotNullParameter(settingsProvider, "settingsProvider");
//         FXJediTermWidget widget = new FXJediTermWidget(settingsProvider);
//         widget.addHyperlinkFilter(new FXHyperlinkFilter());
//         return widget;
//     }
//
//     public static final class LoggingPtyProcessTtyConnector extends PtyProcessTtyConnector
//             implements LoggingTtyConnector {
//
//         private final int MAX_LOG_SIZE = 200;
//
//         @NotNull
//         private final LinkedList<char[]> myDataChunks = new LinkedList<>();
//
//         @NotNull
//         private final LinkedList<TerminalState> myStates = new LinkedList<>();
//
//         @Nullable
//         private FXJediTermWidget myWidget;
//
//         private int logStart;
//
//         public LoggingPtyProcessTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset, @NotNull List command) {
//             super(process, charset, command);
//             Intrinsics.checkNotNullParameter(process, "process");
//             Intrinsics.checkNotNullParameter(charset, "charset");
//             Intrinsics.checkNotNullParameter(command, "command");
//         }
//
//         @Override
//         public int read(@NotNull char[] buf, int offset, int length) throws IOException {
//             Intrinsics.checkNotNullParameter(buf, "buf");
//             int len = super.read(buf, offset, length);
//             if (len > 0) {
//                 char[] arr = ArraysKt.copyOfRange(buf, offset, len);
//                 this.myDataChunks.add(arr);
//                 Intrinsics.checkNotNull(this.myWidget);
//                 TerminalTextBuffer terminalTextBuffer = this.myWidget.getTerminalTextBuffer();
//                 String lines = terminalTextBuffer.getScreenLines();
//                 Intrinsics.checkNotNull(terminalTextBuffer);
//                 TerminalState terminalState =
//                         new TerminalState(lines, TerminalDebugUtil.getStyleLines(terminalTextBuffer),
//                                 terminalTextBuffer.getHistoryBuffer().getLines());
//                 this.myStates.add(terminalState);
//                 if (this.myDataChunks.size() > this.MAX_LOG_SIZE) {
//                     this.myDataChunks.removeFirst();
//                     this.myStates.removeFirst();
//                     this.logStart++;
//                 }
//             }
//             return len;
//         }
//
//         @NotNull
//         @Override
//         public List<char[]> getChunks() {
//             return new ArrayList(this.myDataChunks);
//         }
//
//         @NotNull
//         @Override
//         public List<TerminalState> getStates() {
//             return new ArrayList(this.myStates);
//         }
//
//         @Override
//         public int getLogStart() {
//             return this.logStart;
//         }
//
//         @Override
//         public void write(@NotNull String string) throws IOException {
//             Intrinsics.checkNotNullParameter(string, "string");
//             if(JulLog.isDebugEnabled()) {
//                 JulLog.debug("Writing in OutputStream : " + string);
//             }
//             super.write(string);
//         }
//
//         @Override
//         public void write(@NotNull byte[] bytes) throws IOException {
//             Intrinsics.checkNotNullParameter(bytes, "bytes");
//             if(JulLog.isDebugEnabled()) {
//                 JulLog.debug("Writing in OutputStream : " + Arrays.toString(bytes) + " " + new String(bytes, Charsets.UTF_8));
//             }
//             super.write(bytes);
//         }
//
//         public final void setWidget(@NotNull FXJediTermWidget widget) {
//             Intrinsics.checkNotNullParameter(widget, "widget");
//             this.myWidget = widget;
//         }
//     }
// }
