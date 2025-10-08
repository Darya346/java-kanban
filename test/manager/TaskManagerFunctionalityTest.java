package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskManagerFunctionalityTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void canAddAndFindAllTaskTypes() {
        Task task = taskManager.createTask(new Task("Task", "Description", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Description", Status.NEW, epic.getId()));

        assertNotNull(task);
        assertNotNull(epic);
        assertNotNull(subtask);

        assertNotNull(taskManager.getTaskById(task.getId()));
        assertNotNull(taskManager.getEpicById(epic.getId()));
        assertNotNull(taskManager.getSubtaskById(subtask.getId()));

        assertEquals(1, taskManager.getAllTasks().size());
        assertEquals(1, taskManager.getAllEpics().size());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }
}