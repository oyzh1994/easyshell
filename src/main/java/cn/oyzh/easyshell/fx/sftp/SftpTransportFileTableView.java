package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SftpTransportFileTableView extends SftpFileBaseTableView {

    @Override
    public void loadFile() {
        StageManager.showMask(() -> {
            try {
                super.loadFileInner();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

}
