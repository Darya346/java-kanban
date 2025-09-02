package model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int id, Status status, int epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public int getEpicId() { return epicId; }
    public void setEpicId(int epicId) { this.epicId = epicId; }

    @Override
    public String toString() {
        return String.format("Subtask{name='%s', description='%s', id=%d, status=%s, epicId=%d}",
                name, description, id, status, epicId);
    }
}