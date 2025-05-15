package tracker.httptaskserver.httphandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.ErrorResponse;
import tracker.httptaskserver.typeadapters.DurationAdapter;
import tracker.httptaskserver.typeadapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    protected TaskManager taskManager;
    protected final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    protected void sendResponse(HttpExchange h, int rCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(rCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected String getRequestBody(HttpExchange h) throws IOException {
        InputStream requestBodyStream = h.getRequestBody();
        byte[] requestBodyBytes = requestBodyStream.readAllBytes();
        return new String(requestBodyBytes, StandardCharsets.UTF_8);
    }

    protected void handleBadRequest(HttpExchange h) throws IOException {
        String erMessage = "Сервер обнаружил в запросе клиента синтаксическую ошибку.";
        var resp = new ErrorResponse(erMessage, 400, h.getRequestURI());
        sendResponse(h, resp.getErrorCode(), gson.toJson(resp));
    }

    protected void handleMethodNotAllowed(HttpExchange h, String requestMethod) throws IOException {
        String erMessage = String.format("HTTP-метод %s не поддерживается.", requestMethod);
        var resp = new ErrorResponse(erMessage, 405, h.getRequestURI());
        sendResponse(h, resp.getErrorCode(), gson.toJson(resp));
    }

    protected void handleException(HttpExchange h, int rCode, String erMessage) throws IOException {
        var resp = new ErrorResponse(erMessage, rCode, h.getRequestURI());
        sendResponse(h, resp.getErrorCode(), gson.toJson(resp));
    }

    public Gson getGson() {
        return gson;
    }
}
