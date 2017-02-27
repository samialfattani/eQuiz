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
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import frawla.equiz.util.Channel;
import frawla.equiz.util.Message;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.BlankField;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.RegisterInfo;
import frawla.equiz.util.exam.TimingType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

class FxExamSheet
{
	private FXMLLoader fxmlLoader;
	private FxExamSheetController myController;

	public FxExamSheet()
	{
		try
		{
			fxmlLoader = new FXMLLoader(Util.getResource("fx-exam-sheet.fxml").toURL());			
			
			AnchorPane root = (AnchorPane) fxmlLoader.load();
			myController = (FxExamSheetController) fxmlLoader.getController();

			Scene scene = new Scene(root, 1000, 700);
			scene.getStylesheets().add(Util.getResource("mystyle.css").toURL().toExternalForm());
			 
			
			Stage window = new Stage( );
			window.setScene(scene);
			window.getIcons().add(new Image(Util.getResource("images/exam.png").toString() ));
			window.setTitle("Exam Sheet");
			window.setOnCloseRequest(event -> myController.disconnect());
			window.setMinWidth(1050);
			window.setMinHeight(770);
			window.show();
		}
		catch (IOException e){
			Util.showError(e, e.getMessage());
		}            
		
	}
	
	public FxExamSheetController getMyController(){
		return myController;
	}

}

public class FxExamSheetController implements Initializable
{
	@FXML private AnchorPane pnlRoot ;
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
	@FXML private WebView webCompletion;
	

	private ExamSheet mySheet;
	public String studentID = ""; 
	public String studentName = "";
	
	private List<RadioButton> Radios ;
	private Channel myChannel;
	private Duration timeLeft;
	private Timeline myTimer;
	private ToggleGroup radioGroup;
	private Question currentQues;
	private Map<String, byte[]> myImageList;
	private boolean ExamFinished = false;
	long startCountingTime = 0;
	long endCountingTime = 0;
	
	public void disconnect()
	{
		Optional.ofNullable(currentQues).ifPresent( q -> setAnswer() );
		myChannel.interrupt();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		lblNext.setGraphic(new ImageView( new Image(  Util.getResourceAsStream( "images/next.png" ) )));
		lblPrev.setGraphic(new ImageView( new Image(  Util.getResourceAsStream( "images/previous.png" ) )));
		
		lblNext.setDisable(true);
		lblPrev.setDisable(true);
		txtBlankField.setVisible(false);
		
		pnlChoices.setVisible(false);
		txtBlankField.setVisible(false);
		btnBackup.setVisible(false);
		btnFinish.setDisable(true);
		
		RadioButton[] dummyArr = {ch1, ch2, ch3, ch4, ch5, ch6 };
		Radios = Arrays.asList(dummyArr);

		radioGroup = new ToggleGroup();
		Radios.forEach(ch -> ch.setToggleGroup(radioGroup) );
		
		imgFigure.setSmooth(false);
		imgFigure.setFitWidth(200);
		imgFigure.setPreserveRatio(true);
		imgFigure.setCache(true);
		
		myTimer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() 
		{
		    @Override
		    public void handle(ActionEvent event) 
		    {
				String str = (mySheet.getExamConfig().timingType == TimingType.EXAM_LEVEL)? "Exam Time: " :"Question Time: ";
				str += Util.formatTime(timeLeft); 
				timeLeft = timeLeft.subtract(Duration.seconds(1) );
				lblTime.setText(str);
				setPrgTime();
		    }

			private void setPrgTime()
			{
				double ratio; 
				if(mySheet.getExamConfig().timingType == TimingType.EXAM_LEVEL)
					ratio = timeLeft.toSeconds() / mySheet.getExamConfig().examTime.toSeconds();
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
				
				if(timeLeft.lessThan(Duration.ZERO)){
					setAnswer();
					loadQuestion(currentQues);
				}
			}
		}));
		
		
	}//--------------- inilizse

	public void setChannel(Channel srvrLinker){
		myChannel = srvrLinker;
		myChannel.setOnNewMessage((msg, ch) -> {			
			//any change of GUI MUST be in Application Thread
			Platform.runLater(  ()-> newMessageHasBeenReleased(msg, ch) );			
		});				
	}
	
	private void runExam(Duration tLeft) throws IOException
	{
		currentQues = mySheet.getCurrentQuestion();		
		btnFinish.setDisable(false);

		if(mySheet.getExamConfig().timingType == TimingType.EXAM_LEVEL)
			timeLeft = tLeft;
		else
			timeLeft = currentQues.getTime().subtract(currentQues.getConsumedTime()) ;
		
		myTimer.setCycleCount(Timeline.INDEFINITE);
		myTimer.play();
		startCountingTime = System.currentTimeMillis();
		loadQuestion( currentQues );

	}
	
	public void lblNext_MouseClicked() throws IOException{
		setAnswer();
		//int currIndex = mySheet.getQuestionList().indexOf(currentQues);		
		currentQues = mySheet.getNextQuestion(); //mySheet.getQuestionList().get(currIndex+1);
		loadQuestion( currentQues );
	}
	
	public void lblPrev_MouseClicked() throws IOException{
		setAnswer();
		//int i = mySheet.getQuestionList().indexOf(currentQues);
		currentQues = mySheet.getPreviousQuestion(); //mySheet.getQuestionList().get(i-1);		
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
		
		//mySheet.currentQuesIdx = mySheet.getCurrentQuestion(); //mySheet.getQuestionList().indexOf(currentQues);
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
		
    	Message<ExamSheet> msg = new Message<>(
    			Message.FINAL_COPY_OF_EXAM_WITH_ANSWERS, mySheet);
    	myChannel.sendMessage(msg);
		
    	ExamFinished  = true;
    	myTimer.stop();
    	setDisableAll(true);
	}
	
	
	
	private void loadQuestion(Question q) 
	{
		int idx = mySheet.getQuestionList().indexOf(q) +1;
		txtQuestion.setText(q.getText());
		lblQuesCounter.setText( idx + "/" + mySheet.getQuestionList().size() );
		imgFigure.setImage(  getImage(q.getImgFileName()) );
		txtQuestion.requestFocus();
		
		
		if(mySheet.getExamConfig().timingType == TimingType.QUESTION_LEVEL)
			timeLeft = q.getTime().subtract(q.getConsumedTime()).add(Duration.seconds(2));

		lblMark.setText( String.format("Marks: %.2f", q.getMark()) );
		int i=0;		
		if(q instanceof MultipleChoice)
		{
			MultipleChoice mc = (MultipleChoice)q;
			
			for(i=0; i<mc.getChoices().size() ; i++){
				String ch = mc.getOrderList().get(i); //A, B,...
				String chText =  mc.getChoices().get(ch); //text of the choice.
				Radios.get(i).setText( chText  );
				Radios.get(i).setVisible(true);
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
		i = mySheet.getQuestionList().indexOf(q);
		lblNext.setDisable(i == mySheet.getQuestionList().size()-1);
		lblPrev.setDisable(i == 0);
		loadAnswer();
		
		if(ExamFinished)
			setDisableAll(true);
		
		else if(timeLeft.lessThan(Duration.ZERO) && mySheet.getExamConfig().timingType == TimingType.QUESTION_LEVEL)
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
			style += "font-size: 16; font-weight: bold; font-family: Consolas; "; // 
			
			Question qq =mySheet.getQuestionList().get(i); 
			if( qq.getStudentAnswer() == null || !qq.getStudentAnswer().equals(""))				
				style += "text-decoration: line-through; ";
				//t1.setUnderline(true);
			
			if(i == idx)
				style += "color: red; ";
				//t1.setFill(Paint.valueOf("red") );
			
			
			t1.setStyle(style);
			t1.setText(i+1 + " ");
			chunks.add(t1);
			content.append("<span style='"+style+"'>"+ (i+1) +"</span> ");
		}
		webEngine.loadContent(content.toString());
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
			
			if(me.getButton().equals(MouseButton.PRIMARY) && me.getClickCount() == 2)
			{
				Desktop dt = Desktop.getDesktop();
				File f = new File(getImageLocalFile(currentQues.getImgFileName()));
				dt.open(f);
			}
		
		}catch(IOException | URISyntaxException e){
			Util.showError(e , e.getMessage());
		}
	}
	
	public void imgFigure_mouseEntered(){
		Image image = new Image( Util.getResource("images/zoom.png").toString() );  //pass in the image path
		pnlRoot.getScene().setCursor(new ImageCursor(image));
	}
	public void imgFigure_mouseExited(){
		pnlRoot.getScene().setCursor(Cursor.DEFAULT);
	}
	
	@SuppressWarnings("unchecked")
	private void newMessageHasBeenReleased(final Message<?> msg, final Channel ch)
	{
		
		String code = msg.getCode();
		try{
		switch(code)
		{
			case Message.WELCOME_FROM_SERVER:	
				imgConnection.setImage( Util.getConnectedImage() );
				lblID.setText(studentID);
				lblName.setText(studentName);
				RegisterInfo r = new RegisterInfo(studentID, studentName);
				myChannel.sendMessage(new Message<RegisterInfo>(Message.REGESTER_INFO , r));
			break;
			case Message.YOU_ARE_ALREADY_CONNECTED: 
				Util.showError("You Are Aready Connected\nYou can't connect twice at the same time.");
				btnFinish.setDisable(true);
				
			break;
			case Message.YOU_ARE_REJECTED:
				Util.showError("You Are Rejected\nYou don't have right to enter this exam. Please inform your exam Administrator.");
				btnFinish.setDisable(true);
			break;
			case Message.STUDENT_CUTOFF:
				imgConnection.setImage( Util.getDisconnectedImage() );
			break;
			case Message.YOU_HAVE_ALREADY_FINISHED: 
				Util.showError("You have Finished the exam, you can't connect again");
				btnFinish.setDisable(true);
			break;
			case Message.EXAM_OBJECT: 
				mySheet = (ExamSheet) msg.getData();
				myChannel.sendMessage(new Message<String>(Message.EXAM_OBJECT_RECIVED_SUCCESSFYLLY));								
			break;
			case Message.IMAGES_LIST: 
				myImageList = (Map<String, byte[]>) msg.getData();
				myChannel.sendMessage(new Message<String>(Message.IMAGE_LIST_RECIVED_SUCCESSFYLLY));								
			break;
			case Message.TIME_LEFT:
				runExam( (Duration) msg.getData() );
			break;
			case Message.GIVE_ME_A_BACKUP_NOW:
				setAnswer();
		    	myChannel.sendMessage(
		    			new Message<>(Message.BACKUP_COPY_OF_EXAM_WITH_ANSWERS, mySheet));	
			break;				
			case Message.KHALAS_TIMES_UP:				
				new Alert(AlertType.INFORMATION, "TIME'S UP	").showAndWait();
				btnFinish_click();
			break;
			case Message.FINAL_COPY_IS_RECIEVED:
				if(ExamFinished){
					Alert alert = new Alert(AlertType.INFORMATION);
					ImageView iv = new ImageView ( Util.getResource("images/ok.png").toString());
					iv.setFitWidth(64); iv.setFitHeight(64);
					alert.setGraphic( iv );
					alert.setTitle("Exam Sheet is Submitted");
					alert.setHeaderText(null);
					alert.setContentText("Your Answer is being recieved Successfully!\nThe link to Server will be Disconnected");
					Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
					stage.getIcons().add(new Image(Util.getResource("images/ok.png").toString() ));					
					alert.showAndWait();

					//TODO: remove this
					//myChannel.sendMessage( 
			    	//		new Message<String>(Message.I_HAVE_FINISHED, ""));
					myChannel.getSocket().close();
					System.exit(0);
				}
			break;

		}
		
		}catch(Exception e){
			Util.showError(e , e.getMessage());
		}
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
			return Util.getResource("images/imagebox.png");
		
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
	

	public void btnBackup_click()
	{
		setAnswer();
    	Message<ExamSheet> msg = new Message<>(
    			Message.BACKUP_COPY_OF_EXAM_WITH_ANSWERS, mySheet);
    	myChannel.sendMessage(msg);		
	}

}//end class
