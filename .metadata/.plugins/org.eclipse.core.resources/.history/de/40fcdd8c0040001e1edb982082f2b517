package application;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

public class DictionaryClient extends Application {
	public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dictionary Client");

        // Create GUI components
        TextField wordField = new TextField();
        wordField.setPromptText("Enter Word");

        TextField meaningField = new TextField();
        meaningField.setPromptText("Enter Meaning");

        Button searchButton = new Button("Search");
        Button addButton = new Button("Add");
        Button removeButton = new Button("Remove");
        Button updateButton = new Button("Update");
        
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        
        // Event handlers for the buttons
        searchButton.setOnAction(event -> {
            JSONObject request = new JSONObject();
            request.put("action", "search");
            request.put("word", wordField.getText());
            // TODO: Send this JSON request to the server and handle the response
            resultArea.appendText("Sent search request for word: " + wordField.getText() + "\n");
        });

        addButton.setOnAction(event -> {
            JSONObject request = new JSONObject();
            request.put("action", "add");
            request.put("word", wordField.getText());
            request.put("meaning", meaningField.getText());
            // TODO: Send this JSON request to the server and handle the response
            resultArea.appendText("Sent add request for word: " + wordField.getText() + "\n");
        });

        // TODO: Add event handlers for the buttons to perform operations.
        removeButton.setOnAction(event -> {
            JSONObject request = new JSONObject();
            request.put("action", "remove");
            request.put("word", wordField.getText());
            // TODO: Send this JSON request to the server and handle the response
            resultArea.appendText("Sent remove request for word: " + wordField.getText() + "\n");
        });

        updateButton.setOnAction(event -> {
            JSONObject request = new JSONObject();
            request.put("action", "update");
            request.put("word", wordField.getText());
            request.put("meaning", meaningField.getText());
            // TODO: Send this JSON request to the server and handle the response
            resultArea.appendText("Sent update request for word: " + wordField.getText() + "\n");
        });

        // Layout
        VBox layout = new VBox(10, wordField, meaningField, searchButton, addButton, removeButton, updateButton, resultArea);
        layout.setPadding(new javafx.geometry.Insets(10));

        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
