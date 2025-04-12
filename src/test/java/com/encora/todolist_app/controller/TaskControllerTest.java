package com.encora.todolist_app.controller;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.StateTaskDTO;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@WebMvcTest() // Enfoca la prueba solo en TaskController
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private TaskService taskService;

    @Autowired
    private TaskController taskController;

    private Page<Task> defaultTaskListPage;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUP(){
        // Crear tareas comunes para las pruebas
        task1 = new Task(0, "Tarea Común 1", LocalDateTime.now(),null, null, null, Priority.MEDIUM, false);
        task2 = new Task(1, "Tarea Común 2", LocalDateTime.now().plusHours(1),null, null, null, Priority.HIGH, true);
        defaultTaskListPage = new PageImpl<>(Arrays.asList(task1, task2), PageRequest.of(0, 10), 2);

        taskService.addTask(task1);
        taskService.addTask(task2);
    }

    @Test
    void allTasks_shouldReturnOkAndPageOfTasks() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        //Task task = new Task(0, "Tarea de prueba", LocalDateTime.now(),null, null, null, Priority.MEDIUM, false);
        Page<Task> taskPage = new PageImpl<>(List.of(task1,task2), pageable, 1);

        when(taskService.getAllTasks(eq(null), eq(null), eq(null), eq(pageable))).thenReturn(taskPage);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/todos")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.[0].id").value(0))
                .andExpect(jsonPath("$.content.[0].text").value("Tarea de prueba"));
    }

    @Test
    void timeTask_shouldReturnOkAndMapOfDurations() throws Exception {
        // Arrange
        Map<String, Duration> timeMap = Map.of("AvgTotalTime", Duration.ofMinutes(30));
        when(taskService.avgTimesAllTask()).thenReturn(timeMap);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/todos/time"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.AvgTotalTime").value(30));
    }

    @Test
    void insertTask_shouldReturnCreatedAndInsertedTask() throws Exception {
        // Arrange
        Task taskToInsert = new Task(1, "Nueva Tarea", LocalDateTime.now(),null, null, null, Priority.HIGH, false);
        Task insertedTask = new Task(1, "Nueva Tarea", LocalDateTime.now(),null, null, null, Priority.HIGH, false);
        when(taskService.addTask(any(Task.class))).thenReturn(insertedTask);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskToInsert)))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Or isCreated (201) depending on your design
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Nueva Tarea"));
    }

    @Test
    void updateStatusDoneTask_shouldReturnOkAndUpdatedState() throws Exception {
        // Arrange
        int taskId = 1;
        StateTaskDTO updatedState = new StateTaskDTO(taskId, true);
        when(taskService.updateStatusDoneTask(taskId)).thenReturn(updatedState);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/{id}/done", taskId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    void updateStatusTask_shouldReturnOkAndUpdatedState() throws Exception {
        // Arrange
        int taskId = 2;
        StateTaskDTO updatedState = new StateTaskDTO(taskId, false);
        when(taskService.updateStatusUndoneTask(taskId)).thenReturn(updatedState);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.patch("/todos/{id}/undone", taskId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.state").value(false));
    }

    @Test
    void updateTask_shouldReturnOkAndUpdatedTask() throws Exception {
        // Arrange
        int taskId = 3;
        Task taskToUpdate = new Task(taskId, "Tarea Actualizada", LocalDateTime.now(),null, null, null, Priority.MEDIUM, true);
        when(taskService.updateTask(eq(taskId), any(Task.class))).thenReturn(taskToUpdate);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/todos/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskToUpdate)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.text").value("Tarea Actualizada"));
    }

    @Test
    void deleteTask_shouldReturnNoContent() throws Exception {
        // Arrange
        int taskId = 4;
        doNothing().when(taskService).deleteTask(taskId);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/todos/{id}", taskId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        verify(taskService, times(1)).deleteTask(taskId);
    }
}