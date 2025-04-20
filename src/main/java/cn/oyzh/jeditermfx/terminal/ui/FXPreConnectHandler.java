package cn.oyzh.jeditermfx.terminal.ui;


import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.Terminal;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.function.BiConsumer;

/**
 * @deprecated
 */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public final class FXPreConnectHandler implements Questioner, BiConsumer<EventType<KeyEvent>, KeyEvent> {

    private final Object mySync = new Object();

    private final Terminal myTerminal;

    private StringBuffer myAnswer;

    private boolean myVisible;

    public FXPreConnectHandler(Terminal terminal) {
        this.myTerminal = terminal;
        this.myVisible = true;
    }

    // These methods will suspend the current thread and wait for
    // the event handling thread to provide the answer.
    public String questionHidden(String question) {
        myVisible = false;
        String answer = questionVisible(question, null);
        myVisible = true;
        return answer;
    }

    public String questionVisible(String question, String defValue) {
        synchronized (mySync) {
            myTerminal.writeUnwrappedString(question);
            myAnswer = new StringBuffer();
            if (defValue != null) {
                myAnswer.append(defValue);
                myTerminal.writeUnwrappedString(defValue);
            }
            try {
                mySync.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String answerStr = myAnswer.toString();
            myAnswer = null;
            return answerStr;
        }
    }

    public void showMessage(String message) {
        myTerminal.writeUnwrappedString(message);
        myTerminal.nextLine();
    }

    public void keyPressed(KeyEvent e) {
        if (myAnswer == null) return;
        synchronized (mySync) {
            boolean release = false;

            switch (e.getCode()) {
                case KeyCode.BACK_SPACE:
                    if (!myAnswer.isEmpty()) {
                        myTerminal.backspace();
                        myTerminal.eraseInLine(0);
                        myAnswer.deleteCharAt(myAnswer.length() - 1);
                    }
                    break;
                case KeyCode.ENTER:
                    myTerminal.nextLine();
                    release = true;
                    break;
            }

            if (release) mySync.notifyAll();
        }

    }

    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {
        if (myAnswer == null) return;
        char c = e.getCharacter().charAt(0);
        if (Character.getType(c) != Character.CONTROL) {
            if (myVisible) myTerminal.writeCharacters(Character.toString(c));
            myAnswer.append(c);
        }
    }

    @Override
    public void accept(EventType<KeyEvent> type, KeyEvent keyEvent) {
        if (type == KeyEvent.KEY_PRESSED) {
            this.keyPressed(keyEvent);
        } else if (type == KeyEvent.KEY_TYPED) {
            this.keyTyped(keyEvent);
        } else if (type == KeyEvent.KEY_RELEASED) {
            this.keyReleased(keyEvent);
        }
    }
}
