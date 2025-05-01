package tracker.controllers;

import tracker.exceptions.TaskInteractionException;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.NoSuchElementException;
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
        if (task == null) {
            throw new IllegalArgumentException("В параметр task был передан null");
        }

        if (task.getStartTime() != null) {
            if (hasInteractions(task)) {
                String errorMessage = "Задачи не могут пересекаться по времени выполнения";
                throw new TaskInteractionException(errorMessage);
            }
            prioritizedTasks.add(task);
        }

        int newId = generateNewId();
        task.setId(newId);
        tasks.put(newId, task);
        return newId;
    }

    @Override
    public Task updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("В параметр task был передан null");
        }

        if (!tasks.containsKey(task.getId())) {
            String errorMessage = String.format("В трекере нет задачи с id %d", task.getId());
            throw new NoSuchElementException(errorMessage);
        }

        if (task.getStartTime() != null) {
            if (hasInteractions(task)) {
                String errorMessage = "Задачи не могут пересекаться по времени выполнения";
                throw new TaskInteractionException(errorMessage);
            }
        }

        if (getPrioritizedTasks().contains(task)) {
            prioritizedTasks.remove(tasks.get(task.getId()));
        }

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }

        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task getTaskById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("У тасков не может быть id меньше 1");
        }

        if (!tasks.containsKey(id)) {
            String errorMessage = String.format("В трекере нет задачи с id %d", id);
            throw new NoSuchElementException(errorMessage);
        }

        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public void deleteTaskById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("У тасков не может быть id меньше 1");
        }

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
        if (newEpic == null) {
            throw new IllegalArgumentException("В параметр newEpic был передан null");
        }

        int newId = generateNewId();
        newEpic.setId(newId);
        epicTasks.put(newEpic.getId(), newEpic);
        return newId;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("В параметр epic был передан null");
        }

        if (!epicTasks.containsKey(epic.getId())) {
            String errorMessage = String.format("В трекере нет эпика с id %d", epic.getId());
            throw new NoSuchElementException(errorMessage);
        }

        Epic e = epicTasks.get(epic.getId());

        if (!e.getEpicSubtasks().isEmpty()) {
            epic.setEpicSubtasks(e.getEpicSubtasks());
        }

        epicTasks.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Epic getEpicById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("У эпика не может быть id меньше 1");
        }

        if (!epicTasks.containsKey(id)) {
            String errorMessage = String.format("В трекере нет эпика с id %d", id);
            throw new NoSuchElementException(errorMessage);
        }

        historyManager.add(epicTasks.get(id));
        return epicTasks.get(id);
    }

    @Override
    public List<Epic> getAllEpics() {
        if (epicTasks.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public List<Subtask> getAllEpicSubtasks(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("У эпика не может быть id меньше 1");
        }

        if (!epicTasks.containsKey(id)) {
            String errorMessage = String.format("В трекере нет эпика с id %d", id);
            throw new NoSuchElementException(errorMessage);
        }

        var epic = epicTasks.get(id);

        if (epic.getEpicSubtasks().isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(epic.getEpicSubtasks());
    }

    @Override
    public void deleteEpicById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("У эпика не может быть id меньше 1");
        }

        if (!epicTasks.containsKey(id)) {
            return;
        }

        var epic = epicTasks.get(id);
        epic.getEpicSubtasks().forEach(prioritizedTasks::remove);
        epic.getEpicSubtasks().stream().map(Task::getId).forEach(historyManager::remove);
        epic.getEpicSubtasks().stream().map(Task::getId).forEach(subtasks::remove);
        historyManager.remove(id);
        epicTasks.remove(id);
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
    public int addNewSubtask(Subtask subtask, int epicId) {
        if (subtask == null) {
            throw new IllegalArgumentException("В параметр subtask был передан null");
        }

        if (epicId < 1) {
            throw new IllegalArgumentException("У эпика не может быть id меньше 1");
        }

        if (!epicTasks.containsKey(epicId)) {
            String errorMessage = String.format("В трекере нет эпика с id %d", epicId);
            throw new NoSuchElementException(errorMessage);
        }

        if (subtask.getStartTime() != null) {
            if (hasInteractions(subtask)) {
                String errorMessage = "Задачи не могут пересекаться по времени выполнения";
                throw new TaskInteractionException(errorMessage);
            }
            prioritizedTasks.add(subtask);
        }

        int id = generateNewId();
        subtask.setId(id);
        subtask.setEpicId(epicId);
        subtasks.put(id, subtask);
        var epic = epicTasks.get(epicId);
        epic.addSubtaskInEpic(subtask);
        return id;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("В параметр subtask был передан null");
        }

        if (!subtasks.containsKey(subtask.getId())) {
            String errorMessage = String.format("В трекере нет сабтаска с id %d", subtask.getId());
            throw new NoSuchElementException(errorMessage);
        }

        if (subtask.getStartTime() != null) {
            if (hasInteractions(subtask)) {
                String errorMessage = "Задачи не могут пересекаться по времени выполнения";
                throw new TaskInteractionException(errorMessage);
            }
        }

        int id = subtask.getId();
        var sub = subtasks.get(id);
        subtask.setEpicId(sub.getEpicId());

        if (getPrioritizedTasks().contains(sub)) {
            prioritizedTasks.remove(sub);
        }

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        var epic = epicTasks.get(subtask.getEpicId());
        epic.updateSubtaskInEpic(subtask);
        subtasks.put(id, subtask);
        return subtask;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("У сабтаска не может быть id меньше 1");
        }

        if (!subtasks.containsKey(id)) {
            String errorMessage = String.format("В трекере нет сабтаска с id %d", id);
            throw new NoSuchElementException(errorMessage);
        }

        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        if (subtasks.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("У сабтаска не может быть id меньше 1");
        }

        if (!subtasks.containsKey(id)) {
            return;
        }

        var sub = subtasks.get(id);
        var epic = epicTasks.get(sub.getEpicId());
        epic.deleteSubtaskInEpic(id);
        prioritizedTasks.remove(sub);
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
        return prioritizedTasks.stream().filter(t -> !task.equals(t))
                .anyMatch(t -> task.getStartTime().isEqual(t.getStartTime())
                        || task.getEndTime().isEqual(t.getEndTime())
                        || task.getStartTime().isBefore(t.getStartTime()) && task.getEndTime().isAfter(t.getStartTime())
                        || task.getStartTime().isBefore(t.getEndTime()) && task.getEndTime().isAfter(t.getEndTime())
                        || task.getStartTime().isAfter(t.getStartTime()) && task.getEndTime().isBefore(t.getEndTime())
                );
    }

    private int generateNewId() {
        return id++;
    }
}