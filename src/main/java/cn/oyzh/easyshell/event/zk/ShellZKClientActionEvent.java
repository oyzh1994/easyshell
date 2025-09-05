package cn.oyzh.easyshell.event.zk;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.zk.ShellZKClientActionArgument;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-20
 */
public class ShellZKClientActionEvent extends Event<String> implements EventFormatter {

    private String action;

    public List<ShellZKClientActionArgument> getArguments() {
        return arguments;
    }

    public void setArguments(List<ShellZKClientActionArgument> arguments) {
        this.arguments = arguments;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private List<ShellZKClientActionArgument> arguments = new ArrayList<>(12);

    public void arguments(List<ShellZKClientActionArgument>  arguments) {
        this.arguments.addAll(arguments);
    }

    public void arguments(ShellZKClientActionArgument... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public void argument(ShellZKClientActionArgument argument) {
        this.arguments.add(argument);
    }

    public void argument(String argument, Object value) {
        this.arguments.add(new ShellZKClientActionArgument(argument, value));
    }

    public void argument(Object value) {
        this.arguments.add(new ShellZKClientActionArgument(value));
    }

    // public void params(Object...params) {
    //     if (actionData instanceof String s) {
    //         if (s.length() > 1024) {
    //             this.actionData = I18nHelper.dataTooLarge();
    //         } else {
    //             this.actionData = s;
    //         }
    //     } else if (actionData instanceof byte[] bytes) {
    //         if (bytes.length > 1024) {
    //             this.actionData = I18nHelper.dataTooLarge();
    //         } else {
    //             this.actionData = new String(bytes);
    //         }
    //     } else if (actionData instanceof Number n) {
    //         this.actionData = n.doubleValue();
    //     } else if (actionData != null) {
    //         this.actionData = JSONUtil.toJson(actionData);
    //     }
    // }

    @Override
    public String eventFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.data());
        sb.append(" > ");
        sb.append(this.action);
        for (ShellZKClientActionArgument argument : this.arguments) {
            sb.append(" ");
            if(StringUtil.isNotBlank(argument.getArgument())){
                sb.append(argument.getArgument()).append(" ");
            }
            Object value = argument.getValue();
            if (value instanceof String s) {
                if (s.length() > 1024) {
                    sb.append(I18nHelper.dataTooLarge());
                } else {
                    sb.append(s);
                }
            } else if (value instanceof byte[] bytes) {
                if (bytes.length > 1024) {
                    sb.append(I18nHelper.dataTooLarge());
                } else {
                    sb.append(new String(bytes));
                }
            } else if (value instanceof Number n) {
                sb.append(n);
            } else if (value != null) {
                sb.append(JSONUtil.toJson(value));
            }
        }
        // if (this.params != null && this.actionData != null) {
        //     return String.format("%s > %s %s %s", this.data(), this.action, this.params, this.actionData);
        // }
        // if (this.params != null) {
        //     return String.format("%s > %s %s", this.data(), this.action, this.params);
        // }
        // return String.format("%s > %s", this.data(), this.action);
        return sb.toString();
    }

}
