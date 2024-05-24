package com.snwolf.dada.aiService;

import com.snwolf.dada.exception.AiInvokingException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ZhipuAiServiceImpl {

    private final ClientV4 clientV4;

    private static final float STABLE_TEMPERATURE = 0.05f;

    private static final float UNSTABLE_TEMPERATURE = 0.99f;

    private static final float DEFAULT_TEMPERATURE = 0.80f;


    /**
     * 通用请求
     *
     * @param messages
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(List<ChatMessage> messages, Boolean stream, Float temperature) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .temperature(temperature)
                .stream(stream)
                .build();
        ModelApiResponse invokeModelApiResp = null;
        try {
            invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        } catch (Exception e) {
            throw new AiInvokingException("调用AI接口异常, 异常信息: " + e.getMessage());
        }
        return invokeModelApiResp.getData().getChoices().get(0).toString();
    }

    /**
     * 通用请求 -- 简化消息传递
     * @param systemMessage
     * @param userMessage
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(String systemMessage, String userMessage, Boolean stream, Float temperature) {
        List<ChatMessage> chatMessageList = getMessageListWithSingleUserAndSystemMessage(systemMessage, userMessage);
        return doRequest(chatMessageList, stream, temperature);
    }

    /**
     * 同步请求
     * @param systemMessage
     * @param userMessage
     * @param temperature
     * @return
     */
    public String doRequestSync(String systemMessage, String userMessage, Float temperature) {
        List<ChatMessage> chatMessageList = getMessageListWithSingleUserAndSystemMessage(systemMessage, userMessage);
        return doRequest(chatMessageList, Boolean.FALSE, temperature);
    }

    /**
     * 同步请求 -- 回答比较稳定
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doRequestSyncStable(String systemMessage, String userMessage) {
        List<ChatMessage> chatMessageList = getMessageListWithSingleUserAndSystemMessage(systemMessage, userMessage);
        return doRequest(chatMessageList, Boolean.FALSE, STABLE_TEMPERATURE);
    }

    /**
     * 同步请求 -- 回答不稳定
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doRequestSyncUnStable(String systemMessage, String userMessage) {
        List<ChatMessage> chatMessageList = getMessageListWithSingleUserAndSystemMessage(systemMessage, userMessage);
        return doRequest(chatMessageList, Boolean.FALSE, UNSTABLE_TEMPERATURE);
    }

    /**
     * 同步请求 -- 默认随机程度
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doRequestSyncWithDefaultTemperature(String systemMessage, String userMessage) {
        List<ChatMessage> chatMessageList = getMessageListWithSingleUserAndSystemMessage(systemMessage, userMessage);
        return doRequest(chatMessageList, Boolean.FALSE, DEFAULT_TEMPERATURE);
    }


    /**
     * 通用流式请求
     *
     * @param messages
     * @param temperature
     * @return
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages, Float temperature) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .temperature(temperature)
                .stream(true)
                .build();
        ModelApiResponse invokeModelApiResp = null;
        try {
            invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        } catch (Exception e) {
            throw new AiInvokingException("调用AI接口异常, 异常信息: " + e.getMessage());
        }
        return invokeModelApiResp.getFlowable();
    }

    /**
     * 通用流式请求 -- 简化消息传递
     *
     * @param systemMessage
     * @param userMessage
     * @param temperature
     * @return
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature) {
        List<ChatMessage> chatMessageList = getMessageListWithSingleUserAndSystemMessage(systemMessage, userMessage);
        return doStreamRequest(chatMessageList, temperature);
    }

    private List<ChatMessage> getMessageListWithSingleUserAndSystemMessage(String systemMessage, String userMessage){
        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        chatMessageList.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        chatMessageList.add(userChatMessage);
        return chatMessageList;
    }

}
