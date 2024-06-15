package com.example.miniblog;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView listViewTasks;
    private EditText editTextNewTask;
    private Button buttonAddTask;
    private SearchView searchView;
    private Spinner spinnerFilter;
    private String[] filters = {"Tout", "Todo", "In Progress", "Done", "Bug"};
    private ArrayList<String> listOfTasks = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TaskDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        listViewTasks = findViewById(R.id.ListViewTodo);
        editTextNewTask = findViewById(R.id.editTextNewArticle);
        buttonAddTask = findViewById(R.id.buttonAddArticle);
        searchView = findViewById(R.id.searchView);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        // Initialize database helper
        dbHelper = new TaskDbHelper(this);
        loadTasksFromDatabase();

        // Create adapter and set it to ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfTasks);
        listViewTasks.setAdapter(adapter);

        // Set up button click listener to add new tasks
        buttonAddTask.setOnClickListener(v -> {
            String newTask = editTextNewTask.getText().toString().trim();
            if (!newTask.isEmpty()) {
                addTaskToDatabase(newTask);
                listOfTasks.add(newTask);
                adapter.notifyDataSetChanged();
                listViewTasks.setSelection(adapter.getCount() - 1);
                editTextNewTask.setText("");
            }
        });

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        // Set up Spinner for status filter
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        // Set up item click listener for editing tasks
        listViewTasks.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTask = listOfTasks.get(position);
            showEditDeleteDialog(selectedTask, position);
        });
    }

    public void applyFilter(View view) {
        String selectedFilter = spinnerFilter.getSelectedItem().toString();
        filterTasksByStatus(selectedFilter);
    }

    private void filterTasksByStatus(String status) {
        ArrayList<String> filteredTasks = new ArrayList<>();
        for (String task : listOfTasks) {
            // Exemple de filtrage basique : si le statut de la tâche correspond au statut spécifié
            // Ajoutez la tâche à la liste filtrée
            // Vous devez implémenter cette logique en fonction de votre structure de données
            if (getTaskStatus(task).equals(status)) {
                filteredTasks.add(task);
            }
        }
        // Mettre à jour l'adaptateur de la ListView avec la liste filtrée
        adapter.clear();
        adapter.addAll(filteredTasks);
        adapter.notifyDataSetChanged();
    }

    private String getTaskStatus(String task) {
        // Implémentez cette méthode pour extraire le statut de la tâche
        // à partir de la chaîne de tâche (par exemple, en analysant la chaîne ou en consultant la base de données)
        // Cette méthode devrait retourner le statut de la tâche (Todo, In Progress, Done, Bug, etc.)
        // en fonction de votre logique d'application
        // C'est un exemple simple, vous devrez l'adapter à votre application
        return "Todo"; // Retourne un exemple de statut pour le moment
    }

    private void loadTasksFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE_NAME,
                new String[]{TaskContract.TaskEntry.COLUMN_NAME_TASK},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            String task = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_TASK));
            listOfTasks.add(task);
        }
        cursor.close();
    }

    private void addTaskToDatabase(String task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TASK, task);
        db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
    }

    private void updateTaskInDatabase(String oldTask, String newTask) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TASK, newTask);
        db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_NAME_TASK + " = ?", new String[]{oldTask});
    }

    private void deleteTaskFromDatabase(String task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE_NAME, TaskContract.TaskEntry.COLUMN_NAME_TASK + " = ?", new String[]{task});
    }

    private void showEditDeleteDialog(String task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier ou supprimer la tâche");

        final EditText input = new EditText(this);
        input.setText(task);
        builder.setView(input);

        builder.setPositiveButton("Modifier", (dialog, which) -> {
            String newTask = input.getText().toString().trim();
            if (!newTask.isEmpty()) {
                updateTaskInDatabase(task, newTask);
                listOfTasks.set(position, newTask);
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Supprimer", (dialog, which) -> {
            deleteTaskFromDatabase(task);
            listOfTasks.remove(position);
            adapter.notifyDataSetChanged();
        });

        builder.setNeutralButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
