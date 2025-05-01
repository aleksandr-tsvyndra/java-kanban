package tracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tracker.util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

class TaskTest {
    @Test
    @DisplayName("Сравнение задач с одинаковыми id")
    void shouldBeEqualsWithSameId() {
        // Создаём два Таска с одинаковым индентификатором, но разными полями
        var task1 = new Task("Таск 1", "Описание 1", 1, TaskStatus.NEW);
        var task2 = new Task("Таск 2", "Описание 2", 1, TaskStatus.DONE);

        // Сравниваем Таски - они должны быть равны
        assertEquals(task1, task2, "Таски должны считаться равными, если равен их id");

        // Создаём два Эпика с одинаковым индентификатором, но разными полями
        var epic1 = new Epic("Эпик 1", "Описание 1", 2);
        var epic2 = new Epic("Эпик 2", "Описание 2", 2);

        // Сравниваем Эпики - они должны быть равны
        assertEquals(epic1, epic2, "Эпики должны считаться равными, если равен их id");

        // Создаём два Сабтаска с одинаковым индентификатором, но разными полями
        var sub1 = new Subtask("Сабтаск 1", "Описание 1", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 5, 18, 14, 20), Duration.ofMinutes(5));
        var sub2 = new Subtask("Сабтаск 2", "Описание 2", 3, TaskStatus.DONE,
                LocalDateTime.of(2025, 6, 19, 15, 25), Duration.ofMinutes(15));

        // Сравниваем Сабтаски - они должны быть равны
        assertEquals(sub1, sub2, "Сабтаски должны считаться равными, если равен их id");
    }

    @Test
    @DisplayName("Сравнение задач с разными id")
    void shouldNotBeEqualsWithDifferentId() {
        // Создаём два Таска с разными индентификаторами, но одинаковыми полями
        var task1 = new Task("Таск", "Описание", 1, TaskStatus.NEW);
        var task2 = new Task("Таск", "Описание", 2, TaskStatus.NEW);

        // Сравниваем Таски - они не должны быть равными
        assertNotEquals(task1, task2, "Таски не должны считаться равными, если отличаются их id");
    }

    @Test
    @DisplayName("Проверка полей startTime и duration")
    void shouldInitializeTimeFields() {
        // Создаём Таск с временем начала и длительности задачи
        var task = new Task("Таск", "Описание", 0, TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 16, 12, 14), Duration.ofMinutes(10));

        // Готовим две переменные типа LocalDateTime и Duration для проверки
        var expectedStartTime = LocalDateTime.of(2025, 3, 16, 12, 14);
        var expectedDuration = Duration.ofMinutes(10);

        // Проверяем, что в Таск сохранилось корректное время
        assertEquals(expectedStartTime, task.getStartTime(), "В Таске неверное стартовое время");
        assertEquals(expectedDuration, task.getDuration(), "В Таске неверная длительность задачи");
    }

    @Test
    @DisplayName("Вычисление времени окончания задачи на основе время её начала и длительности")
    void shouldCalculateEndTime() {
        // Создаём Таск с временем начала 2025-16-03 12:15 и длительностью 10 минут
        var startTime = LocalDateTime.of(2025, 3, 16, 12, 15);
        var duration = Duration.ofMinutes(10);
        var task = new Task("Таск", "Описание", 0, TaskStatus.NEW, startTime, duration);

        // Высчитываем "вручную" время завершения задачи
        var expectedTaskEndTime = startTime.plusMinutes(duration.toMinutes());

        // Вызываем метод getEndTime, который рассчитывает время окончания задачи
        var taskEndTime = task.getEndTime();

        // Проверяем, что переменная getEndTime содержит корректное время завершения задачи
        assertEquals(expectedTaskEndTime, taskEndTime, "Метод getEndTime вычислил неверное время");
    }

    @Test
    @DisplayName("Вычисление startTime/duration/endTime-полей Эпика с одним Сабтаском")
    void shouldCalculateEpicTimeFieldsWithOneSub() {
        // Создаём Эпик, в который будем добавлять Сабтаск и смотреть на его поля startTime, duration, endTime
        var epic = new Epic("Эпик", "Описание", 1);

        // Проверяем, чтобы поля времени Эпика без Сабтасков были пустые
        assertNull(epic.getStartTime(), "startTime Эпика должен быть пустым, если он без Сабтасков");
        assertNull(epic.getDuration(), "duration Эпика должен быть пустым, если он без Сабтасков");
        assertNull(epic.getEndTime(), "endTime Эпика должен быть пустым, если он без Сабтасков");

        // Создаём переменные типа LocalDateTime и Duration, которые мы присвоим Сабтаску
        var epicStartTime = LocalDateTime.of(2025, 3, 16, 12, 14);
        var epicDuration = Duration.ofMinutes(10);

        // Добавляем Сабтаск в Эпик
        epic.addSubtaskInEpic(new Subtask("Сабтаск 1", "Описание", 2, TaskStatus.NEW, epicStartTime,
                epicDuration));

        // Высчитываем "вручную" время завершения Эпика
        var epicEndTime = epicStartTime.plusMinutes(epicDuration.toMinutes());

        // Проверяем поля времени Эпика, что они соответствуют значениям переменных epicStartTime, epicDuration
        // и epicEndTime
        assertEquals(epicStartTime, epic.getStartTime(), "startTime Эпика и Сабтаска отличаются");
        assertEquals(epicDuration, epic.getDuration(), "duration Эпика и Сабтаска отличаются");
        assertEquals(epicEndTime, epic.getEndTime(), "endTime Эпика был неверно рассчитан");
    }

    @Test
    @DisplayName("Вычисление startTime/duration/endTime-полей Эпика с тремя Сабтасками")
    void shouldCalculateEpicTimeFieldsWithThreeSubs() {
        // Создаём Эпик, в который будем добавлять Сабтаски и смотреть на его поля startTime, duration, endTime
        var epic = new Epic("Эпик", "Описание", 1);

        // Проверяем, чтобы поля времени Эпика без Сабтасков были пустые
        assertNull(epic.getStartTime(), "startTime Эпика должен быть пустым, если он без Сабтасков");
        assertNull(epic.getDuration(), "duration Эпика должен быть пустым, если он без Сабтасков");
        assertNull(epic.getEndTime(), "endTime Эпика должен быть пустым, если он без Сабтасков");

        // Создаём три Сабтаска для сохранения в Эпике
        var subOne = new Subtask("Сабтаск 1", "Описание", 2, TaskStatus.NEW,
                LocalDateTime.of(2025, 1, 1, 10, 0), Duration.ofMinutes(5));
        var subTwo = new Subtask("Сабтаск 2", "Описание", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 1, 1, 11, 0), Duration.ofMinutes(15));
        var subThree = new Subtask("Сабтаск 3", "Описание", 4, TaskStatus.NEW,
                LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(25));

        // Высчитываем "вручную" поля Эпика для проверки
        var epicStartTime = subOne.getStartTime();
        var epicDuration = subOne.getDuration().plus(subTwo.getDuration().plus(subThree.getDuration()));
        var epicEndTime = subThree.getStartTime().plusMinutes(subThree.getDuration().toMinutes());

        // Сохраняем Сабтаски в Эпике
        epic.addSubtaskInEpic(subOne);
        epic.addSubtaskInEpic(subTwo);
        epic.addSubtaskInEpic(subThree);

        // Проверяем поля времени Эпика, что они соответствуют значениям переменных epicStartTime, epicDuration
        // и epicEndTime
        assertEquals(epicStartTime, epic.getStartTime(), "startTime Эпика задан неверно");
        assertEquals(epicDuration, epic.getDuration(), "duration Эпика вычислен неверно");
        assertEquals(epicEndTime, epic.getEndTime(), "endTime Эпика был вычислен неверно");
    }

    @Test
    @DisplayName("Вычисление статуса Эпика по статусу его Сабтасков")
    void shouldCalculateEpicStatus() {
        // Создаём Эпик, в который будем добавлять Сабтаски и тем самым менять его статус
        var epic = new Epic("Эпик", "Описание", 0);

        // Проверяем, чтобы статус Эпика без Сабтасков был NEW
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус Эпика должен быть NEW");

        // Добавляем в Эпик первый Сабтаск со статусом NEW
        epic.addSubtaskInEpic(new Subtask("Сабтаск 1", "Описание", 1, TaskStatus.NEW));

        // При добавлении в пустой Эпик Сабтаска со статусом NEW его статус должен быть также NEW
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус Эпика должен остаться NEW");

        // Добавляем в Эпик второй Сабтаск, но уже со статусом DONE
        epic.addSubtaskInEpic(new Subtask("Сабтаск 2", "Описание", 2, TaskStatus.DONE));

        // Если у Эпика один из его Сабтасков имеет статус NEW, а второй - DONE,
        // то статус самого Эпика должен быть IN_PROGRESS
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус Эпика должен быть IN_PROGRESS");

        // Добавляем в Эпик третий Сабтаск, статус которого IN_PROGRESS
        epic.addSubtaskInEpic(new Subtask("Сабтаск 3", "Описание", 3, TaskStatus.IN_PROGRESS));

        // Если у Эпика один из его Сабтасков имеет статус NEW, второй - DONE, а третий - IN_PROGRESS,
        // то статус самого Эпика должен быть IN_PROGRESS
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус Эпика должен остаться IN_PROGRESS");

        // Меняем статусы всех трёх Сабтасков Эпика на DONE
        var epicSubsDone = epic.getEpicSubtasks();
        epicSubsDone.forEach(s -> s.setStatus(TaskStatus.DONE));
        epic.setEpicSubtasks(epicSubsDone);

        // Если у Эпика все Сабтаски со статусом DONE, его статус также должен быть DONE
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус Эпика должен быть DONE");

        // Меняем статусы всех трёх Сабтасков Эпика на IN_PROGRESS
        var epicSubsInProgress = epic.getEpicSubtasks();
        epicSubsInProgress.forEach(s -> s.setStatus(TaskStatus.IN_PROGRESS));
        epic.setEpicSubtasks(epicSubsInProgress);

        // Если у Эпика все Сабтаски со статусом IN_PROGRESS, его статус также должен быть IN_PROGRESS
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус Эпика должен быть IN_PROGRESS");
    }
}