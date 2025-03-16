package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

import java.util.List;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager taskManager;
    private File tmpFile;

    @BeforeEach
    void init() throws IOException {
        tmpFile = File.createTempFile("data", ".csv");
        taskManager = new FileBackedTaskManager(tmpFile, new InMemoryHistoryManager());
    }

    @Test
    @DisplayName("Восстановление трекера из пустого файла")
    void shouldLoadFromEmptyFile() {
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(tmpFile);

        assertNotNull(fileBackedTaskManager, "Метод должен возвращать экземпляр класса FileBackedTaskManager");
    }

    @Test
    @DisplayName("Восстановление трекера из файла с 3 задачами")
    void shouldLoadFromFile() throws IOException {
        Writer fileWriter = new FileWriter(tmpFile);
        fileWriter.write("id,type,name,status,description,start,duration,epic");
        fileWriter.write("\n1,TASK,Task1,NEW,Description task1,16.03.2025 12:14,10,");
        fileWriter.write("\n2,EPIC,Epic2,DONE,Description epic2,");
        fileWriter.write("\n3,SUBTASK,Subtask3,DONE,Description subtask3,17.03.2025 13:15,25,2");
        fileWriter.close();

        taskManager = FileBackedTaskManager.loadFromFile(tmpFile);

        assertEquals(1, taskManager.getAllTasks().size(), "В списке тасков должна быть 1 задача");
        assertEquals(1, taskManager.getAllEpics().size(), "В списке эпиков должна быть 1 задача");
        assertEquals(1, taskManager.getAllSubtasks().size(), "В списке сабтасок должна быть 1 задача");

        Task expectedTask = new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));
        Epic expectedEpic = new Epic("Epic2", "Description epic2", 2);
        Subtask expectedSubtask = new Subtask("Subtask3", "Description subtask3", 3, TaskStatus.DONE,
                LocalDateTime.of(2025, 3, 17, 13, 15), Duration.ofMinutes(25));

        assertEquals(expectedTask, taskManager.getAllTasks().getFirst(), "Таски отличаются по ID");
        assertEquals(expectedEpic, taskManager.getAllEpics().getFirst(), "Эпики отличаются по ID");
        assertEquals(expectedSubtask, taskManager.getAllSubtasks().getFirst(), "Сабтаски отличаются по ID");

        assertEquals(expectedTask.getStartTime(), taskManager.getTaskById(1).getStartTime(), "Таски отличаются полем startTime");
        assertEquals(expectedTask.getDuration(), taskManager.getTaskById(1).getDuration(), "Таски отличаются полем Duration");
        assertEquals(expectedTask.getEndTime(), taskManager.getTaskById(1).getEndTime(), "У тасков не сходится endTime");

        assertEquals(expectedSubtask.getStartTime(), taskManager.getSubtaskById(3).getStartTime(), "Сабтаски отличаются полем startTime");
        assertEquals(expectedSubtask.getDuration(), taskManager.getSubtaskById(3).getDuration(), "Сабтаски отличаются полем Duration");
        assertEquals(expectedSubtask.getEndTime(), taskManager.getSubtaskById(3).getEndTime(), "У сабтасков не сходится endTime");

        assertEquals(expectedSubtask.getStartTime(), taskManager.getEpicById(2).getStartTime(), "У эпика неверное поле startTime");
        assertEquals(expectedSubtask.getDuration(), taskManager.getEpicById(2).getDuration(), "У эпика неверное поле Duration");
        assertEquals(expectedSubtask.getEndTime(), taskManager.getEpicById(2).getEndTime(), "У эпика неверное поле endTime");
    }

    @Test
    @DisplayName("Сохранение задачи в файл")
    void shouldSaveToFile() throws IOException {
        Task task = new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));
        taskManager.addNewTask(task);

        assertEquals(1, taskManager.getAllTasks().size(), "В списке тасков должна быть 1 задача");
        assertEquals(task, taskManager.getAllTasks().getFirst(), "Таски должны быть одинаковыми");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedTaskLine = "1,TASK,Task1,NEW,Description task1,16.03.2025 12:14,10,";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно быть 2 строки");
        assertEquals(expectedTaskLine, lines.get(1), "Строки задач должны совпадать");
    }

    @Test
    @DisplayName("Сохранение нескольких задач разных типов в файл")
    void shouldSaveToFileFewTasksInRightOrder() throws IOException {
        Task task = new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));
        Epic epic = new Epic("Epic2", "Description epic2", 2);
        Subtask subtask = new Subtask("Subtask3", "Description subtask3", 3, TaskStatus.DONE,
                LocalDateTime.of(2025, 3, 17, 13, 15), Duration.ofMinutes(25));
        taskManager.addNewTask(task);
        taskManager.addNewEpic(epic);
        taskManager.addNewSubtask(subtask, epic.getId());

        assertEquals(1, taskManager.getAllTasks().size(), "В списке тасков должна быть 1 задача");
        assertEquals(1, taskManager.getAllEpics().size(), "В списке эпиков должна быть 1 задача");
        assertEquals(1, taskManager.getAllSubtasks().size(), "В списке сабтасок должна быть 1 задача");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedTaskLine = "1,TASK,Task1,NEW,Description task1,16.03.2025 12:14,10,";
        String expectedEpicLine = "2,EPIC,Epic2,DONE,Description epic2,";
        String expectedSubtaskLine = "3,SUBTASK,Subtask3,DONE,Description subtask3,17.03.2025 13:15,25,2";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(4, lines.size(), "В файле должно быть 4 строки");
        assertEquals(expectedTaskLine, lines.get(1), "Строки задач типа TASK должны совпадать");
        assertEquals(expectedEpicLine, lines.get(2), "Строки задач типа EPIC должны совпадать");
        assertEquals(expectedSubtaskLine, lines.get(3), "Строки задач типа SUBTASK должны совпадать");
    }

    @Test
    @DisplayName("Обновление таска по ID в трекере и файле")
    void shouldUpdateTaskById() throws IOException {
        taskManager.addNewTask(new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)));

        Task updatedTask = new Task("Task1_UPDATED", "Description task1", 1, TaskStatus.DONE,
                LocalDateTime.of(2025, 3, 17, 13, 15), Duration.ofMinutes(25));
        Task actualUpdatedTask = taskManager.updateTask(updatedTask);

        assertNotNull(actualUpdatedTask, "Обновленная задача не найдена");
        assertEquals(updatedTask.getTitle(), actualUpdatedTask.getTitle(), "Задачи отличаются заголовком");
        assertEquals(updatedTask.getDescription(), actualUpdatedTask.getDescription(), "Задачи отличаются описанием");
        assertEquals(updatedTask.getId(), actualUpdatedTask.getId(), "Задачи отличаются по ID");
        assertEquals(updatedTask.getStatus(), actualUpdatedTask.getStatus(), "У задач отличается статус");
        assertEquals(updatedTask.getStartTime(), actualUpdatedTask.getStartTime(), "У задач отличается стартовое время");
        assertEquals(updatedTask.getDuration(), actualUpdatedTask.getDuration(), "У задач отличается продолжительность");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedTaskLine = "1,TASK,Task1_UPDATED,DONE,Description task1,17.03.2025 13:15,25,";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно быть 2 строки");
        assertEquals(expectedTaskLine, lines.get(1), "Задача не обновилась в файле");
    }

    @Test
    @DisplayName("Получение/удаление таска по ID из трекера и файла")
    void shouldReturnOrDeleteTaskById() throws IOException {
        taskManager.addNewTask(new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)));

        Task actualTask = taskManager.getTaskById(1);

        assertNotNull(actualTask, "Таск не найден");
        assertEquals(1, actualTask.getId(), "У таска неправильное ID");

        taskManager.deleteTaskById(1);

        assertNull(taskManager.getTaskById(1), "Таск не удалилася из списка");
        assertEquals(0, taskManager.getAllTasks().size(), "Неверное количество тасков в списке");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedLine = "id,type,name,status,description,start,duration,epic";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(1, lines.size(), "Таск должен был удалиться из файла");
        assertEquals(expectedLine, lines.getFirst(), "В файле должна была остаться строка с описанием полей");
    }

    @Test
    @DisplayName("Очищение списка тасков в трекере и файле")
    void shouldDeleteAllTasks() throws IOException {
        taskManager.addNewTask(new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)));
        taskManager.addNewTask(new Task("Task2", "Description task2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 4, 17, 13, 15), Duration.ofMinutes(15)));
        taskManager.addNewTask(new Task("Task3", "Description task3", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 5, 18, 14, 16), Duration.ofMinutes(20)));

        taskManager.deleteAllTasks();

        assertEquals(0, taskManager.getAllTasks().size(), "Список тасков не очистился");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedLine = "id,type,name,status,description,start,duration,epic";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(1, lines.size(), "Все 3 таска должны были удалиться из файла");
        assertEquals(expectedLine, lines.getFirst(), "В файле должна была остаться строка с описанием полей");
    }

    @Test
    @DisplayName("Проверка полей startTime/duration в таске")
    void shouldCalculateTaskEndTime() throws IOException {
        Task expectedTask = new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));

        int id = taskManager.addNewTask(expectedTask);
        Task actualTask = taskManager.getTaskById(id);

        assertEquals(expectedTask.getStartTime(), actualTask.getStartTime(), "В таске неверное стартовое время");
        assertEquals(expectedTask.getDuration(), actualTask.getDuration(), "В таске неверная продолжительность");
        assertEquals(expectedTask.getEndTime(), actualTask.getEndTime(), "В таске неверное время окончания задачи");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedTaskLine = "1,TASK,Task1,NEW,Description task1,16.03.2025 12:14,10,";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно быть 2 строки");
        assertEquals(expectedTaskLine, lines.get(1), "В файле указаны неправильные startTime и duration");
    }

    @Test
    @DisplayName("Обновление эпика по ID в трекере и файле")
    void shouldUpdateEpicById() throws IOException {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));

        Epic updatedEpic = new Epic("Epic1_UPDATED", "Description epic1_UPDATED", 1);
        Epic actualUpdatedEpic = taskManager.updateEpic(updatedEpic);

        assertNotNull(actualUpdatedEpic, "Обновленный эпик не найден");
        assertEquals(updatedEpic.getTitle(), actualUpdatedEpic.getTitle(), "Эпики отличаются заголовком");
        assertEquals(updatedEpic.getDescription(), actualUpdatedEpic.getDescription(), "Эпики отличаются описанием");
        assertEquals(updatedEpic.getId(), actualUpdatedEpic.getId(), "Эпики отличаются по ID");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedEpicLine = "1,EPIC,Epic1_UPDATED,NEW,Description epic1_UPDATED,";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно быть 2 строки");
        assertEquals(expectedEpicLine, lines.get(1), "Эпик не обновился в файле");
    }

    @Test
    @DisplayName("Получение/удаление эпика по ID из трекера и файла")
    void shouldReturnOrDeleteEpicById() throws IOException {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));

        Epic actualEpic = taskManager.getEpicById(1);

        assertNotNull(actualEpic, "Эпик не найден");
        assertEquals(1, actualEpic.getId(), "У эпика неправильное ID");

        taskManager.deleteEpicById(1);

        assertNull(taskManager.getEpicById(1), "Эпик не удалилася из списка");
        assertEquals(0, taskManager.getAllEpics().size(), "Неверное количество эпиков в списке");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedLine = "id,type,name,status,description,start,duration,epic";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(1, lines.size(), "Эпик должен был удалиться из файла");
        assertEquals(expectedLine, lines.getFirst(), "В файле должна была остаться строка с описанием полей");
    }

    @Test
    @DisplayName("Очищение списка эпиков в трекере и файле")
    void shouldDeleteAllEpics() throws IOException {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));
        taskManager.addNewEpic(new Epic("Epic2", "Description epic2", 2));
        taskManager.addNewEpic(new Epic("Epic3", "Description epic3", 3));

        taskManager.deleteAllEpics();

        assertEquals(0, taskManager.getAllEpics().size(), "Список эпиков не очистился");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedLine = "id,type,name,status,description,start,duration,epic";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(1, lines.size(), "Все 3 эпика должны были удалиться из файла");
        assertEquals(expectedLine, lines.getFirst(), "В файле должна была остаться строка с описанием полей");
    }

    @Test
    @DisplayName("Рассчёт startTime/duration/endTime-полей эпика по его сабтаскам")
    void shouldCalculateEpicTimeFieldsDependsOnItsSubtasks() {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));

        Epic actualEpic = taskManager.getEpicById(1);

        assertNull(actualEpic.getStartTime(), "startTime эпика должен быть пустым, если в эпике нет сабтасок");
        assertNull(actualEpic.getDuration(), "duration эпика должен быть пустым, если в эпике нет сабтасок");
        assertNull(actualEpic.getEndTime(), "endTime эпика должен быть пустым, если в эпике нет сабтасок");

        Subtask subOne = new Subtask("Subtask2", "Description subtask2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));
        taskManager.addNewSubtask(subOne, 1);

        assertEquals(subOne.getStartTime(), actualEpic.getStartTime(), "startTime эпика и сабтаска отличаются");
        assertEquals(subOne.getDuration(), actualEpic.getDuration(), "duration эпика и сабтаска отличаются");
        assertEquals(subOne.getEndTime(), actualEpic.getEndTime(), "endTime эпика и сабтаска отличаются");

        Subtask subTwo = new Subtask("Subtask3", "Description subtask3", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 13, 30), Duration.ofMinutes(25));
        taskManager.addNewSubtask(subTwo, 1);

        Duration expectedDuration = subOne.getDuration().plus(subTwo.getDuration());

        assertEquals(subOne.getStartTime(), actualEpic.getStartTime(), "startTime эпика и первого сабтаска отличаются");
        assertEquals(expectedDuration, actualEpic.getDuration(), "duration эпика должен быть равен сумме duration-полей двух его сабтасков");
        assertEquals(subTwo.getEndTime(), actualEpic.getEndTime(), "endTime эпика и самого позднего сабтаска отличаются");
    }

    @Test
    @DisplayName("Обновление сабтаска по ID в трекере и файле")
    void shouldUpdateSubtaskById() throws IOException {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));
        taskManager.addNewSubtask(new Subtask("Subtask2", "Description subtask2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)), 1);

        Subtask updatedSubtask = new Subtask("Subtask2_UPDATED", "Description subtask2_UPDATED", 2,
                TaskStatus.DONE, LocalDateTime.of(2025, 3, 17, 13, 15), Duration.ofMinutes(25));
        Subtask actualUpdatedSubtask = taskManager.updateSubtask(updatedSubtask);

        assertNotNull(actualUpdatedSubtask, "Обновленный сабтаск не найден");
        assertEquals(updatedSubtask.getTitle(), actualUpdatedSubtask.getTitle(), "У сабтасок разные заголовки");
        assertEquals(updatedSubtask.getDescription(), actualUpdatedSubtask.getDescription(), "У сабтасок разное описание");
        assertEquals(updatedSubtask.getId(), actualUpdatedSubtask.getId(), "Сабтаски отличаются по ID");
        assertEquals(updatedSubtask.getStatus(), actualUpdatedSubtask.getStatus(), "У сабтасок отличается статус");
        assertEquals(updatedSubtask.getStartTime(), actualUpdatedSubtask.getStartTime(), "Сабтаски отличаются по стартовому времени");
        assertEquals(updatedSubtask.getDuration(), actualUpdatedSubtask.getDuration(), "Сабтаски отличаются по продолжительности");
        assertEquals(1, actualUpdatedSubtask.getEpicId(), "У сабтаска неправильный ID эпика");

        Epic epic = taskManager.getEpicById(actualUpdatedSubtask.getEpicId());

        assertEquals(1, epic.getEpicSubtasks().size(), "Список сабтасок эпика не должен быть пустым");
        assertEquals(updatedSubtask, epic.getEpicSubtasks().getFirst(), "Сабтаска не обновилась в эпике");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedEpicLine = "2,SUBTASK,Subtask2_UPDATED,DONE,Description subtask2_UPDATED,17.03.2025 13:15,25,1";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(3, lines.size(), "В файле должно быть 3 строки");
        assertEquals(expectedEpicLine, lines.get(2), "Сабтаск не обновился в файле");
    }

    @Test
    @DisplayName("Получение/удаление сабтаска по ID из трекера и файла")
    void shouldReturnOrDeleteSubtaskById() throws IOException {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));
        taskManager.addNewSubtask(new Subtask("Subtask2", "Description subtask2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)), 1);

        Subtask actualSubtask = taskManager.getSubtaskById(2);

        assertNotNull(actualSubtask, "Сабтаск не найден");
        assertEquals(2, actualSubtask.getId(), "У сабтаска неправильное ID");
        assertEquals(1, actualSubtask.getEpicId(), "У сабтаска неправильное ID эпика");

        taskManager.deleteSubtaskById(2);
        Epic epic = taskManager.getEpicById(1);

        assertNull(taskManager.getSubtaskById(2), "Сабтаск не удалилася из трекера");
        assertEquals(0, taskManager.getAllSubtasks().size(), "Неверное количество сабтасок в трекере");
        assertEquals(0, epic.getEpicSubtasks().size(), "Список сабтасок эпика должен быть пустым");

        List<String> lines = Files.readAllLines(tmpFile.toPath());

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно остаться 2 строки");
    }

    @Test
    @DisplayName("Очищение списка сабтасок в эпике, трекере и файле")
    void shouldDeleteAllSubtasks() throws IOException {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));
        taskManager.addNewSubtask(new Subtask("Subtask2", "Description subtask2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)), 1);
        taskManager.addNewSubtask(new Subtask("Subtask3", "Description subtask3", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 4, 17, 13, 15), Duration.ofMinutes(15)), 1);
        taskManager.addNewSubtask(new Subtask("Subtask4", "Description subtask4", 4, TaskStatus.NEW,
                LocalDateTime.of(2025, 5, 18, 14, 16), Duration.ofMinutes(20)), 1);

        taskManager.deleteAllSubtasks();
        Epic epic = taskManager.getEpicById(1);

        assertEquals(0, taskManager.getAllSubtasks().size(), "Список сабтасок не очистился");
        assertEquals(0, epic.getEpicSubtasks().size(), "Список сабтасок эпика должен быть пустым");

        List<String> lines = Files.readAllLines(tmpFile.toPath());

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(2, lines.size(), "В файле должно остаться 2 строки");
    }

    @Test
    @DisplayName("Рассчёт статуса эпика по статусу его сабтасок")
    void shouldCalculateEpicStatusDependsOnItsSubtasksStatus() throws IOException {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));

        taskManager.addNewSubtask(new Subtask("Subtask2", "Description subtask2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10)), 1);

        Epic epic = taskManager.getEpicById(1);
        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String[] contents = lines.get(1).split(",");

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика должен быть NEW");
        assertEquals(TaskStatus.NEW, TaskStatus.valueOf(contents[3]), "Статус эпика в файле также должен быть NEW");

        taskManager.addNewSubtask(new Subtask("Subtask3", "Description subtask3", 3, TaskStatus.DONE,
                LocalDateTime.of(2025, 4, 17, 13, 15), Duration.ofMinutes(15)), 1);

        lines = Files.readAllLines(tmpFile.toPath());
        contents = lines.get(1).split(",");

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.valueOf(contents[3]), "Статус эпика в файле также должен стать IN_PROGRESS");

        taskManager.deleteSubtaskById(2);

        lines = Files.readAllLines(tmpFile.toPath());
        contents = lines.get(1).split(",");

        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика должен быть DONE.");
        assertEquals(TaskStatus.DONE, TaskStatus.valueOf(contents[3]), "Статус эпика в файле также должен стать DONE");
    }

    @Test
    @DisplayName("Проверка полей startTime/duration в сабтаске")
    void shouldCalculateSubtaskEndTime() throws IOException {
        taskManager.addNewEpic(new Epic("Epic1", "Description epic1", 1));
        Subtask expectedSubtask = new Subtask("Subtask2", "Description subtask2", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));

        taskManager.addNewSubtask(expectedSubtask, 1);
        Task actualSubtask = taskManager.getSubtaskById(2);

        assertEquals(expectedSubtask.getStartTime(), actualSubtask.getStartTime(), "В сабтаске неверное стартовое время");
        assertEquals(expectedSubtask.getDuration(), actualSubtask.getDuration(), "В сабтаске неверная продолжительность");
        assertEquals(expectedSubtask.getEndTime(), actualSubtask.getEndTime(), "В сабтаске неверное время завершения задачи");

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        String expectedSubtaskLine = "2,SUBTASK,Subtask2,NEW,Description subtask2,16.03.2025 12:14,10,1";

        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(3, lines.size(), "В файле должно быть 2 строки");
        assertEquals(expectedSubtaskLine, lines.get(2), "В файле указаны неправильные startTime и duration");
    }
}