package frawla.equiz.client;

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
import java.util.Optional;
import java.util.ResourceBundle;

import frawla.equiz.util.Channel;
import frawla.equiz.util.EQuizException;
import frawla.equiz.util.Message;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.BlankField;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.TimingType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FxExamSheetController implements Initializable
{
	@FXML private Pane PanRoot;
	@FXML private RadioButton ch1;
	@FXML private RadioButton ch2;
	@FXML private RadioButton ch3;
	@FXML private RadioButton ch4;
	@FXML private RadioButton ch5;
	@FXML private RadioButton ch6;
	@FXML private TextArea txtQuestion;
	@FXML private TextArea txtBlankField;
	@FXML private ImageView imgConnection;
	@FXML private ImageView imgFigure;
	@FXML private Label lblNext;
	@FXML private Label lblPrev;
	@FXML private Label lblID;
	@FXML private Label lblName;
	@FXML private Label lblTime;
	@FXML private Label lblQuesCounter;
	@FXML private Button btnBackup;
	@FXML private ProgressBar prgTime;
	@FXML private VBox pnlChoices;
	@FXML private Button btnFinish;
	@FXML private Label lblMark;
	@FXML private Label lblIVersion;
	@FXML private WebView webCompletion;

	
	private List<RadioButton> Radios ;
	private ClientChannel myChannel;
	private Duration timeLeft;
	private Timeline myTimer;
	private ToggleGroup radioGroup;
	private Question currentQues;
	
	private SimpleBooleanProperty  examIsFinished = new SimpleBooleanProperty(false);
	private long startCountingTime = 0;
	private long endCountingTime = 0;
	
	public void disconnect()
	{
		Optional.ofNullable(currentQues).ifPresent( q -> setAnswer() );
		myChannel.interrupt();
	}

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
			window.setTitle("Exam Sheet");
			window.setOnCloseRequest(event -> disconnect());
			window.show();

			KeyCombination kc;
			kc = new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.CONTROL_DOWN);
			PanRoot.getScene().getAccelerators().put(kc, () -> { lblNext_MouseClicked(); } );
			kc = new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.CONTROL_DOWN);
			PanRoot.getScene().getAccelerators().put(kc, () -> { lblPrev_MouseClicked(); } );

			lblIVersion.setVisible(false);

		});
		
		ImageView img ;
		int size = 26;
		img = new ImageView( new Image(  Util.getResourceAsStream( "images/next.png" ) ));
		img.setFitHeight( size ); 
		img.setFitWidth( size );
		lblNext.setGraphic( img );
		img = new ImageView( new Image(  Util.getResourceAsStream( "images/previous.png" ) ));
		img.setFitHeight( size ); 
		img.setFitWidth( size );
		
		lblPrev.setGraphic(img);
		
		
		lblNext.setDisable(true);
		lblPrev.setDisable(true);
		txtBlankField.setVisible(false);
		
		pnlChoices.setVisible(false);
		txtBlankField.setVisible(false);
		btnBackup.setVisible(false);
		btnFinish.disableProperty().bind( examIsFinished );
		//btnFinish.setDisable(true);
		
		RadioButton[] dummyArr = {ch1, ch2, ch3, ch4, ch5, ch6 };
		Radios = Arrays.asList(dummyArr);

		radioGroup = new ToggleGroup();
		Radios.forEach(ch -> ch.setToggleGroup(radioGroup) );
		
		imgFigure.setSmooth(false);
		imgFigure.setFitWidth(imgFigure.getFitWidth()-10);
		imgFigure.setPreserveRatio(true);
		imgFigure.setCache(true);
		
		
		myTimer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() 
		{
		    @Override
		    public void handle(ActionEvent event) 
		    {
				String str = (myChannel.mySheet.getExamConfig().timingType == TimingType.EXAM_LEVEL)? "Exam Time: " :"Question Time: ";
				str += Util.formatTime(timeLeft); 
				timeLeft = timeLeft.subtract(Duration.seconds(1) );
				lblTime.setText(str);
				setPrgTime();
		    }

			private void setPrgTime()
			{
				double ratio; 
				if(myChannel.mySheet.getExamConfig().timingType == TimingType.EXAM_LEVEL)
					ratio = timeLeft.toSeconds() /myChannel.mySheet.getExamConfig().examTime.toSeconds();
				else
					ratio = timeLeft.toSeconds() / currentQues.getTime().toSeconds();
				
				prgTime.setProgress(ratio);
				if(ratio <= 0.1){
					lblTime.setStyle("-fx-text-fill:red");
					prgTime.getStyleClass().add("red-bar");
				}else{
					lblTime.setStyle("-fx-text-fill:black");
					prgTime.getStyleClass().removeAll("red-bar");					
				}
				
//				if(timeLeft.lessThan(Duration.ZERO)){
//					setAnswer();
//					loadQuestion(currentQues);
//				}
			}
		}));
		
		
		txtBlankField.textProperty().addListener((v, ov, nv) ->{
			if(v.getValue().contains("`")){
				 ((StringProperty)v).setValue(ov);
				 Util.showError("You can't Enter a Text contains (`) !"); 
			}
		});
		
	}//--------------- inilizse -------

	public void setChannel(ClientChannel chnl){
		myChannel = chnl;
		
		myChannel.setOnNewMessage((msg, ch) -> 
		{
			//any change of GUI MUST be in Application Thread
			Platform.runLater(  ()-> newMessageHasDelivered(msg, ch) );			
		});				
	}
	
	private void runExam(Duration tLeft) throws IOException
	{
		currentQues =myChannel.mySheet.getCurrentQuestion();		
		//btnFinish.setDisable(false);

		if(myChannel.mySheet.getExamConfig().timingType == TimingType.EXAM_LEVEL)
			timeLeft = tLeft;
		else
			timeLeft = currentQues.getTime().subtract(currentQues.getConsumedTime()) ;
		
		myTimer.setCycleCount(Timeline.INDEFINITE);
		myTimer.play();
		startCountingTime = System.currentTimeMillis();
		loadQuestion( currentQues );
	}
	
	public void lblNext_MouseClicked() {
		setAnswer();
		currentQues =myChannel.mySheet.getNextQuestion(); 
		loadQuestion( currentQues );
	}
	
	public void lblPrev_MouseClicked() {
		setAnswer();
		currentQues = myChannel.mySheet.getPreviousQuestion(); 		
		loadQuestion( currentQues );
	}

	public void setAnswer()
	{
		if(currentQues instanceof MultipleChoice ) 
		{
			Optional.ofNullable(radioGroup.getSelectedToggle() )
					.ifPresent( rd -> {
			        	int i = Radios.indexOf(rd) ;
			        	MultipleChoice qmc = (MultipleChoice)currentQues;
			        	String key = qmc.getKeyOf(Radios.get(i).getText());
			        	currentQues.setStudentAnswer( key );
					});
		
		}else if(currentQues instanceof BlankField ) {
			currentQues.setStudentAnswer( txtBlankField.getText() );
		}
		endCountingTime = System.currentTimeMillis();
		currentQues.AppendConsumedTime( Duration.millis(endCountingTime - startCountingTime)  );
		startCountingTime = System.currentTimeMillis();
		
		//mySheet.currentQuesIdx =myChannel.mySheet.getCurrentQuestion(); //mySheet.getQuestionList().indexOf(currentQues);
	}
	
	public void btnFinish_click() throws IOException
	{
		setAnswer();
		Optional<ButtonType> res = new Alert(
				AlertType.CONFIRMATION, 
				"By clicking 'OK' your answers will be submitted and you can't modify it any more.\nAre You Sure ?")
				.showAndWait();
		if( res.isPresent() && res.get() == ButtonType.CANCEL) 
			    return;
		
    	examIsFinished.set(true);
    	myTimer.stop();
    	setDisableAll(true);
    	
    	myChannel.sendFinalCopy();
	}
	
	
	
	private void loadQuestion(Question q) 
	{
		int idx =myChannel.mySheet.getQuestionList().indexOf(q) +1;
		txtQuestion.setText(q.getText());
		lblQuesCounter.setText( idx + "/" + myChannel.mySheet.getQuestionList().size() );
		imgFigure.setImage(  getImage(q.getImgFileName()) );
		txtQuestion.requestFocus();
		
		
		if(myChannel.mySheet.getExamConfig().timingType == TimingType.QUESTION_LEVEL)
			timeLeft = q.getTime().subtract(q.getConsumedTime()).add(Duration.seconds(2));

		lblMark.setText( String.format("Marks: %s", Util.MARK_FORMATTER.format( q.getMark())  ) );
		int i=0;		
		if(q instanceof MultipleChoice)
		{
			MultipleChoice mc = (MultipleChoice)q;
			
			pnlChoices.getChildren().clear();
			
			for(i=0; i<mc.getChoices().size() ; i++){
				String ch = mc.getOrderList().get(i); //A, B,...
				String chText =  mc.getChoices().get(ch); //text of the choice.
				Radios.get(i).setText( chText  );
				Radios.get(i).setVisible(true);
				pnlChoices.getChildren().add( Radios.get(i) );
			}
			
			Radios.stream()
				  .filter(rd -> Radios.indexOf(rd) >= mc.getOrderList().size())
				  .forEach(rd -> rd.setVisible(false) );
			
			pnlChoices.setVisible(true);
			txtBlankField.setVisible(false);
			
		}else if( q instanceof BlankField)
		{
			for(i=0; i<Radios.size(); i++){					
				Radios.get(i).setVisible(false);
			}
			pnlChoices.setVisible(false);
			txtBlankField.setVisible(true);		
		}
		
		//enable/disable buttons
		i =myChannel.mySheet.getQuestionList().indexOf(q);
		lblNext.setDisable(i == myChannel.mySheet.getQuestionList().size()-1);
		lblPrev.setDisable(i == 0);
		loadAnswer();
		
		if(examIsFinished.get())
			setDisableAll(true);
		
		else if(timeLeft.lessThan(Duration.ZERO) && myChannel.mySheet.getExamConfig().timingType == TimingType.QUESTION_LEVEL)
			// if the question time is up.
			setDisableAll(true);
		else if(timeLeft.lessThan(Duration.ZERO) && !q.getStudentAnswer().equals(""))
			// if time's up and already answered
			setDisableAll(true);
		else
			setDisableAll(false);
		
		updateCompletionText(q);

	}//load question

	private void updateCompletionText(Question q)
	{
	
		int idx =myChannel.mySheet.getQuestionList().indexOf(q);
		WebEngine webEngine = webCompletion.getEngine();
		
		
		ArrayList<Text> chunks = new ArrayList<>();
		StringBuilder content = new StringBuilder();
		
		for (int i = 0; i < myChannel.mySheet.getQuestionList().size(); i++)
		{
			//TextBuilder.create().text(word).fill(Paint.valueOf(color)).build();
			Text t1 = new Text();
			String style = "";
			t1.setUnderline(false);
			style += "padding:0px; margin:0px; border:2px;";
			style += "font-size: 16; font-weight: bold; font-family: Consolas; "; // 
			
			Question qq = myChannel.mySheet.getQuestionList().get(i); 
			if( qq.isAnswered() )				
				style += "color: lightblue;";//"text-decoration: line-through; ";
			
			//current question style
			if(i == idx)
				style += "font-size: 18; color: blue;";
				//t1.setFill(Paint.valueOf("red") );
			
			
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
		//webCompletion.getChildren().addAll(chunks);
		
	}

	private void loadAnswer()
	{
		if(currentQues.getStudentAnswer().equals("")){
			radioGroup.selectToggle(null);
			txtBlankField.setText("");
			return;
		}
		
		if(currentQues instanceof MultipleChoice)
		{
			String ans = ((MultipleChoice) currentQues).getChoices().get( currentQues.getStudentAnswer() );
			
			Radios.stream()
				  .filter(rd -> rd.getText().equals(ans))
				  .findFirst()
				  .ifPresent(rg -> radioGroup.selectToggle(rg));
		}
		else if(currentQues instanceof BlankField)
		{
			txtBlankField.setText(currentQues.getStudentAnswer());
		}
		
	}

	private void setDisableAll(boolean b)
	{
		txtBlankField.setDisable(b);		
		Radios.forEach(rd -> rd.setDisable(b) );		
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
	
	
	//@SuppressWarnings("unchecked")
	private void newMessageHasDelivered(final Message<?> msg, final Channel ch)
	{
		
		String code = msg.getCode();
		try{
		myChannel.handleTheMsg(msg, ch);		
		switch(code)
		{
			case Message.WELCOME_FROM_SERVER:	
				imgConnection.setImage( Util.getConnectedImage() );
				lblID.setText(myChannel.studentID);
				lblName.setText(myChannel.studentName);
			break;
			case Message.TIME_LEFT:
				runExam( (Duration) msg.getData() );
			break;
			case Message.KHALAS_TIMES_UP:				
				new Alert(AlertType.INFORMATION, "TIME'S UP	").showAndWait();
				examIsFinished.set(true);
			break;
			case Message.FINAL_COPY_IS_RECIEVED:
				Alert alert = new Alert(AlertType.INFORMATION);
				ImageView iv = new ImageView ( Util.getResourceAsURI("images/ok.png").toString());
				iv.setFitWidth(64); iv.setFitHeight(64);
				alert.setGraphic( iv );
				alert.setTitle("Exam Sheet is Submitted");
				alert.setHeaderText(null);
				alert.setContentText("Your Answer is being recieved Successfully!\nThe link to Server will be Disconnected");
				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				stage.getIcons().add(new Image(Util.getResourceAsURI("images/ok.png").toString() ));					
				alert.showAndWait();

				System.exit(0);
			break;
		}
		
		}
		catch(EQuizException e)
		{
			String err = e.getId();
			switch (err) {
			case Message.YOU_ARE_ALREADY_CONNECTED: 
			case Message.YOU_ARE_REJECTED:
			case Message.YOU_HAVE_ALREADY_FINISHED:
				examIsFinished.set(true);
				Util.showError(e.getMessage());
			break;
			case Message.STUDENT_CUTOFF:
				imgConnection.setImage( Util.getDisconnectedImage() );
				if( !examIsFinished.get() )
					Util.showError(e.getMessage());
			break;
			default:
				Util.showError(e.getMessage());
			}
				
		}catch(Exception e){
			Util.showError(e , e.getMessage());
		}
	}//newMessageHasBeenReleased

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
		
		
		if(myChannel.myImageList.get(fileName) == null) //no image in the list			
			return Util.getResourceAsURI("images/imagebox.png");
		
		File myDir = new File(Util.getTempDir() + fs +  "equiz");
		if(!myDir.exists())
			myDir.mkdir();
		
		File imgFile = new File(Util.getTempDir() + fs +  "equiz"+ fs + fileName);		
		if(!imgFile.exists())
		{
			//write image into file for first time
	        FileOutputStream fos = new FileOutputStream(imgFile);
	        byte[] imgData = myChannel.myImageList.get(fileName);
	        fos.write(imgData, 0, imgData.length);
	        fos.flush();
	        fos.close();						
		}
		
		return imgFile.toURI();
	}
	

	public void btnBackup_click()
	{
		setAnswer();
    	Message<ExamSheet> msg = new Message<>(
    			Message.BACKUP_COPY_OF_EXAM_WITH_ANSWERS, myChannel.mySheet);
    	myChannel.sendMessage(msg);		
	}

	// --------- Zoom -------------
	public void imgFigure_mouseEntered(){
		Image image = new Image( Util.getResourceAsURI("images/zoom.png").toString() );  //pass in the image path
		PanRoot.getScene().setCursor(new ImageCursor(image));
	}
	public void imgFigure_mouseExited(){
		PanRoot.getScene().setCursor(Cursor.DEFAULT);
	}
	
	public void mnitmAbout_Clicked() throws IOException {
		new FXMLLoader( Util.getResourceAsURL("fx-about.fxml") ).load();
	}
	

}//end class
