package fur.yxs.mper.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class winAddSrc {
    public gui g;
    public final Text textN = new Text("Name");
    public final TextField inputN = new TextField();
    public final Text textT = new Text("Type");
    public ComboBox<String> inputT = new ComboBox<>();
    public final Text textU = new Text("Url");
    public final TextField inputU = new TextField();
    public Button buttonClose = new Button("Close");
    public final Button buttonTest = new Button("Test");
    public final Button buttonOk = new Button("ok");
    public final GridPane gridPane = new GridPane();
    public final Stage subStage = new Stage();

    winAddSrc(gui G) {
        g = G;
        subStage.setTitle("Add Src");
        
        gridPane.setMinSize(400, 200);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        //
        inputT.getItems().addAll("File","NetApi");
        buttonOk.setOnAction(e -> subStage.close());
        buttonClose.setOnAction(e -> subStage.close());

        gridPane.add(textN, 0, 0);
        gridPane.add(inputN, 1, 0);
        gridPane.add(textT, 0, 1);
        gridPane.add(inputT, 1, 1);
        gridPane.add(textU, 0, 2);
        gridPane.add(inputU, 1, 2);
        gridPane.add(buttonClose, 0, 3);
        gridPane.add(buttonTest, 1, 3);
        gridPane.add(buttonOk, 2, 3);

        //label.setStyle("-fx-font-size: 16px; -fx-padding: 20;");
        
    }
    public void open(){
        subStage.setScene(new Scene(gridPane));
        subStage.show();
    }
}
