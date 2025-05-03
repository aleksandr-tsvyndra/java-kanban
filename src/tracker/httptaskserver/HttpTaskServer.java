package tracker.httptaskserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.sun.net.httpserver.HttpHandler;
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
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
    }

    public void createContext(String path, HttpHandler handler) {
        httpServer.createContext(path, handler);
    }

    public TaskManager getManager() {
        return taskManager;
    }

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault());

        // добавляем обработчики в сервер
        server.createContext("/tasks", new TasksHandler(server.getManager(), server.getGson()));
        server.createContext("/epics", new EpicsHandler(server.getManager(), server.getGson()));
        server.createContext("/subtasks", new SubtasksHandler(server.getManager(), server.getGson()));
        server.createContext("/history", new HistoryHandler(server.getManager(), server.getGson()));
        server.createContext("/prioritized", new PrioritizedHandler(server.getManager(), server.getGson()));

        server.start(); // запускаем сервер
        System.out.println("HTTP-сервер запущен и готов к работе!");

        //server.stop(); // завершение работы сервера
    }
}