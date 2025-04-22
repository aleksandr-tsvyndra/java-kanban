package tracker.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @Test
    @DisplayName("Сохранение Таска")
    void shouldSaveTask() {
        // Сохраняем Таск в трекере
        var taskId = taskManager.addNewTask(new Task("Таск", "Описание", 0, TaskStatus.NEW));

        // Получаем сохранённый Таск для проверки
        var savedTask = taskManager.getTaskById(taskId);

        // Проверяем, что трекер сохранил Таск
        assertNotNull(savedTask, "Таск не сохранился");
    }

    @Test
    @DisplayName("Обновление Таска по id")
    void shouldUpdateTaskById() {
        // Сохраняем Таск в трекере
        var taskId = taskManager.addNewTask(new Task("Таск", "Описание", 0, TaskStatus.NEW));

        // Создаём шаблон Таска, который обновит ранее добавленную задачу
        var expectedTask = new Task("Таск_updated", "Описание_updated", taskId, TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2025, 4, 1, 13, 10), Duration.ofMinutes(15));

        // Обновляем Таск без времени начала на Таск, у которого есть время
        taskManager.updateTask(new Task(expectedTask.getTitle(), expectedTask.getDescription(), expectedTask.getId(),
                expectedTask.getStatus(), expectedTask.getStartTime(), expectedTask.getDuration()));

        // Получаем обновлённый Таск по id для проверки
        var actualTask = taskManager.getTaskById(taskId);

        // Проверяем, что трекер обновил Таск
        assertEquals(expectedTask.getTitle(), actualTask.getTitle(), "Заголовки Тасков отличаются");
        assertEquals(expectedTask.getDescription(), actualTask.getDescription(), "Описания Тасков отличаются");
        assertEquals(expectedTask.getStatus(), actualTask.getStatus(), "Статусы Тасков отличаются");
        assertEquals(expectedTask.getId(), actualTask.getId(), "Id Тасков отличаются");
        assertNotNull(actualTask.getStartTime(), "У обновлённого Таска должно быть время начала");
    }

    @Test
    @DisplayName("Удаление Таска по id")
    void shouldDeleteTaskById() {
        // Добавляем Таск в трекер
        var taskId = taskManager.addNewTask(new Task("Таск", "Описание", 0, TaskStatus.NEW));

        // Получаем сохранённый Таск по id и проверяем, что Таск сохранился в трекере
        var savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Таск не найден");

        // Удаляем Таск из трекера
        taskManager.deleteTaskById(taskId);

        // Проверяем, что Таск действительно удалился из трекера
        assertThrows(NoSuchElementException.class, () -> {
            taskManager.getTaskById(taskId);
        }, "Таск не удалился");
    }

    @Test
    @DisplayName("Очищение списка Тасков")
    void shouldDeleteAllTasks() {
        // Добавляем три Таска в трекер
        taskManager.addNewTask(new Task("Таск 1", "Описание", 0, TaskStatus.NEW));
        taskManager.addNewTask(new Task("Таск 2", "Описание", 0, TaskStatus.NEW));
        taskManager.addNewTask(new Task("Таск 3", "Описание", 0, TaskStatus.NEW));

        // Получаем список Тасков и проверяем, что Таски сохранились в трекере
        var savedTasks = taskManager.getAllTasks();
        assertFalse(savedTasks.isEmpty(), "Таски не сохранились в трекере");

        // Очищаем список Тасков
        taskManager.deleteAllTasks();

        // Проверяем, что список Тасков действительно очистился
        var deletedTasks = taskManager.getAllTasks();
        assertTrue(deletedTasks.isEmpty(), "Список Тасков не очистился");
    }

    @Test
    @DisplayName("Сохранение Эпика")
    void shouldSaveEpic() {
        // Сохраняем Эпик в трекере
        var epicId = taskManager.addNewEpic(new Epic("Эпик", "Описание 1", 0));

        // Получаем сохранённый Эпик для проверки
        var savedEpic = taskManager.getEpicById(epicId);

        // Проверяем, что трекер сохранил Эпик
        assertNotNull(savedEpic, "Эпик не сохранился");
    }

    @Test
    @DisplayName("Обновление Эпика без Сабтасков по id")
    void shouldUpdateEpicById() {
        // Сохраняем Эпик в трекере
        var epicId = taskManager.addNewEpic(new Epic("Эпик", "Описание", 0));

        // Шаблон Эпика, который обновит ранее добавленный Эпик
        var expectedEpic = new Epic("Эпик_updated", "Описание_updated", epicId);

        // Обновляем Эпик
        taskManager.updateEpic(new Epic(expectedEpic.getTitle(), expectedEpic.getDescription(), epicId));

        // Получаем обновлённый Эпик по id для проверки
        var actualEpic = taskManager.getEpicById(epicId);

        // Проверяем, что трекер обновил Эпик
        assertEquals(expectedEpic.getTitle(), actualEpic.getTitle(), "Заголовки Эпиков отличаются");
        assertEquals(expectedEpic.getDescription(), actualEpic.getDescription(), "Описания Эпиков отличаются");
        assertEquals(expectedEpic.getStatus(), actualEpic.getStatus(), "Статусы Эпиков отличаются");
        assertEquals(expectedEpic.getId(), actualEpic.getId(), "Id Эпиков отличаются");
    }

    @Test
    @DisplayName("Обновление Эпика с 3-мя Сабтасками по id")
    void shouldUpdateEpicByIdWith3Subs() {
        // Сохраняем Эпик в трекере
        var epicId = taskManager.addNewEpic(new Epic("Эпик с 3-мя Сабтасками", "Описание", 0));

        // Сохраняем в Эпик три Сабтаски
        taskManager.addNewSubtask(new Subtask("Сабтаск 1", "Описание", 0, TaskStatus.NEW), epicId);
        taskManager.addNewSubtask(new Subtask("Сабтаск 2", "Описание", 0, TaskStatus.NEW), epicId);
        taskManager.addNewSubtask(new Subtask("Сабтаск 3", "Описание", 0, TaskStatus.NEW), epicId);

        // Шаблон Эпика, который обновит ранее добавленный Эпик
        var expectedEpic = new Epic("Эпик с 3-мя Сабтасками_updated", "Описание_updated", epicId);

        // Обновляем Эпик
        taskManager.updateEpic(new Epic(expectedEpic.getTitle(), expectedEpic.getDescription(), epicId));

        // Получаем обновлённый Эпик по id для проверки
        var actualEpic = taskManager.getEpicById(epicId);

        // Проверяем, что трекер обновил Эпик
        assertEquals(expectedEpic.getTitle(), actualEpic.getTitle(), "Заголовки Эпиков отличаются");
        assertEquals(expectedEpic.getDescription(), actualEpic.getDescription(), "Описания Эпиков отличаются");
        assertEquals(expectedEpic.getId(), actualEpic.getId(), "Id Эпиков отличаются");
        assertEquals(3, actualEpic.getEpicSubtasks().size(), "У обновлённого Эпика должны быть три Сабтаска");
    }

    @Test
    @DisplayName("Удаление Эпика по id")
    void shouldDeleteEpicById() {
        // Добавляем Эпик в трекер
        var epicId = taskManager.addNewEpic(new Epic("Эпик", "Описание", 0));

        // Получаем сохранённый Эпик по id и проверяем, что Эпик сохранился в трекере
        var savedEpic = taskManager.getEpicById(epicId);
        assertNotNull(savedEpic, "Эпик не найден");

        // Удаляем Эпик из трекера
        taskManager.deleteEpicById(epicId);

        // Проверяем, что Эпик действительно удалился из трекера
        assertThrows(NoSuchElementException.class, () -> {
            taskManager.getEpicById(epicId);
        }, "Эпик не удалился");
    }

    @Test
    @DisplayName("Удаление Эпика c 3-мя Сабтасками по id")
    void shouldDeleteEpicByIdWith3Subs() {
        // Добавляем Эпик в трекер
        var epicId = taskManager.addNewEpic(new Epic("Эпик с 3-мя Сабтасками", "Описание", 0));

        // Сохраняем в Эпик три Сабтаски
        taskManager.addNewSubtask(new Subtask("Сабтаск 1", "Описание", 0, TaskStatus.NEW), epicId);
        taskManager.addNewSubtask(new Subtask("Сабтаск 2", "Описание", 0, TaskStatus.NEW), epicId);
        taskManager.addNewSubtask(new Subtask("Сабтаск 3", "Описание", 0, TaskStatus.NEW), epicId);

        // Получаем сохранённый Эпик по id и проверяем, что Эпик с 3-мя Сабтасками сохранился в трекере
        var savedEpic = taskManager.getEpicById(epicId);
        var epicSubtasks = savedEpic.getEpicSubtasks();
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(3, epicSubtasks.size(), "У Эпика должны быть три Сабтаски");

        // Удаляем Эпик из трекера
        taskManager.deleteEpicById(epicId);

        // Проверяем, что Эпик действительно удалился из трекера, а вместе с ним и три его Сабтаски
        assertThrows(NoSuchElementException.class, () -> {
            taskManager.getEpicById(epicId);
        }, "Эпик не удалился");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Мапа Сабтасок трекера должна быть пустой");
    }

    @Test
    @DisplayName("Очищение списка Эпиков")
    void shouldDeleteAllEpics() {
        // Добавляем три Эпика в трекер
        taskManager.addNewEpic(new Epic("Эпик 1", "Описание", 0));
        taskManager.addNewEpic(new Epic("Эпик 2", "Описание", 0));
        taskManager.addNewEpic(new Epic("Эпик 2", "Описание", 0));

        // Получаем список Эпиков и проверяем, что Эпики сохранились в трекере
        var savedEpics = taskManager.getAllEpics();
        assertFalse(savedEpics.isEmpty(), "Эпики не сохранились в трекере");

        // Очищаем список Эпиков
        taskManager.deleteAllEpics();

        // Проверяем, что список Эпиков действительно очистился
        var deletedEpics = taskManager.getAllEpics();
        assertTrue(deletedEpics.isEmpty(), "Список Эпиков не очистился");
    }

    @Test
    @DisplayName("Сохранение Сабтаска")
    void shouldSaveSubtask() {
        // Для проверки сохранения Сабтаска нужно для начала в трекер добавить Эпик
        var epicId = taskManager.addNewEpic(new Epic("Эпик с Сабтаском", "Описание", 0));
        // Затем пытаемся сохранить сам Сабтаск
        var subId = taskManager.addNewSubtask(new Subtask("Сабтаск", "Описание", 0, TaskStatus.NEW),
                epicId);

        // Получаем сохранённый Сабтаск и список Сабтасков Эпика для проверки
        var savedSubtask = taskManager.getSubtaskById(subId);
        var epicSubtasks = taskManager.getEpicById(epicId).getEpicSubtasks();

        // Проверяем, что Сабтаск сохранился в трекере и в списке Сабтасков Эпика
        assertNotNull(savedSubtask, "Сабтаск не сохранился в трекер");
        assertFalse(epicSubtasks.isEmpty(), "Сабтаск не добавился в список Сабтасков Эпика");
    }

    @Test
    @DisplayName("Обновление Сабтаска по id")
    void shouldUpdateSubtaskById() {
        // Для проверки обновления Сабтаска нужно для начала в трекер добавить Эпик
        var epicId = taskManager.addNewEpic(new Epic("Эпик с Сабтаском", "Описание", 0));
        // Затем сохраняем Сабтаск, который мы будем обновлять
        var subId = taskManager.addNewSubtask(new Subtask("Сабтаск", "Описание", 0, TaskStatus.NEW),
                epicId);

        // Шаблон Сабтаска, который обновит ранее добавленный в трекер Сабтаск
        var expectedSub = new Subtask("Сабтаск_updated", "Описание_updated", subId, TaskStatus.DONE);

        // Обновляем Сабтаск
        taskManager.updateSubtask(new Subtask(expectedSub.getTitle(), expectedSub.getDescription(),
                expectedSub.getId(), expectedSub.getStatus()));

        // Получаем обновлённый Сабтаск по id для проверки
        var actualSub = taskManager.getSubtaskById(subId);

        // Проверяем, что трекер обновил Сабтаск
        assertEquals(expectedSub.getTitle(), actualSub.getTitle(), "У Сабтасков разные заголовки");
        assertEquals(expectedSub.getDescription(), actualSub.getDescription(), "У Сабтасков разные описания");
        assertEquals(expectedSub.getStatus(), actualSub.getStatus(), "У Сабтасков разные статусы");
    }

    @Test
    @DisplayName("Удаление Сабтаска по id")
    void shouldDeleteSubtaskById() {
        // Сохраняем Эпик в трекере
        var epicId = taskManager.addNewEpic(new Epic("Эпик с Сабтаском", "Описание", 0));
        // Затем сохраняем Сабтаск, который мы будем удалять
        var subId = taskManager.addNewSubtask(new Subtask("Сабтаск", "Описание", 0, TaskStatus.NEW),
                epicId);

        // Получаем сохранённый Сабтаск по id и проверяем, что он также сохранился в Эпике
        var savedSub = taskManager.getSubtaskById(subId);
        var epicSubs = taskManager.getEpicById(epicId).getEpicSubtasks();
        assertNotNull(savedSub, "Сабтаск не найден");
        assertEquals(1, epicSubs.size(), "В списке Сабтасков Эпика должна быть 1 подзадача");

        // Удаляем Сабтаск из трекера
        taskManager.deleteSubtaskById(subId);

        // Проверяем, что Сабтаск удалился из Эпика и трекера
        assertThrows(NoSuchElementException.class, () -> {
            taskManager.getSubtaskById(subId);
        }, "Сабтаск не удалился");
        var epicSubs1 = taskManager.getEpicById(epicId).getEpicSubtasks();
        assertTrue(epicSubs1.isEmpty(), "Сабтаск не удалился из Эпика");
    }

    @Test
    @DisplayName("Очищение списка Сабтасок")
    void shouldDeleteAllSubtasks() {
        // Создаём и добавляем Эпик в трекер задач
        final var epic = new Epic("Эпик с двумя Сабтасками", "Описание", 0);
        final int epicId = taskManager.addNewEpic(epic);

        // Создаём 2 Сабтаска и добавляем их в ранее созданный эпик по его id
        final var subtaskOne = new Subtask("Сабтаск 1", "Описание 1", 0, TaskStatus.NEW);
        final var subtaskTwo = new Subtask("Сабтаск 2", "Описание 2", 0, TaskStatus.NEW);
        taskManager.addNewSubtask(subtaskOne, epicId);
        taskManager.addNewSubtask(subtaskTwo, epicId);

        // Удаляем все Сабтаски
        taskManager.deleteAllSubtasks();

        // Получаем списки Эпиков и Сабтасок
        final var epics = taskManager.getAllEpics();
        final var subtasks = taskManager.getAllSubtasks();

        // Проверяем, удалились ли Сабтаски из трекера
        assertTrue(subtasks.isEmpty(), "Сабтаски не удалились");
        // Проверяем, что Эпик очистился от сабтасок, но сам не удалился
        assertEquals(1, epics.size(), "Эпик не должен удалиться вместе с Сабтасками");
        assertTrue(epics.getFirst().getEpicSubtasks().isEmpty(), "Список Сабтасок Эпика должен быть пустым");
    }

    @Test
    @DisplayName("Получение пустой истории просмотренных задач")
    void shouldReturnEmptyHistory() {
        // При создании трекера история задач должна быть пустой
        var historyManager = taskManager.getHistory();
        assertEquals(0, historyManager.size(), "История задач должна быть пустой");
    }

    @Test
    @DisplayName("Получение истории задач с 1-м просмотренным Таском")
    void shouldReturnHistoryWithOneTask() {
        // Добавляем Таск в трекер и получаем его по id
        final var task = new Task("Таск 1", "Описание", 0, TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);
        taskManager.getTaskById(taskId);

        // Проверяем, добавился ли Таск в историю просмотренных задач
        final var history = taskManager.getHistory();
        assertFalse(history.isEmpty(), "История задач не должна быть пустой");
        assertEquals(task, history.getFirst(), "В историю задач был добавлен не тот Таск");
    }

    @Test
    @DisplayName("Получение истории задач с 2-мя просмотренными задачами (Таском и Эпиком)")
    void shouldReturnHistoryWithTaskAndEpic() {
        // Добавляем Таск в трекер и получаем его по id
        final var task = new Task("Таск 1", "Описание", 0, TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);
        taskManager.getTaskById(taskId);

        // Добавляем Эпик в трекер и получаем его по id
        final var epic = new Epic("Эпик 1", "Описание", 0);
        final int epicId = taskManager.addNewEpic(epic);
        taskManager.getEpicById(epicId);

        // Проверяем, добавились ли Таск и Эпик в историю просмотренных задач
        final var history = taskManager.getHistory();
        assertFalse(history.isEmpty(), "История задач не должна быть пустой");
        assertEquals(2, history.size(), "В истории задач должны быть 2 задачи");
        assertEquals(epic, history.get(0), "В историю задач был добавлен не тот Эпик");
        assertEquals(task, history.get(1), "В историю задач был добавлен не тот Таск");
    }

    @Test
    @DisplayName("Получение истории задач с 3-мя просмотренными задачами (Таском, Эпиком и Сабтаском)")
    void shouldReturnHistoryWithTaskEpicAndSubtask() {
        // Добавляем Таск в трекер и получаем его по id
        final var task = new Task("Таск 1", "Описание", 0, TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);
        taskManager.getTaskById(taskId);

        // Добавляем Эпик в трекер и получаем его по id
        final var epic = new Epic("Эпик 1", "Описание", 0);
        final int epicId = taskManager.addNewEpic(epic);
        taskManager.getEpicById(epicId);

        // Добавляем Сабтаск в трекер и получаем его по id
        final var subtask = new Subtask("Сабтаск 1", "Описание", 0, TaskStatus.NEW);
        final int subtaskId = taskManager.addNewSubtask(subtask, epicId);
        taskManager.getSubtaskById(subtaskId);

        // Проверяем, добавились ли Таск, Эпик и Сабтаск в историю просмотренных задач
        final var history = taskManager.getHistory();
        assertFalse(history.isEmpty(), "История задач не должна быть пустой");
        assertEquals(3, history.size(), "В истории задач должны быть 3 задачи");
        assertEquals(subtask, history.get(0), "В историю задач был добавлен не тот Сабтаск");
        assertEquals(epic, history.get(1), "В историю задач был добавлен не тот Эпик");
        assertEquals(task, history.get(2), "В историю задач был добавлен не тот Таск");
    }

    @Test
    @DisplayName("Таск/Сабтаск без времени начала не должен учитываться в списке приоритетных задач")
    void shouldNotAddTaskWithNoStartTimeToPrioritizedList() {
        // Добавляем Таск без времени начала в трекер
        final var task = new Task("Таск 1", "Описание", 0, TaskStatus.NEW);
        taskManager.addNewTask(task);

        // Получаем список Тасков и список приоритетных задач, отсортированных по дате начала
        final var tasks = taskManager.getAllTasks();
        final var prioritizedTasks = taskManager.getPrioritizedTasks();

        // Проверяем, добавился ли Таск в список всех Тасков
        assertFalse(tasks.isEmpty(), "Список Тасков не должен быть пустым");
        // Проверяем, чтобы Таск без времени начала не добавился в список приоритетных задач
        assertTrue(prioritizedTasks.isEmpty() , "Список приоритетных задач должен быть пуст");
    }

    @Test
    @DisplayName("Таск/Сабтаск c временем начала должен учитываться в списке приоритетных задач")
    void shouldAddTaskWithStartTimeToPrioritizedList() {
        // Добавляем Таск с временем начала в трекер
        final var task = new Task("Таск 1", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 4,18, 12, 10), Duration.ofMinutes(5));
        taskManager.addNewTask(task);

        // Получаем список Тасков и список приоритетных задач, отсортированных по дате начала
        final var tasks = taskManager.getAllTasks();
        final var prioritizedTasks = taskManager.getPrioritizedTasks();

        // Проверяем, добавился ли Таск в список всех Тасков
        assertFalse(tasks.isEmpty(), "Список Тасков не должен быть пустым");
        // Проверяем, чтобы Таск c временем начала также добавился в список приоритетных задач
        assertFalse(prioritizedTasks.isEmpty() , "Список приоритетных задач не должен быть пустым");
    }

    @Test
    @DisplayName("Компаратор списка приоритетных задач должен сортировать Таски/Сабтаски по startTime")
    void shouldSortTasksByStartTimeInPrioritizedList() {
        // Добавляем Таск с временем начала 2025-18-04 12:10 в трекер
        final var firstPriorityTask = new Task("Таск 1", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 4,18, 12, 10), Duration.ofMinutes(5));
        taskManager.addNewTask(firstPriorityTask);

        // Также добавляем два Сабтаска с временем начала 2025-18-04 12:20 и 2025-18-04 12:30 в трекер
        final int epicId = taskManager.addNewEpic(new Epic("Эпик 1", "Описание 1", 0));
        final var secondPriorityTask = new Subtask("Сабтаск 1", "Описание 1", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 4,18, 12, 20), Duration.ofMinutes(10));
        final var thirdPriorityTask = new Subtask("Сабтаск 2", "Описание 2", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 4,18, 12, 30), Duration.ofMinutes(15));
        taskManager.addNewSubtask(secondPriorityTask, epicId);
        taskManager.addNewSubtask(thirdPriorityTask, epicId);

        // Получаем список приоритетных задач, отсортированных по дате начала
        final var prioritizedTasks = taskManager.getPrioritizedTasks();

        // Проверяем, чтобы в списке приоритетных задач было три элемента (Эпик не учитывается)
        assertEquals(3, prioritizedTasks.size(), "В списке приоритетных задач должно быть 3 элемента");
        // Первой по приоритету задачей должен быть Таск с временем начала 2025-18-04 12:10
        assertEquals(firstPriorityTask, prioritizedTasks.getFirst(), "Таск firstPriorityTask должен идти " +
                "первым в списке приоритетных задач");
        // Второй по приоритету задачей должен быть Сабтаск с временем начала 2025-18-04 12:20
        assertEquals(secondPriorityTask, prioritizedTasks.get(1), "Сабтаск secondPriorityTask должен идти" +
                " вторым в списке приоритетных задач");
        // Последней по приоритету задачей должен быть Сабтаск с временем начала 2025-18-04 12:30
        assertEquals(thirdPriorityTask, prioritizedTasks.getLast(), "Сабтаск thirdPriorityTask должен идти" +
                " последним в списке приоритетных задач");
    }
}