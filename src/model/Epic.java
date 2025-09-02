package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() { return new ArrayList<>(subtaskIds); }
    public void addSubtaskId(int subtaskId) { if (!subtaskIds.contains(subtaskId)) subtaskIds.add(subtaskId); }
    public void removeSubtaskId(int subtaskId) { subtaskIds.remove(Integer.valueOf(subtaskId)); }
    public void clearSubtasks() { subtaskIds.clear(); }

    @Override
    public void setStatus(Status status) {
        throw new UnsupportedOperationException("Статус эпика рассчитывается автоматически");
    }

    public void updateStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Epic{name='%s', description='%s', id=%d, status=%s, subtaskIds=%s}",
                name, description, id, status, subtaskIds);
    }
}