package model;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskIdConflictTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testNoIdConflict() {
        Task autoTask = taskManager.createTask(new Task("Auto", "Description", Status.NEW));

        Task manualTask = new Task("Manual", "Description", autoTask.getId(), Status.NEW);

        taskManager.updateTask(manualTask);

        assertEquals(1, taskManager.getAllTasks().size());
        assertEquals("Manual", taskManager.getTaskById(autoTask.getId()).getName());
    }

    @Test
    void testDifferentIds() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Description", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description", Status.NEW));

        assertNotEquals(task1.getId(), task2.getId());
        assertEquals(2, taskManager.getAllTasks().size());
    }
}