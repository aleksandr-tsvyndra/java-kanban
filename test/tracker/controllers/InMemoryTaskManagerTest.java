package tracker.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.Epic;
import tracker.util.Managers;
import tracker.util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.List;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    @DisplayName("Сохранение задачи")
    void shouldSaveTask() {
        Task task = new Task("Задача 1", "Описание 1", 1, TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    @DisplayName("Обновление задачи по Id")
    void shouldUpdateTaskWithSpecifiedId() {
        taskManager.addNewTask(new Task("Задача 1", "Описание 1", 1, TaskStatus.NEW));
        Task updatedTask = new Task("Задача 1_updated", "Описание 1_updated", 1, TaskStatus.DONE);

        final Task expectedUpdatedTask = new Task("Задача 1_updated", "Описание 1_updated",
                1, TaskStatus.DONE);

        final Task actualUpdatedTask = taskManager.updateTask(updatedTask);

        assertNotNull(actualUpdatedTask, "Обновленная задача не найдена.");
        assertEquals(expectedUpdatedTask.getTitle(), actualUpdatedTask.getTitle(), "Задачи не совпадают.");
        assertEquals(expectedUpdatedTask.getDescription(), actualUpdatedTask.getDescription(), "Задачи не совпадают.");
        assertEquals(expectedUpdatedTask.getId(), actualUpdatedTask.getId(), "Задачи не совпадают.");
        assertEquals(expectedUpdatedTask.getStatus(), actualUpdatedTask.getStatus(), "Задачи не совпадают.");
    }

    @Test
    @DisplayName("Вызов или удаление тасков по Id")
    void shouldReturnOrDeleteTaskWithSpecifiedId() {
        Task task = new Task("Таск 1", "Описание 1", 1, TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);

        final Task actualTask = taskManager.getTaskById(taskId);

        assertNotNull(actualTask, "Таск не найден");
        assertEquals(task, actualTask, "Таски не совпадают");

        taskManager.deleteTaskById(taskId);
        final List<Task> tasks = taskManager.getAllTasks();

        assertNull(taskManager.getTaskById(taskId), "Таск не удалилася");
        assertEquals(0, tasks.size(), "Неверное количество тасков");
    }

    @Test
    @DisplayName("Очищение списка тасков")
    void shouldDeleteAllTasks() {
        Task taskOne = new Task("Таск 1", "Описание 1", 1, TaskStatus.NEW);
        Task taskTwo = new Task("Таск 2", "Описание 2", 2, TaskStatus.NEW);
        taskManager.addNewTask(taskOne);
        taskManager.addNewTask(taskTwo);

        taskManager.deleteAllTasks();
        final List<Task> tasks = taskManager.getAllTasks();

        assertEquals(0, tasks.size(), "Таски не удалились");
    }

    @Test
    @DisplayName("Проверка полей startTime/duration в таске")
    void shouldCalculateTaskEndTime() {
        Task expectedTask = new Task("Task1", "Description task1", 1, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));
        LocalDateTime expectedEndTime = expectedTask.getEndTime();

        int id = taskManager.addNewTask(expectedTask);
        Task actualTask = taskManager.getTaskById(id);

        assertEquals(expectedTask.getStartTime(), actualTask.getStartTime(), "В таске установлено неверное стартовое время");
        assertEquals(expectedTask.getDuration(), actualTask.getDuration(), "В таске установлена неверная продолжительность");
        assertEquals(expectedEndTime, actualTask.getEndTime(), "В таске неверное время окончания задачи");
    }

    @Test
    @DisplayName("Сохранение эпика")
    void shouldSaveEpic() {
        Epic epic = new Epic("Эпик 1", "Описание 1", 1);
        final int epicId = taskManager.addNewEpic(epic);

        final Epic actualEpic = taskManager.getEpicById(epicId);

        assertNotNull(actualEpic, "Эпик не найден.");
        assertEquals(epic, actualEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    @DisplayName("Обновление эпика по Id")
    void shouldUpdateEpicWithSpecifiedId() {
        Epic epic = new Epic("Эпик 1", "Описание 1", 1);
        final int epicId = taskManager.addNewEpic(epic);
        Epic updatedEpic = new Epic("Эпик 1_updated", "Описание 1_updated", epicId);

        final Epic expectedUpdatedEpic = new Epic("Эпик 1_updated", "Описание 1_updated",
                epicId);

        final Epic actualUpdatedEpic = taskManager.updateEpic(updatedEpic);

        assertNotNull(actualUpdatedEpic, "Обновленный эпик не найден.");
        assertEquals(expectedUpdatedEpic.getTitle(), actualUpdatedEpic.getTitle(), "Эпики не совпадают.");
        assertEquals(expectedUpdatedEpic.getDescription(), actualUpdatedEpic.getDescription(), "Эпики не совпадают.");
        assertEquals(expectedUpdatedEpic.getId(), actualUpdatedEpic.getId(), "Эпики не совпадают.");
        assertEquals(expectedUpdatedEpic.getStatus(), actualUpdatedEpic.getStatus(), "Эпики не совпадают.");
    }

    @Test
    @DisplayName("Вызов или удаление эпика по Id")
    void shouldReturnOrDeleteEpicWithSpecifiedId() {
        Epic epic = new Epic("Эпик 1", "Описание 1", 1);
        final int epicId = taskManager.addNewEpic(epic);

        final Epic actualEpic = taskManager.getEpicById(epicId);

        assertNotNull(actualEpic, "Эпик не найден.");
        assertEquals(epic, actualEpic, "Эпики не совпадают.");

        taskManager.deleteEpicById(epicId);
        final List<Epic> epics = taskManager.getAllEpics();

        assertNull(taskManager.getEpicById(epicId), "Эпик не удалился.");
        assertEquals(0, epics.size(), "Неверное количество эпиков.");
    }

    @Test
    @DisplayName("Очищение списка эпиков")
    void shouldDeleteAllEpics() {
        Epic epicOne = new Epic("Эпик 1", "Описание 1", 1);
        Epic epicTwo = new Epic("Эпик 2", "Описание 2", 2);
        taskManager.addNewEpic(epicOne);
        final int epicTwoId = taskManager.addNewEpic(epicTwo);
        Subtask subtask = new Subtask("Подзадача 1", "Описание 1", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 10, 1, 10, 0), Duration.ofMinutes(5));
        taskManager.addNewSubtask(subtask, epicTwoId);

        taskManager.deleteAllEpics();
        final List<Epic> epics = taskManager.getAllEpics();
        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertEquals(0, epics.size(), "Эпики не удалились.");
        assertEquals(0, subtasks.size(), "Подзадачи должны удалиться вместе с эпиками.");
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
    @DisplayName("Сохранение подзадачи")
    void shouldSaveSubtask() {
        Epic epic = new Epic("Эпик с подзадачей 1", "Описание 1", 1);
        Subtask subtask = new Subtask("Подзадача 1", "Описание 1", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 10, 1, 10, 0), Duration.ofMinutes(5));
        final int epicId = taskManager.addNewEpic(epic);
        final int subtaskId = taskManager.addNewSubtask(subtask, epicId);

        final Subtask actualSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(actualSubtask, "Подзадача не найдена.");
        assertEquals(subtask, actualSubtask, "Подзадачи не совпадают.");
        assertEquals(epicId, actualSubtask.getEpicId(), "Подзадача не привязана к эпику.");

        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    @DisplayName("Обновление подзадачи по Id")
    void shouldUpdateSubtaskWithSpecifiedId() {
        Epic epic = new Epic("Эпик с подзадачей 1", "Описание 1", 1);
        Subtask subtask = new Subtask("Подзадача 1", "Описание 1", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 10, 1, 10, 0), Duration.ofMinutes(5));
        final int epicId = taskManager.addNewEpic(epic);
        final int subtaskId = taskManager.addNewSubtask(subtask, epicId);
        Subtask updatedSubtask = new Subtask("Подзадача 1_updated", "Описание 1_updated", subtaskId,
                TaskStatus.DONE, LocalDateTime.of(2026, 12, 15, 20, 0),
                Duration.ofMinutes(5));

        final Subtask expectedUpdatedSubtask = new Subtask("Подзадача 1_updated", "Описание 1_updated",
                subtaskId, TaskStatus.DONE, LocalDateTime.of(2026, 12, 15, 20, 0),
                Duration.ofMinutes(5));
        expectedUpdatedSubtask.setEpicId(epicId);

        final Subtask actualUpdatedSubtask = taskManager.updateSubtask(updatedSubtask);

        assertNotNull(actualUpdatedSubtask, "Обновленная подзадача не найдена.");
        assertEquals(expectedUpdatedSubtask.getTitle(), actualUpdatedSubtask.getTitle(),
                "У подзадач разные заголовки.");
        assertEquals(expectedUpdatedSubtask.getDescription(), actualUpdatedSubtask.getDescription(),
                "У подзадач другое описание.");
        assertEquals(expectedUpdatedSubtask.getId(), actualUpdatedSubtask.getId(), "Подзадачи не совпадают.");
        assertEquals(expectedUpdatedSubtask.getStatus(), actualUpdatedSubtask.getStatus(),
                "У подзадач разный статус.");
        assertEquals(expectedUpdatedSubtask.getEpicId(), actualUpdatedSubtask.getEpicId(),
                "У подзадач не совпадает id эпика.");
        assertEquals(expectedUpdatedSubtask.getStartTime(), actualUpdatedSubtask.getStartTime(),
                "У подзадач не совпадает время начала задачи.");
        assertEquals(expectedUpdatedSubtask.getDuration(), actualUpdatedSubtask.getDuration(),
                "У подзадач не совпадает продолжительность задачи.");
        assertEquals(expectedUpdatedSubtask.getEndTime(), actualUpdatedSubtask.getEndTime(),
                "У подзадач не совпадает время окончания задачи.");
    }

    @Test
    @DisplayName("Вызов или удаление подзадачи по Id")
    void shouldReturnOrDeleteSubtaskWithSpecifiedId() {
        Epic epic = new Epic("Эпик 1", "Описание 1", 1);
        Subtask subtask = new Subtask("Подзадача 1", "Описание 1", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 10, 1, 10, 0), Duration.ofMinutes(5));
        final int epicId = taskManager.addNewEpic(epic);
        final int subtaskId = taskManager.addNewSubtask(subtask, epicId);

        final Subtask actualSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(actualSubtask, "Подзадача не найдена.");
        assertEquals(subtask, actualSubtask, "Подзадачи не совпадают.");

        taskManager.deleteSubtaskById(subtaskId);
        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNull(taskManager.getSubtaskById(subtaskId), "Подзадача не удалилась.");
        assertEquals(0, subtasks.size(), "Неверное количество подзадач.");
    }

    @Test
    @DisplayName("Очищение списка подзадач")
    void shouldDeleteAllSubtasks() {
        Epic epic = new Epic("Эпик 1", "Описание 1", 1);
        final int epicId = taskManager.addNewEpic(epic);
        Subtask subtaskOne = new Subtask("Подзадача 1", "Описание 1", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 10, 1, 10, 0), Duration.ofMinutes(5));
        Subtask subtaskTwo = new Subtask("Подзадача 2", "Описание 2", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 12, 6, 15, 10), Duration.ofMinutes(10));
        taskManager.addNewSubtask(subtaskOne, epicId);
        taskManager.addNewSubtask(subtaskTwo, epicId);

        taskManager.deleteAllSubtasks();
        final List<Epic> epics = taskManager.getAllEpics();
        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertEquals(1, epics.size(), "Эпик не должен удалиться вместе с подзадачами.");
        assertEquals(0, subtasks.size(), "Подзадачи не удалились.");
    }

    @Test
    @DisplayName("Рассчёт статуса эпика по статусу его подзадач")
    void shouldCalculateEpicStatusDependsOnItsSubtasksStatus() {
        Epic epic = new Epic("Эпик 1", "Описание 1", 1);
        final int epicId = taskManager.addNewEpic(epic);

        Subtask subtaskOne = new Subtask("Подзадача 1", "Описание 1", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 10, 1, 10, 0), Duration.ofMinutes(5));
        taskManager.addNewSubtask(subtaskOne, epicId);
        final Epic epicWithNewStatus = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, epicWithNewStatus.getStatus(), "Статус Эпика должен быть NEW.");

        Subtask subtaskTwo = new Subtask("Подзадача 2", "Описание 2", 3, TaskStatus.DONE,
                LocalDateTime.of(2025, 10, 1, 10, 0), Duration.ofMinutes(5));
        taskManager.addNewSubtask(subtaskTwo, epicId);
        final Epic epicWithInProgressStatus = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, epicWithInProgressStatus.getStatus(), "Статус Эпика должен быть IN_PROGRESS.");

        taskManager.deleteSubtaskById(subtaskOne.getId());
        final Epic epicWithDoneStatus = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, epicWithDoneStatus.getStatus(), "Статус Эпика должен быть DONE.");
    }
}