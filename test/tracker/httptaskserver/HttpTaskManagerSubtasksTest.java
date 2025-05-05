package tracker.httptaskserver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
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

public class HttpTaskManagerSubtasksTest {
    // создаём экземпляр InMemoryTaskManager с помощью утилитарного класса Managers
    TaskManager manager = Managers.getDefault();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = server.getGson();
    int epicId;

    static class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
    }

    public HttpTaskManagerSubtasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
        server.start();
        epicId = manager.addNewEpic(new Epic("Epic 1", "Testing epic 1", 0));
    }

    @AfterEach
    public void shutDown() {
        server.stop();
    }

    @Test
    @DisplayName("Добавляем подзадачу в трекер")
    public void shouldAddSubtask() throws IOException, InterruptedException {
        // создаём подзадачу для добавления в трекер и привязываем её к эпику
        Subtask sub = new Subtask("Sub 1", "Testing sub 1", 0, TaskStatus.NEW);
        sub.setEpicId(epicId);

        // конвертируем подзадачу в JSON
        String subJson = gson.toJson(sub);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна подзадача с корректным именем
        List<Subtask> subsFromManager = manager.getAllSubtasks();

        assertNotNull(subsFromManager, "Подзадача не добавилась в трекер");
        assertEquals(1, subsFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Sub 1", subsFromManager.get(0).getTitle(), "Некорректное имя подзадачи");
    }

    @Test
    @DisplayName("Добавляем подзадачу, пересекающуюся по времени с существующей")
    public void shouldReturn406StatusCode() throws IOException, InterruptedException {
        // создаём подзадачу и устанавливаем ей стартовое время
        LocalDateTime subStartTime = LocalDateTime.parse("2025-05-05T15:00:00.0");
        Subtask sub = new Subtask("Sub", "Testing sub", 0,
                TaskStatus.NEW, subStartTime, Duration.ofMinutes(10));
        // добавляем подзадачу в трекер
        manager.addNewSubtask(sub, epicId);

        // проверяем, что подзадача добавилась в трекер
        List<Subtask> subsFromManager = manager.getAllSubtasks();

        assertNotNull(subsFromManager, "Подзадача не добавилась в трекер");
        assertEquals(1, subsFromManager.size(), "Некорректное количество подзадач");

        // устанавливаем время старта второй подзадачи, чтобы оно пересекалось с временем первой
        LocalDateTime task1StartTime = LocalDateTime.parse("2025-05-05T15:05:00.0");
        Subtask sub1 = new Subtask("Sub 1", "Testing sub 1", 0,
                TaskStatus.NEW, task1StartTime, Duration.ofMinutes(10));
        sub1.setEpicId(epicId);

        // конвертируем подзадачу с пересекающимся временем в JSON
        String sub1Json = gson.toJson(sub1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(sub1Json)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode());

        // проверяем, что в трекере по-прежнему осталась одна подзадача
        List<Subtask> subsFromManager1 = manager.getAllSubtasks();

        assertNotNull(subsFromManager1, "Список подзадач не возвращается");
        assertEquals(1, subsFromManager1.size(), "Некорректное количество подзадач");
    }

    @Test
    @DisplayName("Обновляем подзадачу по id в трекере")
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        // добавляем в трекер подзадачу, которую мы будем обновлять
        Subtask sub = new Subtask("Sub", "Testing sub", 0, TaskStatus.NEW);
        int subId = manager.addNewSubtask(sub, epicId);

        // создаём подзадачу, которая обновит добавленную ранее
        Subtask sub1 = new Subtask("Sub_UPDATED", "Testing sub", subId, TaskStatus.NEW);
        // конвертируем её в JSON
        String sub1Json = gson.toJson(sub1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%d", subId));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(sub1Json)).build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что подзадача корректно обновилась
        List<Subtask> subsFromManager = manager.getAllSubtasks();

        assertNotNull(subsFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subsFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Sub_UPDATED", subsFromManager.get(0).getTitle(), "Некорректное имя подзадачи");
    }

    @Test
    @DisplayName("Запрашиваем подзадачу по id")
    public void shouldGetSubtaskById() throws IOException, InterruptedException {
        // добавляем подзадачу в трекер
        manager.addNewSubtask(new Subtask("Sub", "Testing sub", 2, TaskStatus.NEW), epicId);

        // проверяем, что подзадача добавилась в трекер
        List<Subtask> subsFromManager = manager.getAllSubtasks();

        assertNotNull(subsFromManager, "Подзадача не добавилась в трекер");
        assertEquals(1, subsFromManager.size(), "Некорректное количество подзадач");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%d", 2));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученную задачу из JSON в Task
        Subtask subFromJson = gson.fromJson(response.body(), Subtask.class);

        assertNotNull(subFromJson, "Подзадача не вернулась");
        assertEquals("Sub", subFromJson.getTitle(), "Некорректное имя подзадачи");
        assertEquals(2, subFromJson.getId(), "Некорректный id подзадачи");
    }

    @Test
    @DisplayName("Запрашиваем все подзадачи из трекера")
    public void shouldGetAllSubtasks() throws IOException, InterruptedException {
        // добавляем пять подзадач в трекер
        for (int i = 0; i < 5; i++) {
            Subtask sub = new Subtask("Sub", "Testing sub", 0, TaskStatus.NEW);
            manager.addNewSubtask(sub, epicId);
        }

        // проверяем, что подзадачи добавились в трекер
        List<Subtask> subsFromManager = manager.getAllSubtasks();

        assertNotNull(subsFromManager, "Подзадачи не добавилась в трекер");
        assertEquals(5, subsFromManager.size(), "Некорректное количество подзадач");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученные подзадачи из JSON в список объектов Subtasks
        List<Subtask> subsFromJson = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertNotNull(subsFromJson, "Подзадачи не вернулись");
        assertEquals(5, subsFromJson.size(), "Некорректное количество подзадач");
    }

    @Test
    @DisplayName("Запрашиваем удаление подзадачи по id")
    public void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        // добавляем подзадачу в трекер
        int id = manager.addNewSubtask(new Subtask("Sub", "Testing sub", 0, TaskStatus.NEW), epicId);

        // проверяем, что подзадача добавилась в трекер
        List<Subtask> subsFromManager = manager.getAllSubtasks();

        assertNotNull(subsFromManager, "Подзадача не добавилась в трекер");
        assertEquals(1, subsFromManager.size(), "Некорректное количество подзадач");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%d", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что подзадача удалилась из трекера
        List<Subtask> subsFromManager1 = manager.getAllSubtasks();

        assertNotNull(subsFromManager1, "Список подзадач не вернулся");
        assertEquals(0, subsFromManager1.size(), "Некорректное количество подзадач");
    }

    @Test
    @DisplayName("Запрашиваем подзадачу по id, которой нет в трекере")
    public void shouldReturn404StatusCode() throws IOException, InterruptedException {
        // создаём подзадачу
        Subtask sub = new Subtask("Sub 1", "Testing sub 1", 0, TaskStatus.NEW);
        // добавляем подзадачу в трекер
        manager.addNewSubtask(sub, epicId);

        // проверяем, что подзадача добавилась в трекер
        List<Subtask> subsFromManager = manager.getAllSubtasks();

        assertNotNull(subsFromManager, "Подзадача не добавилась в трекер");
        assertEquals(1, subsFromManager.size(), "Некорректное количество подзадач");
        assertEquals(2, subsFromManager.get(0).getId(), "У добавленной задачи некорректный id");

        // будем запрашивать подзадачу по id 3, которой нет в трекере
        int idForRequest = 3;
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/subtasks/%d", idForRequest));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }
}
