package tracker.httptaskserver;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.Gson;
import tracker.controllers.TaskManager;
import tracker.util.Managers;
import tracker.model.Task;
import tracker.util.TaskStatus;

public class HttpTaskManagerTasksTest {
    // создаём экземпляр InMemoryTaskManager с помощью утилитарного класса Managers
    TaskManager manager = Managers.getDefault();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = server.getGson();

    static class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
        server.start();
    }

    @AfterEach
    public void shutDown() {
        server.stop();
    }

    @Test
    @DisplayName("Добавляем задачу в трекер")
    public void shouldAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 1", "Testing task 1", 0,
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задача не добавилась в трекер");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 1", tasksFromManager.get(0).getTitle(), "Некорректное имя задачи");
    }

    @Test
    @DisplayName("Обновляем задачу в трекере")
    public void shouldUpdateTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 1", "Testing task 1", 0,
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        // добавляем задачу в трекер
        int id = manager.addNewTask(task);

        // создаём задачу, которая обновит добавленную ранее
        Task task1 = new Task("Test_UPDATED", "Testing task_UPDATED", id,
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        // конвертируем её в JSON
        String taskJson = gson.toJson(task1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/tasks/%d", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что задача корректно обновилась
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test_UPDATED", tasksFromManager.get(0).getTitle(), "Некорректное имя задачи");
        assertEquals("Testing task_UPDATED", tasksFromManager.get(0).getDescription(), "Некорректное описание задачи");
    }

    @Test
    @DisplayName("Получаем задачу по id из трекера")
    public void shouldGetTaskById() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 1", "Testing task 1", 0,
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        // добавляем задачу в трекер
        int id = manager.addNewTask(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/tasks/%d", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученную задачу из JSON в Task
        Task taskFromJson = gson.fromJson(response.body(), Task.class);

        assertNotNull(taskFromJson, "Задача не вернулась");
        assertEquals("Test 1", taskFromJson.getTitle(), "Некорректное имя задачи");
        assertEquals(id, taskFromJson.getId(), "Некорректный id задачи");
    }

    @Test
    @DisplayName("Получаем все задачи из трекера")
    public void shouldGetAllTasks() throws IOException, InterruptedException {
        // создаём и добавляем пять задач в трекер
        for (int i = 0; i < 5; i++) {
            Task task = new Task("Test 1", "Testing task 1", 0, TaskStatus.NEW);
            manager.addNewTask(task);
        }

        // проверяем, что задачи добавились в трекер
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не добавились в трекер");
        assertEquals(5, tasksFromManager.size(), "Некорректное количество задач");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученные задачи из JSON в список объектов Task
        List<Task> tasksFromJson = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertNotNull(tasksFromJson, "Задачи не вернулись");
        assertEquals(5, tasksFromJson.size(), "Некорректное количество задач");
    }

    @Test
    @DisplayName("Удаляем задачу по id из трекера")
    public void shouldDeleteTaskById() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 1", "Testing task 1", 0,
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        // добавляем задачу в трекер
        int id = manager.addNewTask(task);

        // проверяем, что задача добавилась в трекер
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задача не добавилась в трекер");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/tasks/%d", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что задача корректно обновилась
        List<Task> tasksFromManager1 = manager.getAllTasks();

        assertNotNull(tasksFromManager1, "Список задач не возвращается");
        assertEquals(0, tasksFromManager1.size(), "Некорректное количество задач");
    }

    @Test
    @DisplayName("Запрашиваем задачу по id, которой нет в трекере")
    public void shouldReturn404StatusCode() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 1", "Testing task 1", 0,
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        // добавляем задачу в трекер
        manager.addNewTask(task);

        // проверяем, что задача добавилась в трекер
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задача не добавилась в трекер");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(1, tasksFromManager.get(0).getId(), "У добавленной задачи некорректный id");

        // будем запрашивать задачу по id 2, которой нет в трекере
        int idForRequest = 2;
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/tasks/%d", idForRequest));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("Добавляем задачу, пересекающуюся по времени с существующей")
    public void shouldReturn406StatusCode() throws IOException, InterruptedException {
        // создаём задачу и устанавливаем ей стартовое время
        LocalDateTime taskStartTime = LocalDateTime.parse("2025-05-05T15:00:00.0");
        Task task = new Task("Test", "Testing task", 0,
                TaskStatus.NEW, taskStartTime, Duration.ofMinutes(10));
        // добавляем задачу в трекер
        manager.addNewTask(task);

        // проверяем, что задача добавилась в трекер
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задача не добавилась в трекер");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");

        // устанавливаем время старта второй задачи, чтобы оно пересекалось с временем первой
        LocalDateTime task1StartTime = LocalDateTime.parse("2025-05-05T15:05:00.0");
        Task task1 = new Task("Test 1", "Testing task 1", 0,
                TaskStatus.NEW, task1StartTime, Duration.ofMinutes(10));

        // конвертируем задачу с пересекающимся временем в JSON
        String task1Json = gson.toJson(task1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(task1Json)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode());

        // проверяем, что в трекере по-прежнему осталась одна задача
        List<Task> tasksFromManager1 = manager.getAllTasks();

        assertNotNull(tasksFromManager1, "Список задач не возвращается");
        assertEquals(1, tasksFromManager1.size(), "Некорректное количество задач");
    }
}
