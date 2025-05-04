package tracker.httptaskserver.httphandlers;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpExchange;

import tracker.controllers.TaskManager;
import tracker.exceptions.ErrorResponse;
import tracker.exceptions.TaskInteractionException;
import tracker.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.NoSuchElementException;

public class TasksHandler extends BaseHttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String requestMethod = h.getRequestMethod();
            String[] pathParts = h.getRequestURI().getPath().split("/");
            switch (requestMethod) {
                case "GET":
                    if (pathParts.length == 3) {
                        int id = Integer.parseInt(pathParts[2]);
                        handleGetId(h, id);
                    } else if (pathParts.length == 2) {
                        handleGet(h);
                    } else {
                        handleBadRequest(h);
                    }
                    break;
                case "POST":
                    if (pathParts.length == 3) {
                        int id = Integer.parseInt(pathParts[2]);
                        handlePostId(h, id);
                    } else if (pathParts.length == 2) {
                        handlePost(h);
                    } else {
                        handleBadRequest(h);
                    }
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        int id = Integer.parseInt(pathParts[2]);
                        handleDeleteId(h, id);
                    } else {
                        handleBadRequest(h);
                    }
                    break;
                default:
                    handleMethodNotAllowed(h, requestMethod);
            }
        } catch (NumberFormatException e) {
            String erMessage = "Указанный вами идентификатор не является целым числом.";
            handleException(h, 400, erMessage);
        } catch (NoSuchElementException e) {
            handleException(h, 404, e.getMessage());
        } catch (TaskInteractionException e) {
            handleException(h, 406, e.getMessage());
        } catch (Exception e) {
            handleException(h, 500, e.getMessage());
        } finally {
            h.close();
        }
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

    private void handleGetId(HttpExchange h, int id) throws IOException {
        var task = taskManager.getTaskById(id);
        sendResponse(h, 200, gson.toJson(task));
    }

    private void handleGet(HttpExchange h) throws IOException {
        var allTasks = taskManager.getAllTasks();
        sendResponse(h, 200, gson.toJson(allTasks));
    }

    private void handlePostId(HttpExchange h, int id) throws IOException {
        var task = gson.fromJson(getRequestBody(h), Task.class);
        task.setId(id);
        taskManager.updateTask(task);
        sendResponse(h, 201, gson.toJson(task));
    }

    private void handlePost(HttpExchange h) throws IOException {
        var task = gson.fromJson(getRequestBody(h), Task.class);
        taskManager.addNewTask(task);
        sendResponse(h, 201, gson.toJson(task));
    }

    private void handleDeleteId(HttpExchange h, int id) throws IOException {
        var task = taskManager.getTaskById(id);
        taskManager.deleteTaskById(id);
        sendResponse(h, 200, gson.toJson(task));
    }
}