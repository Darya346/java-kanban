
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @Test
    void testIsTasksOverlapping() {
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(60), baseTime);
        Task task2 = new Task("Task 2", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusMinutes(30));

        assertTrue(taskManager.isTasksOverlapping(task1, task2));
    }

    @Test
    void testIsTasksNotOverlapping() {
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("Task 2", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusHours(1));

        assertFalse(taskManager.isTasksOverlapping(task1, task2));
    }

    @Test
    void testIsTasksTouchingNotOverlapping() {
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("Task 2", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusMinutes(30));

        assertFalse(taskManager.isTasksOverlapping(task1, task2));
    }

    @Test
    void testHasAnyTimeOverlaps() {
        LocalDateTime baseTime = LocalDateTime.now();

        taskManager.createTask(new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(60), baseTime));
        taskManager.createTask(new Task("Task 2", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusMinutes(30)));

        assertTrue(taskManager.hasAnyTimeOverlaps());
    }

    @Test
    void testHasNoTimeOverlaps() {
        LocalDateTime baseTime = LocalDateTime.now();

        taskManager.createTask(new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime));
        taskManager.createTask(new Task("Task 2", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusHours(1)));

        assertFalse(taskManager.hasAnyTimeOverlaps());
    }

    @Test
    void testGetTaskByIdOptional() {
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));

        Optional<Task> result = taskManager.getTaskByIdOptional(task.getId());
        assertTrue(result.isPresent());
        assertEquals(task.getId(), result.get().getId());
    }

    @Test
    void testGetTaskByIdOptionalNotFound() {
        Optional<Task> result = taskManager.getTaskByIdOptional(999);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetEpicByIdOptional() {
        Epic epic = taskManager.createEpic(new Epic("Test", "Description"));

        Optional<Epic> result = taskManager.getEpicByIdOptional(epic.getId());
        assertTrue(result.isPresent());
        assertEquals(epic.getId(), result.get().getId());
    }

    @Test
    void testGetSubtaskByIdOptional() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Test", "Description", Status.NEW, epic.getId()));

        Optional<Subtask> result = taskManager.getSubtaskByIdOptional(subtask.getId());
        assertTrue(result.isPresent());
        assertEquals(subtask.getId(), result.get().getId());
    }

    @Test
    void getPrioritizedTasks_shouldReturnSortedTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = taskManager.createTask(new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), now.plusHours(2)));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description", Status.NEW,
                Duration.ofMinutes(45), now.plusHours(1)));
        Task task3 = taskManager.createTask(new Task("Task 3", "Description", Status.NEW,
                Duration.ofMinutes(15), null));

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        assertEquals(2, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId());
        assertEquals(task1.getId(), prioritized.get(1).getId());
    }

    @Test
    void testTaskOverlapping() {
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = taskManager.createTask(new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(60), baseTime));

        Task overlappingTask = new Task("Overlapping", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusMinutes(30));

        assertTrue(taskManager.isTaskOverlapping(overlappingTask));

        Task nonOverlappingTask = new Task("Non-overlapping", "Description", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusHours(2));

        assertFalse(taskManager.isTaskOverlapping(nonOverlappingTask));
    }

    @Test
    void testEpicTimingCalculation() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        LocalDateTime start1 = LocalDateTime.now();
        LocalDateTime start2 = start1.plusHours(1);

        Subtask subtask1 = taskManager.createSubtask(new Subtask("Sub 1", "Description", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), start1));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Sub 2", "Description", Status.NEW, epic.getId(),
                Duration.ofMinutes(45), start2));

        Epic retrievedEpic = taskManager.getEpicById(epic.getId());

        assertEquals(Duration.ofMinutes(75), retrievedEpic.getDuration());
        assertEquals(start1, retrievedEpic.getStartTime());
        assertEquals(start2.plusMinutes(45), retrievedEpic.getEndTime());
    }

    @Test
    void testTaskWithNullTimeNotInPrioritized() {
        Task task = taskManager.createTask(new Task("Task", "Description", Status.NEW,
                Duration.ofMinutes(30), null));

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        assertTrue(prioritized.isEmpty());
    }

    @Test
    void testGetEndTime() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(90);
        Task task = new Task("Task", "Description", Status.NEW, duration, startTime);

        LocalDateTime endTime = task.getEndTime();

        assertEquals(startTime.plusMinutes(90), endTime);
    }

    @Test
    void testGetEndTimeWithNullStartTime() {
        Task task = new Task("Task", "Description", Status.NEW, Duration.ofMinutes(30), null);

        assertNull(task.getEndTime());
    }

    @Test
    void testEpicStatusAllNew() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createSubtask(new Subtask("Sub 1", "Description", Status.NEW, epic.getId()));
        taskManager.createSubtask(new Subtask("Sub 2", "Description", Status.NEW, epic.getId()));

        assertEquals(Status.NEW, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void testEpicStatusAllDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask sub1 = taskManager.createSubtask(new Subtask("Sub 1", "Description", Status.NEW, epic.getId()));
        Subtask sub2 = taskManager.createSubtask(new Subtask("Sub 2", "Description", Status.NEW, epic.getId()));

        taskManager.updateSubtask(new Subtask("Sub 1", "Description", sub1.getId(), Status.DONE, epic.getId()));
        taskManager.updateSubtask(new Subtask("Sub 2", "Description", sub2.getId(), Status.DONE, epic.getId()));

        assertEquals(Status.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void testEpicStatusMixedNewAndDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask sub1 = taskManager.createSubtask(new Subtask("Sub 1", "Description", Status.NEW, epic.getId()));
        Subtask sub2 = taskManager.createSubtask(new Subtask("Sub 2", "Description", Status.NEW, epic.getId()));

        taskManager.updateSubtask(new Subtask("Sub 1", "Description", sub1.getId(), Status.DONE, epic.getId()));

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void testEpicStatusAllInProgress() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        Subtask sub1 = taskManager.createSubtask(new Subtask("Sub 1", "Description", Status.NEW, epic.getId()));
        Subtask sub2 = taskManager.createSubtask(new Subtask("Sub 2", "Description", Status.NEW, epic.getId()));

        taskManager.updateSubtask(new Subtask("Sub 1", "Description", sub1.getId(), Status.IN_PROGRESS, epic.getId()));
        taskManager.updateSubtask(new Subtask("Sub 2", "Description", sub2.getId(), Status.IN_PROGRESS, epic.getId()));

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void testHistoryManagerEmptyHistory() {
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void testHistoryManagerDuplicates() {
        Task task = taskManager.createTask(new Task("Task", "Description", Status.NEW));

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    void testHistoryManagerRemoveFromBeginning() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Description", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description", Status.NEW));
        Task task3 = taskManager.createTask(new Task("Task 3", "Description", Status.NEW));

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());

        taskManager.deleteTaskById(task1.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }

    @Test
    void testHistoryManagerRemoveFromMiddle() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Description", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description", Status.NEW));
        Task task3 = taskManager.createTask(new Task("Task 3", "Description", Status.NEW));

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());

        taskManager.deleteTaskById(task2.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }

    @Test
    void testHistoryManagerRemoveFromEnd() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Description", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description", Status.NEW));
        Task task3 = taskManager.createTask(new Task("Task 3", "Description", Status.NEW));

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());

        taskManager.deleteTaskById(task3.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
    }
}