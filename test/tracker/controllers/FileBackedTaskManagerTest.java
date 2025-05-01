package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;

import tracker.model.Task;
import tracker.model.Epic;
import tracker.model.Subtask;

import java.time.Duration;
import java.time.LocalDateTime;

import tracker.util.TaskStatus;

class FileBackedTaskManagerTest extends TaskManagerTest<TaskManager> {
    private File tmpFile;

    @BeforeEach
    void init() throws IOException {
        tmpFile = File.createTempFile("data", ".csv");
        taskManager = new FileBackedTaskManager(tmpFile, new InMemoryHistoryManager());
    }

    @Test
    @DisplayName("Восстановление трекера из пустого файла")
    void shouldLoadFromEmptyFile() {
        // Восстанавливаем трекер из пустого файла tmpFile
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(tmpFile);

        // Метод loadFromFile должен вернуть трекер
        assertNotNull(fileBackedTaskManager, "Метод должен возвращать экземпляр класса FileBackedTaskManager");
    }

    @Test
    @DisplayName("Восстановление трекера из файла с 3 задачами")
    void shouldLoadFromFile() throws IOException {
        // Создаём поток вывода и делаем запись в файл tmpFile
        Writer fileWriter = new FileWriter(tmpFile);
        fileWriter.write("id,type,name,status,description,start,duration,epic");
        fileWriter.write("\n1,TASK,Task1,NEW,Description task1,16.03.2025 12:14,10,");
        fileWriter.write("\n2,EPIC,Epic2,DONE,Description epic2,");
        fileWriter.write("\n3,SUBTASK,Subtask3,DONE,Description subtask3,17.03.2025 13:15,25,2");
        fileWriter.close();

        // Восстанавливаем трекер из файла tmpFile, в котором записаны три задачи
        taskManager = FileBackedTaskManager.loadFromFile(tmpFile);

        // Проверяем, что метод loadFromFile корректно обработал данные файла и добавил задачи в трекер
        assertEquals(1, taskManager.getAllTasks().size(), "В списке Тасков должна быть 1 задача");
        assertEquals(1, taskManager.getAllEpics().size(), "В списке Эпиков должна быть 1 задача");
        assertEquals(1, taskManager.getAllSubtasks().size(), "В списке Сабтасков должна быть 1 задача");

        // Создаём для проверки три задачи, идентичные тем, что были сохранены в файл
        var task = new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));
        var epic = new Epic("Epic2", "Description epic2", 2);
        var sub = new Subtask("Subtask3", "Description subtask3", 3, TaskStatus.DONE,
                LocalDateTime.of(2025, 3, 17, 13, 15), Duration.ofMinutes(25));
        epic.addSubtaskInEpic(sub);

        // Сравниваем задачи по id
        assertEquals(task, taskManager.getAllTasks().getFirst(), "Таски отличаются по id");
        assertEquals(epic, taskManager.getAllEpics().getFirst(), "Эпики отличаются по id");
        assertEquals(sub, taskManager.getAllSubtasks().getFirst(), "Сабтаски отличаются по id");

        // Сравниваем задачи по полям title
        assertEquals(task.getTitle(), taskManager.getTaskById(1).getTitle(), "Таски отличаются заголовком");
        assertEquals(epic.getTitle(), taskManager.getEpicById(2).getTitle(),
                "Эпики отличаются заголовком");
        assertEquals(sub.getTitle(), taskManager.getSubtaskById(3).getTitle(),
                "Сабтаски отличаются заголовком");

        // Сравниваем задачи по полям description
        assertEquals(task.getDescription(), taskManager.getTaskById(1).getDescription(),
                "Таски отличаются описанием");
        assertEquals(epic.getDescription(), taskManager.getEpicById(2).getDescription(),
                "Эпики отличаются описанием");
        assertEquals(sub.getDescription(), taskManager.getSubtaskById(3).getDescription(),
                "Сабтаски отличаются описанием");

        // Сравниваем задачи по полям status
        assertEquals(task.getStatus(), taskManager.getTaskById(1).getStatus(), "Таски отличаются статусом");
        assertEquals(epic.getStatus(), taskManager.getEpicById(2).getStatus(),
                "Эпики отличаются статусом");
        assertEquals(sub.getStatus(), taskManager.getSubtaskById(3).getStatus(),
                "Сабтаски отличаются статусом");

        // Сравниваем Таски по времени начала, длительности и окончания задачи
        assertEquals(task.getStartTime(), taskManager.getTaskById(1).getStartTime(),
                "Таски отличаются полем startTime");
        assertEquals(task.getDuration(), taskManager.getTaskById(1).getDuration(),
                "Таски отличаются полем Duration");
        assertEquals(task.getEndTime(), taskManager.getTaskById(1).getEndTime(),
                "У тасков не сходится endTime");

        // Сравниваем Сабтаски по времени начала, длительности и окончания задачи
        assertEquals(sub.getStartTime(), taskManager.getSubtaskById(3).getStartTime(),
                "Сабтаски отличаются полем startTime");
        assertEquals(sub.getDuration(), taskManager.getSubtaskById(3).getDuration(),
                "Сабтаски отличаются полем Duration");
        assertEquals(sub.getEndTime(), taskManager.getSubtaskById(3).getEndTime(),
                "У сабтасков не сходится endTime");

        // Сравниваем Таски по времени начала, длительности и окончания задачи
        assertEquals(epic.getStartTime(), taskManager.getEpicById(2).getStartTime(),
                "У эпика неверное поле startTime");
        assertEquals(epic.getDuration(), taskManager.getEpicById(2).getDuration(),
                "У эпика неверное поле Duration");
        assertEquals(epic.getEndTime(), taskManager.getEpicById(2).getEndTime(),
                "У эпика неверное поле endTime");
    }

    @Test
    @DisplayName("Сохранение задачи в файл")
    void shouldSaveToFile() throws IOException {
        // Добавляем Таск в трекер
        taskManager.addNewTask(new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)));

        // Считываем из файла данные, а также создаём строку-шаблон для сравнения
        var lines = Files.readAllLines(tmpFile.toPath());
        var expectedTaskLine = "1,TASK,Task1,NEW,Description task1,16.03.2025 12:14,10,";

        // Проверяем, что трекер корректно сохранил Таск в файл
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно быть 2 строки");
        assertEquals(expectedTaskLine, lines.get(1), "Строки задач должны совпадать");
    }

    @Test
    @DisplayName("Сохранение нескольких задач разных типов в файл")
    void shouldSaveToFileFewTasksInRightOrder() throws IOException {
        // Добавляем в трекер Таск, Эпик и Сабтаск
        taskManager.addNewTask(new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)));
        taskManager.addNewEpic(new Epic("Epic2", "Description epic2", 2));
        taskManager.addNewSubtask(new Subtask("Subtask3", "Description subtask3", 3,
                TaskStatus.DONE), 2);

        // Считываем из файла данные, а также создаём строки-шаблоны для сравнения
        var lines = Files.readAllLines(tmpFile.toPath());
        var taskLine = "1,TASK,Task1,NEW,Description task1,16.03.2025 12:14,10,";
        var epicLine = "2,EPIC,Epic2,DONE,Description epic2,";
        var subLine = "3,SUBTASK,Subtask3,DONE,Description subtask3,2,";

        // Проверяем, что трекер корректно сохранил задачи в файл
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(4, lines.size(), "В файле должно быть 4 строки");
        assertEquals(taskLine, lines.get(1), "Строки задач типа TASK должны совпадать");
        assertEquals(epicLine, lines.get(2), "Строки задач типа EPIC должны совпадать");
        assertEquals(subLine, lines.get(3), "Строки задач типа SUBTASK должны совпадать");
    }

    @Test
    @DisplayName("Обновление Таска в файле")
    void shouldUpdateTaskInFile() throws IOException {
        // Добавляем в трекер Таск, который будем обновлять
        taskManager.addNewTask(new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)));

        // Обновляем ранее добавленный Таск
        taskManager.updateTask(new Task("Task1_UPDATED", "Description task1", 1, TaskStatus.DONE,
                LocalDateTime.of(2025, 3, 17, 13, 15), Duration.ofMinutes(25)));

        // Считываем из файла данные, а также создаём строку-шаблон для сравнения обновлённого Таска
        var lines = Files.readAllLines(tmpFile.toPath());
        var expectedTaskLine = "1,TASK,Task1_UPDATED,DONE,Description task1,17.03.2025 13:15,25,";

        // Проверяем, что трекер обновил Таск в файле
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно быть 2 строки");
        assertEquals(expectedTaskLine, lines.get(1), "Таск не обновился в файле");
    }

    @Test
    @DisplayName("Удаление Таска из файла")
    void shouldDeleteTaskInFile() throws IOException {
        // Для начала добавляем в трекер Таск, который будем удалять
        taskManager.addNewTask(new Task("Task1", "Description task1", 1, TaskStatus.NEW));

        // Смотрим, что Таск записался в файл
        var fileWithTask = Files.readAllLines(tmpFile.toPath());
        assertEquals(2, fileWithTask.size(), "Таск не сохранился в файл");

        // Удаляем Таск из файла
        taskManager.deleteTaskById(1);

        // Считываем из файла данные
        var fileWithoutTask = Files.readAllLines(tmpFile.toPath());
        var testLine = "id,type,name,status,description,start,duration,epic";

        // Проверяем, что Таск удалился из файла
        assertFalse(fileWithoutTask.isEmpty(), "Файл не должен быть пустым");
        assertEquals(1, fileWithoutTask.size(), "Таск должен был удалиться из файла");
        assertEquals(testLine, fileWithoutTask.getFirst(), "В файле должна была остаться строка с описанием полей");
    }

    @Test
    @DisplayName("Удаление всех Тасков из файла")
    void shouldDeleteAllTasksInFile() throws IOException {
        // Для начала добавляем в трекер Таски, которые будем удалять
        taskManager.addNewTask(new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)));
        taskManager.addNewTask(new Task("Task2", "Description task2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 4, 17, 13, 15), Duration.ofMinutes(15)));
        taskManager.addNewTask(new Task("Task3", "Description task3", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 5, 18, 14, 16), Duration.ofMinutes(20)));

        // Смотрим, что Таски записались в файл
        var fileWithTasks = Files.readAllLines(tmpFile.toPath());
        assertEquals(4, fileWithTasks.size(), "Таски не сохранились в файл");

        // Удаляем Таски из файла
        taskManager.deleteAllTasks();

        // Считываем из файла данные
        var fileWithoutTasks = Files.readAllLines(tmpFile.toPath());
        var testLine = "id,type,name,status,description,start,duration,epic";

        // Проверяем, что трекер очистил файл от всех трех Тасков
        assertFalse(fileWithoutTasks.isEmpty(), "Файл не должен быть пустым");
        assertEquals(1, fileWithoutTasks.size(), "Все 3 Таска должны были удалиться из файла");
        assertEquals(testLine, fileWithoutTasks.getFirst(), "В файле должна была остаться строка с описанием полей");
    }

    @Test
    @DisplayName("Обновление Эпика в файле")
    void shouldUpdateEpicInFile() throws IOException {
        // Добавляем Эпик в трекер и файл
        var epicId = taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));

        // Обновляем Эпик в трекере
        var updatedEpic = new Epic("Epic1_UPDATED", "Description epic1_UPDATED", epicId);
        taskManager.updateEpic(new Epic(updatedEpic.getTitle(), updatedEpic.getDescription(), updatedEpic.getId()));

        // Получаем содержимое файла и создаём строку-шаблон для проверки
        var lines = Files.readAllLines(tmpFile.toPath());
        var expectedEpicLine = "1,EPIC,Epic1_UPDATED,NEW,Description epic1_UPDATED,";

        // Проверяем, что Эпик успешно обновился в файле
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно быть 2 строки");
        assertEquals(expectedEpicLine, lines.get(1), "Эпик не обновился в файле");
    }

    @Test
    @DisplayName("Удаление Эпика из файла")
    void shouldReturnOrDeleteEpicById() throws IOException {
        // Добавляем Эпик в трекер и файл
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));

        // Удаляем Эпик из трекера и файла
        taskManager.deleteEpicById(1);

        // Получаем содержимое файла
        var lines = Files.readAllLines(tmpFile.toPath());
        var expectedLine = "id,type,name,status,description,start,duration,epic";

        // Проверяем, удалился ли Эпик из файла
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(1, lines.size(), "Эпик должен был удалиться из файла");
        assertEquals(expectedLine, lines.getFirst(), "В файле должна была остаться строка с описанием полей");
    }

    @Test
    @DisplayName("Очищение списка Эпиков в файле")
    void shouldDeleteAllEpicsInFile() throws IOException {
        // Добавляем три Эпика в трекер и файл
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));
        taskManager.addNewEpic(new Epic("Epic2", "Description epic2", 2));
        taskManager.addNewEpic(new Epic("Epic3", "Description epic3", 3));

        // Удаляем все три Эпика
        taskManager.deleteAllEpics();

        // Получаем содержимое файла для проверки
        var lines = Files.readAllLines(tmpFile.toPath());
        var expectedLine = "id,type,name,status,description,start,duration,epic";

        // Смотрим, что все три Эпика успешно удалились из файла
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(1, lines.size(), "Все 3 эпика должны были удалиться из файла");
        assertEquals(expectedLine, lines.getFirst(), "В файле должна была остаться строка с описанием полей");
    }

    @Test
    @DisplayName("Сохранение Сабтаска в файл")
    void shouldSaveSubtaskToFile() throws IOException {
        // Добавляем Эпик с Сабтаском в трекер и файл
        var epicId = taskManager.addNewEpic(new Epic("Эпик", "Описание эпика", 0));
        taskManager.addNewSubtask(new Subtask("Сабтаск", "Описание саб", 0, TaskStatus.NEW), epicId);

        // Считываем из файла данные, а также создаём две строки с шаблоном задач для сравнения
        var lines = Files.readAllLines(tmpFile.toPath());
        var expectedEpicLine = "1,EPIC,Эпик,NEW,Описание эпика,";
        var expectedSubLine = "2,SUBTASK,Сабтаск,NEW,Описание саб,1,";

        // Проверяем, что трекер корректно сохранил Эпик вместе с Сабтаском в файл
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(3, lines.size(), "В файле должно быть 3 строки");
        assertEquals(expectedEpicLine, lines.get(1), "Строки Эпиков должны совпадать");
        assertEquals(expectedSubLine, lines.get(2), "Строки Сабтасков должны совпадать");
    }

    @Test
    @DisplayName("Обновление Сабтаска в файле")
    void shouldUpdateSubtaskInFile() throws IOException {
        // Добавляем Эпик с Сабтаском в трекер и файл
        var epicId = taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));
        taskManager.addNewSubtask(new Subtask("Sub2", "Description sub2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)),
                epicId);

        // Обновляем Сабтаск в Эпике и файле
        var updatedSub = new Subtask("Sub2_UPDATED", "Description sub2_UPDATED", 2, TaskStatus.DONE,
                LocalDateTime.of(2025, 3, 17, 13, 15), Duration.ofMinutes(25));
        taskManager.updateSubtask(updatedSub);

        // Считываем из файла данные, а также создаём строку-шаблон для проверки Сабтаска
        var lines = Files.readAllLines(tmpFile.toPath());
        var expectedEpicLine = "2,SUBTASK,Sub2_UPDATED,DONE,Description sub2_UPDATED,17.03.2025 13:15,25,1,";

        // Проверяем, что трекер корректно обновил Сабтаск в файле
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(3, lines.size(), "В файле должно быть 3 строки");
        assertEquals(expectedEpicLine, lines.get(2), "Сабтаск не обновился в файле");
    }

    @Test
    @DisplayName("Удаление Сабтаска из файла")
    void shouldDeleteSubtaskInFile() throws IOException {
        // Добавляем Сабтаск в Эпик и файл
        var epicId = taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 10));
        var subId = taskManager.addNewSubtask(new Subtask("Subtask2", "Description subtask2", 0,
                TaskStatus.NEW, LocalDateTime.of(2025, 3, 16, 12, 14),
                Duration.ofMinutes(10)), epicId);

        // Получаем данные из файла
        var lines = Files.readAllLines(tmpFile.toPath());

        // Смотрим, чтобы трекер записал в файл Эпик и Сабтаск
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(3, lines.size(), "В файле должны быть 3 строки");

        // Удаляем Сабтаск из Эпика и файла
        taskManager.deleteSubtaskById(subId);

        // Получаем данные из файла
        lines = Files.readAllLines(tmpFile.toPath());

        // Проверяем, удалился ли Сабтаск из файла
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно остаться 2 строки");
    }

    @Test
    @DisplayName("Очищение списка Сабтасков в файле")
    void shouldDeleteAllSubtasksInFile() throws IOException {
        // Добавляем три Сабтаска в Эпик и файл
        var epicId = taskManager.addNewEpic(new Epic("Epic", "Description", 0));
        taskManager.addNewSubtask(new Subtask("Subtask1", "Description", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)),
                epicId);
        taskManager.addNewSubtask(new Subtask("Subtask2", "Description", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 4, 17, 13, 15), Duration.ofMinutes(15)),
                epicId);
        taskManager.addNewSubtask(new Subtask("Subtask3", "Description", 4, TaskStatus.NEW,
                LocalDateTime.of(2025, 5, 18, 14, 16), Duration.ofMinutes(20)),
                epicId);

        // Получаем данные из файла
        var lines = Files.readAllLines(tmpFile.toPath());

        // Смотрим, чтобы трекер записал в файл Эпик и три Сабтаска
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(5, lines.size(), "В файле должно быть 5 строк");

        // Очищаем Эпик от Сабтасков
        taskManager.deleteAllSubtasks();

        // Получаем данные из файла
        lines = Files.readAllLines(tmpFile.toPath());

        // Проверяем, удалились ли все три Сабтаска из файла
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно остаться 2 строки");
    }
}