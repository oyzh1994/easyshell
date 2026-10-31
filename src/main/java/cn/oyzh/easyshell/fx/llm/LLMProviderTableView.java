package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * LLM provider table.
 *
 * @author oyzh
 * @since 2026-10-30
 */
public class LLMProviderTableView extends FXTableView<LLMProvider> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }
}
