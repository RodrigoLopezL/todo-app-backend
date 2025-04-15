package com.encora.todolist_app.controller;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.StateTaskDTO;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private final LocalDateTime now = LocalDateTime.now();

    private Task createTask(Integer id, String text, Priority priority, boolean state) {
        return new Task(id, text, now, null, null, null, priority, state);
    }

    @Test
    void allTasks_shouldReturnOkAndPageOfTasks() throws Exception {
        Task task1 = createTask(1, "Task 1", Priority.MEDIUM, false);
        Task task2 = createTask(2, "Task 2", Priority.HIGH, true);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(task1, task2), PageRequest.of(0, 10), 2);

        when(taskService.getAllTasks(eq(false), eq("MEDIUM"), eq("Task"), any(Pageable.class))).thenReturn(taskPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/todos")
                        .param("state", "false")
                        .param("priority", "MEDIUM")
                        .param("text", "Task")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].text", is("Task 1")));
        verify(taskService, times(1)).getAllTasks(eq(false), eq("MEDIUM"), eq("Task"), any(Pageable.class));
    }

    @Test
    void timeTask_shouldReturnOkAndMapOfDurations() throws Exception {
        Map<String, Duration> avgTimes = Map.of("AvgTotalTime", Duration.ofMinutes(30));
        when(taskService.avgTimesAllTask()).thenReturn(avgTimes);

        mockMvc.perform(MockMvcRequestBuilders.get("/todos/time"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.AvgTotalTime", is("PT30M")));
        verify(taskService, times(1)).avgTimesAllTask();
    }

    @Test
    void timeTask_shouldReturnNoContentIfNoTimesAvailable() throws Exception {
        when(taskService.avgTimesAllTask()).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/todos/time"))
                .andExpect(status().isNoContent());
        verify(taskService, times(1)).avgTimesAllTask();

        when(taskService.avgTimesAllTask()).thenReturn(Collections.emptyMap());

        mockMvc.perform(MockMvcRequestBuilders.get("/todos/time"))
                .andExpect(status().isNoContent());
        verify(taskService, times(2)).avgTimesAllTask();
    }

    @Test
    void insertTask_shouldReturnCreatedAndInsertedTask() throws Exception {
        Task newTask = createTask(null, "New Task", Priority.LOW, false);
        Task savedTask = createTask(3, "New Task", Priority.LOW, false);
        when(taskService.addTask(any(Task.class))).thenReturn(savedTask);

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.text", is("New Task")));
        verify(taskService, times(1)).addTask(any(Task.class));
    }

    @Test
    void insertTask_shouldReturnNotAcceptableIfServiceReturnsNull() throws Exception {
        Task newTask = createTask(null, "New Task", Priority.LOW, false);
        when(taskService.addTask(any(Task.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isNotAcceptable());
        verify(taskService, times(1)).addTask(any(Task.class));
    }

    @Test
    void updateStatusDoneTask_shouldReturnOkAndUpdatedState() throws Exception {
        StateTaskDTO updatedState = new StateTaskDTO(1, true);
        when(taskService.updateStatusDoneTask(1)).thenReturn(updatedState);

        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/1/done"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(true)));
        verify(taskService, times(1)).updateStatusDoneTask(1);
    }

    @Test
    void updateStatusDoneTask_shouldReturnNotFoundIfTaskNotFound() throws Exception {
        when(taskService.updateStatusDoneTask(1)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/1/done"))
                .andExpect(status().isNotFound());
        verify(taskService, times(1)).updateStatusDoneTask(1);
    }

    @Test
    void updateStatusTask_shouldReturnOkAndUpdatedState() throws Exception {
        StateTaskDTO updatedState = new StateTaskDTO(2, false);
        when(taskService.updateStatusUndoneTask(2)).thenReturn(updatedState);

        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/2/undone"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.status", is(false)));
        verify(taskService, times(1)).updateStatusUndoneTask(2);
    }

    @Test
    void updateStatusTask_shouldReturnNotFoundIfTaskNotFound() throws Exception {
        when(taskService.updateStatusUndoneTask(2)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/2/undone"))
                .andExpect(status().isNotFound());
        verify(taskService, times(1)).updateStatusUndoneTask(2);
    }

    @Test
    void updateTask_shouldReturnOkAndUpdatedTask() throws Exception {
        Task updatedTask = createTask(1, "Updated Task", Priority.HIGH, true);
        when(taskService.updateTask(eq(1), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(MockMvcRequestBuilders.put("/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Updated Task")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.state", is(true)));
        verify(taskService, times(1)).updateTask(eq(1), any(Task.class));
    }

    @Test
    void updateTask_shouldReturnNotFoundIfTaskNotFound() throws Exception {
        Task updatedTask = createTask(1, "Updated Task", Priority.HIGH, true);
        when(taskService.updateTask(eq(1), any(Task.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.put("/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isNotFound());
        verify(taskService, times(1)).updateTask(eq(1), any(Task.class));
    }

    @Test
    void deleteTask_shouldReturnNoContent() throws Exception {
        doNothing().when(taskService).deleteTask(1);

        mockMvc.perform(MockMvcRequestBuilders.delete("/todos/1"))
                .andExpect(status().isNoContent());
        verify(taskService, times(1)).deleteTask(1);
    }
}