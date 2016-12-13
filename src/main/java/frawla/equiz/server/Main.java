
package frawla.equiz.server;

import java.io.File;

import frawla.equiz.util.Util;
import javafx.application.Application;
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
			ExamConfig examConfig = new ExamConfig();
			File f = new File("data/IT100-2.xlsx");
			if(f.exists())
				examConfig.getMyController().setExamFile(f);
		}catch(Exception ex){
			Util.showError(ex, ex.getMessage());
		}

	}

}
