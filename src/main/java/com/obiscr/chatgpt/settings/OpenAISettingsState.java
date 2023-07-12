
package com.obiscr.chatgpt.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.obiscr.OpenAIProxy;
import org.apache.groovy.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.obiscr.chatgpt.MyToolWindowFactory.*;

/**
 * @author Wuzi
 * Supports storing the application settings in a persistent way.
 * The {@link State} and {@link Storage} annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
        name = "com.obiscr.chatgpt.settings.OpenAISettingsState",
        storages = @Storage("ChatGPTSettingsPlugin.xml")
)
public class OpenAISettingsState implements PersistentStateComponent<OpenAISettingsState> {

  public String customizeUrl = "";

  public String readTimeout = "50000";
  public String connectionTimeout = "50000";
  public Boolean enableProxy = false;
  public Boolean enableAvatar = true;
  public SettingConfiguration.SettingProxyType proxyType =
          SettingConfiguration.SettingProxyType.DIRECT;

  public String proxyHostname = "";
  public String proxyPort = "10000";

  public String accessToken = "";
  public String expireTime = "";
  public String imageUrl = "https://cdn.auth0.com/avatars/me.png";
  public String apiKey = "";
  public Map<Integer,String> contentOrder = new HashMap<>(){{
    put(2, CHATGPT_CONTENT_NAME);
    put(3, GPT35_TRUBO_CONTENT_NAME);
    put(4, ONLINE_CHATGPT_CONTENT_NAME);
    put(1,STAR_CHAT_CONTENT_NAME);
  }};

  public Boolean enableLineWarp = true;

  @Deprecated
  public List<String> customActionsPrefix = new ArrayList<>();

  public String chatGptModel = "text-davinci-002-render-sha";
  public String gpt35Model = "gpt-3.5-turbo";
  public Boolean enableContext = false;
  public String assistantApiKey = "";
  public Boolean enableTokenConsumption = false;
  public Boolean enableGPT35StreamResponse = false;
  public String gpt35TurboUrl = "https://api.openai.com/v1/chat/completions";

  public Boolean enableProxyAuth = false;
  public String proxyUsername = "";
  public String proxyPassword = "";

  public Boolean enableCustomizeGpt35TurboUrl = false;
  public Boolean enableCustomizeChatGPTUrl = false;

  public String gpt35RoleText = "You are a helpful language assistant";

  public String prompt1Name = "单元测试";
  public String prompt1Value = "为以下java代码生成单元测试：";
  public String prompt2Name = "代码解释";
  public String prompt2Value = "解释这段代码：";
  public String prompt3Name = "BUG检测";
  public String prompt3Value = "寻找以下代码的BUG：";

  public static final String STAR_CHAT_BETA_PROMPT_TMPL="<|system|>\\n${system}<|end|>\\n<|user|>\\n${user}<|end|>\\n<|assistant|>";
  public static final String WIZARD_PROMPT_TMPL="${system}\\n\\n### Instruction:\\n${user}\\n\\n### Response:";
  public static final String STAR_CHAT_BETA_SYSTEM_PROMPT="Below is a conversation between a human user and a helpful AI coding assistant.";
  public static final String WIZARD_SYSTEM_PROMPT="Below is an instruction that describes a task. Write a response that appropriately completes the request.";
  public String starPromptTmpl = WIZARD_PROMPT_TMPL;
  public String starSystemPrompt = WIZARD_SYSTEM_PROMPT;
  public String starUrl = "http://122.178.8.121:1111/json-stream";
  public String starLen = "3072";
  public String starStep = "32";
  public String starTemperature = "0.2";
  public String starTopP = "0.95";
  public String starTopK = "50";
  public String starRepetitionPenalty = "0.8";


  @Tag("customPrompts")
  public Map<String, String> customPrompts = new HashMap<>();
//  public Map<String, String> customPrompts = Maps.of(
//          "Explain","Explain this code",
//          "Optimize Code","Optimize this code",
//          "Translate into Python","Translate the following code into Python");

  {
    customPrompts.put("Generate unit tests","Generate unit test case for this code");
    customPrompts.put("Generate Comment","Add line-level comments for this code");
    customPrompts.put("Find Bug","Find the bug in the code below");
    customPrompts.put("Explain","Explain this code");
    customPrompts.put("Optimize Code","Optimize this code");
    customPrompts.put("Translate into Python","Translate the following code into Python");
    customPrompts.put("注释生成","为以下java代码增加行级注释");
    customPrompts.put("代码优化","优化以下代码");
  }

  public static OpenAISettingsState getInstance() {
    return ApplicationManager.getApplication().getService(OpenAISettingsState.class);
  }

  @Nullable
  @Override
  public OpenAISettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull OpenAISettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public void reload() {
    loadState(this);
  }

  public Proxy getProxy() {
    Proxy proxy = null;
    if (enableProxy) {
      Proxy.Type type = proxyType ==
              SettingConfiguration.SettingProxyType.HTTP ? Proxy.Type.HTTP :
              proxyType == SettingConfiguration.SettingProxyType.SOCKS ? Proxy.Type.SOCKS :
                      Proxy.Type.DIRECT;
      proxy = new OpenAIProxy(proxyHostname, Integer.parseInt(proxyPort),
              type).build();
    }
    return proxy;
  }
}
