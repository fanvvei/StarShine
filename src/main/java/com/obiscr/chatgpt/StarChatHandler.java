package com.obiscr.chatgpt;

import com.alibaba.fastjson2.JSON;
import com.obiscr.chatgpt.core.builder.OfficialBuilder;
import com.obiscr.chatgpt.core.parser.OfficialParser;
import com.obiscr.chatgpt.settings.OpenAISettingsState;
import com.obiscr.chatgpt.ui.MainPanel;
import com.obiscr.chatgpt.ui.MessageComponent;
import com.obiscr.chatgpt.ui.MessageGroupComponent;
import com.obiscr.chatgpt.util.StringUtil;
import okhttp3.*;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Proxy;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author vizz
 * @since 2023/6/13 11:06
 */
public class StarChatHandler extends AbstractHandler {
    private static final Logger LOG = LoggerFactory.getLogger(StarChatHandler.class);
    OpenAISettingsState instance = OpenAISettingsState.getInstance();

    public Call handle(MainPanel mainPanel, MessageComponent component, String question) {
        MessageGroupComponent contentPanel = mainPanel.getContentPanel();

        Map<String, Object> map = new HashMap<>();
        map.put("question", question);
        String finalPrompt = instance.starPromptTmpl.replace("${system}", instance.starSystemPrompt).replace("${user}", question);
        map.put("text", finalPrompt);
        map.put("len", instance.starLen);
        map.put("step", instance.starStep);
        map.put("system_prompt", instance.starSystemPrompt);
        map.put("temperature", instance.starTemperature);
        map.put("top_p", instance.starTopP);
        map.put("top_k", instance.starTopK);
        map.put("repetition_penalty", instance.starRepetitionPenalty);


        // Define the default system role
        if (contentPanel.getMessages().isEmpty()) {
            String text = contentPanel.getSystemRole();
            contentPanel.getMessages().add(OfficialBuilder.systemMessage(text));
        }
        Call call = null;
        RequestProvider provider = new RequestProvider().create(mainPanel, question);
        try {
            System.out.println(JSON.toJSONString(map));
            RequestBody body = RequestBody.create(JSON.toJSONString(map).getBytes(StandardCharsets.UTF_8),
                    MediaType.parse("application/json"));

            LOG.info("GPT 3.5 Turbo Request: question={}", question);
            Request request = new Request.Builder()
//                    .url(provider.getUrl())
                    .url(instance.starUrl)
                    .addHeader("Accept", "application/x-json-stream")
                    .post(body)
                    .build();
            OpenAISettingsState instance = OpenAISettingsState.getInstance();
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(Integer.parseInt(instance.connectionTimeout), TimeUnit.MILLISECONDS)
                    .readTimeout(Integer.parseInt(instance.readTimeout), TimeUnit.MILLISECONDS);
            builder.hostnameVerifier(getHostNameVerifier());
            builder.sslSocketFactory(getSslContext().getSocketFactory(), (X509TrustManager) getTrustAllManager());
            if (instance.enableProxy) {
                Proxy proxy = getProxy();
                builder.proxy(proxy);
            }
            if (instance.enableProxyAuth) {
                Authenticator proxyAuth = getProxyAuth();
                builder.proxyAuthenticator(proxyAuth);
            }
            OkHttpClient httpClient = builder.build();
            call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    String errorMessage = StringUtil.isEmpty(e.getMessage()) ? "None" : e.getMessage();
                    if (e instanceof SocketException) {
                        LOG.info("GPT 3.5 Turbo: Stop generating");
                        component.setContent("Stop generating");
                        e.printStackTrace();
                        return;
                    }
                    LOG.error("GPT 3.5 Turbo Request failure. Url={}, error={}",
                            call.request().url(),
                            errorMessage);
                    errorMessage = "GPT 3.5 Turbo Request failure, cause: " + errorMessage;
                    component.setSourceContent(errorMessage);
                    component.setContent(errorMessage);
                    mainPanel.aroundRequest(false);
                    component.scrollToBottom();
                    mainPanel.getExecutorService().shutdown();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() != 200) {
                        String responseMessage = response.body().string();
                        LOG.info("GPT 3.5 Turbo: Request failure. Url={}, response={}", provider.getUrl(), responseMessage);
                        component.setContent("Response failure, please try again. Error message: " + responseMessage);
                        mainPanel.aroundRequest(false);
                        return;
                    }

                    BufferedSource source = response.body().source();
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = source.readUtf8Line()) != null) {
                        // 处理每一行的JSON数据
                        OfficialParser.ParseResult parseResult = OfficialParser.
                                parseStarCoderWithStream(line);

                        if (parseResult.getSource().length() == 0) {
                            break;
                        }
                        mainPanel.getContentPanel().getMessages().add(OfficialBuilder.assistantMessage(parseResult.getSource()));
                        component.setSourceContent(parseResult.getSource());
                        component.setContent(parseResult.getHtml());
                        component.scrollToBottom();
                        System.out.println(line);
                    }
                    mainPanel.aroundRequest(false);
                    source.close();

                }
            });
        } catch (Exception e) {
            component.setSourceContent(e.getMessage());
            component.setContent(e.getMessage());
            mainPanel.aroundRequest(false);
        } finally {
            mainPanel.getExecutorService().shutdown();
        }
        return call;
    }
}
