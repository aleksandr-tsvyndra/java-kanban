package tracker.controllers;

import tracker.exceptions.TaskInteractionException;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.TreeSet;
import java.util.Set;

import java.util.HashMap;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epicTasks;
    protected final Map<Integer, Subtask> subtasks;

    protected Set<Task> prioritizedTasks;

    private final HistoryManager historyManager;

    protected int id = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subtasks = new HashMap<>();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        this.historyManager = historyManager;
    }

    @Override
    public int addNewTask(Task task) {
        if (hasInteractions(task)) {
            String errorMessage = "Ошибка при добавлении: задачи не могут пересекаться по времени выполнения";
            throw new TaskInteractionException(errorMessage);
        }
        int newId = generateNewId();
        task.setId(newId);
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return newId;
    }

    @Override
    public Task updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return null;
        }
        if (hasInteractions(task)) {
            String errorMessage = "Ошибка при обновлении: задачи не могут пересекаться по времени выполнения";
            throw new TaskInteractionException(errorMessage);
        }
        prioritizedTasks.remove(tasks.get(task.getId()));
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return tasks.get(task.getId());
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
        prioritizedTasks.remove(tasks.get(id));
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        if (tasks.isEmpty()) {
            return;
        }
        tasks.values().forEach(prioritizedTasks::remove);
        tasks.keySet().forEach(historyManager::remove);
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
        updatedEpic.setEpicSubtasks(epic.getEpicSubtasks());
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
        Epic epic = epicTasks.get(epicId);
        if (epic.getEpicSubtasks().isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(epic.getEpicSubtasks());
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
            prioritizedTasks.remove(sub);
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
        subtasks.values().forEach(prioritizedTasks::remove);
        subtasks.keySet().forEach(historyManager::remove);
        epicTasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epicTasks.clear();
    }

    @Override
    public Integer addNewSubtask(Subtask subtask, int epicId) {
        if (!epicTasks.containsKey(epicId)) {
            return null;
        }
        if (hasInteractions(subtask)) {
            String errorMessage = "Ошибка при добавлении: задачи не могут пересекаться по времени выполнения";
            throw new TaskInteractionException(errorMessage);
        }
        int newId = generateNewId();
        subtask.setId(newId);
        subtask.setEpicId(epicId);
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);

        Epic epic = epicTasks.get(epicId);
        epic.addSubtaskInEpic(subtask);
        return newId;
    }

    @Override
    public Subtask updateSubtask(Subtask updatedSubtask) {
        if (!subtasks.containsKey(updatedSubtask.getId())) {
            return null;
        }
        if (hasInteractions(updatedSubtask)) {
            String errorMessage = "Ошибка при обновлении: задачи не могут пересекаться по времени выполнения";
            throw new TaskInteractionException(errorMessage);
        }
        int subtaskId = updatedSubtask.getId();
        Subtask sub = subtasks.get(subtaskId);
        updatedSubtask.setEpicId(sub.getEpicId());
        prioritizedTasks.remove(sub);
        subtasks.put(subtaskId, updatedSubtask);
        prioritizedTasks.add(updatedSubtask);

        int epicId = updatedSubtask.getEpicId();
        Epic epic = epicTasks.get(epicId);
        epic.updateSubtaskInEpic(updatedSubtask);
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
        prioritizedTasks.remove(subtask);
        historyManager.remove(id);
        subtasks.remove(id);
    }

    @Override
    public void deleteAllSubtasks() {
        if (subtasks.isEmpty()) {
            return;
        }
        subtasks.values().forEach(prioritizedTasks::remove);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epicTasks.values().forEach(Epic::deleteAllEpicSubtasks);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        if (prioritizedTasks.isEmpty()) {
            return Collections.emptyList();
        }
        return prioritizedTasks.stream().toList();
    }

    private boolean hasInteractions(Task task) {
        for (Task t : prioritizedTasks) {
            if (task.getStartTime().isEqual(t.getStartTime()) || task.getEndTime().isEqual(t.getEndTime())) {
                return true;
            } else if (task.getStartTime().isBefore(t.getStartTime()) && task.getEndTime().isAfter(t.getStartTime())) {
                return true;
            } else if (task.getStartTime().isBefore(t.getEndTime()) && task.getEndTime().isAfter(t.getEndTime())) {
                return true;
            } else if (task.getStartTime().isAfter(t.getStartTime()) && task.getEndTime().isBefore(t.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    private int generateNewId() {
        return id++;
    }
}