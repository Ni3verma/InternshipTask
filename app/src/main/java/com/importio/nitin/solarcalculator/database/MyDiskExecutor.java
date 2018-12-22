package com.importio.nitin.solarcalculator.database;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MyDiskExecutor {
    public static final Object LOCK = new Object();
    private static MyDiskExecutor sInstance;
    private final Executor diskIO;

    private MyDiskExecutor(Executor diskIO) {
        this.diskIO = diskIO;
    }

    public static MyDiskExecutor getsInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new MyDiskExecutor(Executors.newSingleThreadExecutor());
            }
        }
        return sInstance;
    }

    public Executor getDiskIO() {
        return diskIO;
    }
}
