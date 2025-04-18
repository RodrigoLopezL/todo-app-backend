package com.encora.todolist_app.repository;


import com.encora.todolist_app.models.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task save(Task task);
    Optional<Task> findById(Integer id);
    Page<Task> findAll(Pageable pageable);
    void deleteById(Integer id);
    Task replaceTask(Integer id,Task task);
    List<Task> getAllTask();

    Page<Task> findAllByStateAndPriorityAndText(Boolean state, String priority, String text, Pageable pageable);
}