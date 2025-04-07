package com.encora.todolist_app.utils.comparators;

import com.encora.todolist_app.models.Task;

import java.util.Comparator;

public class TextTaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task t1, Task t2) {
        return t1.getText().compareTo(t2.getText());
    }
}
