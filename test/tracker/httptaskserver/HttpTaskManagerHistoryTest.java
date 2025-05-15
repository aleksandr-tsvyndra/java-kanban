package tracker.httptaskserver;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.controllers.HistoryManager;
import tracker.controllers.InMemoryHistoryManager;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Task;
import tracker.util.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerHistoryTest {
    // создаём экземпляр InMemoryHistoryManager
    HistoryManager historyManager = new InMemoryHistoryManager();
    // передаём его в качестве аргумента в конструктор TaskManager
    TaskManager taskManager = new InMemoryTaskManager(historyManager);
    // экземпляр TaskManager передаём в конструктор класса HttpTaskServer
    HttpTaskServer server = new HttpTaskServer(taskManager);
    Gson gson = server.getGson();

    public HttpTaskManagerHistoryTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        server.start();
        for (int i = 1; i <= 5; i++) {
            Task task = new Task("Test", "Testing task", i, TaskStatus.NEW);
            historyManager.add(task);
        }
    }

    @AfterEach
    public void shutDown() {
        for (int i = 1; i <= 5; i++) {
            historyManager.remove(i);
        }
        server.stop();
    }

    @Test
    @DisplayName("Запрашиваем задачи из истории просмотров")
    public void shouldGetHistoryViews() throws IOException, InterruptedException {
        // проверяем, есть ли в истории просмотров задачи для запроса
        List<Task> history = taskManager.getHistory();

        assertNotNull(history, "История просмотров не возвращается");
        assertEquals(5, history.size(), "В истории просмотров некорректное количество задач");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученные задачи из JSON в список объектов Task
        List<Task> historyFromJson = gson.fromJson(response.body(), new HttpTaskManagerTasksTest.TaskListTypeToken().getType());

        assertNotNull(historyFromJson, "Задачи не вернулись");
        assertEquals(5, historyFromJson.size(), "Некорректное количество задач");
    }

    @Test
    @DisplayName("Пробуем отправить HTTP-запрос с методом DELETE")
    public void shouldReturn405StatusCode() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос c методом DELETE, который не поддерживается
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа - должен вернуться код ошибки 405
        assertEquals(405, response.statusCode());
    }

    @Test
    @DisplayName("Пробуем получить задачу по конкретному id")
    public void shouldReturn400StatusCode() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос c эндпоинтом, который не поддерживается сервером
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа - должен вернуться код ошибки 400
        assertEquals(400, response.statusCode());
    }
}
