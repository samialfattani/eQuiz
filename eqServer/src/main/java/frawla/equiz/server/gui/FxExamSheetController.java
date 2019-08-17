package frawla.equiz.server.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.BlankField;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.Question;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class FxExamSheetController implements Initializable
{
	@FXML private Pane PanRoot;
	@FXML private RadioButton ch1;
	@FXML private RadioButton ch2;
	@FXML private RadioButton ch3;
	@FXML private RadioButton ch4;
	@FXML private RadioButton ch5;
	@FXML private RadioButton ch6;
	@FXML private TextArea txtQuestion ;
	@FXML private TextArea txtBlankField ;
	@FXML private ImageView imgConnection;
	@FXML private ImageView imgFigure ;
	@FXML private Label lblNext ;
	@FXML private Label lblPrev ;
	@FXML private Label lblID ;
	@FXML private Label lblName;
	@FXML private Label lblTime;
	@FXML private Label lblQuesCounter;
	@FXML private ProgressBar prgTime;
	@FXML private VBox pnlChoices;
	@FXML private Label lblIVersion;
	@FXML private WebView webCompletion;
	@FXML private TextField txtTeacherNote ;
	@FXML private TextField txtStudentMark;
	@FXML private TextField txtMark;
	@FXML private ToggleButton tglCorrect;
	@FXML private ToggleButton tglHalfCorrect;
	@FXML private ToggleButton tglWrong;
	
	private List<RadioButton> Radios ;
	private ToggleGroup raGrpChoices;
	private ToggleGroup rdGrpCorrect;
	private Question currentQues;
	private ExamSheet mySheet;
	private Map<String, byte[]> myImageList;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		//to be executed after initialize()
		Platform.runLater(() -> {

			Stage window = new Stage( );
			Scene scene = new Scene(PanRoot, PanRoot.getPrefWidth(), PanRoot.getPrefHeight());
			scene.getStylesheets().add(Util.getResourceAsURL("mystyle.css").toExternalForm());

			window.setScene(scene);
			window.getIcons().add(new Image(Util.getResourceAsURI("images/exam.png").toString() ));			
			window.setTitle("Exam Sheet - Correction");
			window.setOnCloseRequest( (w) -> { setCorrection(); });
			window.show();

			lblIVersion.setVisible(false);
			loadQuestion(currentQues);
		});
		
		ImageView img ;
		int size = 26 ;
		img = new ImageView( new Image(  Util.getResourceAsStream( "images/next.png" ) ));
		img.setFitHeight( size ); 
		img.setFitWidth( size ) ;
		lblNext.setGraphic( img );
		img = new ImageView( new Image(  Util.getResourceAsStream( "images/previous.png" ) ));
		img.setFitHeight( size ); 
		img.setFitWidth( size ) ;
		
		lblPrev.setGraphic(img);

		lblNext.setDisable(true);
		lblPrev.setDisable(true);
		txtBlankField.setVisible(false);
		
		pnlChoices.setVisible(false);
		txtBlankField.setVisible(false);
		
		raGrpChoices = new ToggleGroup();
		Radios = Arrays.asList(ch1, ch2, ch3, ch4, ch5, ch6);
		Radios.forEach(ch -> ch.setToggleGroup(raGrpChoices) );
		
		rdGrpCorrect = new ToggleGroup();
		List<ToggleButton> R = Arrays.asList(tglCorrect,  tglWrong, tglHalfCorrect );
		R.forEach(ch -> ch.setToggleGroup(rdGrpCorrect) );
		
		rdGrpCorrect.selectedToggleProperty().addListener((ov, tgl, newTgl) -> 
		{
            if (newTgl == null)
            	rdGrpCorrect.selectToggle(tgl);
            else if (tgl != null && newTgl != null)
            {
            	((ToggleButton)newTgl).setStyle("-fx-border-color: red;");
            	((ToggleButton)tgl).setStyle("-fx-border: green;");
        		if(newTgl == tglCorrect)
        			txtStudentMark.setText( Util.MARK_FORMATTER.format( currentQues.getMark() )  );
        		else if(newTgl == tglWrong)
        			txtStudentMark.setText( "0" );
            }
		});		
		
		imgFigure.setSmooth(false);
		imgFigure.setFitWidth(imgFigure.getFitWidth()-10);
		imgFigure.setPreserveRatio(true);
		imgFigure.setCache(true);
		
		txtBlankField.setEditable(false);
		Radios.forEach(rd -> rd.setDisable(false) );		

		txtBlankField.textProperty().addListener((v, ov, nv) -> {
			if(v.getValue().contains("`")) {
				 ((StringProperty)v).setValue(ov);
				 Util.showError("You can't Enter a Text contains (`) !"); 
			}
		});
		
		size = 24;
		img = new ImageView(new Image(Util.getResourceAsStream("images/correct.png")));
		img.setFitHeight( size ); 
		img.setFitWidth( size ) ;
	    tglCorrect.setGraphic(img);		
		img = new ImageView(new Image(Util.getResourceAsStream("images/wrong.png")));
		img.setFitHeight( size ); 
		img.setFitWidth( size ) ;
		tglWrong.setGraphic(img);		
		img = new ImageView(new Image(Util.getResourceAsStream("images/half-correct.png")));
		img.setFitHeight( size-3 ); 
		img.setFitWidth( size-3 ) ;
		tglHalfCorrect.setGraphic(img);
	    txtStudentMark.disableProperty().bind( Bindings.not(tglHalfCorrect.selectedProperty())  );
		
		
	}//--------------- inilizse -------

	public void setStudentSheet(String id, String name, ExamSheet es, Map<String, byte[]> imglst) 
	{
		lblID.setText(id);
		lblName.setText(name);
		mySheet = es;
		myImageList = imglst;
		currentQues = mySheet.getCurrentQuestion();
		loadQuestion( currentQues );
		
	}
	
	public void lblNext_MouseClicked() {
		setCorrection();
		currentQues = mySheet.getNextQuestion();
		loadQuestion( currentQues );
	}
	
	public void lblPrev_MouseClicked() {
		setCorrection();
		currentQues = mySheet.getPreviousQuestion();
		loadQuestion( currentQues );
	}

	public void setCorrection()
	{
		currentQues.setStudentMark( Double.parseDouble( txtStudentMark.getText()) );
		currentQues.setTeacherNote( txtTeacherNote.getText()   );
	}
	
	private void loadQuestion(Question q) 
	{
		int idx = mySheet.getQuestionList().indexOf(q) +1;
		txtQuestion.setText(q.getText());
		lblQuesCounter.setText( idx + "/" + mySheet.getQuestionList().size() );
		imgFigure.setImage(  getImage(q.getImgFileName()) );
		txtQuestion.requestFocus();
		
		
		int i=0;		
		if(q instanceof MultipleChoice)
		{
			MultipleChoice mc = (MultipleChoice)q;
			pnlChoices.getChildren().clear();
			
			for(i=0; i<mc.getChoices().size() ; i++) {
				String ch = mc.getOrderList().get(i); //A, B,...
				String chText =  mc.getChoices().get(ch); //text of the choice.
				Radios.get(i).setText( chText  );
				Radios.get(i).setVisible(true);
				pnlChoices.getChildren().add( Radios.get(i) );
			}
			
			pnlChoices.setVisible(true);
			txtBlankField.setVisible(false);
			
		}else if( q instanceof BlankField)
		{
			Radios.forEach(rd -> rd.setVisible(false) );

			pnlChoices.setVisible(false);
			txtBlankField.setVisible(true);		
		}
		
		//enable/disable buttons
		i = mySheet.getQuestionList().indexOf(q);
		lblNext.setDisable(i == mySheet.getQuestionList().size()-1);
		lblPrev.setDisable(i == 0);
		loadAnswer();
		
		Radios.stream()
			.filter(rd -> !rd.isSelected())
			.forEach(rd -> rd.setDisable(true) );
	
		updateCompletionList(q);
		updateCorrect(q);
		txtStudentMark.setText( Util.MARK_FORMATTER.format( q.getStudentMark() ) );
		txtMark.setText( Util.MARK_FORMATTER.format( q.getMark() ) );
		txtTeacherNote.setText( q.getTeacherNote() );
		((ToggleButton)rdGrpCorrect.getSelectedToggle()).requestFocus();
		
	}//load question

	private void updateCorrect(Question q) 
	{
		double m = q.getStudentMark(); 
		if ( m == q.getMark() )
			rdGrpCorrect.selectToggle( tglCorrect );
		else if ( m == 0 )
			rdGrpCorrect.selectToggle( tglWrong );
		else
			rdGrpCorrect.selectToggle( tglHalfCorrect );
	}

	private void updateCompletionList(Question q)
	{
	
		int idx = mySheet.getQuestionList().indexOf(q);
		WebEngine webEngine = webCompletion.getEngine();
		
		
		ArrayList<Text> chunks = new ArrayList<>();
		StringBuilder content = new StringBuilder();
		
		for (int i = 0; i < mySheet.getQuestionList().size(); i++)
		{
			//TextBuilder.create().text(word).fill(Paint.valueOf(color)).build();
			Text t1 = new Text();
			String style = "";
			t1.setUnderline(false);
			style += "padding:0px; margin:0px; border:2px;";
			style += "font-size: 16; font-weight: bold; font-family: Consolas; "; // 
			
			Question qq = mySheet.getQuestionList().get(i); 
			if( qq.isAnswered() )				
				style += "color: lightblue;";//"text-decoration: line-through; ";
			
			//current question style
			if(i == idx)
				style += "font-size: 18; color: blue;";
			
			t1.setStyle(style);
			t1.setText(i+1 + " ");
			chunks.add(t1);
			content.append("<span style='"+style+"'>"+ (i+1) +"</span> ");
		}
		//webEngine.loadContent( );

        String s = 
	        "<html>\n" +
	        "<body style='margin: 0; padding: 2px;'>\n" +
	        content.toString() +
	        "</body>\n" +
	        "</html>";
        webEngine.loadContent(s);
	}

	private void loadAnswer()
	{
		if(currentQues.getStudentAnswer().equals("")){
			raGrpChoices.selectToggle(null);
			txtBlankField.setText("");
			return;
		}
		
		if(currentQues instanceof MultipleChoice)
		{
			String ans = ((MultipleChoice) currentQues).getChoices().get( currentQues.getStudentAnswer() );
			
			Radios.stream()
				  .filter(rd -> rd.getText().equals(ans))
				  .findFirst()
				  .ifPresent(rd -> {
					  raGrpChoices.selectToggle(rd);
					  rd.setDisable(false);
				  });
		}
		else if(currentQues instanceof BlankField) {
			txtBlankField.setText(currentQues.getStudentAnswer());
		}
	}
	
	public void tglWrong_click() {
		
	}
	public void tglCorrect_click() {
	}


	public void imgFigure_click(MouseEvent me)
	{
		try{
			if(currentQues == null)
				return;
			
			if(getImageLocalFile(currentQues.getImgFileName()) == null)
				return;
			
			if( isDoubleClick(me) )
			{
				Desktop dt = Desktop.getDesktop();
				File f = new File(getImageLocalFile(currentQues.getImgFileName()));
				dt.open(f);
			}
		
		}catch(IOException | URISyntaxException e){
			Util.showError(e , e.getMessage());
		}
	}

	public boolean isDoubleClick(MouseEvent me) {
		return me.getButton().equals(MouseButton.PRIMARY) && me.getClickCount() == 2;
	}

	private Image getImage(String fileName) 
	{
		Image img=null;
		try
		{
			//File localImgFile = getImageLocalFile(fileName);
			img = new Image( getImageLocalFile(fileName).toString() );
			
		}catch(IOException | URISyntaxException e){
			Util.showError(e, e.getMessage());
		}
		return img;
		
	}
	
	/**
	 * get the image file name locally, if it's not exsists wirte it then get it.
	 * @param fileName
	 * @return the file name that is loacally written
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	private URI getImageLocalFile(String fileName) throws IOException, URISyntaxException
	{
		String fs = File.separator;
		
		
		if(myImageList.get(fileName) == null) //no image in the list			
			return Util.getResourceAsURI("images/imagebox.png");
		
		File myDir = new File(Util.getTempDir() + fs +  "equiz");
		if(!myDir.exists())
			myDir.mkdir();
		
		File imgFile = new File(Util.getTempDir() + fs +  "equiz"+ fs + fileName);		
		if(!imgFile.exists())
		{
			//write image into file for first time
	        FileOutputStream fos = new FileOutputStream(imgFile);
	        byte[] imgData = myImageList.get(fileName);
	        fos.write(imgData, 0, imgData.length);
	        fos.flush();
	        fos.close();						
		}
		
		return imgFile.toURI();
	}

	// --------- Zoom -------------
	public void imgFigure_mouseEntered(){
		Image image = new Image( Util.getResourceAsURI("images/zoom.png").toString() );  //pass in the image path
		PanRoot.getScene().setCursor(new ImageCursor(image));
	}
	public void imgFigure_mouseExited(){
		PanRoot.getScene().setCursor(Cursor.DEFAULT);
	}

}//end class