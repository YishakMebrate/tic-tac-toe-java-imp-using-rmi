package tic_tac_toe.layout;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import tic_tac_toe.client.TicTacToeClient;
import tic_tac_toe.server.TicTacToeServer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    private TextField textField;
    @FXML
    private Button serverButton;
    @FXML
    private Button clientButton;
    @FXML
    private Button restartButton;
    @FXML
    private AnchorPane panel;
    @FXML
    private Label endGame;
    @FXML
    private Label whoWin;
    @FXML
    private Label connectionStatus;

    private List<Rectangle> rectangles = new ArrayList<>(9);
    private List<Cell> cellList = new ArrayList<>(9);
    private boolean yourTurn;

    private Image imgX = new Image("/tic_tac_toe/assets/x-image.jpg");
    private Image imgO = new Image("/tic_tac_toe/assets/0-image.jpg");

    private TicTacToeServer server = null;
    private TicTacToeClient client = null;

    @FXML
    public void initialize() {
        createTable();
        endGame.setVisible(false);
        whoWin.setVisible(false);
        connectionStatus.setVisible(false);
        Image restartImg = new Image("/tic_tac_toe/assets/reset-icon.png");
        restartButton.setGraphic(new ImageView(restartImg));
        restartButton.setVisible(false);

        for (Rectangle rectangle : rectangles) {
            rectangle.setOnMouseClicked(event -> {
                if (cellList.get(rectangles.indexOf(rectangle)).getStatus() == 0 && client != null && yourTurn) {
                    cellList.get(rectangles.indexOf(rectangle)).setStatus(1);
                    rectangle.setFill(new ImagePattern(imgX));
                    rectangle.setDisable(true);
                    client.sendToServer(1, rectangles.indexOf(rectangle));
                    yourTurn = false;
                } else if (cellList.get(rectangles.indexOf(rectangle)).getStatus() == 0 && server != null && yourTurn) {
                    cellList.get(rectangles.indexOf(rectangle)).setStatus(2);
                    rectangle.setFill(new ImagePattern(imgO));
                    rectangle.setDisable(true);
                    server.sendToClient(2, rectangles.indexOf(rectangle));
                    yourTurn = false;
                }
                if (whoWin.getText().toString().equals("DRAW!")){
                    restartButton.setVisible(true);
                }
                if (checkForEndGame(1) == 1) {
                    endGame.setVisible(true);
                    whoWin.setVisible(true);
                    whoWin.setText("You WON!");
                    restartButton.setVisible(true);
                    for (Rectangle rect : rectangles) {
                        rect.setDisable(true);
                    }
                } else if (checkForEndGame(2) == 2) {
                    endGame.setVisible(true);
                    whoWin.setVisible(true);
                    whoWin.setText("You WON!");
                    restartButton.setVisible(true);
                    for (Rectangle rect : rectangles) {
                        rect.setDisable(true);
                    }
                }
            });
        }
    }

    @FXML
    private void handleServerButton() {
        try {
            server = new TicTacToeServer();
            clientButton.setDisable(true);
            serverButton.setDisable(true);
            textField.setDisable(true);
            yourTurn = true;
            server.setGuiCellListener(event -> {
                if(event.getStatus() == 0 && event.getIndex() == -1){ // restart initaited
                    reset();
                    return;
                }
                rectangles.get(event.getIndex()).setFill(new ImagePattern(imgX));
                rectangles.get(event.getIndex()).setDisable(true);
                cellList.get(event.getIndex()).setStatus(event.getStatus());
                yourTurn = true;
                if (checkForEndGame(1) == 1) {
                    for (Rectangle rect : rectangles) {
                        rect.setDisable(true);
                    }
                    endGame.setVisible(true);
                    whoWin.setVisible(true);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClientButton() {
        try {
            client = new TicTacToeClient(textField);
            clientButton.setDisable(true);
            serverButton.setDisable(true);
            textField.setDisable(true);
            yourTurn = false;
            client.setGuiCellListener(event -> {
                if(event.getStatus() == 0 && event.getIndex() == -1){ // restart initaited
                    reset();
                    return;
                }
                rectangles.get(event.getIndex()).setFill(new ImagePattern(imgO));
                rectangles.get(event.getIndex()).setDisable(true);
                cellList.get(event.getIndex()).setStatus(event.getStatus());
                yourTurn = true;
                if (checkForEndGame(2) == 2) {
                    for (Rectangle rect : rectangles) {
                        rect.setDisable(true);
                    }
                    endGame.setVisible(true);
                    whoWin.setVisible(true);
                }
            });
            connectionStatus.setVisible(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Rectangle rect = new Rectangle(153, 150);
                rect.setLayoutX(14 + 160 * j);
                rect.setLayoutY(14 + 156 * i);
                rect.setFill(Color.web("#34495E")); //#34495E
                rectangles.add(rect);
            }
        }
        for (int i = 0; i < 9; ++i) {
            cellList.add(new Cell(this, 0, i));
        }
        panel.getChildren().addAll(rectangles);
    }

    private int checkForEndGame(int status) {
        int counter = 0;
        for (int i = 0; i < 3; ++i) {    // vertically for O
            for (int j = 3 * i; j < 3 + 3 * i; ++j) {
                if (cellList.get(j).getStatus() == status) {
                    counter++;
                } else {
                    counter = 0;
                    break;
                }
                if (counter == 3) {
                    return status;
                }
            }
        }

        counter = 0;
        for (int i = 0; i < 3; ++i) {     // vertically for 0
            for (int j = i; j < i + 7; j += 3) {
                if (cellList.get(j).getStatus() == status) {
                    counter++;
                } else {
                    counter = 0;
                    break;
                }
                if (counter == 3) {
                    return status;
                }
            }
        }
        if (cellList.get(0).getStatus() == status && cellList.get(4).getStatus() == status && cellList.get(8).getStatus() == status) {   // diagonal
            return status;
        }
        if (cellList.get(2).getStatus() == status && cellList.get(4).getStatus() == status && cellList.get(6).getStatus() == status) {   // diagonal
            return status;
        }

        for (int i = 0; i < 9; ++i) {
            if (cellList.get(i).getStatus() == 0) return 0;
        }

        Platform.runLater(() -> {
            endGame.setVisible(true);
            whoWin.setVisible(true);
            whoWin.setText("DRAW!");
        });
        return 0;
    }


    @FXML
    private void handleRestartButton(){
        restartButton.setVisible(false);
        whoWin.setText("You LOST!");
        if(server != null ){
           server.sendToClient(0, -1);
        }
        if (client != null){
            reset();
            client.sendToServer(0, -1);
        }
        reset();
    }
    private void reset(){
        endGame.setVisible(false);
        whoWin.setVisible(false);
        for(int i = 0; i < 9; i++) {
            rectangles.get(i).setFill(Color.web("#34495E"));
            rectangles.get(i).setDisable(false);
            cellList.get(i).setStatus(0);
        }
    }
}
