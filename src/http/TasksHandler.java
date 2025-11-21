package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;
import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendBadRequest(exchange, "Метод не поддерживается");
            }
        } catch (Exception exception) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            String response = JsonConverter.tasksToJson(tasks);
            sendText(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            String[] pathParts = path.split("/");
            int taskId = Integer.parseInt(pathParts[2]);

            Task task = taskManager.getTaskById(taskId);
            if (task != null) {
                String response = JsonConverter.taskToJson(task);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        Task task = JsonConverter.taskFromJson(requestBody);

        if (task == null) {
            sendBadRequest(exchange, "Неверный формат задачи");
            return;
        }

        if (task.getId() == 0) {
            // Создание новой задачи
            Task createdTask = taskManager.createTask(task);
            if (createdTask != null) {
                sendSuccess(exchange);
            } else {
                if (taskManager.isTaskOverlapping(task)) {
                    sendHasInteractions(exchange);
                } else {
                    sendInternalError(exchange);
                }
            }
        } else {
            // Обновление существующей задачи
            taskManager.updateTask(task);
            sendSuccess(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            taskManager.deleteAllTasks();
            sendSuccess(exchange);
        } else if (path.matches("/tasks/\\d+")) {
            String[] pathParts = path.split("/");
            int taskId = Integer.parseInt(pathParts[2]);

            Task task = taskManager.getTaskById(taskId);
            if (task != null) {
                taskManager.deleteTaskById(taskId);
                sendSuccess(exchange);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}