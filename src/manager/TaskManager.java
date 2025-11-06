package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import java.util.List;
import java.util.Optional;

public interface TaskManager {
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    Optional<Task> getTaskByIdOptional(int id);

    Task createTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(int id);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    Optional<Epic> getEpicByIdOptional(int id);

    Epic createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpicById(int id);

    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(int id);

    Optional<Subtask> getSubtaskByIdOptional(int id);

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    List<Subtask> getSubtasksByEpicId(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    boolean isTaskOverlapping(Task task);

    boolean isTasksOverlapping(Task task1, Task task2);

    boolean hasAnyTimeOverlaps();
}