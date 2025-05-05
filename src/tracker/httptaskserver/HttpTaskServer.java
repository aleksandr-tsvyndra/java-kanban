package tracker.httptaskserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import tracker.controllers.TaskManager;
import tracker.httptaskserver.typeadapters.DurationAdapter;
import tracker.httptaskserver.typeadapters.LocalDateTimeAdapter;
import tracker.util.Managers;

import tracker.httptaskserver.httphandlers.EpicsHandler;
import tracker.httptaskserver.httphandlers.HistoryHandler;
import tracker.httptaskserver.httphandlers.PrioritizedHandler;
import tracker.httptaskserver.httphandlers.SubtasksHandler;
import tracker.httptaskserver.httphandlers.TasksHandler;

public class HttpTaskServer {
    private final HttpServer httpServer;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        httpServer.createContext("/tasks", new TasksHandler(taskManager, gson));
        httpServer.createContext("/epics", new EpicsHandler(taskManager, gson));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager, gson));
        httpServer.createContext("/history", new HistoryHandler(taskManager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
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
        return gson;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault());

        server.start(); // запускаем сервер
        System.out.println("HTTP-сервер запущен и готов к работе!");

        server.stop(); // завершение работы сервера
    }
}