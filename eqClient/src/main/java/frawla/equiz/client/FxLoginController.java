package frawla.equiz.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import frawla.equiz.util.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class FxLoginController implements Initializable
{
	@FXML private TextField txtIP;
	@FXML private TextField txtPort;
	@FXML private TextField txtID;
	@FXML private TextField txtName;
	@FXML private Pane PanRoot;

	private Socket mySocket;	

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

		//to be executed after initialize()
		Platform.runLater(() -> {
			Stage window = new Stage( );
			Scene scene = new Scene(PanRoot, PanRoot.getPrefWidth(), PanRoot.getPrefHeight());
			window.setScene(scene);

			window.getIcons().add(new Image(Util.getResourceAsURI("images/aplus.png").toString() ));
			window.setTitle("eQuiz-CLIENT");
			window.setOnCloseRequest(event -> System.exit(0) );
			window.show();
		});

		txtIP.setText(Main.jsap.getResult().getString("host"));
		txtPort.setText(Main.jsap.getResult().getInt("port")+"");		
		txtID.setText(Main.jsap.getResult().getString("id"));
		txtName.setText(Main.jsap.getResult().getString("user"));
		
		txtID.setText("xxx" + "100"); //(new Random().nextInt(999-100+1)+100)


		txtID.setTextFormatter( new TextFormatter<String>( change ->
		{
			change.setText(change.getText().toUpperCase());
			return  change;
		} ));		

		//clean all old images
		Util.cleanDirectory(new File(Util.getTempDir() + File.separator + "equiz"));
	}

	public void btnJoin_click()
	{

		try {
			mySocket = new Socket(txtIP.getText(), Integer.parseInt(txtPort.getText()));
			ClientChannel myChannel = new ClientChannel("myClient", mySocket);
	
			FXMLLoader exam = new FXMLLoader(Util.getResourceAsURL("fx-exam-sheet.fxml"));
			exam.load();
			myChannel.studentID =  txtID.getText() ;
			myChannel.studentName = txtName.getText() ;
			
			((FxExamSheetController)exam.getController()).setChannel(myChannel);
	
			myChannel.start();
			PanRoot.getScene().getWindow().hide();
			
		} catch (NumberFormatException | IOException e) {
			Util.showError(e, e.getMessage());
		}

	}
}
