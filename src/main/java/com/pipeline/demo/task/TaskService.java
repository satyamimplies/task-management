package com.pipeline.demo.task;

import com.pipeline.demo.task.dto.CreateTaskRequest;
import com.pipeline.demo.task.dto.TaskResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        return TaskResponse.from(findTask(id));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = new Task(request.title().trim(), normalizeDescription(request.description()), request.completed());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
