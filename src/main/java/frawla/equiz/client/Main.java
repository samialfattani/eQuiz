
package frawla.equiz.client;
import com.martiansoftware.jsap.JSAPException;

import javafx.application.Application;
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
		new FxLogin();
	}

}
