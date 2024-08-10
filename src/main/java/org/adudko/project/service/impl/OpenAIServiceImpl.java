package org.adudko.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adudko.project.model.Question;
import org.adudko.project.service.OpenAIService;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIServiceImpl implements OpenAIService {

    private final OpenAiImageModel imageModel;
    private final ChatModel chatModel;

    @Override
    public String getDescription(MultipartFile file) throws IOException {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .withModel(OpenAiApi.ChatModel.GPT_4_O.getValue())
                .build();
        Resource resource = new ByteArrayResource(file.getBytes());
        var userMessage = new UserMessage(
                "Explain what do you see in this picture?", // content
                List.of(new Media(MimeTypeUtils.IMAGE_JPEG, resource))); // media
        return chatModel.call(new Prompt(List.of(userMessage), chatOptions)).getResult().getOutput().toString();
    }

    @Override
    public byte[] getImage(Question question) {
        var options = OpenAiImageOptions.builder()
                .withHeight(1024).withWidth(1792)
                .withResponseFormat("b64_json")
                .withModel("dall-e-3")
                .withQuality("hd") //default standard
//                .withStyle("natural") //default vivid
                .build();
        ImagePrompt imagePrompt = new ImagePrompt(question.question(), options);
        var imageResponse = imageModel.call(imagePrompt);
        return Base64.getDecoder().decode(imageResponse.getResult().getOutput().getB64Json());
    }

}
