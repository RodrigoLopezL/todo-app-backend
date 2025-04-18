package com.encora.todolist_app.service;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.StateTaskDTO;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page<Task> getAllTasks(Boolean state, String priority, String text, Pageable pageable) {
        return taskRepository.findAllByStateAndPriorityAndText(state, priority, text, pageable);
    }

    public Task addTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Integer id, Task task) {
        if (taskRepository.findById(id).isPresent()) {
            task.setId(id);
            return taskRepository.replaceTask(id,task);
        }
        return null; // Considera lanzar una excepción si no se encuentra
    }

    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    public Map<String, Duration> avgTimesAllTask() {

        List<Task> allTasks = taskRepository.getAllTask();
        if(allTasks.isEmpty()){
            return null;
        }

        Map<String,Duration> mapAvgTimes = new HashMap<>();
        Duration avgTotalTime = Duration.ZERO;
        Duration avgTimeLowP = Duration.ZERO;
        Duration avgTimeMediumP = Duration.ZERO;
        Duration avgTimeHighP = Duration.ZERO;

        int totalTask= 0;
        int amountLowTask = 0;
        int amountMediumTask = 0;
        int amountHighTask=0;

        for (Task task:allTasks) {
            if (task.isState()){
                totalTask++;
                avgTotalTime = avgTotalTime.plus(task.getTimeFrame());
                if(task.getPriority() == Priority.LOW){
                    avgTimeLowP = avgTimeLowP.plus(task.getTimeFrame());
                    amountLowTask++;
                } else if (task.getPriority() == Priority.MEDIUM) {
                    avgTimeMediumP = avgTimeMediumP.plus(task.getTimeFrame());
                    amountMediumTask++;
                }else{
                    avgTimeHighP = avgTimeHighP.plus(task.getTimeFrame());
                    amountHighTask++;
                }
            }
        }

        if(totalTask == 0){
            return null;
        }

        mapAvgTimes.put("AvgTotalTime", avgTotalTime.dividedBy(totalTask));
        mapAvgTimes.put("avgTimeLowPriority",amountLowTask != 0 ? avgTimeLowP.dividedBy(amountLowTask): Duration.ZERO);
        mapAvgTimes.put("avgTimeMediumPriority",amountMediumTask != 0? avgTimeMediumP.dividedBy(amountMediumTask): Duration.ZERO);
        mapAvgTimes.put("avgTimeHighPriority",amountHighTask != 0? avgTimeHighP.dividedBy(amountHighTask): Duration.ZERO);

        return mapAvgTimes;
    }

    public StateTaskDTO updateStatusDoneTask(int id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            task.setState(true);
            task.setDoneDate(LocalDateTime.now());
            task.setTimeFrame(Duration.between(task.getCreationDate(),task.getDoneDate()));
            taskRepository.replaceTask(id,task);
            return new StateTaskDTO(task.getId(), task.isState());
        }
        return null; // Considera lanzar una excepción si no se encuentra
    }

    public StateTaskDTO updateStatusUndoneTask(int id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            task.setState(false);
            task.setDoneDate(null);
            task.setTimeFrame(null);
            taskRepository.replaceTask(id,task);
            return new StateTaskDTO(task.getId(), task.isState());
        }
        return null; // Considera lanzar una excepción si no se encuentra
    }
}