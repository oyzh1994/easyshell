//package cn.oyzh.jeditermfx.terminal;
//
//import cn.oyzh.common.log.JulLog;
//import cn.oyzh.common.system.OSUtil;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.awt.*;
//import java.awt.datatransfer.Clipboard;
//import java.awt.datatransfer.ClipboardOwner;
//import java.awt.datatransfer.DataFlavor;
//import java.awt.datatransfer.StringSelection;
//import java.awt.datatransfer.Transferable;
//
//public class DefaultTerminalCopyPasteHandler implements TerminalCopyPasteHandler, ClipboardOwner {
//
//    @Override
//    public void setContents(@NotNull String text, boolean useSystemSelectionClipboardIfAvailable) {
//        if (useSystemSelectionClipboardIfAvailable) {
//            Clipboard systemSelectionClipboard = getSystemSelectionClipboard();
//            if (systemSelectionClipboard != null) {
//                setClipboardContents(new StringSelection(text), systemSelectionClipboard);
//                return;
//            }
//        }
//        setSystemClipboardContents(text);
//    }
//
//    @Nullable
//    @Override
//    public String getContents(boolean useSystemSelectionClipboardIfAvailable) {
//        if (useSystemSelectionClipboardIfAvailable) {
//            Clipboard systemSelectionClipboard = getSystemSelectionClipboard();
//            if (systemSelectionClipboard != null) {
//                return getClipboardContents(systemSelectionClipboard);
//            }
//        }
//        return getSystemClipboardContents();
//    }
//
//    @SuppressWarnings("WeakerAccess")
//    protected void setSystemClipboardContents(@NotNull String text) {
//        setClipboardContents(new StringSelection(text), getSystemClipboard());
//    }
//
//    @Nullable
//    private String getSystemClipboardContents() {
//        return getClipboardContents(getSystemClipboard());
//    }
//
//    private void setClipboardContents(@NotNull Transferable contents, @Nullable Clipboard clipboard) {
//        if (clipboard != null) {
//            try {
//                clipboard.setContents(contents, this);
//            } catch (IllegalStateException e) {
//                logException("Cannot set contents", e);
//            }
//        }
//    }
//
//    @Nullable
//    private String getClipboardContents(@Nullable Clipboard clipboard) {
//        if (clipboard != null) {
//            try {
//                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
//                    return (String) clipboard.getData(DataFlavor.stringFlavor);
//                }
//            } catch (Exception e) {
//                logException("Cannot get clipboard contents", e);
//            }
//        }
//        return null;
//    }
//
//    @Nullable
//    private static Clipboard getSystemClipboard() {
//        try {
//            return Toolkit.getDefaultToolkit().getSystemClipboard();
//        } catch (IllegalStateException e) {
//            logException("Cannot get system clipboard", e);
//            return null;
//        }
//    }
//
//    @Nullable
//    private static Clipboard getSystemSelectionClipboard() {
//        try {
//            return Toolkit.getDefaultToolkit().getSystemSelection();
//        } catch (IllegalStateException e) {
//            logException("Cannot get system selection clipboard", e);
//            return null;
//        }
//    }
//
//    private static void logException(@NotNull String message, @NotNull Exception e) {
//        if (OSUtil.isWindows() && e instanceof IllegalStateException) {
//            JulLog.debug(message, e);
//        } else {
//            JulLog.warn(message, e);
//        }
//    }
//
//    @Override
//    public void lostOwnership(Clipboard clipboard, Transferable contents) {
//    }
//}
