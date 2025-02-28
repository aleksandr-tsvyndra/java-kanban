package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epicTasks;
    protected final Map<Integer, Subtask> subtasks;

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
    public void deleteTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            return;
        }
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        if (tasks.isEmpty()) {
            return;
        }
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public List<Task> getAllTasks() {
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(tasks.values());
    }

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
    public List<Epic> getAllEpics() {
        if (epicTasks.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public List<Subtask> getAllEpicSubtasks(int epicId) {
        final Epic epic = epicTasks.get(epicId);
        final List<Subtask> subs = epic.getEpicSubtasks();
        if (subs.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(subs);
    }

    @Override
    public void deleteEpicById(Integer epicId) {
        if (!epicTasks.containsKey(epicId)) {
            return;
        }
        Epic epic = epicTasks.get(epicId);
        List<Subtask> deletedSubs = epic.getEpicSubtasks();
        epic.deleteAllEpicSubtasks();
        for (Subtask sub : deletedSubs) {
            historyManager.remove(sub.getId());
            subtasks.remove(sub.getId());
        }
        historyManager.remove(epicId);
        epicTasks.remove(epicId);
    }

    @Override
    public void deleteAllEpics() {
        if (epicTasks.isEmpty()) {
            return;
        }
        for (Integer taskId : subtasks.keySet()) {
            historyManager.remove(taskId);
        }
        for (Integer taskId : epicTasks.keySet()) {
            historyManager.remove(taskId);
        }
        subtasks.clear();
        epicTasks.clear();
    }

    @Override
    public Integer addNewSubtask(Subtask newSubtask, int epicId) {
        if (!epicTasks.containsKey(epicId)) {
            return null;
        }
        int newId = generateNewId();
        newSubtask.setId(newId);
        newSubtask.setEpicId(epicId);
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epicTasks.get(epicId);
        epic.addSubtaskInEpic(newSubtask);
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
    public List<Subtask> getAllSubtasks() {
        if (subtasks.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
            return;
        }
        Subtask subtask = subtasks.get(id);
        Epic epic = epicTasks.get(subtask.getEpicId());
        epic.deleteSubtaskInEpic(id);
        epic.calculateEpicStatus();
        historyManager.remove(id);
        subtasks.remove(id);
    }

    @Override
    public void deleteAllSubtasks() {
        if (subtasks.isEmpty()) {
            return;
        }
        for (Integer taskId : subtasks.keySet()) {
            historyManager.remove(taskId);
        }
        subtasks.clear();
        for (Epic epic : epicTasks.values()) {
            epic.deleteAllEpicSubtasks();
            epic.calculateEpicStatus();
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private int generateNewId() {
        return id++;
    }
}