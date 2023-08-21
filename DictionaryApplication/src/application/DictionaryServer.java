package application;

import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONException;

public class DictionaryServer {
	private int numberOfWorkers;
    private ThreadPool threadPool;
    private int port;
    private String dictionaryFilePath;
    private HashMap<String, String> dictionary = new HashMap<>();
    private ServerSocket serverSocket;
    private Label lblCurrentWorkers;

    public DictionaryServer(String[] args) {
        if (args.length >= 2) {
            port = Integer.parseInt(args[0]);
            dictionaryFilePath = args[1];
        }
        loadDictionary();
    }

    public static void main(String[] args) {
        new DictionaryServer(args).initializeServerGUI();
    }
    private void loadDictionary() {
        // TODO: Load words and meanings from the dictionary file into the dictionary HashMap
        try (BufferedReader reader = new BufferedReader(new FileReader(dictionaryFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    dictionary.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 // Start the server logic (like listening for client connections)
    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                lblCurrentWorkers.setText("Current Workers: " + numberOfWorkers);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClientRequest(clientSocket));
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleClientRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String clientMessage = in.readLine();
            JSONObject request = new JSONObject(clientMessage);
            String action = request.getString("action");
            JSONObject response = new JSONObject();

            switch (action) {
                case "search":
                    String wordToSearch = request.getString("word");
                    if (dictionary.containsKey(wordToSearch)) {
                        response.put("status", "success");
                        response.put("meaning", dictionary.get(wordToSearch));
                    } else {
                        response.put("status", "not found");
                    }
                    break;
                case "add":
                    String wordToAdd = request.getString("word");
                    String meaningToAdd = request.getString("meaning");
                    if (!dictionary.containsKey(wordToAdd)) {
                        dictionary.put(wordToAdd, meaningToAdd);
                        response.put("status", "success");
                    } else {
                        response.put("status", "duplicate");
                    }
                    break;
                case "remove":
                    String wordToRemove = request.getString("word");
                    if (dictionary.containsKey(wordToRemove)) {
                        dictionary.remove(wordToRemove);
                        response.put("status", "success");
                    } else {
                        response.put("status", "not found");
                    }
                    break;
                case "update":
                    String wordToUpdate = request.getString("word");
                    String newMeaning = request.getString("meaning");
                    if (dictionary.containsKey(wordToUpdate)) {
                        dictionary.put(wordToUpdate, newMeaning);
                        response.put("status", "success");
                    } else {
                        response.put("status", "not found");
                    }
                    break;
            }

            out.println(response.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    private void initializeServerGUI() {
        Frame frame = new Frame("Dictionary Server Management");

        Panel serverControl = new Panel();
        Label lblServerStatus = new Label("Status: Stopped");
        TextField workerField = new TextField(String.valueOf(Runtime.getRuntime().availableProcessors()));
        workerField.setColumns(5);
        Button startButton = new Button("Start Server");
        Button stopButton = new Button("Stop Server");

        startButton.addActionListener(event -> {
            numberOfWorkers = Integer.parseInt(workerField.getText());
            threadPool = new ThreadPool(numberOfWorkers);
            lblServerStatus.setText("Status: Running");

            // Start the actual server logic using the worker-pool architecture
            startServer();
        });

        stopButton.addActionListener(event -> {
            if (threadPool != null) {
                threadPool.shutdown();
            }

            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            lblServerStatus.setText("Status: Stopped");
        });

        serverControl.add(new Label("Initial Workers:"));
        serverControl.add(workerField);
        serverControl.add(startButton);
        serverControl.add(stopButton);
        serverControl.add(lblServerStatus);

        Panel workerControl = new Panel();
        Button increaseWorkersButton = new Button("Increase Workers");
        Button decreaseWorkersButton = new Button("Decrease Workers");
        lblCurrentWorkers = new Label("Current Workers: " + numberOfWorkers);

        increaseWorkersButton.addActionListener(event -> {
            // TODO: Logic to increase the number of workers
            if (threadPool != null) {
                threadPool.increaseWorkers();
                numberOfWorkers++;
                lblCurrentWorkers.setText("Current Workers: " + numberOfWorkers);
            }
        });

        decreaseWorkersButton.addActionListener(event -> {
            // TODO: Logic to decrease the number of workers
            if (threadPool != null && numberOfWorkers > 0) {
                threadPool.decreaseWorkers();
                numberOfWorkers--;
                lblCurrentWorkers.setText("Current Workers: " + numberOfWorkers);
            }
        });

        workerControl.add(increaseWorkersButton);
        workerControl.add(decreaseWorkersButton);
        workerControl.add(lblCurrentWorkers);

        Panel dictionaryDisplay = new Panel();
        Button showDictionaryButton = new Button("Show Dictionary");
        TextArea dictionaryArea = new TextArea(5, 50);
        
        showDictionaryButton.addActionListener(event -> {
            StringBuilder content = new StringBuilder();
            for (Map.Entry<String, String> entry : dictionary.entrySet()) {
                content.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            dictionaryArea.setText(content.toString());
        });
        
        dictionaryArea.setEditable(false);
        dictionaryDisplay.add(showDictionaryButton);
        dictionaryDisplay.add(dictionaryArea);

        TextArea logArea = new TextArea(10, 50);
        logArea.setEditable(false);

        Panel mainPanel = new Panel();
        mainPanel.setLayout(new GridLayout(4, 1));  // 4 rows, 1 column
        mainPanel.add(serverControl);
        mainPanel.add(workerControl);
        mainPanel.add(dictionaryDisplay);
        mainPanel.add(logArea);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
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
