package org.JScarlet;


import okhttp3.*;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class AI_Poster {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
//    String API_KEY = System.getenv("OPENAI_API_KEY");
    private String filePath = null;
    private String resultContent = null;
    private String finalResult = null;
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    AI_Poster(String path){
        filePath = path;
    }

    public void AudioTranscriber(){
        HttpPost post = new HttpPost("https://api.openai.com/v1/audio/transcriptions");
        post.setHeader("Authorization", "Bearer " + API_KEY);
        File audioFile = new File(filePath);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", audioFile, ContentType.DEFAULT_BINARY, audioFile.getName())
                .addTextBody("model", "gpt-4o-mini-transcribe")//預設 gpt-4o-transcribe
                .addTextBody("timestamp_granularities[]","segment")
                .build();

        post.setEntity(entity);

        try {
            URL url = new URL("https://api.openai.com/v1/models");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.connect();
            System.out.println("API status: " + conn.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            resultContent = client.execute(post, httpResponse ->
                    EntityUtils.toString(httpResponse.getEntity()));
            JSONObject json = new JSONObject(resultContent);
            finalResult = json.getString("text");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String tidyTheContent(String content){

        JSONObject messageObjectSet = new JSONObject()
                .put("role","system")
                .put("content","這是一個音訊檔的解析文字檔案，你要判斷哪個位置是段落結束而應該換行");

        JSONObject messageObjectSet2 = new JSONObject()
                .put("role","user")
                .put("content", content);

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-mini") // 預設 gpt-4.1-nano
                .put("messages", new org.json.JSONArray()
                        .put(messageObjectSet)
                        .put(messageObjectSet2));

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json")
        );

        //解析回傳
        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();

        String responseBody = null;
        try (Response responseSet = client.newCall(requestSet).execute()) {

            if (!responseSet.isSuccessful()) {
                if (responseBody != null) {
                }else if (responseBody == null || responseBody.trim().isEmpty()) {// ✅ response 成功但空內容
                    throw new RuntimeException("API 回傳為空，請確認網路或參數\n" + responseSet);
                } else {
//                    responseBody = "{\"error\": \"空內容\"}";
                }
                throw new RuntimeException("API 回傳錯誤（" + responseSet.code() + "）");
            }else if(responseSet.isSuccessful()){
                ResponseBody body = responseSet.body();
                if (body != null) {
                    responseBody = body.string();
                }
            }
        }catch (SocketTimeoutException e){
            throw new RuntimeException("net串流異常\n" + e);
        }
        catch (IOException e) {
            throw new RuntimeException("檔案處理異常\n" + e);
        }
        JSONObject jsonResponse = new JSONObject(responseBody);
        if (!jsonResponse.has("choices")) {
            throw new RuntimeException("GPT 回傳 JSON 格式錯誤：缺少 'choices'");
        }
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices.length() == 0) {
            throw new RuntimeException("GPT 回傳 'choices' 為空");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        if (!firstChoice.has("message")) {
            throw new RuntimeException("GPT 回傳缺少 'message'");
        }

        JSONObject messageResponse = firstChoice.getJSONObject("message");

        return  messageResponse.getString("content");
    }

    public String getOraginalResult(){
        return resultContent;
    }

    public String getFinalResult(){
        return finalResult;
    }

}
