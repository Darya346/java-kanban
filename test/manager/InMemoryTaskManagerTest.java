package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testCreateTask() {
        Task task = new Task("Test Task", "Test Description", Status.NEW);
        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask);
        assertNotNull(createdTask.getId());
        assertEquals("Test Task", createdTask.getName());
        assertEquals("Test Description", createdTask.getDescription());
        assertEquals(Status.NEW, createdTask.getStatus());

        Task retrievedTask = taskManager.getTaskById(createdTask.getId());
        assertEquals(createdTask, retrievedTask);
    }

    @Test
    void testCreateEpic() {
        Epic epic = new Epic("Test Epic", "Test Epic Description");
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic);
        assertNotNull(createdEpic.getId());
        assertEquals("Test Epic", createdEpic.getName());
        assertEquals("Test Epic Description", createdEpic.getDescription());
        assertEquals(Status.NEW, createdEpic.getStatus());
        assertTrue(createdEpic.getSubtaskIds().isEmpty());

        Epic retrievedEpic = taskManager.getEpicById(createdEpic.getId());
        assertEquals(createdEpic, retrievedEpic);
    }

    @Test
    void testCreateSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
        Subtask subtask = new Subtask("Test Subtask", "Test Description", Status.NEW, epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask);
        assertNotNull(createdSubtask.getId());
        assertEquals("Test Subtask", createdSubtask.getName());
        assertEquals("Test Description", createdSubtask.getDescription());
        assertEquals(Status.NEW, createdSubtask.getStatus());
        assertEquals(epic.getId(), createdSubtask.getEpicId());

        Subtask retrievedSubtask = taskManager.getSubtaskById(createdSubtask.getId());
        assertEquals(createdSubtask, retrievedSubtask);

        List<Subtask> epicSubtasks = taskManager.getSubtasksByEpicId(epic.getId());
        assertEquals(1, epicSubtasks.size());
        assertEquals(createdSubtask.getId(), epicSubtasks.get(0).getId());
    }

    @Test
    void testUpdateTask() {
        Task task = taskManager.createTask(new Task("Original", "Desc", Status.NEW));
        Task updatedTask = new Task("Updated", "Updated Desc", task.getId(), Status.IN_PROGRESS);

        taskManager.updateTask(updatedTask);

        Task retrievedTask = taskManager.getTaskById(task.getId());
        assertEquals("Updated", retrievedTask.getName());
        assertEquals("Updated Desc", retrievedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, retrievedTask.getStatus());
    }

    @Test
    void testUpdateEpic() {
        Epic epic = taskManager.createEpic(new Epic("Original", "Desc"));
        Epic updatedEpic = new Epic("Updated", "Updated Desc");
        updatedEpic.setId(epic.getId());

        taskManager.updateEpic(updatedEpic);

        Epic retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals("Updated", retrievedEpic.getName());
        assertEquals("Updated Desc", retrievedEpic.getDescription());
    }

    @Test
    void testUpdateSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Original", "Desc", Status.NEW, epic.getId()));
        Subtask updatedSubtask = new Subtask("Updated", "Updated Desc", subtask.getId(), Status.DONE, epic.getId());

        taskManager.updateSubtask(updatedSubtask);

        Subtask retrievedSubtask = taskManager.getSubtaskById(subtask.getId());
        assertEquals("Updated", retrievedSubtask.getName());
        assertEquals("Updated Desc", retrievedSubtask.getDescription());
        assertEquals(Status.DONE, retrievedSubtask.getStatus());
    }

    @Test
    void testDeleteTask() {
        Task task = taskManager.createTask(new Task("Test", "Desc", Status.NEW));
        int taskId = task.getId();

        taskManager.deleteTaskById(taskId);

        assertNull(taskManager.getTaskById(taskId));
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testDeleteEpic() {
        Epic epic = taskManager.createEpic(new Epic("Test", "Desc"));
        int epicId = epic.getId();

        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getEpicById(epicId));
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    void testDeleteSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Test", "Desc", Status.NEW, epic.getId()));
        int subtaskId = subtask.getId();

        taskManager.deleteSubtaskById(subtaskId);

        assertNull(taskManager.getSubtaskById(subtaskId));
        assertTrue(taskManager.getAllSubtasks().isEmpty());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void testDeleteAllTasks() {
        taskManager.createTask(new Task("Task 1", "Desc", Status.NEW));
        taskManager.createTask(new Task("Task 2", "Desc", Status.IN_PROGRESS));

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testDeleteAllEpics() {
        taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createEpic(new Epic("Epic 2", "Desc"));

        taskManager.deleteAllEpics();

        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testDeleteAllSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        taskManager.createSubtask(new Subtask("Sub 1", "Desc", Status.NEW, epic.getId()));
        taskManager.createSubtask(new Subtask("Sub 2", "Desc", Status.DONE, epic.getId()));

        taskManager.deleteAllSubtasks();

        assertTrue(taskManager.getAllSubtasks().isEmpty());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void testGetAllTasks() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Desc", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Desc", Status.IN_PROGRESS));

        List<Task> allTasks = taskManager.getAllTasks();

        assertEquals(2, allTasks.size());
        assertTrue(allTasks.contains(task1));
        assertTrue(allTasks.contains(task2));
    }

    @Test
    void testGetAllEpics() {
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Desc"));

        List<Epic> allEpics = taskManager.getAllEpics();

        assertEquals(2, allEpics.size());
        assertTrue(allEpics.contains(epic1));
        assertTrue(allEpics.contains(epic2));
    }

    @Test
    void testGetAllSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Sub 1", "Desc", Status.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Sub 2", "Desc", Status.DONE, epic.getId()));

        List<Subtask> allSubtasks = taskManager.getAllSubtasks();

        assertEquals(2, allSubtasks.size());
        assertTrue(allSubtasks.contains(subtask1));
        assertTrue(allSubtasks.contains(subtask2));
    }

    @Test
    void testEpicStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));

        assertEquals(Status.NEW, epic.getStatus());

        Subtask sub1 = taskManager.createSubtask(new Subtask("Sub 1", "Desc", Status.NEW, epic.getId()));
        Epic updatedEpic1 = taskManager.getEpicById(epic.getId());
        assertEquals(Status.NEW, updatedEpic1.getStatus());

        taskManager.updateSubtask(new Subtask("Sub 1", "Desc", sub1.getId(), Status.DONE, epic.getId()));
        Epic updatedEpic2 = taskManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, updatedEpic2.getStatus());

        Subtask sub2 = taskManager.createSubtask(new Subtask("Sub 2", "Desc", Status.NEW, epic.getId()));
        Epic updatedEpic3 = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic3.getStatus());

        taskManager.updateSubtask(new Subtask("Sub 1", "Desc", sub1.getId(), Status.IN_PROGRESS, epic.getId()));
        taskManager.updateSubtask(new Subtask("Sub 2", "Desc", sub2.getId(), Status.IN_PROGRESS, epic.getId()));
        Epic updatedEpic4 = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic4.getStatus());
    }

    @Test
    void testHistoryNoDuplicates() {
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    void testTaskImmutability() {
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));
        int originalId = task.getId();

        task.setId(999);
        task.setName("Modified");

        Task managedTask = taskManager.getTaskById(originalId);

        assertEquals(originalId, managedTask.getId());
        assertEquals("Test", managedTask.getName());
    }

    @Test
    void testEpicImmutability() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
        int originalId = epic.getId();

        epic.setId(999);
        epic.setName("Modified");

        Epic managedEpic = taskManager.getEpicById(originalId);

        assertEquals(originalId, managedEpic.getId());
        assertEquals("Test Epic", managedEpic.getName());
    }

    @Test
    void testHistoryAfterDelete() {
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));

        taskManager.getTaskById(task.getId());
        assertEquals(1, taskManager.getHistory().size());

        taskManager.deleteTaskById(task.getId());

        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void testDataIntegrity() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Desc", Status.NEW, epic.getId()));

        int subtaskId = subtask.getId();
        int epicId = epic.getId();

        taskManager.deleteSubtaskById(subtaskId);

        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertFalse(updatedEpic.getSubtaskIds().contains(subtaskId));

        assertNull(taskManager.getSubtaskById(subtaskId));
    }

    @Test
    void testCreateNullTask() {
        Task result = taskManager.createTask(null);
        assertNull(result);
    }

    @Test
    void testCreateNullEpic() {
        Epic result = taskManager.createEpic(null);
        assertNull(result);
    }

    @Test
    void testCreateNullSubtask() {
        Subtask result = taskManager.createSubtask(null);
        assertNull(result);
    }

    @Test
    void testCreateSubtaskBadEpic() {
        Subtask subtask = new Subtask("Test", "Desc", Status.NEW, 999);
        Subtask result = taskManager.createSubtask(subtask);
        assertNull(result);
    }

    @Test
    void testUpdateBadTask() {
        Task task = new Task("Test", "Desc", 999, Status.NEW);
        taskManager.updateTask(task);

        assertNull(taskManager.getTaskById(999));
    }

    @Test
    void testUpdateBadEpic() {
        Epic epic = new Epic("Test", "Desc");
        epic.setId(999);
        taskManager.updateEpic(epic);

        assertNull(taskManager.getEpicById(999));
    }

    @Test
    void testUpdateBadSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = new Subtask("Test", "Desc", 999, Status.NEW, epic.getId());
        taskManager.updateSubtask(subtask);

        assertNull(taskManager.getSubtaskById(999));
    }

    @Test
    void testDeleteBadTask() {
        taskManager.deleteTaskById(999);
    }

    @Test
    void testDeleteBadEpic() {
        taskManager.deleteEpicById(999);
    }

    @Test
    void testDeleteBadSubtask() {
        taskManager.deleteSubtaskById(999);
    }

    @Test
    void testGetBadTask() {
        Task task = taskManager.getTaskById(999);
        assertNull(task);
    }

    @Test
    void testGetBadEpic() {
        Epic epic = taskManager.getEpicById(999);
        assertNull(epic);
    }

    @Test
    void testGetBadSubtask() {
        Subtask subtask = taskManager.getSubtaskById(999);
        assertNull(subtask);
    }

    @Test
    void testGetSubtasksBadEpic() {
        List<Subtask> subtasks = taskManager.getSubtasksByEpicId(999);
        assertTrue(subtasks.isEmpty());
    }
}