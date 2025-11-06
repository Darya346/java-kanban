package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
        this.duration = Duration.ZERO;
    }

    public Epic(String name, String description, int id, Status status, Duration duration,
                LocalDateTime startTime, LocalDateTime endTime) {
        super(name, description, id, status, duration, startTime);
        this.subtaskIds = new ArrayList<>();
        this.endTime = endTime;
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public void setStatus(Status status) {
        throw new UnsupportedOperationException("Статус эпика рассчитывается автоматически");
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("Epic{name='%s', description='%s', id=%d, status=%s, "
                        + "duration=%s, startTime=%s, endTime=%s, subtaskIds=%s}",
                name, description, id, status, duration, startTime, endTime, subtaskIds);
    }
}