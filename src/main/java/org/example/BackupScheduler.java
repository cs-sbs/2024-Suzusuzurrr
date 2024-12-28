package org.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupScheduler {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void startBackupScheduler() {
        scheduler.scheduleAtFixedRate(new DatabaseBackupTask(), 0, 60, TimeUnit.SECONDS);
    }
}
