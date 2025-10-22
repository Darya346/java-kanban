package manager;

import manager.FileBackedTaskManager;
import manager.ManagerSaveException;
import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoadEmptyFile() {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Сохраняем пустой менеджер
        manager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создаем задачи
        Task task1 = manager.createTask(new Task("Task 1", "Description 1", Status.NEW));
        manager.createTask(new Task("Task 2", "Description 2", Status.DONE));

        Epic epic = manager.createEpic(new Epic("Epic", "Epic description"));
        manager.createSubtask(new Subtask("Subtask", "Sub description", Status.IN_PROGRESS, epic.getId()));

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(2, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        Task loadedTask = loadedManager.getTaskById(task1.getId());
        assertNotNull(loadedTask);
        assertEquals("Task 1", loadedTask.getName());
        assertEquals(Status.NEW, loadedTask.getStatus());
    }

    @Test
    void testSaveException() {
        File file = new File("/invalid/path/tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        assertThrows(ManagerSaveException.class, () -> {
            manager.createTask(new Task("Test", "Test", Status.NEW));
        });
    }

    @Test
    void testLoadFromNonExistentFile() {
        File file = new File("nonexistent.csv");
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        assertNotNull(manager);
        assertTrue(manager.getAllTasks().isEmpty());
    }
}