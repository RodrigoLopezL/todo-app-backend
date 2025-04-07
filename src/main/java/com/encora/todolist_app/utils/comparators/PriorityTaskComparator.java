package com.encora.todolist_app.utils.comparators;

import com.encora.todolist_app.models.Task;

import java.util.Comparator;

public class PriorityTaskComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {
        //compare to know which is most important
        return t2.getPriority().compareTo(t1.getPriority());
    }
}
