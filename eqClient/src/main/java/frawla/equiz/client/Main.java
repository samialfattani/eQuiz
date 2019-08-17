	
package frawla.equiz.client;

import java.io.IOException;

import com.martiansoftware.jsap.JSAPException;

import frawla.equiz.util.Splasher;
import frawla.equiz.util.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

 
public class Main extends Application
{
	public static JSAPClient jsap;
	public static void main (String[] args) throws JSAPException
	{
		jsap = new JSAPClient(args);
		launch(args);
		
	}

	@Override
	public void start(Stage window) 
	{
		try 
		{
			new FXMLLoader(Util.getResourceAsURL("fx-login.fxml")).load();
			Splasher.close();
		}
		catch (IOException e) 
		{ 
			Util.showError(e, e.getMessage());
		}
		
	}

}//end class
