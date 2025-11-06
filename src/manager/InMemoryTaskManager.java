package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager;
    protected int nextId = 1;
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    protected int generateId() {
        return nextId++;
    }

    @Override
    public Optional<Task> getTaskByIdOptional(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return Optional.of(createTaskCopy(task));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Epic> getEpicByIdOptional(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return Optional.of(createEpicCopy(epic));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subtask> getSubtaskByIdOptional(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return Optional.of(createSubtaskCopy(subtask));
        }
        return Optional.empty();
    }

    @Override
    public boolean isTasksOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null ||
                task1.getEndTime() == null || task2.getEndTime() == null) {
            return false;
        }

        return !task1.getEndTime().isBefore(task2.getStartTime()) &&
                !task1.getStartTime().isAfter(task2.getEndTime());
    }

    @Override
    public boolean hasAnyTimeOverlaps() {
        List<Task> prioritized = getPrioritizedTasks();

        for (int i = 1; i < prioritized.size(); i++) {
            Task current = prioritized.get(i);
            Task previous = prioritized.get(i - 1);

            if (isTasksOverlapping(previous, current)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isTaskOverlapping(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }

        LocalDateTime taskStart = task.getStartTime();
        LocalDateTime taskEnd = task.getEndTime();

        if (taskEnd == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .filter(t -> t.getId() != task.getId())
                .anyMatch(existingTask -> isTasksOverlapping(task, existingTask));
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(this::createSubtaskCopy)
                .collect(Collectors.toList());
    }

    private Task createTaskCopy(Task original) {
        return new Task(original.getName(), original.getDescription(),
                original.getId(), original.getStatus(),
                original.getDuration(), original.getStartTime());
    }

    private Epic createEpicCopy(Epic original) {
        Epic copy = new Epic(original.getName(), original.getDescription(),
                original.getId(), original.getStatus(),
                original.getDuration(), original.getStartTime(), original.getEndTime());
        for (Integer subtaskId : original.getSubtaskIds()) {
            copy.addSubtaskId(subtaskId);
        }
        return copy;
    }

    private Subtask createSubtaskCopy(Subtask original) {
        return new Subtask(original.getName(), original.getDescription(),
                original.getId(), original.getStatus(), original.getEpicId(),
                original.getDuration(), original.getStartTime());
    }

    @Override
    public List<Task> getAllTasks() {
        return tasks.values().stream()
                .map(this::createTaskCopy)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
            prioritizedTasks.removeIf(task -> task.getId() == taskId);
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        return getTaskByIdOptional(id).orElse(null);
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            return null;
        }

        if (isTaskOverlapping(task)) {
            return null;
        }

        Task newTask = new Task(task.getName(), task.getDescription(),
                generateId(), task.getStatus(),
                task.getDuration(), task.getStartTime());
        tasks.put(newTask.getId(), newTask);

        if (newTask.getStartTime() != null) {
            prioritizedTasks.add(newTask);
        }

        return createTaskCopy(newTask);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }

        Task existingTask = tasks.get(task.getId());
        prioritizedTasks.remove(existingTask);

        if (isTaskOverlapping(task)) {
            prioritizedTasks.add(existingTask);
            return;
        }

        tasks.put(task.getId(), task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return epics.values().stream()
                .map(this::createEpicCopy)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllEpics() {
        for (Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
            prioritizedTasks.removeIf(task -> task.getId() == subtaskId);
        }

        for (Epic epic : epics.values()) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        return getEpicByIdOptional(id).orElse(null);
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        Epic newEpic = new Epic(epic.getName(), epic.getDescription());
        newEpic.setId(generateId());
        epics.put(newEpic.getId(), newEpic);
        return createEpicCopy(newEpic);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return;
        }
        Epic existingEpic = epics.get(epic.getId());
        existingEpic.setName(epic.getName());
        existingEpic.setDescription(epic.getDescription());
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.remove(id);
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
                prioritizedTasks.removeIf(task -> task.getId() == subtaskId);
            }
            epics.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return subtasks.values().stream()
                .map(this::createSubtaskCopy)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
            prioritizedTasks.removeIf(task -> task.getId() == subtaskId);
        }

        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic.getId());
            updateEpicTiming(epic.getId());
        }
        subtasks.clear();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        return getSubtaskByIdOptional(id).orElse(null);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null || !epics.containsKey(subtask.getEpicId())) {
            return null;
        }

        Epic epic = epics.get(subtask.getEpicId());

        if (subtask.getId() != 0) {
            if (subtask.getEpicId() == subtask.getId()) {
                return null;
            }
            if (epic.getId() == subtask.getId()) {
                return null;
            }
        }

        if (isTaskOverlapping(subtask)) {
            return null;
        }

        Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDescription(),
                generateId(), subtask.getStatus(), subtask.getEpicId(),
                subtask.getDuration(), subtask.getStartTime());
        subtasks.put(newSubtask.getId(), newSubtask);
        epic.addSubtaskId(newSubtask.getId());

        if (newSubtask.getStartTime() != null) {
            prioritizedTasks.add(newSubtask);
        }

        updateEpicStatus(epic.getId());
        updateEpicTiming(epic.getId());
        return createSubtaskCopy(newSubtask);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return;
        }

        if (subtask.getEpicId() == subtask.getId()) {
            return;
        }

        if (!epics.containsKey(subtask.getEpicId())) {
            return;
        }

        Subtask existingSubtask = subtasks.get(subtask.getId());
        prioritizedTasks.remove(existingSubtask);

        if (isTaskOverlapping(subtask)) {
            prioritizedTasks.add(existingSubtask);
            return;
        }

        int oldEpicId = existingSubtask.getEpicId();
        int newEpicId = subtask.getEpicId();

        if (oldEpicId != newEpicId) {
            Epic oldEpic = epics.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.removeSubtaskId(subtask.getId());
                updateEpicStatus(oldEpicId);
                updateEpicTiming(oldEpicId);
            }
            Epic newEpic = epics.get(newEpicId);
            if (newEpic != null) {
                newEpic.addSubtaskId(subtask.getId());
            }
        }

        existingSubtask.setName(subtask.getName());
        existingSubtask.setDescription(subtask.getDescription());
        existingSubtask.setStatus(subtask.getStatus());
        existingSubtask.setEpicId(subtask.getEpicId());
        existingSubtask.setDuration(subtask.getDuration());
        existingSubtask.setStartTime(subtask.getStartTime());

        if (existingSubtask.getStartTime() != null) {
            prioritizedTasks.add(existingSubtask);
        }

        updateEpicStatus(subtask.getEpicId());
        updateEpicTiming(subtask.getEpicId());
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
                updateEpicTiming(epic.getId());
            }
            subtasks.remove(id);
            historyManager.remove(id);
            prioritizedTasks.remove(subtask);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.updateStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            epic.updateStatus(Status.DONE);
        } else if (allNew) {
            epic.updateStatus(Status.NEW);
        } else {
            epic.updateStatus(Status.IN_PROGRESS);
        }
    }

    private void updateEpicTiming(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> epicSubtasks = getSubtasksByEpicId(epicId).stream()
                .map(subtask -> subtasks.get(subtask.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (epicSubtasks.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        Duration totalDuration = epicSubtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
        epic.setDuration(totalDuration);

        LocalDateTime earliestStart = epicSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        epic.setStartTime(earliestStart);

        LocalDateTime latestEnd = epicSubtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        epic.setEndTime(latestEnd);
    }
}