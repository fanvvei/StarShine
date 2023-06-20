package com.obiscr.chatgpt.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author vizz
 * @since 2023/6/14 8:55
 */
public class StarChatSettingsPanel implements Configurable, Disposable {
    private JTextField optionSystemPrompt;
    private JTextField optionURL;
    private JTextField optionMaxNewTokens;
    private JTextField optionsStep;
    private JTextField optionsTemperature;
    private JTextField optionsTopK;
    private JTextField optionsTopP;
    private JTextField optionsRepetitionPenalty;
    private JPanel mainPanel;

    public StarChatSettingsPanel(){
    }

    @Override
    public void reset() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        optionSystemPrompt.setText(state.starSystemPrompt);
        optionURL.setText(state.starUrl);
        optionMaxNewTokens.setText(state.starLen);
        optionsStep.setText(state.starStep);
        optionsTemperature.setText(state.starTemperature);
        optionsTopK.setText(state.starTopK);
        optionsTopP.setText(state.starTopP);
        optionsRepetitionPenalty.setText(state.starRepetitionPenalty);
    }

    @Override
    public void dispose() {

    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Star Chat";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        return !state.starUrl.equals(optionURL.getText())
                || !state.starLen.equals(optionMaxNewTokens.getText())
                || !state.starStep.equals(optionsStep.getText())
                || !state.starTemperature.equals(optionsTemperature.getText())
                || !state.starTopK.equals(optionsTopK.getText())
                || !state.starTopP.equals(optionsTopP.getText())
                || !state.starRepetitionPenalty.equals(optionsRepetitionPenalty.getText())
                || !state.starSystemPrompt.equals(optionSystemPrompt.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        OpenAISettingsState state = OpenAISettingsState.getInstance();
        state.starUrl = optionURL.getText();
        state.starLen = optionMaxNewTokens.getText();
        state.starStep = optionsStep.getText();
        state.starTemperature = optionsTemperature.getText();
        state.starTopK = optionsTopK.getText();
        state.starTopP = optionsTopP.getText();
        state.starRepetitionPenalty = optionsRepetitionPenalty.getText();
        state.starSystemPrompt = optionSystemPrompt.getText();
    }
}
