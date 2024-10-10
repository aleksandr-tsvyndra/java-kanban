package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks;
    private Map<Integer, Epic> epicTasks;
    private Map<Integer, Subtask> subtasks;
    private List<Task> history;

    private static int id = 1;
    private static final int HISTORY_LIMIT = 10;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subtasks = new HashMap<>();
        history = new ArrayList<>();
    }

    @Override
    public void addNewTask(Task newTask) {
        int newId = generateNewId();
        newTask.setId(newId);
        tasks.put(newTask.getId(), newTask);
    }

    @Override
    public void updateTask(Task updatedTask) {
        if (!tasks.containsKey(updatedTask.getId())) {
            System.out.println("Ошибка: задачи с таким id не существует!");
            return;
        }
        tasks.put(updatedTask.getId(), updatedTask);
    }

    @Override
    public Task getTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            System.out.println("Ошибка: задачи с таким id не существует!");
            return null;
        }
        if (history.size() == HISTORY_LIMIT) {
            history.remove(0);
        }
        history.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public void deleteTaskById(Integer id) { tasks.remove(id); }

    @Override
    public void deleteAllTasks() { tasks.clear(); }

    @Override
    public List<Task> getAllTasks() { return new ArrayList<>(tasks.values()); }

    @Override
    public void addNewEpic(Epic newEpic) {
        int newId = generateNewId();
        newEpic.setId(newId);
        epicTasks.put(newEpic.getId(), newEpic);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        int epicId = updatedEpic.getId();
        if (!epicTasks.containsKey(epicId)) {
            System.out.println("Ошибка: эпика с таким id не существует!");
            return;
        }
        Epic epic = epicTasks.get(epicId);
        ArrayList<Subtask> subtasks = epic.getEpicSubtasks();
        updatedEpic.setEpicSubtasks(subtasks);
        updatedEpic.calculateEpicStatus();
        epicTasks.put(epicId, updatedEpic);
    }

    @Override
    public Epic getEpicById(Integer epicId) {
        if (!epicTasks.containsKey(epicId)) {
            System.out.println("Ошибка: эпика с таким id не существует!");
            return null;
        }
        if (history.size() == HISTORY_LIMIT) {
            history.remove(0);
        }
        history.add(epicTasks.get(epicId));
        return epicTasks.get(epicId);
    }

    @Override
    public List<Epic> getAllEpics() { return new ArrayList<>(epicTasks.values()); }

    @Override
    public List<Subtask> getAllEpicSubtasks(int epicId) {
        Epic epic = epicTasks.get(epicId);
        return epic.getEpicSubtasks();
    }

    @Override
    public void deleteEpicById(Integer epicId) {
        if (!epicTasks.containsKey(epicId)) {
            System.out.println("Ошибка: эпика с таким id не существует!");
            return;
        }
        Epic epic = epicTasks.get(epicId);
        ArrayList<Subtask> deletedSubs = epic.getEpicSubtasks();
        for (Subtask sub : deletedSubs) {
            subtasks.remove(sub.getId());
        }
        epicTasks.remove(epicId);
    }

    @Override
    public void deleteAllEpics() {
        epicTasks.clear();
        subtasks.clear();
    }

    @Override
    public void addNewSubtask(Subtask newSubtask, int epicId) {
        if (!epicTasks.containsKey(epicId)) {
            System.out.println("Ошибка: эпика с таким id не существует!");
            return;
        }
        int newId = generateNewId();
        newSubtask.setId(newId);
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epicTasks.get(epicId);
        ArrayList<Subtask> epicSubtasks = epic.getEpicSubtasks();
        newSubtask.setEpicId(epicId);
        epicSubtasks.add(newSubtask);
        epic.calculateEpicStatus();
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        int subtaskId = updatedSubtask.getId();
        Subtask sub = subtasks.get(subtaskId);
        updatedSubtask.setEpicId(sub.getEpicId());
        subtasks.put(subtaskId, updatedSubtask);
        int epicId = updatedSubtask.getEpicId();
        Epic epic = epicTasks.get(epicId);
        epic.updateSubtaskInEpic(updatedSubtask);
        epic.calculateEpicStatus();
    }

    @Override
    public Subtask getSubtaskById(Integer subtaskId) {
        if (!subtasks.containsKey(subtaskId)) {
            System.out.println("Ошибка: подзадачи с таким id не существует!");
            return null;
        }
        if (history.size() == HISTORY_LIMIT) {
            history.remove(0);
        }
        history.add(subtasks.get(subtaskId));
        return subtasks.get(subtaskId);
    }

    @Override
    public List<Subtask> getAllSubtasks() { return new ArrayList<>(subtasks.values()); }

    @Override
    public void deleteSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("Ошибка: подзадачи с таким id не существует!");
            return;
        }
        Subtask subtask = subtasks.get(id);
        Epic epic = epicTasks.get(subtask.getEpicId());
        epic.deleteSubtaskInEpic(subtask.getId());
        epic.calculateEpicStatus();
        subtasks.remove(subtask.getId());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epicTasks.values()) {
            epic.deleteAllEpicSubtasks();
            epic.calculateEpicStatus();
        }
    }

    @Override
    public List<Task> getHistory() { return history; }

    private int generateNewId() { return id++; }
}
