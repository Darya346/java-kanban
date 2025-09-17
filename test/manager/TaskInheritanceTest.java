package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskInheritanceTest {

    @Test
    void taskAndSubtaskWithSameIdShouldNotBeEqual() {
        Task task = new Task("Task", "Description", 1, Status.NEW);
        Subtask subtask = new Subtask("Subtask", "Description", 1, Status.NEW, 2);

        assertNotEquals(task, subtask, "Task и Subtask с одинаковым ID не должны быть равны");
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", 1, Status.NEW, 10);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", 1, Status.DONE, 20);

        assertEquals(subtask1, subtask2, "Subtask с одинаковым ID должны быть равны");
    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        epic1.setId(1);
        Epic epic2 = new Epic("Epic 2", "Description 2");
        epic2.setId(1);

        assertEquals(epic1, epic2, "Epic с одинаковым ID должны быть равны");
    }
}