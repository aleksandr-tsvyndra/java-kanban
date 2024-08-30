public class Subtask extends Task {
    private String title;
    private String description;
    private int id;
    private int epicId;
    private TaskStatus status;

    public Subtask(String title, String description, int id, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public String getTitle() { return title; }

    public void setTitle(String newTitle) { title = newTitle; }

    public String getDescription() { return description; }

    public void setDescription(String newDescription) { description = newDescription; }

    public int getId() { return id; }

    public void setId(int newId) { id = newId; }

    public TaskStatus getStatus() { return status; }

    public void setStatus(TaskStatus newStatus) { status = newStatus; }

    public int getEpicId() { return epicId; }

    public void setEpicId(int epicId) { this.epicId = epicId; }

    @Override
    public String toString() {
        return "Subtask{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", epicId=" + epicId +
                ", status=" + status +
                '}';
    }
}
