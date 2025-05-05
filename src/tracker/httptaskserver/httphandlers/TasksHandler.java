package tracker.httptaskserver.httphandlers;

import com.sun.net.httpserver.HttpExchange;

import com.sun.net.httpserver.HttpHandler;
import tracker.exceptions.TaskInteractionException;
import tracker.model.Task;
import tracker.controllers.TaskManager;
import java.io.IOException;
import java.util.NoSuchElementException;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    public TasksHandler(TaskManager taskManger) {
        super(taskManger);
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