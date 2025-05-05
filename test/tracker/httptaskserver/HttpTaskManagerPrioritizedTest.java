package tracker.httptaskserver;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.controllers.TaskManager;
import tracker.model.Task;
import tracker.util.Managers;
import tracker.util.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerPrioritizedTest {
    // создаём экземпляр трекера InMemoryHistoryManager
    TaskManager taskManager = Managers.getDefault();
    // экземпляр TaskManager передаём в конструктор класса HttpTaskServer
    HttpTaskServer server = new HttpTaskServer(taskManager);
    Gson gson = server.getGson();

    public HttpTaskManagerPrioritizedTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        server.start();
    }

    @AfterEach
    public void shutDown() {
        taskManager.deleteAllTasks();
        server.stop();
    }

    @Test
    @DisplayName("Запрашиваем список приоритетных задач")
    public void shouldGetPrioritizedTasksList() throws IOException, InterruptedException {
        // добавляем три таска в список приоритетных задач
        taskManager.addNewTask(new Task("Task 1", "description", 0, TaskStatus.NEW,
                LocalDateTime.parse("2025-05-05T15:00:00.0"), Duration.ofMinutes(0)));
        taskManager.addNewTask(new Task("Task 2", "description", 0, TaskStatus.NEW,
                LocalDateTime.parse("2025-05-05T16:00:00.0"), Duration.ofMinutes(0)));
        taskManager.addNewTask(new Task("Task 3", "description", 0, TaskStatus.NEW,
                LocalDateTime.parse("2025-05-05T17:00:00.0"), Duration.ofMinutes(0)));

        // проверяем, попали ли таски в список приоритетных задач
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertNotNull(prioritizedTasks, "Список приоритетных задач не возвращается");
        assertEquals(3, prioritizedTasks.size(), "Некорректное количество задач");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученные задачи из JSON в список объектов Task
        List<Task> prioritizedFromJson = gson.fromJson(response.body(), new HttpTaskManagerTasksTest.TaskListTypeToken().getType());

        assertNotNull(prioritizedFromJson, "Задачи не вернулись");
        assertEquals(3, prioritizedFromJson.size(), "Некорректное количество задач");
    }

    @Test
    @DisplayName("Пробуем отправить HTTP-запрос с методом DELETE")
    public void shouldReturn405StatusCode() throws IOException, InterruptedException {
        // добавляем таск в список приоритетных задач
        taskManager.addNewTask(new Task("Task 1", "description", 0, TaskStatus.NEW,
                LocalDateTime.parse("2025-05-05T15:00:00.0"), Duration.ofMinutes(0)));

        // создаём HTTP-клиент и запрос c методом DELETE, который не поддерживается
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа - должен вернуться код ошибки 405
        assertEquals(405, response.statusCode());
    }

    @Test
    @DisplayName("Пробуем получить задачу по конкретному id")
    public void shouldReturn400StatusCode() throws IOException, InterruptedException {
        // добавляем таск в список приоритетных задач
        int id = taskManager.addNewTask(new Task("Task 1", "description", 0, TaskStatus.NEW,
                LocalDateTime.parse("2025-05-05T15:00:00.0"), Duration.ofMinutes(0)));

        // создаём HTTP-клиент и запрос c эндпоинтом, который не поддерживается сервером
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа - должен вернуться код ошибки 400
        assertEquals(400, response.statusCode());
    }
}
