package frawla.equiz.server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.itextpdf.text.DocumentException;

import frawla.equiz.util.Channel;
import frawla.equiz.util.EQDate;
import frawla.equiz.util.EQuizException;
import frawla.equiz.util.Heap;
import frawla.equiz.util.Log;
import frawla.equiz.util.Message;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.Student;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FxMonitorController implements Initializable
{

	@FXML private Pane PanRoot ;
	@FXML private BorderPane pnlTable;
	@FXML private Label lblHost;
	@FXML private Label lblStatus;
	@FXML private TextArea txtLog;
	@FXML private Button btnChangePort;
	@FXML private Label lblTotalHeap;
	@FXML private Label lblTotalStudents;
	@FXML private Label lblVersion;
	
	@FXML private ProgressBar prgHeap;
	@FXML private ToggleButton btnAutoBackup;
	@FXML private Button btnFinish;
	@FXML private ContextMenu mnuStudents;
	@FXML private MenuItem mntmAutoCorrect;
	@FXML private MenuItem mntmRecordOnExcel;
	@FXML private MenuItem mntmFinish;
	@FXML private MenuItem mntmBackup;
	@FXML private MenuItem mntmExportAllToPDF;
	@FXML private MenuItem mntmUngradeHim;
	@FXML private MenuItem mntmFinishHim;
	@FXML private MenuItem mnitmUnfinishHim;


	private ExamTableView examTable;
	//private ServerListener listenter;

	private BooleanProperty  examIsRunning = new SimpleBooleanProperty(false);
	private Workbook wrkBook;
	private Timeline backupTimer;

	private ServerEngine serverEngine;
	private ObservableList<Student> Students; //= FXCollections.observableArrayList();
	private ExamConfig examConfig;
	private List<Log> Logs = new ArrayList<>();


	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

		//to be executed after initialize()
		Platform.runLater(() -> {
			Stage window = new Stage( );
			Scene scene = new Scene(PanRoot, PanRoot.getPrefWidth(), PanRoot.getPrefHeight());
			scene.getStylesheets().add(Util.getResourceAsURL("mystyle.css").toExternalForm());
			
			window.setScene(scene);

			window.getIcons().add(new Image(Util.getResourceAsURL("images/servericon.png").toString() ));
			window.setTitle("Exam Monitor");
			window.setOnCloseRequest(event -> System.exit(0) );
			window.show();
			window.focusedProperty().addListener((ov, o1, o2) ->{
				mntmRefresh_click();
			});
			
			lblVersion.setVisible(false);

			//--------------------------------
			startLisening(10000);
			//--------------------------------
			mntmRefresh_click();
		});        

		//------ INITIALIZATION -----------A
		examTable = new ExamTableView();
		pnlTable.setCenter(examTable);
		
		btnChangePort.disableProperty().bind( examIsRunning );
		mntmRecordOnExcel.disableProperty().bind( examIsRunning );
		mntmAutoCorrect.disableProperty().bind( examIsRunning );
		
		btnFinish.disableProperty().bind( examIsRunning.not() );
		mntmFinish.disableProperty().bind( examIsRunning.not() );
		mntmBackup.disableProperty().bind( examIsRunning.not() );
		mntmExportAllToPDF.setDisable(false); //bind after loading students
		
		examTable.setContextMenu(mnuStudents);
		examTable.setDisable(false);
		
		
		examTable.setOnMouseClicked( e -> {
			examTable.requestFocus();
			Platform.runLater( () ->{
				handleMouseClicked(e);				
			});
			
		});
		
		examTable.setOnKeyTyped( e -> {
			mntmGrading_click();
		});


		//never put less than 5 min. it will lead to very low performance and 
		//possibly "Out Of Memory Exception".
		backupTimer = new Timeline(new KeyFrame(Duration.minutes(5), e -> {
			mntmBackup_click();
		}));
		backupTimer.setCycleCount(Timeline.INDEFINITE);
	}


	public void btnAutoBackup_click()
	{
		if(btnAutoBackup.isSelected())
			backupTimer.play();
		else
			backupTimer.stop();
	}

	public void startLisening(int port) 
	{
		try {
			serverEngine = new ServerEngine("listenter", port);

			serverEngine.setOnNewMessage( (msg, ch) -> 
			{
				newMessageHasBeenReleased(msg, ch);
			});
		
			serverEngine.setOnAllBackupsAreRecieved( ()->
			{
				log("Done Backup");
				//System.err.println(ObjectSizeFetcher.getObjectSize(  new Date() ));
				//System.err.println(ObjectSizeFetcher.getObjectSize(  Students ));
			});
			serverEngine.start();

			serverEngine.setExamData();
			Students = serverEngine.Students;
			examConfig = serverEngine.examConfig;
			Logs = serverEngine.Logs;
			examTable.setStudentList(Students);
		} 
		catch (NumberFormatException | IOException e) 
		{Util.showError(e, e.getMessage());}
	}


	public void btnDistributeSheet_click()
	{
		log("Sending...");
		serverEngine.sendExamToAll();

		backupTimer.play();
		btnAutoBackup.setSelected(true);

		log("Exam Sheets are sent to 'Ready' and 'Resume' Students");
		examIsRunning.set(true);
	}

	public void mntmAutoCorrect_click() 
	{
		Students.stream()
	        .filter(st -> st.isFINISHED() )
	        .forEach(st -> {
	        	st.correctAndGradeAllQuestions();
	        });
		mntmRefresh_click();
	}

	public void mntmRecordOnExcel_click()
	{
		try {
			if( Students.stream()
					.allMatch( s -> not(s.isREADY() || s.isRESUMED()) ) )
			{
				log("Start Recording...");
				RecordOnExcel();
				log("Finished and Recorded !");
			}else{
				new Alert(AlertType.ERROR, "Some of Students didn't Finished yet.").showAndWait();
			}
			mntmRefresh_click();
		}catch(FileNotFoundException ex) {
			Util.showError(ex.getMessage());
			log("Failed to Record XX");
		}catch(IOException ex) {
			Util.showError(ex, ex.getMessage());
			log("Failed to Record XX");
		}
	}

	private void RecordOnExcel() throws IOException, FileNotFoundException
	{
		FileInputStream fin = new FileInputStream(examConfig.SourceFile);
		wrkBook = WorkbookFactory.create( fin );
		
		Students.sort( (s1, s2) -> s1.getId().compareTo(s2.getId()) );
		int quesCount = ExamLoader.getInstance().getQuestionCount() ;
		
		ExcelRecorder.RecordAnswers(wrkBook, Students, quesCount);
		ExcelRecorder.RecordTimer(wrkBook, Students, quesCount);
		ExcelRecorder.RecordLogs(wrkBook, Logs);
		HSSFFormulaEvaluator.evaluateAllFormulaCells(wrkBook);

		updateAndSaveExcelFile();

	}//RecordOnExcel

	
	private void updateAndSaveExcelFile() throws IOException, FileNotFoundException 
	{
		FileOutputStream excelFOut = new FileOutputStream( examConfig.SourceFile );

		wrkBook.write( excelFOut );
		excelFOut.flush();
		excelFOut.close();
		wrkBook.close();
	}

	public void disconnectServer() 
	{
		serverEngine.interrupt();
	}

	
	protected void StartTakingBackupForAll() throws ExecutionException 
	{
		log("Start Taking Backup for All Students...");
		serverEngine.backupAll();
	}
	
	private void newMessageHasBeenReleased(final Message<?> msg, final Channel chnl)
	{
		//any change of GUI must be in Application thread(not normal thread);
		Platform.runLater(()-> {
			try{
				handleMsgReleased(msg, chnl) ;
			}
			catch (Exception e)
			{Util.showError(e, e.getMessage());}	
		});
	}
	private void handleMsgReleased(final Message<?> msg, final Channel chnl) throws InterruptedException, EQuizException
	{
		Student student;

		String code = msg.getCode();
		String stringData;

		switch(code)		
		{
			case Message.SERVER_IS_INITIALIZED:
				stringData = (String) msg.getData();
				lblStatus.setText("Listening to " + stringData);
				log("Server is Initialized, " + stringData);
				break;
			case Message.SERVER_CLOSED:
				stringData = (String) msg.getData();
				lblStatus.setText("Closed");
				log(stringData);
				break;
			case Message.SERVER_HOST_NAME:
				stringData = (String) msg.getData();
				lblHost.setText(stringData);
				break;
			case Message.NEW_CLIENT_HAS_BEEN_CONNECTED:
				log("New Client has been joined, socketID = " + chnl.getSocketID());
				break;
			case Message.REGESTER_INFO:
				break;			
			case Message.STUDENT_CUTOFF:
				student = serverEngine.findStudent(chnl);
				if(student.isCUTOFF())
					log(student.getName() + " is Cut-Off his exam"  );
				break;			
			case  Message.PLAIN_TEXT_FROM_CLIENT:
				stringData = (String) msg.getData();
				student = serverEngine.findStudent(chnl);
				log(  student.getName() + ": " + stringData );
				break;
			case Message.EXAM_OBJECT_RECIVED_SUCCESSFYLLY:
			case Message.IMAGE_LIST_RECIVED_SUCCESSFYLLY:
				break;
			case Message.BACKUP_COPY_OF_EXAM_WITH_ANSWERS:
				student = serverEngine.findStudent(chnl);
				log(student.getId() + " sent a Copy");
				break;
			case  Message.FINAL_COPY_OF_EXAM_WITH_ANSWERS:
				student = serverEngine.findStudent(chnl);
				log( student.getName() + " -> sent his Final-Copy and Finished");
				break;				
			case Message.STOP_RECIEVING_MESSAGES:
				break;

				//*************************
			default:
				log( "Known msg: ##" + msg.getCode() + "##");
				break;
		}
		mntmRefresh_click();
		
	}

	private void log(String str)
	{
		//must be runLater cause it may called from non-FX-thread
		Platform.runLater(( )->{
			Log log = new Log(new EQDate(), str);
			Logs.add( log );
			txtLog.appendText(  log + "\n" );	
		});
	}



	public void mntmRemoveRejected_click(){
		Students.stream()
		.filter(st -> st.isREJECTED() )
		.forEach(st -> {
			st.getServerLinker().interrupt();
			Students.remove(st);
			//Connections.remove(st.getServerLinker());
		});
		mntmRefresh_click();
	}


	
	public void btnChangePort_click()
	{
		TextInputDialog dialog = new TextInputDialog("10000");
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Util.getResourceAsURI("images/servericon.png").toString() ));
		ImageView iv = new ImageView ( Util.getResourceAsURI("images/port1.png").toString());
		iv.setFitWidth(70); iv.setFitHeight(70);
		dialog.setGraphic( iv );		
		dialog.setTitle("Change port");		
		dialog.setHeaderText(null);
		dialog.setContentText("Please enter a new Port number:");

		Optional<String> userInput = dialog.showAndWait();
		userInput.ifPresent(port -> 
		{
			try{
				if(serverEngine.isAlive()) {
					serverEngine.interrupt();
					log("Server has been intuerrupted");
				}

				startLisening( Integer.parseInt(port)  );
			}
			catch (Exception e){ }//keep it empty
		});

	}

	public void mntmDistributeSheet_click(){
		btnDistributeSheet_click();
	}
	
	public void mntmBackup_click()
	{
		try{
			StartTakingBackupForAll();
		}
		catch (ExecutionException e)
		{ Util.showError(e, e.getMessage()); }
		mntmRefresh_click();
	}

	public void mntmFinish_click()
	{
		log("Finishing...");
		backupTimer.stop();

		serverEngine.finishAllStudents();

		examIsRunning.set(false);
		log("All Students are alerted to Finish now!");
		mntmRefresh_click();
	}


	private void handleMouseClicked(MouseEvent e) 
	{
		//on Double click open Grading...
		if (e.getClickCount() == 2) {
			mntmGrading_click();
			return;
		}
		
		int i = examTable.getSelectionModel().getSelectedIndex();
		Student st = Students.get(i);
		mntmUngradeHim.disableProperty()
              .bind( 
                  st.getStatusProperty().isNotEqualTo( Student.GRADED ) 
               );
		
		mntmFinishHim.disableProperty()
       		  .bind( Bindings.and(
        				st.getStatusProperty().isNotEqualTo(Student.STARTED),
        				st.getStatusProperty().isNotEqualTo(Student.RESUMED)
        			));

		mnitmUnfinishHim.disableProperty()
	        .bind( 
	            st.getStatusProperty().isNotEqualTo( Student.FINISHED ) 
	         );
	}

	public void mntmFinishHim_click()
	{		
		int i = examTable.getSelectionModel().getSelectedIndex();
		if(Students.get(i).isConnected())
			Students.get(i)
			.getServerLinker()
			.sendMessage( new Message<>(Message.KHALAS_TIMES_UP) );

	}
	public void mnitmUnfinishHim_click() {
		int i = examTable.getSelectionModel().getSelectedIndex();
		Student st = Students.get(i);
		st.UnFinishNow();
		mntmRefresh_click();
	}
	public void mntmUngradeHim_click() {
		int i = examTable.getSelectionModel().getSelectedIndex();
		Student st = Students.get(i);
		st.UnGradeNow();
		mntmRefresh_click();
	}
	public void mnitmUnfinishAll_click() {
		Students
			.stream()
			.filter(st -> st.isFINISHED() )
			.forEach(st -> st.UnFinishNow());
		mntmRefresh_click();
	}
	public void mntmUngradeAll_click() {
		Students
			.stream()
			.filter(st -> st.isGRADED() )
			.forEach(st -> st.UnGradeNow() );
		mntmRefresh_click();
	}
	
	
	public void mntmDelete_click(){		
		int i = examTable.getSelectionModel().getSelectedIndex();
		if(Students.get(i).isConnected())
			Students.get(i).getServerLinker().interrupt();
		Students.remove(i);
		mntmRefresh_click();
	}

	public void mntmExportPDF_click()
	{

		int i = examTable.getSelectionModel().getSelectedIndex();
		Exporter exp = new Exporter();
		

		Util.getFileChooserForSavePDF()
		.ifPresent( (f) -> {
			try {
				exp.exportToPDF(Students.get(i), f, true);
				Util.RunApplication(f);
			}catch(Exception e) { 
				Util.showError(e, e.getMessage());
			}
		});
		mntmRefresh_click();
	}

	public void mntmExportAllToPDF_click() throws FileNotFoundException, DocumentException
	{
		//export only when all is graded.
		if( Students.stream().anyMatch(st -> !st.isGRADED() )) {
			Util.showError("Some students didn't graded yet!");
			return;
		}
		
		Exporter exp = new Exporter();

		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select a folder to place All Exported Sheets in PDF files");
		File selectedDirectory = dirChooser.showDialog(examTable.getScene().getWindow());

		if(selectedDirectory != null)
			exp.exportToPDF(Students, selectedDirectory,  true);
		mntmRefresh_click();
	}

	public void mntmExportAllToPDFAndSendEmail_click()
	{
		//mntmExportAllToPDF_click();
		//SendEmail
	}


	public void mntmRefresh_click()
	{
		refreshHeap();
		refreshStudentStatistics();
		examTable.refresh();
		
		String str = String.format("server at %s:%d", serverEngine.getIP(),  serverEngine.getLocalPort( ) );
		
		if(serverEngine.isAlive())
			lblStatus.setText( str );
		else
			lblStatus.setText("Closed");
	}

	private void refreshStudentStatistics()
	{

		StringBuilder str = new StringBuilder("");
		String[] l = {"Started", "Cutoff", "Resumed", "Working Now", "Finished"};

		String[] d = {
				Students.stream().filter(st -> st.isSTARTED() ).count() + "", 
				Students.stream().filter(st -> st.isCUTOFF() ).count() + "", 
				Students.stream().filter(st -> st.isRESUMED() ).count() + "",
				Students.stream().filter(st -> st.isRESUMED() || st.isSTARTED() ).count() + "",
				Students.stream().filter(st -> st.isFINISHED() ).count() + "" 
		};

		List<String> labels = Arrays.asList(l);
		List<String> data = Arrays.asList(d);

		int max = labels
				.stream()
				.mapToInt( String::length ).max().getAsInt();

		for (int i=0; i<d.length; i++)
		{
			str.append( String.format("%-"+max+"s | %s\n", labels.get(i), data.get(i)) );
		}
		Tooltip tTip = new Tooltip(str.toString());
		tTip.setFont(new Font("Consolas", 14.0));
		tTip.setAutoHide(false);
		lblTotalStudents.setTooltip(tTip);
		lblTotalStudents.setText( Students.stream().count() + " Students");

	}

	public void mntmOpenExcelFile_click(){
		Util.RunApplication( examConfig.SourceFile);

	}
	public void btnGC_click(){
		System.gc();
		mntmRefresh_click();
	}

	private void refreshHeap()
	{
		lblTotalHeap.setText(  String.format("%s / %s, %s", 
				Heap.ReadableByte( Heap.getUsedSize())  ,
				Heap.ReadableByte( Heap.getAvailableSize() ), 
				Heap.ReadableByte( Heap.getMaxMemory() )
				));

		double prog = 	Double.parseDouble(Heap.getUsedSize() + "") / 
				Double.parseDouble(Heap.getAvailableSize() + ""); 
		prgHeap.setProgress( prog );

	}
	
	
	public void mntmGrading_click() 
	{
		try {
			FXMLLoader loader = new FXMLLoader( Util.getResourceAsURL("fx-exam-sheet.fxml") );
			loader.load();
		
			//get Controller Object.
			FxExamSheetController fxExamSheet = (FxExamSheetController) loader.getController();
			
			Student st = examTable.getSelectionModel().getSelectedItem();
			fxExamSheet.setStudentSheet( 
				st.getId(),
				st.getName(),
				st.getOptionalExamSheet().get(), 
				serverEngine.getImageList()
			);
			
		} catch (IOException e) {
			Util.showError(e, e.getMessage());
		}
	}
	
	public void mntmTest_click() {
		//TODO: just for test anything
	}
	
	private boolean not(boolean b) {
		return !b;
	}

	public void mnutmAbout_click() throws IOException
	{
		new FXMLLoader( Util.getResourceAsURL("fx-about.fxml") ).load();
	}


}//end MonitorController
