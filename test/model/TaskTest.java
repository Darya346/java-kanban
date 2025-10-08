package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testSameIdEqual() {
        Task task1 = new Task("Task 1", "Description 1", 1, Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", 1, Status.DONE);

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void testDifferentIdNotEqual() {
        Task task1 = new Task("Task", "Description", 1, Status.NEW);
        Task task2 = new Task("Task", "Description", 2, Status.NEW);

        assertNotEquals(task1, task2);
    }
}