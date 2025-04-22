package tracker.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tracker.exceptions.TaskInteractionException;
import tracker.model.Task;

import tracker.util.Managers;
import tracker.util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    @DisplayName("Трекер должен генерировать уникальное id для каждой новой задачи")
    void shouldGenerateNewId() {
        // Добавляем в трекер три Таска с идентификатором 0
        taskManager.addNewTask(new Task("Таск 1", "Описание", 0, TaskStatus.NEW));
        taskManager.addNewTask(new Task("Таск 2", "Описание", 0, TaskStatus.NEW));
        taskManager.addNewTask(new Task("Таск 3", "Описание", 0, TaskStatus.NEW));

        // Получаем сохранённые Таски из трекера
        var tasks = taskManager.getAllTasks();

        // Проверяем, что трекер сгенерировал для Тасков новые последовательные идентификаторы - 1, 2 и 3
        assertEquals(1, tasks.getFirst().getId(), "Идентификатор первого Таска должен быть равен 1");
        assertEquals(2, tasks.get(1).getId(), "Идентификатор второго Таска должен быть равен 2");
        assertEquals(3, tasks.getLast().getId(), "Идентификатор третьего Таска должен быть равен 3");
    }

    @Test
    @DisplayName("Трекер должен бросать исключение при пересечении временных интервалов задач")
    void shouldThrowTaskInteractionException() {
        // Добавляем Таск в трекер
        taskManager.addNewTask(new Task("Таск", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 20), Duration.ofMinutes(10)));

        // Создаём Таск, который пересекается с началом ранее добавленной задачи
        var testTask1 = new Task("Таск 1", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 15), Duration.ofMinutes(10));

        // Проверяем, что трекер бросает исключение TaskInteractionException
        assertThrows(TaskInteractionException.class, () -> {
            taskManager.addNewTask(testTask1);
        }, "Пересечение временных интервалов задач должно приводить к исключению");

        // Создаём Таск, который пересекается с концом ранее добавленной задачи
        var testTask2 = new Task("Таск 2", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 25), Duration.ofMinutes(10));

        // Проверяем, что трекер бросает исключение TaskInteractionException
        assertThrows(TaskInteractionException.class, () -> {
            taskManager.addNewTask(testTask2);
        }, "Пересечение временных интервалов задач должно приводить к исключению");

        // Создаём Таск, чей временной интервал находится внутри временного интервала ранее добавленной задачи
        var testTask3 = new Task("Таск 3", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 21), Duration.ofMinutes(8));

        // Проверяем, что трекер бросает исключение TaskInteractionException
        assertThrows(TaskInteractionException.class, () -> {
            taskManager.addNewTask(testTask3);
        }, "Пересечение временных интервалов задач должно приводить к исключению");

        // Создаём Таск, который начинается раньше и заканчивается позже ранее добавленной задачи
        var testTask4 = new Task("Таск 4", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 19), Duration.ofMinutes(12));

        // Проверяем, что трекер бросает исключение TaskInteractionException
        assertThrows(TaskInteractionException.class, () -> {
            taskManager.addNewTask(testTask4);
        }, "Пересечение временных интервалов задач должно приводить к исключению");

        // Создаём Таск, чей временной интервал идентичен временному интервалу ранее добавленной задачи
        var testTask5 = new Task("Таск 5", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 20), Duration.ofMinutes(10));

        // Проверяем, что трекер бросает исключение TaskInteractionException
        assertThrows(TaskInteractionException.class, () -> {
            taskManager.addNewTask(testTask5);
        }, "Пересечение временных интервалов задач должно приводить к исключению");

        // Создаём Таск, который начинается в то же время, что и ранее добавленная задача, но позже заканчивается
        var testTask6 = new Task("Таск 6", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 20), Duration.ofMinutes(11));

        // Проверяем, что трекер бросает исключение TaskInteractionException
        assertThrows(TaskInteractionException.class, () -> {
            taskManager.addNewTask(testTask6);
        }, "Пересечение временных интервалов задач должно приводить к исключению");

        // Создаём Таск, который начинается раньше, но заканчивается в то же время, что и ранее добавленная задача
        var testTask7 = new Task("Таск 7", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 19), Duration.ofMinutes(11));

        // Проверяем, что трекер бросает исключение TaskInteractionException
        assertThrows(TaskInteractionException.class, () -> {
            taskManager.addNewTask(testTask7);
        }, "Пересечение временных интервалов задач должно приводить к исключению");
    }

    @Test
    @DisplayName("Трекер не должен бросать исключение, если начало одной задачи совпадает с концом другой")
    void shouldNotThrowTaskInteractionException() {
        // Добавляем Таск в трекер
        taskManager.addNewTask(new Task("Таск 1", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 20), Duration.ofMinutes(10)));

        // Создаём Таск, начало которого приходится ровно на конец ранее сохранённого Таска
        var task = new Task("Таск 2", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 11,1, 12, 30), Duration.ofMinutes(25));

        // Проверяем, что трекер бросает исключение TaskInteractionException
        assertDoesNotThrow(() -> {
            taskManager.addNewTask(task);
        }, "Ситуация, когда начало одной задачи совпадает с концом другой, не должна приводить к исключению");
    }
}