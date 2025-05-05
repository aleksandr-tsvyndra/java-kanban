package tracker.httptaskserver.httphandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.exceptions.ErrorResponse;
import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    public PrioritizedHandler(TaskManager taskManger) {
        super(taskManger);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String requestMethod = h.getRequestMethod();
            if (requestMethod.equals("GET")) {
                String[] pathParts = h.getRequestURI().getPath().split("/");
                if (pathParts.length == 2) {
                    var prioritizedTasks = taskManager.getPrioritizedTasks();
                    String responseBody = gson.toJson(prioritizedTasks);
                    sendResponse(h, 200, responseBody);
                } else {
                    String erMessage = "Сервер обнаружил в запросе клиента синтаксическую ошибку.";
                    var resp = new ErrorResponse(erMessage, 400, h.getRequestURI());
                    sendResponse(h, resp.getErrorCode(), gson.toJson(resp));
                }
            } else {
                String erMessage = String.format("HTTP-метод %s не поддерживается.", requestMethod);
                var resp = new ErrorResponse(erMessage, 405, h.getRequestURI());
                sendResponse(h, resp.getErrorCode(), gson.toJson(resp));
            }
        } catch (Exception e) {
            var resp = new ErrorResponse(e.getMessage(), 500, h.getRequestURI());
            sendResponse(h, resp.getErrorCode(), gson.toJson(resp));
        } finally {
            h.close();
        }
    }
}
