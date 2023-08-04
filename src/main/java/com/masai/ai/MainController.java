package com.masai.ai;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class MainController {

    String token = System.getenv("TOKEN");

    @PostMapping({"/api"})
    public ResponseEntity<String> api(@RequestBody String message) {

        StringBuffer response;
        String body = "{\n"
                + "  \"text\": \"" + message + "\",\n"
                + "  \"action\": \"new\",\n"
                + "  \"id\": \"\",\n"
                + "  \"parentId\": \"a9c5d4a0-e519-4750-9995-3b9dfadce096\",\n"
                + "  \"workspaceId\": \"a9c5d4a0-e519-4750-9995-3b9dfadce096\",\n"
                + "  \"messagePersona\": \"default\",\n"
                + "  \"model\": \"gpt-3.5-turbo\",\n"
                + "  \"messages\": [],\n"
                + "  \"internetMode\": \"never\",\n"
                + "  \"hidden\": false\n"
                + "}";

        try {
            URL url = new URL("https://streaming-worker.forefront.workers.dev/chat");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(body);
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
            else {
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        Pattern regex = Pattern.compile("event: enddata: (.+)");
        Matcher matcher = regex.matcher(response);

        if (matcher.find()) {
            String matchedString = matcher.group(1);
            response = new StringBuffer(matchedString);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set("Access-Control-Allow-Origin", "*");
        return new ResponseEntity<>("{ \"data\": " + response + "}", headers, HttpStatus.OK);
    }
}
