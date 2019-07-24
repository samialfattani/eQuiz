package frawla.equiz.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamSheet;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class FxExamConfigController implements Initializable
{
	@FXML private Label lblFile; 
	@FXML private TextArea txtInfo;
	@FXML private Pane PanRoot;

	private File lastDirectory = new File(".");

	@Override
	public void initialize(URL location, ResourceBundle resources) 
	{
		//to be executed after initialize()
		Platform.runLater(() -> {
			Stage window = new Stage( );
			Scene scene = new Scene(PanRoot, PanRoot.getPrefWidth(), PanRoot.getPrefHeight());
			window.setScene(scene);

			window.getIcons().add(new Image(Util.getResourceAsURL("images/servericon.png").toString() ));
			window.setTitle("eQuiz-SERVER");
			window.setOnCloseRequest(event -> System.exit(0) );
			window.show();
		});        
		
	}//initialize
	
	public void setExamFile(File examFile) 
	{
		try
		{
			lastDirectory = new File(examFile.getParent() );
			txtInfo.setText("");

			ExamLoader.getInstance(examFile);
			
			lblFile.setText(examFile.getAbsolutePath());
			txtInfo.setText(ExamLoader.getInstance().getExamConfig().toString() );
			txtInfo.appendText("\n" + ExamLoader.getInstance().getQuestionStatistics());
			txtInfo.appendText("\n\nexamConfig Object has been configured");
		}
		catch (Exception e){
			Util.showError(e, e.getMessage());
		}

	}

	public void btnStart_click()
	{
		ExamConfig examConfig = ExamLoader.getInstance().getExamConfig();
		if (examConfig == null)
			return;
		
		try {
			FXMLLoader loader = new FXMLLoader( Util.getResourceAsURL("fx-monitor.fxml") );
			loader.load();
		
			//get Controller Object.
			//FxMonitorController monitor = (FxMonitorController) loader.getController();
	
			//hide this current window
			txtInfo.getScene().getWindow().hide();
			
		} catch (IOException e) {
			Util.showError(e, e.getMessage());
		}
	}
	
	public void btnSheetGenerator_click()
	{
		TextInputDialog dialog = new TextInputDialog("3");
		Stage window = (Stage) dialog.getDialogPane().getScene().getWindow();
		window.getIcons().add(new Image(Util.getResourceAsURI("images/servericon.png").toString() ));
		window.setWidth(500);
		ImageView iv = new ImageView ( Util.getResourceAsURI("images/sheets.png").toString());
		iv.setFitWidth(70); iv.setFitHeight(70);
		dialog.setGraphic( iv );
		
		dialog.setTitle("Generate Random Sheets as PDF");		
		dialog.setHeaderText("Make different copies where questions are shuffled randomly in each copy.");
		dialog.setContentText("Enter number of copies:");

		Optional<String> userInput = dialog.showAndWait();
		userInput.ifPresent(copies -> 
		{
			List<ExamSheet> examSheetList= new ArrayList<>();
			for (int i = 0; i < Integer.parseInt(copies) ; i++)
			{
				ExamSheet newSheet = ExamLoader.getInstance().generateNewSheet(); 
				examSheetList.add(newSheet);
			}
			
			Util.getFileChooserForSavePDF().ifPresent( f -> 
			{
				Exporter exp = new Exporter();
				exp.GeneratePDF(examSheetList, f, false);
			});
		});

	}//end method
	
	
	public void mnutmOpen_click() 
	{
		
		Util.getFileChooserForOpen(lastDirectory)
		.filter( f -> f.exists() )
		.ifPresent( f -> setExamFile(f) );
	}

	public void mnutmNew_click()
	{
		Util.getFileChooserForSaveExcel()
		.filter( f -> !f.exists() )
		.ifPresent( f -> {
			//getClass().getClassLoader()
			Util.copyFile( Util.getResourceAsFile("Template.xlsx") ,  f);
			Util.RunApplication(f);
		});

	}

	public void mnutmAbout_click()
	{
		//TODO: complete this
//		Dialog<String> dialog = new Dialog<>();
//		Stage window = (Stage) dialog.getDialogPane().getScene().getWindow();
//		Image img = new Image( Util.getResourceAsStream("images/servericon.png"));
//		window.getIcons().add(img);
//		window.setWidth(500);
//		ImageView iv = new ImageView ( img );
//		//iv.setFitWidth(70); iv.setFitHeight(70);
//		dialog.setGraphic( iv );
//		dialog.setTitle("About eQuiz-Server");
//		dialog.setHeaderText("");
//		dialog.show();
	}

	public void mnutmExit_click()
	{
		System.exit(0);
	}

}//end class
