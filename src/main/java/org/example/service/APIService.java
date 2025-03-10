package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.model.APIParam;
import org.example.model.ModelResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



public class APIService {
    private static final APIService instance = new APIService();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String groqToken;
    private final String togetherToken;
    private String instruction;

    public static APIService getInstance() {
        return instance;
    }
    private APIService() {
        Dotenv dotenv = Dotenv.load();
        groqToken = dotenv.get("GROQ_KEY");
        togetherToken = dotenv.get("TOGETHER_KEY");
    }


    public String callAPI(APIParam apiParam) throws Exception {

        String url = "";
        String token = "";

        switch (apiParam.model().platform) {
            case GROQ -> {
                url = "https://api.groq.com/openai/v1/chat/completions";
                token = groqToken;
                instruction = "한글로 출력한다";
            }
            case TOGETHER -> {
                url = "https://api.together.xyz/v1/chat/completions";
                token = togetherToken;
                instruction = "한글로 말해야한다";
            }
            default -> throw new Exception("Invalid API Param");
        }


        String body = """
                {
                         "messages": [
                           {
                             "role": "system",
                             "content": "%s"
                           },
                           {
                             "role": "user",
                             "content": "%s"
                           }
                         ],
                         "model": "%s"
                       }
                """.formatted(instruction, apiParam.prompt(), apiParam.model().name);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Authorization", "Bearer %s".formatted(token))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        ObjectMapper objectMapper = new ObjectMapper();
        ModelResponse modelResponse = objectMapper.readValue(responseBody, ModelResponse.class);
        String content = modelResponse.choices().get(0).message().content();


        ObjectNode jsonResponse = objectMapper.createObjectNode(); // JSON 객체 생성
        jsonResponse.put("content", content); // "content" 속성에 값 설정

        return objectMapper.writeValueAsString(jsonResponse); // JSON 객체를 JSON 문자열로 직렬화 (Gson의 toJson() 과 유사)
    }
}
