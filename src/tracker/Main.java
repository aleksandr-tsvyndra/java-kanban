package tracker;

import tracker.controllers.TaskManager;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import tracker.util.TaskStatus;
import tracker.util.Managers;

public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefault();

        Task taskOne = new Task("Переезд", "Задача 1", 0, TaskStatus.NEW);
        Task taskTwo = new Task("Помыть посуду", "Задача 2", 0, TaskStatus.NEW);
        taskManager.addNewTask(taskOne);
        taskManager.addNewTask(taskTwo);

        Epic epicOne = new Epic("Эпик 1", "Эпик с 2-мя подзадачами", 0, TaskStatus.NEW);
        Subtask subOne = new Subtask("Подзадача 1", "Подзадача 1-го эпика", 0, TaskStatus.NEW);
        Subtask subTwo = new Subtask("Подзадача 2", "Подзадача 1-го эпика", 0, TaskStatus.NEW);
        taskManager.addNewEpic(epicOne);
        taskManager.addNewSubtask(subOne, epicOne.getId());
        taskManager.addNewSubtask(subTwo, epicOne.getId());

        Epic epicTwo = new Epic("Эпик 2", "Эпик с 1-ой подзадачей", 0, TaskStatus.NEW);
        Subtask subThree = new Subtask("Подзадача 3", "Подзадача 2-го эпика", 0, TaskStatus.NEW);
        taskManager.addNewEpic(epicTwo);
        taskManager.addNewSubtask(subThree, epicTwo.getId());

        System.out.println("Список задач: " + taskManager.getAllTasks());
        System.out.println("Список эпиков: " + taskManager.getAllEpics());
        System.out.println("Список подзадач: " + taskManager.getAllSubtasks());

        Task updatedTaskOne = new Task("Вынести мусор", "Задача 1", 1, TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTaskOne);
        System.out.println("Обновленная задача 1: " + taskManager.getTaskById(taskOne.getId()));

        System.out.println("Список подзадач эпика 1: " + taskManager.getAllEpicSubtasks(epicOne.getId()));

        Subtask updatedSubOne = new Subtask("Обновленная подзадача 1", "Подзадача 1-го эпика", 4,
                TaskStatus.DONE);
        taskManager.updateSubtask(updatedSubOne);
        System.out.println("Обновленная подзадача 1: " + taskManager.getSubtaskById(subOne.getId()));
        System.out.println("Статус Эпика 1 поменялся с NEW на IN_PROGRESS: " + epicOne.getStatus());

        Subtask updatedSubTwo = new Subtask("Обновленная подзадача 2", "Подзадача 1-го эпика", 5,
                TaskStatus.DONE);
        taskManager.updateSubtask(updatedSubTwo);
        System.out.println("Обновленная подзадача 2: " + taskManager.getSubtaskById(subTwo.getId()));
        System.out.println("Статус Эпика 1 поменялся с IN_PROGRESS на DONE: " + epicOne.getStatus());

        System.out.println("Получаем Эпик 1 по id: " + taskManager.getEpicById(3));

        Subtask updatedSubThree = new Subtask("Обновленная подзадача 3", "Подзадача 2-го эпика", 7,
                TaskStatus.DONE);
        taskManager.updateSubtask(updatedSubThree);
        System.out.println("Обновленная подзадача 3: " + taskManager.getSubtaskById(subThree.getId()));
        System.out.println("Статус Эпика 2 поменялся с NEW на DONE: " + epicTwo.getStatus());

        Epic updatedEpicOne = new Epic("Обновленный эпик 1", "Эпик с 2-мя подзадачами", 3,
                TaskStatus.NEW);
        taskManager.updateEpic(updatedEpicOne);
        System.out.println("Обновленный эпик 1: " + taskManager.getEpicById(updatedEpicOne.getId()));

        taskManager.deleteTaskById(taskTwo.getId());
        System.out.println("Список задач стал меньше на одну задачу: " + taskManager.getAllTasks());
        taskManager.deleteAllTasks();
        System.out.println("Список задач стал пустым: " + taskManager.getAllTasks());

        taskManager.deleteEpicById(epicTwo.getId());
        System.out.println("Список эпиков уменьшился на один: " + taskManager.getAllEpics());
        System.out.println("Из списка подзадач удалилась подзадача 3, привязанная к ранее удаленному эпику 2: " +
                taskManager.getAllSubtasks());

        taskManager.deleteAllEpics();
        System.out.println("Список эпиков очистился: " + taskManager.getAllEpics());
        System.out.println("Список подзадач очистился вместе с эпиками: " + taskManager.getAllSubtasks());

        Epic epicWithThreeSubs = new Epic("Новый эпик", "Эпик с 3-мя подзадачами", 0, TaskStatus.NEW);
        Subtask sub1 = new Subtask("Подзадача 1", "Описание", 0, TaskStatus.DONE);
        Subtask sub2 = new Subtask("Подзадача 2", "Описание", 0, TaskStatus.NEW);
        Subtask sub3 = new Subtask("Подзадача 3", "Описание", 0, TaskStatus.NEW);
        taskManager.addNewEpic(epicWithThreeSubs);
        taskManager.addNewSubtask(sub1, epicWithThreeSubs.getId());
        taskManager.addNewSubtask(sub2, epicWithThreeSubs.getId());
        taskManager.addNewSubtask(sub3, epicWithThreeSubs.getId());

        System.out.println("Получаем Эпик 1 по id: " + taskManager.getEpicById(epicWithThreeSubs.getId()));

        taskManager.deleteAllSubtasks();
        System.out.println("Список подзадач очистился: " + taskManager.getAllSubtasks());
        System.out.println("Эпик также очистился от подзадач, но сам не удалился: " + taskManager.getAllEpics());
    }
}