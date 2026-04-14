package com.pipeline.demo.task;

import com.pipeline.demo.task.dto.CreateTaskRequest;
import com.pipeline.demo.task.dto.TaskResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse createdTask = taskService.createTask(request);
        return ResponseEntity.created(URI.create("/tasks/" + createdTask.id())).body(createdTask);
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
