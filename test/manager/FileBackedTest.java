package manager;

import manager.FileBackedTaskManager;
import manager.ManagerSaveException;
import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

    // Новые тесты для спринта 8
    @Test
    void testSaveAndLoadWithTimeFields() {
        File file = tempDir.resolve("test_time.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(45);

        // Создаем задачи с временными полями
        Task task = manager.createTask(new Task("Task", "Description", Status.NEW, duration, startTime));
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", Status.NEW,
                epic.getId(), Duration.ofMinutes(30), startTime.plusHours(1)));

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertNotNull(loadedTask);
        assertEquals(duration, loadedTask.getDuration());
        assertEquals(startTime, loadedTask.getStartTime());

        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());
        assertNotNull(loadedSubtask);
        assertEquals(Duration.ofMinutes(30), loadedSubtask.getDuration());
        assertEquals(startTime.plusHours(1), loadedSubtask.getStartTime());

        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertNotNull(loadedEpic);
        assertEquals(Duration.ofMinutes(30), loadedEpic.getDuration());
        assertEquals(startTime.plusHours(1), loadedEpic.getStartTime());
    }

    @Test
    void testPrioritizedTasksAfterLoad() {
        File file = tempDir.resolve("test_prioritized.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        LocalDateTime now = LocalDateTime.now();

        manager.createTask(new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), now.plusHours(2)));
        manager.createTask(new Task("Task 2", "Description", Status.NEW,
                Duration.ofMinutes(45), now.plusHours(1)));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> prioritized = loadedManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals(now.plusHours(1), prioritized.get(0).getStartTime());
        assertEquals(now.plusHours(2), prioritized.get(1).getStartTime());
    }

    @Test
    void testSaveAndLoadEpicWithTimeCalculation() {
        File file = tempDir.resolve("test_epic_time.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Epic epic = manager.createEpic(new Epic("Epic", "Description"));

        LocalDateTime start1 = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime start2 = LocalDateTime.of(2023, 1, 1, 11, 0);

        manager.createSubtask(new Subtask("Sub 1", "Description", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), start1));
        manager.createSubtask(new Subtask("Sub 2", "Description", Status.NEW, epic.getId(),
                Duration.ofMinutes(45), start2));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertNotNull(loadedEpic);
        assertEquals(Duration.ofMinutes(75), loadedEpic.getDuration());
        assertEquals(start1, loadedEpic.getStartTime());
        assertEquals(start2.plusMinutes(45), loadedEpic.getEndTime());
    }
}