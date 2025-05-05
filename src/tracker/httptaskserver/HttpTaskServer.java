package tracker.httptaskserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.io.IOException;
import tracker.controllers.TaskManager;
import tracker.httptaskserver.httphandlers.BaseHttpHandler;
import tracker.util.Managers;

import tracker.httptaskserver.httphandlers.EpicsHandler;
import tracker.httptaskserver.httphandlers.HistoryHandler;
import tracker.httptaskserver.httphandlers.PrioritizedHandler;
import tracker.httptaskserver.httphandlers.SubtasksHandler;
import tracker.httptaskserver.httphandlers.TasksHandler;

public class HttpTaskServer {
    private final HttpServer httpServer;
    private final TaskManager taskManager;
    private final BaseHttpHandler baseHttpHandler;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        this.taskManager = taskManager;
        baseHttpHandler = new BaseHttpHandler(taskManager);
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
    }

    public TaskManager getManager() {
        return taskManager;
    }

    public Gson getGson() {
        return baseHttpHandler.getGson();
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault());

        server.start(); // запускаем сервер
        System.out.println("HTTP-сервер запущен и готов к работе!");

        server.stop(); // завершение работы сервера
    }
}