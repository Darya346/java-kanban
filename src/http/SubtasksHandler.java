package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Subtask;
import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
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
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            String response = JsonConverter.subtasksToJson(subtasks);
            sendText(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Subtask subtask = taskManager.getSubtaskById(id);
            if (subtask != null) {
                String response = JsonConverter.subtaskToJson(subtask);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Subtask subtask = JsonConverter.subtaskFromJson(body);

        if (subtask == null) {
            sendBadRequest(exchange, "Неверный формат подзадачи");
            return;
        }

        if (subtask.getId() == 0) {
            Subtask created = taskManager.createSubtask(subtask);
            if (created != null) {
                sendSuccess(exchange);
            } else {
                if (taskManager.isTaskOverlapping(subtask)) {
                    sendHasInteractions(exchange);
                } else {
                    sendInternalError(exchange);
                }
            }
        } else {
            taskManager.updateSubtask(subtask);
            sendSuccess(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            taskManager.deleteAllSubtasks();
            sendSuccess(exchange);
        } else if (path.matches("/subtasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Subtask subtask = taskManager.getSubtaskById(id);
            if (subtask != null) {
                taskManager.deleteSubtaskById(id);
                sendSuccess(exchange);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}