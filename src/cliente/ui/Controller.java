package cliente.ui;

import cliente.Cliente;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    private TextField nameField;

    @FXML
    private Button timbreButton;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<String> listView;

    private final Cliente cliente = new Cliente();

    @FXML
    private void initialize() {
        statusLabel.setText("");
    }

    @FXML
    private void onTimbre() {
        String nombre = nameField.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            statusLabel.setText("Ingrese un nombre");
            return;
        }

        timbreButton.setDisable(true);
        statusLabel.setText("Registrando...");

        // Ejecutar la comunicación en un hilo de fondo
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String respuesta = cliente.enviar(nombre.trim());
                    Platform.runLater(() -> procesarRespuesta(respuesta));
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
                } finally {
                    Platform.runLater(() -> timbreButton.setDisable(false));
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void procesarRespuesta(String respuesta) {
        if (respuesta == null) {
            statusLabel.setText("Respuesta vacía del servidor");
            return;
        }

        if (respuesta.startsWith("OK;")) {
            // OK;index;timestamp
            String[] parts = respuesta.split(";", 3);
            if (parts.length >= 3) {
                String index = parts[1];
                String timestamp = parts[2];
                listView.getItems().add(index + ". " + timestamp);
                statusLabel.setText("Timbre registrado (#" + index + ")");
                if ("4".equals(index)) {
                    statusLabel.setText("Se registraron los 4 timbres");
                    timbreButton.setDisable(true);
                }
            } else {
                statusLabel.setText("Respuesta inesperada: " + respuesta);
            }
        } else if (respuesta.startsWith("ERROR;")) {
            String[] parts = respuesta.split(";", 3);
            String msg = parts.length >= 3 ? parts[2] : respuesta;
            statusLabel.setText("Error: " + msg);
        } else {
            statusLabel.setText("Respuesta desconocida: " + respuesta);
        }
    }
}

