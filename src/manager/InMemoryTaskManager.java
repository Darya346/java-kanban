package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
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

    // Строка 20 - исправленный метод
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    // Остальные методы get...ById тоже нужно проверить на аналогичные ошибки

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Строка 131 - исправленный метод createTask
    @Override
    public Task createTask(Task task) {
        if (task == null) {
            return null;
        }
        Task newTask = new Task(task.getName(), task.getDescription(), generateId(), task.getStatus());
        tasks.put(newTask.getId(), newTask);
        return newTask;
    }

    // Строка 148 - исправленный метод createEpic
    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        Epic newEpic = new Epic(epic.getName(), epic.getDescription());
        newEpic.setId(generateId());
        epics.put(newEpic.getId(), newEpic);
        return newEpic;
    }

    // Строка 168 - исправленный метод createSubtask
    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null || !epics.containsKey(subtask.getEpicId())) {
            return null;
        }
        Subtask newSubtask = new Subtask(subtask.getName(), subtask.getDescription(),
                generateId(), subtask.getStatus(), subtask.getEpicId());
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epics.get(newSubtask.getEpicId());
        epic.addSubtaskId(newSubtask.getId());
        updateEpicStatus(epic.getId());
        return newSubtask;
    }

    // Также проверь другие методы с условиями if - они тоже могут требовать переноса

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }
        tasks.put(task.getId(), task);
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
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return;
        }
        // ... остальная логика метода
    }

    // Остальные методы остаются без изменений...
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    // ... и т.д.
}