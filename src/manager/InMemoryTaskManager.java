package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;
    private int nextId = 1;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private int generateId() {
        return nextId++;
    }

    private Task createTaskCopy(Task original) {
        return new Task(original.getName(), original.getDescription(),
                original.getId(), original.getStatus());
    }

    private Epic createEpicCopy(Epic original) {
        Epic copy = new Epic(original.getName(), original.getDescription());
        copy.setId(original.getId());
        copy.updateStatus(original.getStatus());
        for (Integer subtaskId : original.getSubtaskIds()) {
            copy.addSubtaskId(subtaskId);
        }
        return copy;
    }

    private Subtask createSubtaskCopy(Subtask original) {
        return new Subtask(original.getName(), original.getDescription(),
                original.getId(), original.getStatus(), original.getEpicId());
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> taskCopies = new ArrayList<>();
        for (Task task : tasks.values()) {
            taskCopies.add(createTaskCopy(task));
        }
        return taskCopies;
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return createTaskCopy(task);
        }
        return null;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            return null;
        }
        Task newTask = new Task(task.getName(), task.getDescription(), generateId(), task.getStatus());
        tasks.put(newTask.getId(), newTask);
        return createTaskCopy(newTask);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> epicCopies = new ArrayList<>();
        for (Epic epic : epics.values()) {
            epicCopies.add(createEpicCopy(epic));
        }
        return epicCopies;
    }

    @Override
    public void deleteAllEpics() {
        for (Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }

        for (Epic epic : epics.values()) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
        epics.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return createEpicCopy(epic);
        }
        return null;
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
            }
            epics.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> subtaskCopies = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            subtaskCopies.add(createSubtaskCopy(subtask));
        }
        return subtaskCopies;
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }

        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return createSubtaskCopy(subtask);
        }
        return null;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null || !epics.containsKey(subtask.getEpicId())) {
            return null;
        }

        // Проверка: подзадача не может быть своим же эпиком
        if (subtask.getEpicId() == subtask.getId()) {
            return null;
        }

        Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDescription(),
                generateId(), subtask.getStatus(), subtask.getEpicId());
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epics.get(newSubtask.getEpicId());
        epic.addSubtaskId(newSubtask.getId());
        updateEpicStatus(epic.getId());
        return createSubtaskCopy(newSubtask);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return;
        }

        // Проверка: подзадача не может быть своим же эпиком
        if (subtask.getEpicId() == subtask.getId()) {
            return;
        }

        // Проверка: эпик должен существовать
        if (!epics.containsKey(subtask.getEpicId())) {
            return;
        }

        Subtask existingSubtask = subtasks.get(subtask.getId());
        int oldEpicId = existingSubtask.getEpicId();
        int newEpicId = subtask.getEpicId();

        if (oldEpicId != newEpicId) {
            Epic oldEpic = epics.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.removeSubtaskId(subtask.getId());
                updateEpicStatus(oldEpicId);
            }
            Epic newEpic = epics.get(newEpicId);
            if (newEpic != null) {
                newEpic.addSubtaskId(subtask.getId());
            }
        }

        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
            subtasks.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    result.add(createSubtaskCopy(subtask));
                }
            }
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.updateStatus(model.Status.NEW);
            return;
        }
        boolean allDone = true;
        boolean allNew = true;
        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }
            if (subtask.getStatus() != model.Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != model.Status.NEW) {
                allNew = false;
            }
            if (!allDone && !allNew) {
                break;
            }
        }
        if (allDone) {
            epic.updateStatus(model.Status.DONE);
        } else if (allNew) {
            epic.updateStatus(model.Status.NEW);
        } else {
            epic.updateStatus(model.Status.IN_PROGRESS);
        }
    }
}