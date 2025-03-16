package tracker.model;

import tracker.util.TaskStatus;

import java.util.Objects;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task {
    private String title;
    private String description;
    private int id;
    private TaskStatus status;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(String title, String description, int id, TaskStatus status,
                LocalDateTime startTime, Duration duration) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String title, String description, int id, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration.toMinutes());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        description = newDescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int newId) {
        id = newId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus newStatus) {
        status = newStatus;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "tracker.model.Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task that = (Task) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
