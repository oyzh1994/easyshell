//package cn.oyzh.jeditermfx.terminal.model;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//class TextBufferChangesMulticaster implements TextBufferChangesListener {
//    private List<TextBufferChangesListener> listeners = new CopyOnWriteArrayList<>();
//
//    public void addListener(TextBufferChangesListener listener) {
//        listeners.add(listener);
//    }
//
//    public void removeListener(TextBufferChangesListener listener) {
//        listeners.remove(listener);
//    }
//
//    @Override
//    public void linesChanged(int fromIndex) {
//        forEachListeners(listener -> listener.linesChanged(fromIndex));
//    }
//
//    @Override
//    public void linesDiscardedFromHistory(List<TerminalLine> lines) {
//        forEachListeners(listener -> listener.linesDiscardedFromHistory(lines));
//    }
//
//    @Override
//    public void historyCleared() {
//        forEachListeners(TextBufferChangesListener::historyCleared);
//    }
//
//    @Override
//    public void widthResized() {
//        forEachListeners(TextBufferChangesListener::widthResized);
//    }
//
//    private void forEachListeners(ListenerAction action) {
//        for (TextBufferChangesListener listener : listeners) {
//            action.perform(listener);
//        }
//    }
//
//    @FunctionalInterface
//    private interface ListenerAction {
//        void perform(TextBufferChangesListener listener);
//    }
//}
//
