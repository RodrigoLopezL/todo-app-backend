package com.encora.todolist_app.controller;

import com.encora.todolist_app.models.StateTaskDTO;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/todos")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Page<Task>> allTasks(
            @RequestParam(value = "state", required = false) Boolean state,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "text", required = false) String text,
            Pageable pageable) {
        Page<Task> tasks = taskService.getAllTasks(state, priority, text, pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @GetMapping("/todos/time")
    public ResponseEntity<Map<String, Duration>> timeTask() {
        Map<String, Duration> avgTimes = taskService.avgTimesAllTask();
        if (avgTimes == null || avgTimes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(avgTimes, HttpStatus.OK);
    }

    @PostMapping("/todos")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Task> insertTask(@RequestBody Task task) {
        Task insertedTask = taskService.addTask(task);
        if (insertedTask != null) {
            return new ResponseEntity<>(insertedTask, HttpStatus.CREATED);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PatchMapping("/todos/{id}/done")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<StateTaskDTO> updateStatusDoneTask(@PathVariable int id) {
        StateTaskDTO updatedState = taskService.updateStatusDoneTask(id);
        if (updatedState != null) {
            return new ResponseEntity<>(updatedState, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("todos/{id}/undone")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<StateTaskDTO> updateStatusTask(@PathVariable int id) {
        StateTaskDTO updatedState = taskService.updateStatusUndoneTask(id);
        if (updatedState != null) {
            return new ResponseEntity<>(updatedState, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping("/todos/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable int id, @RequestBody Task task) {
        Task updatedTask = taskService.updateTask(id, task);
        if (updatedTask != null) {
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id) {
        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}


