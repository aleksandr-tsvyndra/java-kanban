package tracker.httptaskserver.httphandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.controllers.TaskManager;
import tracker.exceptions.TaskInteractionException;
import tracker.model.Epic;

import java.io.IOException;
import java.util.NoSuchElementException;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    public EpicsHandler(TaskManager taskManger) {
        super(taskManger);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String requestMethod = h.getRequestMethod();
            String[] pathParts = h.getRequestURI().getPath().split("/");
            switch (requestMethod) {
                case "GET":
                    if (pathParts.length == 4) {
                        int id = Integer.parseInt(pathParts[2]);
                        handleGetIdSubtasks(h, id);
                    } else if (pathParts.length == 3) {
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

    private void handleGetIdSubtasks(HttpExchange h, int id) throws IOException {
        var epic = taskManager.getEpicById(id);
        var epicSubs = epic.getEpicSubtasks();
        sendResponse(h, 200, gson.toJson(epicSubs));
    }

    private void handleGetId(HttpExchange h, int id) throws IOException {
        var epic = taskManager.getEpicById(id);
        sendResponse(h, 200, gson.toJson(epic));
    }

    private void handleGet(HttpExchange h) throws IOException {
        var allEpics = taskManager.getAllEpics();
        sendResponse(h, 200, gson.toJson(allEpics));
    }

    private void handlePostId(HttpExchange h, int id) throws IOException {
        var epic = gson.fromJson(getRequestBody(h), Epic.class);
        epic.setId(id);
        taskManager.updateEpic(epic);
        sendResponse(h, 201, gson.toJson(epic));
    }

    private void handlePost(HttpExchange h) throws IOException {
        var epic = gson.fromJson(getRequestBody(h), Epic.class);
        taskManager.addNewEpic(epic);
        sendResponse(h, 201, gson.toJson(epic));
    }

    private void handleDeleteId(HttpExchange h, int id) throws IOException {
        var epic = taskManager.getEpicById(id);
        taskManager.deleteEpicById(id);
        sendResponse(h, 200, gson.toJson(epic));
    }
}
