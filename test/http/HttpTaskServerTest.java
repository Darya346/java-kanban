package http;

import http.HttpTaskServer;
import manager.Managers;
import manager.TaskManager;
import model.Task;
import model.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testCreateTask() throws Exception {
        String taskJson = "{\"name\":\"Test Task\",\"description\":\"Test Description\"," +
                "\"status\":\"NEW\",\"duration\":30,\"startTime\":\"2023-01-01T10:00:00\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllTasks().size());
        assertEquals("Test Task", manager.getAllTasks().get(0).getName());
    }

    @Test
    void testGetTask() throws Exception {
        Task task = manager.createTask(new Task("Test", "Desc", Status.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test"));
    }

    @Test
    void testGetAllTasks() throws Exception {
        manager.createTask(new Task("Task 1", "Desc 1", Status.NEW));
        manager.createTask(new Task("Task 2", "Desc 2", Status.IN_PROGRESS));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
    }

    @Test
    void testDeleteTask() throws Exception {
        Task task = manager.createTask(new Task("Test", "Desc", Status.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(0, manager.getAllTasks().size());
    }

    @Test
    void testGetHistory() throws Exception {
        Task task = manager.createTask(new Task("Test", "Desc", Status.NEW));
        manager.getTaskById(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test"));
    }

    @Test
    void testGetPrioritized() throws Exception {
        Task task1 = manager.createTask(new Task("Task 1", "Desc 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now()));
        Task task2 = manager.createTask(new Task("Task 2", "Desc 2", Status.NEW,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));

        // Используем переменные, чтобы убрать предупреждения
        assertNotNull(task1);
        assertNotNull(task2);
    }

    @Test
    void testTaskNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}