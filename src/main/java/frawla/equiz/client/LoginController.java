package frawla.equiz.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import frawla.equiz.util.Channel;
import frawla.equiz.util.Util;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


class Login
{
	private FXMLLoader fxmlLoader;
	private LoginController myController;

	public Login(){		
		try
		{
			fxmlLoader = new FXMLLoader(Util.getResource("login.fxml").toURL());			

			AnchorPane root = (AnchorPane) fxmlLoader.load();
			myController = (LoginController) fxmlLoader.getController();

			Stage window = new Stage( );
			window.setScene(new Scene(root, 400, 280));
			window.getIcons().add(new Image(Util.getResource("images/aplus.png").toString() ));
			window.setTitle("eQuiz-CLIENT");
			window.setOnCloseRequest(event -> System.exit(0) );
			window.show();
		}
		catch (IOException e){
			Util.showError(e, e.getMessage());
		}            
	}

	public LoginController getMyController(){
		return myController;
	}
}

public class LoginController implements Initializable
{
	@FXML private TextField txtIP;
	@FXML private TextField txtPort;
	@FXML private TextField txtID;
	@FXML private TextField txtName;
	@FXML private AnchorPane pnlRoot;

	private Socket mySocket;	

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		txtIP.setText("localhost");
		txtPort.setText("10000");
		//txtID.setText("NBE"  +(new Random().nextInt(999-100+1)+100));
		txtID.setText("");
		txtName.setText("");


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

		try{
			mySocket = new Socket(txtIP.getText(), Integer.parseInt(txtPort.getText()));
			Channel srvrLinker = new Channel("myClient", mySocket);

			ExamSheet exam = new ExamSheet();
			exam.getMyController().setChannel(srvrLinker);
			exam.getMyController().studentID = txtID.getText();
			exam.getMyController().studentName =  txtName.getText();
			srvrLinker.start();
			pnlRoot.getScene().getWindow().hide();

		}
		catch (NumberFormatException | IOException e){
			Util.showError(e, e.getMessage());
		}
	}
}
