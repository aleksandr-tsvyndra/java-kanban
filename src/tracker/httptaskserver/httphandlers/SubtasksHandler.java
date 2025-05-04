package tracker.httptaskserver.httphandlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.TaskInteractionException;
import tracker.model.Subtask;

import java.io.IOException;
import java.util.NoSuchElementException;

public class SubtasksHandler extends TasksHandler {
    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
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

    private void handleGetId(HttpExchange h, int id) throws IOException {
        var sub = taskManager.getSubtaskById(id);
        sendResponse(h, 200, gson.toJson(sub));
    }

    private void handleGet(HttpExchange h) throws IOException {
        var allSubs = taskManager.getAllSubtasks();
        sendResponse(h, 200, gson.toJson(allSubs));
    }

    private void handlePostId(HttpExchange h, int id) throws IOException {
        var sub = gson.fromJson(getRequestBody(h), Subtask.class);
        sub.setId(id);
        taskManager.updateSubtask(sub);
        sendResponse(h, 201, gson.toJson(sub));
    }

    private void handlePost(HttpExchange h) throws IOException {
        var sub = gson.fromJson(getRequestBody(h), Subtask.class);
        taskManager.addNewSubtask(sub, sub.getEpicId());
        sendResponse(h, 201, gson.toJson(sub));
    }

    private void handleDeleteId(HttpExchange h, int id) throws IOException {
        var sub = taskManager.getSubtaskById(id);
        taskManager.deleteSubtaskById(id);
        sendResponse(h, 200, gson.toJson(sub));
    }
}
