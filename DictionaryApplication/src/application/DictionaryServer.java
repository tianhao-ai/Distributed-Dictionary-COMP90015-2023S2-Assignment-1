package application;

import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class DictionaryServer extends Application {
    private int numberOfWorkers;
    private ThreadPool threadPool;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dictionary Server Management");

        // Server Control Section
        Label lblServerStatus = new Label("Status: Stopped");
        TextField workerField = new TextField(String.valueOf(Runtime.getRuntime().availableProcessors()));
        workerField.setPrefWidth(50);
        Button startButton = new Button("Start Server");
        Button stopButton = new Button("Stop Server");
        startButton.setOnAction(event -> {
            numberOfWorkers = Integer.parseInt(workerField.getText());
            threadPool = new ThreadPool(numberOfWorkers);
            lblServerStatus.setText("Status: Running");
            // TODO: Start the actual server logic (like listening for client connections)
        });
        stopButton.setOnAction(event -> {
            if (threadPool != null) {
                threadPool.shutdown();
            }
            lblServerStatus.setText("Status: Stopped");
        });
        HBox serverControl = new HBox(10, new Label("Initial Workers:"), workerField, startButton, stopButton, lblServerStatus);
        // Worker Control Section
        Button increaseWorkersButton = new Button("Increase Workers");
        Button decreaseWorkersButton = new Button("Decrease Workers");
        Label lblCurrentWorkers = new Label("Current Workers: " + numberOfWorkers);
        HBox workerControl = new HBox(10, increaseWorkersButton, decreaseWorkersButton, lblCurrentWorkers);
        increaseWorkersButton.setOnAction(event -> {
            // TODO: Logic to increase the number of workers
            if (threadPool != null) {
                threadPool.increaseWorkers();
                numberOfWorkers++;
                lblCurrentWorkers.setText("Current Workers: " + numberOfWorkers);
            }
        });

        decreaseWorkersButton.setOnAction(event -> {
            // TODO: Logic to decrease the number of workers
            if (threadPool != null && numberOfWorkers > 0) {
                threadPool.decreaseWorkers();
                numberOfWorkers--;
                lblCurrentWorkers.setText("Current Workers: " + numberOfWorkers);
            }
        });
        // Dictionary Display Section
        Button showDictionaryButton = new Button("Show Dictionary");
        TextArea dictionaryArea = new TextArea();
        dictionaryArea.setPromptText("Words in Dictionary");
        dictionaryArea.setEditable(false);
        VBox dictionaryDisplay = new VBox(10, showDictionaryButton, dictionaryArea);

        // Logs Section
        TextArea logArea = new TextArea();
        logArea.setPromptText("Server Logs");
        logArea.setEditable(false);

        // Layout
        VBox layout = new VBox(20, serverControl, workerControl, dictionaryDisplay, logArea);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
class WorkerThread extends Thread {
    private final Queue<Runnable> taskQueue;
    private boolean isStopped = false;

    public WorkerThread(Queue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        while (!isStopped) {
            Runnable task = null;
            synchronized (taskQueue) {
                while (taskQueue.isEmpty() && !isStopped) {
                    try {
                        taskQueue.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (!taskQueue.isEmpty()) {
                    task = taskQueue.poll();
                }
            }
            if (task != null) {
                task.run();
            }
        }
    }

    public void stopWorker() {
        isStopped = true;
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
    }
}

class ThreadPool {
    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private final List<WorkerThread> threads;
    private final int poolSize;

    public ThreadPool(int poolSize) {
        this.poolSize = poolSize;
        threads = new ArrayList<>(poolSize);

        // Initialize and start worker threads
        for (int i = 0; i < poolSize; i++) {
            WorkerThread worker = new WorkerThread(taskQueue);
            threads.add(worker);
            worker.start();
        }
    }

    public void submit(Runnable task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.notify();
        }
    }

    public void shutdown() {
        for (WorkerThread worker : threads) {
            worker.stopWorker();
        }
    }

    public void increaseWorkers() {
        WorkerThread worker = new WorkerThread(taskQueue);
        threads.add(worker);
        worker.start();
    }

    public void decreaseWorkers() {
        if (!threads.isEmpty()) {
            WorkerThread worker = threads.remove(threads.size() - 1);
            worker.stopWorker();
        }
    }
}
