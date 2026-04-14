package com.pipeline.demo.task;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateAndFetchTask() throws Exception {
        MvcResult result = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Prepare CI",
                                  "description": "Add workflow and Docker image",
                                  "completed": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/tasks/\\d+")))
                .andExpect(jsonPath("$.title").value("Prepare CI"))
                .andExpect(jsonPath("$.description").value("Add workflow and Docker image"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();

        Number createdTaskId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/tasks/{id}", createdTaskId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTaskId.longValue()))
                .andExpect(jsonPath("$.title").value("Prepare CI"));
    }

    @Test
    void shouldReturnAllTasks() throws Exception {
        taskRepository.save(new Task("First task", "One", false));
        taskRepository.save(new Task("Second task", "Two", true));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("First task"))
                .andExpect(jsonPath("$[1].title").value("Second task"));
    }

    @Test
    void shouldDeleteTaskAndReturnNotFoundAfterward() throws Exception {
        Task task = taskRepository.save(new Task("Delete task", "Remove from db", false));

        mockMvc.perform(delete("/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", task.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id " + task.getId() + " was not found"));
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.validationErrors.title").value("title is required"));
    }
}
