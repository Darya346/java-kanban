package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            String response = JsonConverter.epicsToJson(epics);
            sendText(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                String response = JsonConverter.epicToJson(epic);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            String[] pathParts = path.split("/");
            int epicId = Integer.parseInt(pathParts[2]);

            Epic epic = taskManager.getEpicById(epicId);
            if (epic != null) {
                List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epicId);
                String response = JsonConverter.subtasksToJson(subtasks);
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
        Epic epic = JsonConverter.epicFromJson(body);

        if (epic == null) {
            sendBadRequest(exchange, "Неверный формат эпика");
            return;
        }

        if (epic.getId() == 0) {
            Epic created = taskManager.createEpic(epic);
            if (created != null) {
                sendSuccess(exchange);
            } else {
                sendInternalError(exchange);
            }
        } else {
            taskManager.updateEpic(epic);
            sendSuccess(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            taskManager.deleteAllEpics();
            sendSuccess(exchange);
        } else if (path.matches("/epics/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                taskManager.deleteEpicById(id);
                sendSuccess(exchange);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}