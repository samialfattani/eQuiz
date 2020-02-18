
package frawla.equiz.server.gui;

import java.io.File;

import frawla.equiz.util.Splasher;
import frawla.equiz.util.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class Main extends Application
{
	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage window) throws Exception
	{
		try{
			
			FXMLLoader loader = new FXMLLoader( Util.getResourceAsURL("fx-exam-config.fxml") );
			loader.load();
			Splasher.close();
			
			//File f = new File("../data/MidTerm-1/MidTerm-2019.xlsx");
			File f = new File("../data/MidTerm-1/SE260-MidTerm-12.xlsx");
			
			if(f.exists()) {
				FxExamConfigController cntrl = (FxExamConfigController) loader.getController();
				cntrl.setExamFile(f);
			}
			
		}catch(Exception ex){
			Util.showError(ex, ex.getMessage());
		}

	}

}//end class
