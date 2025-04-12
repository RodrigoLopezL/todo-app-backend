package com.encora.todolist_app.service;

import com.encora.todolist_app.models.Priority;
import com.encora.todolist_app.models.StateTaskDTO;
import com.encora.todolist_app.models.Task;
import com.encora.todolist_app.utils.comparators.PriorityTaskComparator;
import com.encora.todolist_app.utils.comparators.UrgentTaskComparator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    Map<Integer,Task> taskMap = new HashMap<>();

    //quick solution id
    int id = 0;

    public TaskService() {
    }

    public void setTaskMap(Map<Integer,Task> map){
        this.taskMap = map;
    }

    public Map<Integer, Task> getTaskMap() {
        return taskMap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Page<Task> getAllTasks(
            @RequestParam(value = "state", required = false) Boolean state,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "text", required = false) String text,
            Pageable pageable
    ){

        List<Task> allTasksList = new ArrayList<>(taskMap.values());

        if(state != null){
            allTasksList = allTasksList.stream()
                    .filter(task -> task.isState() == state)
                    .collect(Collectors.toList());
        }

        if (StringUtils.hasText(priority)) {
            try {
                Priority searchPriority = Priority.valueOf(priority.toUpperCase());
                allTasksList = allTasksList.stream()
                        .filter(task -> task.getPriority() == searchPriority)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return Page.empty(); //
            }
        }

        if (StringUtils.hasText(text)) {
            allTasksList = allTasksList.stream()
                    .filter(task -> task.getText().toLowerCase().contains(text.toLowerCase()))
                    .collect(Collectors.toList());
        }
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int start = currentPage * pageSize;

        if (pageable.getSort().isSorted()) {
            Comparator<Task> comparator = getTaskComparator(pageable);
            if (comparator != null) {
                allTasksList.sort(comparator);
            }

        }

        List<Task> pagedTasks;
        if (start >= allTasksList.size()) {
            pagedTasks = new ArrayList<>(); // Page empty if home index is out of range
        } else {
            int end = Math.min(start + pageSize, allTasksList.size());
            pagedTasks = allTasksList.subList(start, end);
        }

        return new PageImpl<>(pagedTasks, pageable, allTasksList.size());
    }

    private static Comparator<Task> getTaskComparator(Pageable pageable) {
        Comparator<Task> comparator = null;
        for (Sort.Order order : pageable.getSort()){
            Comparator<Task> currentComparator = switch (order.getProperty()) {
                case "priority" -> Comparator.comparing(Task::getPriority);
                case "dueDate" -> Comparator.comparing(Task::getDueDate,Comparator.nullsFirst(Comparator.naturalOrder()));
                case "urgency" -> new UrgentTaskComparator();
                default -> null;
            };
            if (currentComparator != null) {
                if (order.getDirection() == Sort.Direction.DESC) {
                    currentComparator = currentComparator.reversed();
                }
                if (comparator == null) {
                    comparator = currentComparator;
                } else {
                    comparator = comparator.thenComparing(currentComparator);
                }
            }
        }
        return comparator;
    }

    public Task addTask(Task task){
        task.setId(id);
        task.setCreationDate(LocalDateTime.now());
        taskMap.put(id,task);
        id++;
        return task;
    }

    public  Task updateTask(int id,Task task){
        taskMap.replace(id,task);
        return task;
    }

    public StateTaskDTO updateStatusDoneTask(int id){
        Task task = taskMap.get(id);
        if(task != null){
            task.setState(true);
            task.setDoneDate(LocalDateTime.now());
            task.setTimeFrame(Duration.between(task.getCreationDate(),task.getDoneDate()));
            taskMap.replace(id,task);
            return new StateTaskDTO(task.getId(),task.isState());
        }
        return null;
    }
    public StateTaskDTO updateStatusUndoneTask(int id){
        Task task = taskMap.get(id);
        if(task != null){
            task.setState(false);
            task.setDoneDate(null);
            task.setTimeFrame(null);
            taskMap.replace(id,task);
            return new StateTaskDTO(task.getId(),task.isState());
        }
        return null;
    }

    public void deleteTask(int id){
        taskMap.remove(id);
    }

    public Map<String,Duration> avgTimesAllTask(){
        if(taskMap.isEmpty()){
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

        for (Map.Entry<Integer, Task> entry : taskMap.entrySet()) {
            Task task = entry.getValue();
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

        mapAvgTimes.put("AvgTotalTime",totalTask != 0 ? avgTotalTime.dividedBy(totalTask): Duration.ZERO);
        mapAvgTimes.put("avgTimeLowPriority",amountLowTask != 0 ? avgTimeLowP.dividedBy(amountLowTask): Duration.ZERO);
        mapAvgTimes.put("avgTimeMediumPriority",amountMediumTask != 0? avgTimeMediumP.dividedBy(amountMediumTask): Duration.ZERO);
        mapAvgTimes.put("avgTimeHighPriority",amountHighTask != 0? avgTimeHighP.dividedBy(amountHighTask): Duration.ZERO);

        return mapAvgTimes;
    }
}
