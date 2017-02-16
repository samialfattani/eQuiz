package frawla.equiz.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamLoader;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.Exporter;
import frawla.equiz.util.exam.Student;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

class FxExamConfig
{
	private FXMLLoader fxmlLoader;
	private FxExamConfigController myController;

	public FxExamConfig()
	{		
		try
		{
			fxmlLoader = new FXMLLoader( Util.getResource("fx-exam-config.fxml").toURL() );			

			Parent root = (Parent) fxmlLoader.load();
			myController = (FxExamConfigController) fxmlLoader.getController();

			Stage window = new Stage( );
			window.setScene(new Scene(root, 700, 450));
			window.getIcons().add(new Image(Util.getResource("images/servericon.png").toString() ));
			window.setTitle("eQuiz-SERVER");
			window.setOnCloseRequest(event -> System.exit(0));
			window.show();
		}
		catch (IOException e){
			Util.showError(e, e.getMessage());
		}            

	}

	public FxExamConfigController getMyController(){
		return myController;
	}

}


public class FxExamConfigController
{
	@FXML private Label lblFile; 
	@FXML private TextArea txtInfo;


	//private static AnchorPane root ;
	private ExamConfig examConfig;
	private Map<String, byte[]> imageMap = new HashMap<>();
	ObservableList<Student> Students;

	public void setExamFile(File examFile) 
	{
		try
		{
			txtInfo.setText("");
			ExamLoader.getInstance().load(examFile);
			examConfig = ExamLoader.getInstance().getExamConfig();

			for(File f : ExamLoader.getInstance().getImageFiles())
			{
				imageMap.put(f.getName(), Util.fileToByteArray(f));	
			}

			Students = ExamLoader.getInstance().getStudentList();

			lblFile.setText(examFile.getAbsolutePath());
			txtInfo.setText(examConfig.toString() );
			txtInfo.appendText("\n" + ExamLoader.getInstance().getQuestionStatistics());
			txtInfo.appendText("\n\nexamConfig Object has been configured");
		}
		catch (EncryptedDocumentException | InvalidFormatException | IOException e){
			Util.showError(e, e.getMessage());
		}

	}




	public void mnutmOpen_click() 
	{
		Util.getFileChooserForOpen()
		.filter( f -> f.exists() )
		.ifPresent( f -> setExamFile(f) );
	}

	public void mnutmNew_click()
	{
		Util.getFileChooserForSaveExcel()
		.filter( f -> !f.exists() )
		.ifPresent( f -> {
			Util.copyFile(new File(Util.getResource("Template.xlsx")),  f);
			Util.RunApplication(f);
		});

	}

	public void btnStart_click()
	{
		Optional.ofNullable(examConfig).ifPresent( examConf ->
		{
			FxMonitor monitor = new FxMonitor();
			monitor.getMyController().setExamConfig(examConf);
			monitor.getMyController().setImageList(imageMap);
			monitor.getMyController().setStudentList(Students);

			//hide this current window
			txtInfo.getScene().getWindow().hide();

		});

	}

	public void mnutmSheetGenerator_click()
	{
		TextInputDialog dialog = new TextInputDialog("3");
		Stage window = (Stage) dialog.getDialogPane().getScene().getWindow();
		window.getIcons().add(new Image(Util.getResource("images/servericon.png").toString() ));
		window.setWidth(500);
		ImageView iv = new ImageView ( Util.getResource("images/sheets.png").toString());
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
				Exporter exp = new Exporter(examSheetList);
				exp.exportToPDF(f, false);
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
