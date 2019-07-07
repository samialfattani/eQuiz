package frawla.equiz.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import frawla.equiz.util.Log;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.Student;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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

	private ExamConfig examConfig;
	private Map<String, byte[]> imageMap = new HashMap<>();
	private ObservableList<Student> Students;
	private List<Log> Logs;
	private File lastDirectory = new File("");

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
			examConfig = ExamLoader.getInstance().getExamConfig();

			for(File f : ExamLoader.getInstance().getImageFiles()) 
			{
				imageMap.put(f.getName(), Util.fileToByteArray(f));	
			}

			Students = ExamLoader.getInstance().getStudentList();
			Logs = ExamLoader.getInstance().getLog();

			lblFile.setText(examFile.getAbsolutePath());
			txtInfo.setText(examConfig.toString() );
			txtInfo.appendText("\n" + ExamLoader.getInstance().getQuestionStatistics());
			txtInfo.appendText("\n\nexamConfig Object has been configured");
		}
		catch (Exception e){
			Util.showError(e, e.getMessage());
		}

	}




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

	public void btnStart_click()
	{
		if (examConfig == null)
			return;
		
		try {
			FXMLLoader loader = new FXMLLoader( Util.getResourceAsURL("fx-monitor.fxml") );
			loader.load();
		
			FxMonitorController monitor = (FxMonitorController) loader.getController();
			monitor.setExamData(examConfig, imageMap,Students, Logs);
	
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
				ExamSheet newSheet = new ExamSheet(); 
				newSheet.setExamConfig(examConfig);
				newSheet.setQustionList( ExamLoader.getInstance().getCloneOfQustionList() ); 
				newSheet.shuffle();
				examSheetList.add(newSheet);
			}
			
			Util.getFileChooserForSavePDF().ifPresent( f -> 
			{
				Exporter exp = new Exporter();
				exp.GeneratePDF(examSheetList, f, false);
			});
		});

	}//end method
	
	
	public void mnutmAbout_click()
	{
		//TODO: complete this
//		Dialog<String> dialog = new Dialog<>();
//		Stage window = (Stage) dialog.getDialogPane().getScene().getWindow();
//		window.getIcons().add(new Image(Util.getResource("images/servericon.png").toString() ));
//		window.setWidth(500);
//		ImageView iv = new ImageView ( Util.getResource("images/server-splash.png").toString());
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

	//*************************** SETTERS AND GETTERS
	public ExamConfig getExamConfig()
	{
		return examConfig;
	}





}//end class
