package cn.oyzh.easyshell.zmodem;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.jeditermfx.app.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXTerminalPanel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import zmodem1.FileCopyStreamEvent;
import zmodem1.ZModem;
import zmodem1.util.CustomFile;
import zmodem1.util.EmptyFileAdapter;
import zmodem1.util.FileAdapter;
import zmodem1.xfer.zm.util.ZModemCharacter;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
* https://wiki.synchro.net/ref:zmodem
*/
public class ZModemPtyConnectorAdaptor1 implements TtyConnector {

   private final char[] prefix = new char[] {
       (char) ZModemCharacter.ZPAD.value(),
       (char) ZModemCharacter.ZPAD.value(),
       (char) ZModemCharacter.ZDLE.value()
   };

   private volatile ZModemProcessor zmodem;

   private Terminal  terminal;
   private FXTerminalPanel terminalPanel;

   private ShellSSHTtyConnector connector;

   public PtyProcessTtyConnector getConnector() {
       return connector;
   }

   public ZModemPtyConnectorAdaptor1(Terminal terminal, FXTerminalPanel terminalPanel, ShellSSHTtyConnector connector) {
       this.terminal = terminal;
       this.connector = connector;
       this.terminalPanel = terminalPanel;
   }

   @Override
   public int read(char[] buffer, int offset, int length) throws IOException {
       if (zmodem != null) {
           try {
               zmodem.process();
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
           zmodem = null;
       }

       int i = connector.read(buffer,offset,length);
       if (i < 1) {
           return i;
       }

       char[] bufferSlice = Arrays.copyOfRange(buffer, 0, i);
       int e = indexOf(bufferSlice, prefix);
       if (e == -1) {
           return i;
       }

       //char[] zmodemFrame = buffer;
       char[] zmodemFrame = Arrays.copyOfRange(buffer, e, i);

       zmodem = new ZModemProcessor(
           // sz: * * 0x18 B 0 0
           // rz: * * 0x18 B 0 1
           zmodemFrame.length > 5 && zmodemFrame[5] == 48,
           connector,
           terminal,
           new ZModemInputStream(connector.input(),new String(zmodemFrame).getBytes()),
               connector.output()
       //new ZModemInputStream(connector.input(), CharBuffer.wrap(zmodemFrame).toString().getBytes()),
       //        connector.output()
       );

       return e;
   }

   @Override
   public void write(byte[] bytes) throws IOException {
       this.connector.write(bytes);
   }

   @Override
   public void write(String string) throws IOException {
       this.connector.write(string);

   }

   @Override
   public boolean isConnected() {
       return  this.connector.isConnected();
   }

   @Override
   public int waitFor() throws InterruptedException {
       return  this.connector.waitFor();
   }

   @Override
   public boolean ready() throws IOException {
       return  this.connector.ready();
   }

   @Override
   public String getName() {
       return  this.connector.getName();
   }

   @Override
   public void close() {

       this.connector.close();
   }

   private int indexOf(char[] a, char[] b) {
       if (a.length < b.length) {
           return -1;
       }
       for (int i = 0; i <= a.length - b.length; i++) {
           char[] range = Arrays.copyOfRange(a, i, i + b.length);
           if (Arrays.equals(range, b)) {
               return i;
           }
       }
       return -1;
   }

   public void write(byte[] buffer, int offset, int len) throws IOException {
       if (zmodem != null) {
           if (buffer[offset] == 0x03) {
               zmodem.cancel();
           }
           return;
       }
       buffer = Arrays.copyOfRange(buffer, offset, offset + len);
       connector.write(buffer);
   }

   private static class ZModemInputStream extends InputStream {
       private final InputStream input;
       private final byte[] buffer;
       private int index = 0;

       public ZModemInputStream(InputStream input, byte[] buffer) {
           this.input = input;
           this.buffer = buffer;
       }

       @Override
       public int read() throws IOException {
           if (index < buffer.length) {
               return buffer[index++];
           }
           return input.read();
       }
   }

   private class ZModemProcessor implements CopyStreamListener {
       // 如果为 true 表示是接收（sz）文件
       private final boolean sz;
       private final ShellSSHTtyConnector connector;
       private final Terminal terminal;
       private final ZModem zmodem;
       private long lastRefreshTime = 0L;

       public ZModemProcessor(boolean sz, ShellSSHTtyConnector connector, Terminal terminal, InputStream input, OutputStream output) {
           this.sz = sz;
           this.connector = connector;
           this.terminal = terminal;
           this.zmodem = new ZModem(input, output,connector);
       }

       public void process() throws Exception {
           if (sz) {
               receive();
           } else {
               send();
           }
       }

       private void receive() throws IOException {
           zmodem.receive(new Supplier<FileAdapter>() {
               @Override
               public FileAdapter get() {
                   try {
                       List<File> files = openFilesDialog(JFileChooser.DIRECTORIES_ONLY);
                       File file = files.isEmpty() ? null : files.get(0);
                       if (file != null) {
                           FileUtil.forceMkdir(file);
                       }
                       return file == null ? EmptyFileAdapter.INSTANCE : new CustomFile(file);
                   } catch (Exception e) {
                       return EmptyFileAdapter.INSTANCE;
                   }
               }
           }, this);
       }

       private void send() throws Exception {
           zmodem.send(new Supplier<List<FileAdapter>>() {
               @Override
               public List<FileAdapter> get() {
                   List<FileAdapter> files = new ArrayList<>();
                   try {
                       for (File file : openFilesDialog(JFileChooser.FILES_ONLY)) {
                           files.add(new CustomFile(file));
                       }
                   } catch (Exception e) {
                   }
                   return files;
               }
           }, this);
       }

       private void refreshProgress(FileCopyStreamEvent event) throws IOException {
           int width = 24;
           boolean skip = event.isSkip();
           boolean completed = event.getBytesTransferred() >= event.getTotalBytesTransferred();
           double rate = (event.getBytesTransferred() * 1.0 / event.getTotalBytesTransferred()) * 100.0;
           String progress = completed ? "100" : String.format("%.2f", Math.min(rate, 99.99));
           long total = event.getRemaining() + event.getIndex() - 1;
           StringBuilder sb = new StringBuilder();

           sb.append(ControlCharacters.CR);
           sb.append(ControlCharacters.ESC).append("[0J");
           sb.append('[').append(ControlCharacters.ESC).append("[35m").append(event.getIndex());
           sb.append(ControlCharacters.ESC).append("[39m").append('/');
           sb.append(ControlCharacters.ESC).append("[35m").append(total)
               .append(ControlCharacters.ESC).append("[39m").append(']');
           sb.append(ControlCharacters.TAB);
           sb.append(StringUtils.abbreviate(StringUtils.rightPad(event.getFilename(), width), width));
           sb.append(ControlCharacters.TAB);
           sb.append(
               StringUtils.abbreviate(
                   StringUtils.rightPad(
                       String.format("%d/%d", event.getBytesTransferred(), event.getTotalBytesTransferred()),
                       width
                   ), width
               )
           );
           sb.append(ControlCharacters.TAB);

           if (skip) {
               sb.append("[skip]");
           } else {
               sb.append(progress).append('%');
           }

           // 换行
           if ((completed && event.getRemaining() > 1) || skip) {
               sb.append(ControlCharacters.LF);
               sb.append(ControlCharacters.CR);
           }

           //if (completed && total == event.getIndex()) {
           //    sb.append(ControlCharacters.LF);
           //    sb.append(ControlCharacters.CR);
           //}

           if (completed || skip) {
               terminal.writeUnwrappedString(sb.toString());
               //terminal.writeCharacters(sb.toString());
               //terminal.writeDoubleByte(sb.toString().toCharArray());
               //zmodem.getNetOs().write(sb.toString().getBytes());
               //SwingUtilities.invokeLater(() -> terminal.writeUnwrappedString("\n\r"));
               return;
           }

           long now = System.currentTimeMillis();
           if (now - lastRefreshTime > 100) {
               lastRefreshTime = now;
               //zmodem.getNetOs().write(sb.toString().getBytes());
               //terminal.writeCharacters(sb.toString());
               //terminal.writeUnwrappedString(sb.toString());
                //terminal.writeCharacters(sb.toString());
               SwingUtilities.invokeLater(() -> terminal.writeUnwrappedString(sb.toString()));
           }
       }

       private List<File> openFilesDialog(int fileSelectionMode) {
           CompletableFuture<List<File>> future = new CompletableFuture<>();

           try {
               //SwingUtilities.invokeAndWait(() -> {
                   try {
                       List<File> files= FileChooserHelper.chooseMultiple("请选择文件", FXChooser.allExtensionFilter());
                       future.complete(files);
                   } catch (Exception e) {
                       future.completeExceptionally(e);
                   }
               //});
           } catch (Exception e) {
               return Collections.emptyList();
           }

           try {
               return future.get();
           } catch (Exception e) {
               return Collections.emptyList();
           }
       }

       @Override
       public void bytesTransferred(CopyStreamEvent event) {
           if (event instanceof FileCopyStreamEvent) {
               try {
                   refreshProgress((FileCopyStreamEvent) event);
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
           }
       }

       @Override
       public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
           // TODO("Not yet implemented")
       }

       public void cancel() {
           zmodem.cancel();
       }
   }
}