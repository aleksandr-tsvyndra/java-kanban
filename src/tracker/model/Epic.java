package tracker.model;

import tracker.util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Epic extends Task {
    private final Map<Integer, Subtask> epicSubtasks;
    private LocalDateTime endTime;

    public Epic(String title, String description, int id) {
        super(title, description, id, TaskStatus.NEW);
        epicSubtasks = new HashMap<>();
    }

    private void calculateEpicStatus() {
        int newSubtasks = 0;
        int doneSubtasks = 0;

        for (Subtask sub : epicSubtasks.values()) {
            if (sub.getStatus() == TaskStatus.NEW) {
                newSubtasks += 1;
            } else {
                doneSubtasks += 1;
            }
        }

        if (epicSubtasks.isEmpty() || newSubtasks > 0 && doneSubtasks == 0) {
            setStatus(TaskStatus.NEW);
        } else if (newSubtasks == 0 && doneSubtasks > 0) {
            setStatus(TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void calculateEpicTimeFields() {
        if (epicSubtasks.isEmpty()) {
            resetEpicTimeFields();
            return;
        }
        calculateEpicStartTime();
        calculateEpicDuration();
        calculateEpicEndTime();
    }

    private void resetEpicTimeFields() {
        setStartTime(null);
        setDuration(null);
        setEndTime(null);
    }

    private void calculateEpicStartTime() {
        LocalDateTime epicStartTime = LocalDateTime.MAX;
        for (Subtask sub : epicSubtasks.values()) {
            if (sub.getStartTime().isBefore(epicStartTime)) {
                epicStartTime = sub.getStartTime();
            }
        }
        setStartTime(epicStartTime);
    }

    private void calculateEpicDuration() {
        long epicDuration = 0;
        for (Subtask sub : epicSubtasks.values()) {
            epicDuration += sub.getDuration().toMinutes();
        }
        setDuration(Duration.ofMinutes(epicDuration));
    }

    private void calculateEpicEndTime() {
        LocalDateTime epicEndTime = LocalDateTime.MIN;
        for (Subtask sub : epicSubtasks.values()) {
            if (sub.getEndTime().isAfter(epicEndTime)) {
                epicEndTime = sub.getEndTime();
            }
        }
        setEndTime(epicEndTime);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void addSubtaskInEpic(Subtask subtask) {
        epicSubtasks.put(subtask.getId(), subtask);
        calculateEpicStatus();
        calculateEpicTimeFields();
    }

    public void deleteSubtaskInEpic(int subId) {
        epicSubtasks.remove(subId);
        calculateEpicStatus();
        calculateEpicTimeFields();
    }

    public void deleteAllEpicSubtasks() {
        if (!epicSubtasks.isEmpty()) {
            epicSubtasks.clear();
            calculateEpicStatus();
            calculateEpicTimeFields();
        }
    }

    public List<Subtask> getEpicSubtasks() {
        return new ArrayList<>(epicSubtasks.values());
    }

    public void setEpicSubtasks(List<Subtask> subtasks) {
        for (Subtask sub : subtasks) {
            epicSubtasks.put(sub.getId(), sub);
        }
        calculateEpicStatus();
        calculateEpicTimeFields();
    }

    public void updateSubtaskInEpic(Subtask updatedSubtask) {
        Integer subId = updatedSubtask.getId();
        epicSubtasks.put(subId, updatedSubtask);
        calculateEpicStatus();
        calculateEpicTimeFields();
    }

    @Override
    public String toString() {
        return "tracker.model.Epic{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration() +
                ", endTime=" + endTime +
                ", epicSubtasks=" + getEpicSubtasks() +
                '}';
    }
}