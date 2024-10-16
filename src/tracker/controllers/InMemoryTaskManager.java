package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epicTasks;
    private final Map<Integer, Subtask> subtasks;

    private final HistoryManager historyManager;

    private int id = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = historyManager;
    }

    @Override
    public int addNewTask(Task newTask) {
        int newId = generateNewId();
        newTask.setId(newId);
        tasks.put(newTask.getId(), newTask);
        return newId;
    }

    @Override
    public Task updateTask(Task updatedTask) {
        if (!tasks.containsKey(updatedTask.getId())) {
            return null;
        }
        tasks.put(updatedTask.getId(), updatedTask);
        return tasks.get(updatedTask.getId());
    }

    @Override
    public Task getTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            return null;
        }
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public void deleteTaskById(Integer id) { tasks.remove(id); }

    @Override
    public void deleteAllTasks() { tasks.clear(); }

    @Override
    public List<Task> getAllTasks() { return new ArrayList<>(tasks.values()); }

    @Override
    public int addNewEpic(Epic newEpic) {
        int newId = generateNewId();
        newEpic.setId(newId);
        epicTasks.put(newEpic.getId(), newEpic);
        return newId;
    }

    @Override
    public Epic updateEpic(Epic updatedEpic) {
        int epicId = updatedEpic.getId();
        if (!epicTasks.containsKey(epicId)) {
            return null;
        }
        Epic epic = epicTasks.get(epicId);
        List<Subtask> subtasks = epic.getEpicSubtasks();
        updatedEpic.setEpicSubtasks(subtasks);
        updatedEpic.calculateEpicStatus();
        epicTasks.put(epicId, updatedEpic);
        return epicTasks.get(epicId);
    }

    @Override
    public Epic getEpicById(Integer epicId) {
        if (!epicTasks.containsKey(epicId)) {
            return null;
        }
        historyManager.add(epicTasks.get(epicId));
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
            return;
        }
        Epic epic = epicTasks.get(epicId);
        List<Subtask> deletedSubs = epic.getEpicSubtasks();
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
    public Integer addNewSubtask(Subtask newSubtask, int epicId) {
        if (!epicTasks.containsKey(epicId)) {
            return null;
        }
        int newId = generateNewId();
        newSubtask.setId(newId);
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epicTasks.get(epicId);
        List<Subtask> epicSubtasks = epic.getEpicSubtasks();
        newSubtask.setEpicId(epicId);
        epicSubtasks.add(newSubtask);
        epic.calculateEpicStatus();
        return newId;
    }

    @Override
    public Subtask updateSubtask(Subtask updatedSubtask) {
        if (!subtasks.containsKey(updatedSubtask.getId())) {
            return null;
        }
        int subtaskId = updatedSubtask.getId();
        Subtask sub = subtasks.get(subtaskId);
        updatedSubtask.setEpicId(sub.getEpicId());
        subtasks.put(subtaskId, updatedSubtask);

        int epicId = updatedSubtask.getEpicId();
        Epic epic = epicTasks.get(epicId);
        epic.updateSubtaskInEpic(updatedSubtask);
        epic.calculateEpicStatus();
        return subtasks.get(subtaskId);
    }

    @Override
    public Subtask getSubtaskById(Integer subtaskId) {
        if (!subtasks.containsKey(subtaskId)) {
            return null;
        }
        historyManager.add(subtasks.get(subtaskId));
        return subtasks.get(subtaskId);
    }

    @Override
    public List<Subtask> getAllSubtasks() { return new ArrayList<>(subtasks.values()); }

    @Override
    public void deleteSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
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
    public List<Task> getHistory() { return historyManager.getHistory(); }

    private int generateNewId() { return id++; }
}
