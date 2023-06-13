package com.obiscr.chatgpt;

import com.intellij.openapi.project.Project;
import com.obiscr.chatgpt.ui.MainPanel;
import com.obiscr.chatgpt.util.MyUIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author vizz
 * @since 2023/6/13 10:21
 */
public class StarChatToolWindow {

    private final MainPanel panel;

    public StarChatToolWindow(@NotNull Project project){
        panel = new MainPanel(project, true);
    }
    public JPanel getContent() {
        return panel.init();
    }

    public MainPanel getPanel() {
        return panel;
    }

    public void registerKeystrokeFocus(){
        MyUIUtil.registerKeystrokeFocusForInput(panel.getSearchTextArea().getTextArea());
    }
}
