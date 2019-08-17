package frawla.equiz.util;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class FxAboutController implements Initializable
{
	@FXML private Pane PanRoot;
	@FXML private ImageView img;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		//to be executed after initialize()
		Platform.runLater(() -> {

			Scene scene = new Scene(PanRoot, PanRoot.getPrefWidth(), PanRoot.getPrefHeight());
			Stage window = new Stage( );

			window.setScene(scene);
			window.getIcons().add(new Image(Util.getResourceAsURI("images/about.png").toString() ));			
			window.setTitle("eQuiz - About");
			window.setOnCloseRequest( (w) -> { } );			
			
			window.setWidth(  img.getFitWidth() );
			window.setHeight( img.getFitHeight() +50);
			window.setResizable(false);
			window.show();
		} );
		
		//int size = 26 ;
		img.setImage( new Image(  Util.getResourceAsStream( "images/splash.jpg" ) )  ); 
		img.setFitHeight( 250 ); 
		img.setFitWidth( 600 ) ;
	}//--------------- inilizse -------

}//end class