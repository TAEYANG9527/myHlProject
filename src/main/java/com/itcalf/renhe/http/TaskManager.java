package com.itcalf.renhe.http;

import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public final class TaskManager {

    private static TaskManager instance;
    private static AtomicInteger counter;
    private SparseArray<Callback> taskContainer = new SparseArray<Callback>();

    private TaskManager() {

    }

    public static synchronized TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    /**
     * generate task identification
     *
     * @return
     */
    public static int getTaskId() {
        if (counter == null) {
            counter = new AtomicInteger();
        }
        return counter.incrementAndGet();
    }

    /**
     * retrieve and remove
     *
     * @param key
     * @return
     */
    public void removeTask(int... key) {
        for (int i : key) {
            taskContainer.remove(i);
        }
    }

    public void addTask(Callback value, int... key) {
        for (int i : key) {
            if (!exist(i)) {
                taskContainer.put(i, value);
            }
        }
    }

    public Callback get(int key) {
        return taskContainer.get(key);
    }

    public boolean exist(int key) {
        return taskContainer.get(key) != null;
    }

    public void clear() {
        counter = null;
        taskContainer.clear();
    }

}
