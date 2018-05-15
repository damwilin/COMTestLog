import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    public Button refreshPortListButton;
    public Button connectTo;
    public ComboBox portBox;
    public TextArea dataLog;
    public TextArea log;
    private SerialPort[] ports;
    private SerialPort choosenPort;


    public Controller() {
    }


    public void refreshPortList(ActionEvent actionEvent) {
        initializePortBox();
    }

    public void openConnection(ActionEvent actionEvent) {
        int choosen = portBox.getSelectionModel().getSelectedIndex();
        System.out.println(choosen);
        if (choosen != -1){
           printPortDataWithListener(choosen);
        }
        else
            log.appendText("Please choose COM port.\n");

    }

    public void closeConnection(ActionEvent actionEvent) {
        closePort();
    }

    private void initializePortBox(){
        portBox.getItems().clear();
        int portCount = initialize();
        log("Found: " + portCount + " ports.");
        portBox.getItems().setAll(printAvailablePorts());
    }

    public void clearData(ActionEvent actionEvent) {
        dataLog.clear();
        log.clear();
        log("All data cleared.");
    }

    public void saveData(ActionEvent actionEvent) {
        String data = dataLog.getText();
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save data");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null){
            Utilities.exportToFile(data,file, log);
            log(file.getName() + " saved.");
        }

    }

    private void log(String data){
        log.appendText(data + "\n");
    }

    public void printPortDataWithListener(int portNum) {
        choosenPort = ports[portNum];
        if (openPort(choosenPort)) {
            Thread thread = new Thread(){
                @Override
                public void run() {
                    super.run();

                    choosenPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
                    //log.appendText("Listening to data...\n");
                    choosenPort.addDataListener(new SerialPortDataListener() {
                        @Override
                        public int getListeningEvents() {
                            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                        }

                        @Override
                        public void serialEvent(SerialPortEvent event) {
                            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                                return;
                            InputStream dataStream = choosenPort.getInputStream();
                            Platform.runLater(() -> dataLog.appendText(convertStreamToString(dataStream)));
                        }
                    });
                }
            };
            thread.start();
        }
    }

    public List<String> printAvailablePorts() {
        List<String> portList = new ArrayList<>();
        if (ports.length > 0) {
            for (int i = 0; i < ports.length; i++) {
                portList.add(ports[i].getSystemPortName());
            }
        } else {
            System.out.println("No ports available.\n");
            log("No ports available.");
        }
        return portList;
    }

    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private boolean openPort(SerialPort port) {
        if (port.openPort()) {
            System.out.println("Port open successfully");
            log("Port OPEN successfully.");
            return true;
        } else {
            System.out.println("Unable to open the port");
            log("Unable to open the port.");
            return false;
        }
    }
    public void closePort() {
        if (choosenPort!= null || choosenPort.isOpen()){
            choosenPort.closePort();
            System.out.println("Port CLOSED successfully.\n");
            log("Port CLOSED successfully.\n");
        }
    }

    public int initialize() {
        ports = null;
        ports = SerialPort.getCommPorts();
        return  ports.length;
    }
}
