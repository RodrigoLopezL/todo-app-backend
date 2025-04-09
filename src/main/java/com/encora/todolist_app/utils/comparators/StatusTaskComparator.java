package com.encora.todolist_app.utils.comparators;

import com.encora.todolist_app.models.Task;

import java.util.Comparator;

public class StatusTaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task t1, Task t2) {
        //This compare is made for show first the undone task
        //inverting the parameters of method compare will result show first the done task
        return Boolean.compare(t2.isState(),t1.isState());
    }
}
