package com.example.miniblog;

import android.provider.BaseColumns;

public final class TaskContract {
    private TaskContract() {}

    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_NAME_TASK = "task";
        public static final String COLUMN_NAME_STATUS = "status"; // Ajout de la colonne de statut
    }
}

