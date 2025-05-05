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
import java.util.List;

import com.google.gson.Gson;
import tracker.controllers.TaskManager;
import tracker.util.Managers;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.util.TaskStatus;

public class HttpTaskManagerEpicsTest {
    // создаём экземпляр InMemoryTaskManager с помощью утилитарного класса Managers
    TaskManager manager = Managers.getDefault();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = server.getGson();

    static class EpicListTypeToken extends TypeToken<List<Epic>> {
    }

    public HttpTaskManagerEpicsTest() throws IOException {
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
    @DisplayName("Добавляем эпик в трекер")
    public void shouldAddEpic() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Epic 1", "Testing epic 1", 0);
        // конвертируем его в JSON
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создался один эпик с корректным именем
        List<Epic> epicsFromManager = manager.getAllEpics();

        assertNotNull(epicsFromManager, "Эпик не добавился в трекер");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 1", epicsFromManager.get(0).getTitle(), "Некорректное имя эпика");
    }

    @Test
    @DisplayName("Обновляем эпик в трекере")
    public void shouldUpdateEpic() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Epic 1", "Testing epic 1", 0);
        // добавляем эпик в трекер
        int id = manager.addNewEpic(epic);

        // создаём эпик, который обновит добавленный ранее
        Epic epic1 = new Epic("Epic 1_UPDATED", "Testing epic 1_UPDATED", id);
        // конвертируем его в JSON
        String epicJson = gson.toJson(epic1);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/epics/%d", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что эпик корректно обновился
        List<Epic> epicsFromManager = manager.getAllEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 1_UPDATED", epicsFromManager.get(0).getTitle(), "Некорректное имя эпика");
        assertEquals("Testing epic 1_UPDATED", epicsFromManager.get(0).getDescription(), "Некорректное описание эпика");
    }

    @Test
    @DisplayName("Получаем эпик по id из трекера")
    public void shouldGetEpicById() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Epic 1", "Testing epic 1", 0);
        // добавляем эпик в трекер
        int id = manager.addNewEpic(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/epics/%d", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученный эпик из JSON в Task
        Epic epicFromJson = gson.fromJson(response.body(), Epic.class);

        assertNotNull(epicFromJson, "Эпик не вернулся");
        assertEquals("Epic 1", epicFromJson.getTitle(), "Некорректное имя эпика");
        assertEquals(id, epicFromJson.getId(), "Некорректный id эпика");
    }

    @Test
    @DisplayName("Получаем эпик с подзадачей по id из трекера")
    public void shouldGetEpicWithSubById() throws IOException, InterruptedException {
        // создаём эпик и добавляем в него подзадачу
        int id = manager.addNewEpic(new Epic("Epic 1", "Testing epic 1", 0));
        manager.addNewSubtask(new Subtask("Sub 1", "Testing sub 1", 0, TaskStatus.NEW), id);


        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/epics/%d", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученный эпик из JSON в Task
        Epic epicFromJson = gson.fromJson(response.body(), Epic.class);

        assertNotNull(epicFromJson, "Эпик не вернулся");
        assertEquals("Epic 1", epicFromJson.getTitle(), "Некорректное имя эпика");
        assertEquals(id, epicFromJson.getId(), "Некорректный id эпика");

        // проверяем, что подзадача вернулась вместе с эпиком
        List<Subtask> subsFromEpic = epicFromJson.getEpicSubtasks();

        assertNotNull(subsFromEpic, "Подзадача не вернулась вместе с эпиком");
        assertEquals(1, subsFromEpic.size(), "Некорректное количество подзадач");
        assertEquals("Sub 1", subsFromEpic.get(0).getTitle(), "Некорректное имя подзадачи");
    }

    @Test
    @DisplayName("Получаем все эпики из трекера")
    public void shouldGetAllEpics() throws IOException, InterruptedException {
        // создаём и добавляем пять эпиков в трекер
        for (int i = 0; i < 5; i++) {
            Epic epic = new Epic("Epic 1", "Testing epic 1", 0);
            manager.addNewEpic(epic);
        }

        // проверяем, что эпики добавились в трекер
        List<Epic> epicsFromManager = manager.getAllEpics();

        assertNotNull(epicsFromManager, "Эпики не добавились в трекер");
        assertEquals(5, epicsFromManager.size(), "Некорректное количество эпиков");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученные эпики из JSON в список объектов Epic
        List<Epic> epicsFromJson = gson.fromJson(response.body(), new EpicListTypeToken().getType());

        assertNotNull(epicsFromJson, "Эпики не вернулись");
        assertEquals(5, epicsFromJson.size(), "Некорректное количество эпиков");
    }

    @Test
    @DisplayName("Удаляем эпик по id из трекера")
    public void shouldDeleteEpicById() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Epic 1", "Testing epic 1", 0);
        // добавляем эпик в трекер
        int id = manager.addNewEpic(epic);

        // проверяем, что эпик добавился в трекер
        List<Epic> epicsFromManager = manager.getAllEpics();

        assertNotNull(epicsFromManager, "Эпик не добавился в трекер");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/epics/%d", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что эпик удалился из трекера
        List<Epic> epicsFromManager1 = manager.getAllEpics();

        assertNotNull(epicsFromManager1, "Список эпиков не возвращается");
        assertEquals(0, epicsFromManager1.size(), "Некорректное количество эпиков");
    }

    @Test
    @DisplayName("Запрашиваем эпик по id, которого нет в трекере")
    public void shouldReturn404StatusCode() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Epic 1", "Testing epic 1", 0);
        // добавляем эпик в трекер
        manager.addNewEpic(epic);

        // проверяем, что эпик добавился в трекер
        List<Epic> epicsFromManager = manager.getAllEpics();

        assertNotNull(epicsFromManager, "Эпик не добавился в трекер");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals(1, epicsFromManager.get(0).getId(), "У добавленного эпика некорректный id");

        // будем запрашивать эпик по id 2, которого нет в трекере
        int idForRequest = 2;
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/epics/%d", idForRequest));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("Запрашиваем подзадачи эпика по его id")
    public void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Epic 1", "Testing epic 1", 0);
        // добавляем эпик в трекер
        int id = manager.addNewEpic(epic);

        // создаём две подзадачи и добавляем их в эпик
        Subtask sub1 = new Subtask("Sub 1", "Testing sub 1", 0, TaskStatus.NEW);
        Subtask sub2 = new Subtask("Sub 2", "Testing sub 2", 0, TaskStatus.NEW);
        manager.addNewSubtask(sub1, id);
        manager.addNewSubtask(sub2, id);

        // проверяем, что эпик и подзадачи добавились в трекер
        List<Epic> epicsFromManager = manager.getAllEpics();
        List<Subtask> subsFromEpic = epicsFromManager.get(0).getEpicSubtasks();

        assertNotNull(epicsFromManager, "Эпик не добавился в трекер");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertNotNull(subsFromEpic, "Подзадачи не добавились в трекер");
        assertEquals(2, subsFromEpic.size(), "Некорректное количество подзадач");

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(String.format("http://localhost:8080/epics/%d/subtasks", id));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // отправляем запрос с обновлением на сервер и получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученные эпики из JSON в список объектов Epic
        List<Subtask> subsFromJson = gson.fromJson(response.body(), new HttpTaskManagerSubtasksTest.SubtaskListTypeToken().getType());

        assertNotNull(subsFromJson, "Подзадачи не вернулись");
        assertEquals(2, subsFromJson.size(), "Некорректное количество подзадач");
    }
}
