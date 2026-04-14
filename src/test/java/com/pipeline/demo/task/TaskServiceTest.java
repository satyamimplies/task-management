package com.pipeline.demo.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pipeline.demo.task.dto.CreateTaskRequest;
import com.pipeline.demo.task.dto.TaskResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskWithTrimmedValues() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            setPrivateField(task, "id", 1L);
            setPrivateField(task, "createdAt", Instant.parse("2026-04-14T10:15:30Z"));
            return task;
        });

        TaskResponse response = taskService.createTask(new CreateTaskRequest("  Buy milk  ", "  From store  ", false));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task savedTask = captor.getValue();

        assertThat(savedTask.getTitle()).isEqualTo("Buy milk");
        assertThat(savedTask.getDescription()).isEqualTo("From store");
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Buy milk");
        assertThat(response.description()).isEqualTo("From store");
    }

    @Test
    void shouldReturnAllTasks() {
        Task task = new Task("Write tests", "Cover task API", true);
        setPrivateField(task, "id", 9L);
        setPrivateField(task, "createdAt", Instant.parse("2026-04-14T10:15:30Z"));
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<TaskResponse> tasks = taskService.getAllTasks();

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).id()).isEqualTo(9L);
        assertThat(tasks.get(0).completed()).isTrue();
    }

    @Test
    void shouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id 99 was not found");
    }

    @Test
    void shouldDeleteExistingTask() {
        Task task = new Task("Remove me", null, false);
        when(taskRepository.findById(4L)).thenReturn(Optional.of(task));

        taskService.deleteTask(4L);

        verify(taskRepository, times(1)).delete(task);
    }

    private static void setPrivateField(Task task, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = Task.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(task, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
