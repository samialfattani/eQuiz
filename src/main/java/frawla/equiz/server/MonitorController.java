package frawla.equiz.server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.rits.cloning.Cloner;

import frawla.equiz.util.Channel;
import frawla.equiz.util.Heap;
import frawla.equiz.util.Message;
import frawla.equiz.util.ServerListener;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.Exam;
import frawla.equiz.util.exam.RegisterInfo;
import frawla.equiz.util.exam.Student;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

class Monitor
{
	private FXMLLoader fxmlLoader;
	private MonitorController myController;

	public Monitor()
	{		
		try
		{
			fxmlLoader = new FXMLLoader(Util.getResource("monitor.fxml").toURL());			
			
			Parent root = (Parent) fxmlLoader.load();
			myController = (MonitorController) fxmlLoader.getController();
			
			Stage window = new Stage( );
			window.setScene(new Scene(root, 720, 500));
			window.setMinWidth(720);
			window.setMinHeight(500);			
			window.getIcons().add(new Image(Util.getResource("images/servericon.png").toString() ));
			window.setTitle("Exam Monitor");
			window.setOnCloseRequest(event -> {
				myController.disconnectServer();
				System.exit(0);
			});
			window.show();
			
			//--------------------------------
			myController.startLisening();
			//--------------------------------
		}
		catch (IOException e){
			Util.showError(e, e.getMessage());
		}            
		
	}
	public MonitorController getMyController()
	{
		return myController;
	}

}


public class MonitorController implements Initializable
{
	
	@FXML private AnchorPane pnlRoot ;
	@FXML private BorderPane pnlTable;
	@FXML private Label lblIP;
	@FXML private Label lblHost;
	@FXML private Label lblPort;
	@FXML private Label lblStatus;
	@FXML private TextArea txtLog;
	@FXML private Button btnChangePort;
	@FXML private ContextMenu mnuStudents;
	@FXML private MenuItem mntmDelete;
	@FXML private MenuItem mntmRefresh;
	@FXML private Label lblTotalHeap;
	@FXML private ProgressBar prgHeap;
	@FXML private ToggleButton btnAutoBackup;
	
	private ExamTableView examTable;
	private ServerListener listenter;

	public ObservableList<Student> Students = FXCollections.observableArrayList();
	public List<Channel> Connections = new ArrayList<>();
	private boolean examIsRunning =false;
	private Workbook wrkBook;
	private Exam examConfig;
	private Map<String, byte[]> imageList = new HashMap<>();
	private Timeline backupTimer;
	private Object backupLock = new Object();

	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		//------ INITIALIZATION -----------A
		lblPort.setText("10000");
		examTable = new ExamTableView();
		pnlTable.setCenter(examTable);
		
		//TODO: Later
		//Students = examConfig.getStudentList();
		 
		
		examTable.setContextMenu(mnuStudents);
		examTable.setStudentList(Students);
		examTable.setDisable(false);
		examTable.setOnMouseClicked( e -> {
			examTable.requestFocus();
		});
		
		
		
		
		//never put less than 5 min. it will lead to very low performance and 
		//possibly "Out Of Memory Exception".
		backupTimer = new Timeline(new KeyFrame(Duration.seconds(300), e -> {
	    	try{
	    		StartTakingBackupForAll();	
	    	}
	    	catch(InterruptedException | ExecutionException ex){
	    		Util.showError(ex, ex.getMessage());
	    	}
		}));
		backupTimer.setCycleCount(Timeline.INDEFINITE);
		
		//btnAutoBackup.seton
		
	}

	public void btnAutoBackup_click()
	{
		if(btnAutoBackup.isSelected()){
			backupTimer.play();
		}else{
			backupTimer.stop();
		}
			
	}
	
	public void startLisening() throws NumberFormatException, IOException
	{
		listenter = new ServerListener("listenter", Integer.parseInt(lblPort.getText()));
		lblPort.setText(listenter.getLocalPort());
		listenter.setOnNewMessage( (msg, ch) -> 
		{					
			//any change of GUI must be in Application thread(not normal thread);
			Platform.runLater(()-> {
				try{
					newMessageHasBeenReleased(msg, ch) ;
				}
				catch (Exception e)
				{Util.showError(e, e.getMessage());}	
			});
		});
		listenter.start();
		lblStatus.setText("listening...\n");
	}

	protected void StartTakingBackupForAll() throws InterruptedException, ExecutionException 
	{
		log("Start Taking Backup for All Students...");
    	//TODO: remove this
    	//System.out.println("Start Taking Backup for All Students...");

		Runnable task = () -> 
		{
			//Integer backupCount = new Integer(0);
        	synchronized(backupLock)
    		{
        		for (int i=0; i< Students.size(); i++)
    			{
        			Student st = Students.get(i);
    				if(st.isSTARTED() || st.isRESUMED()){
    			    	Message<Exam> msg = new Message<>(Message.GIVE_ME_A_BACKUP_NOW);
    			    	st.getServerLinker().sendMessage(msg);
    			    	
    			    	try{ backupLock.wait(1000); } catch (InterruptedException e){ Util.showError(e, e.getMessage()); }
    			    	//backupCount++ ;
    				}
    			}
    		}//synchronized
			//return backupCount;
		};
		

		ExecutorService  exec = Executors.newSingleThreadExecutor();
		exec.execute(task);
		//log("Backup has been taken for " + i + " Clients.");
		//System.out.println("Backup has been taken for " + i + " Clients.");
	}
	
	public void btnRunExam_click()
	{
		btnChangePort.setDisable(true);
		log("Sending...");
		Students
		.parallelStream()
		.filter(st -> st.isConnected())
		.filter(st -> st.isREADY() || st.isRESUMED() )
		.forEach(st -> { 
			sendExam(st );
		});
		
		//TODO: comment this for Test.
		backupTimer.play();
		btnAutoBackup.setSelected(true);
		log("Exam Sheets are sent to 'Ready' and 'Resume' Students");
		examIsRunning = true;
	}
	
	public void btnFinish_click() 
	{
		log("Finishing...");
		backupTimer.stop();
		
		Students
		.stream()
		.filter( st -> st.isSTARTED() || st.isRESUMED())
		.forEach(st ->{
			st.getServerLinker()
			  .sendMessage( new Message<>(Message.KHALAS_TIMES_UP) );
		});

		examIsRunning = false;
		log("make sure that all students has been finished");
	}
	
	public void btnRecordOnExcel_click() 
	{
		if(Students
				.stream()
				.allMatch( s -> !s.isREADY() && !s.isRESUMED() )
		){	
			RecordOnExcel();
			log("Finished and Recorded !");
		}else{
			new Alert(AlertType.ERROR, "Some of Students didn't Finished yet.").showAndWait();
		}
		
	}
	
	private void RecordOnExcel() 
	{
		try(
			FileOutputStream fout = new FileOutputStream( examConfig.SourceFile );
		){
		
			Students.sort( (s1, s2) -> s1.getId().compareTo(s2.getId()) );
			int quesCount = examConfig.getQustionList().size() ;
			ExcelRecorder.RecordAnswers(wrkBook, Students, quesCount);
			ExcelRecorder.RecordTimer(wrkBook, Students, quesCount);
			HSSFFormulaEvaluator.evaluateAllFormulaCells(wrkBook);
			 
	        wrkBook.write( fout );
	        fout.flush();
	        fout.close();
		}catch(IOException e){
			Util.showError(e, e.getMessage());
		}
        
	}//RecordOnExcel

	public void disconnectServer() 
	{
		try{
			wrkBook.close();
			listenter.interrupt();
		}
		catch (IOException e){ 
			Util.showError(e, e.getMessage()); 
		}
	}


	private void newMessageHasBeenReleased(final Message<?> msg, final Channel chnl) throws InterruptedException
	{
		Student student;
		
		String code = msg.getCode();
		String stringData;
		Exam examData;
		
		switch(code)		
		{
			case Message.SERVER_LOG:
				stringData = (String) msg.getData();
				log(stringData);
			break;
			case Message.SERVER_CLOSED:
				stringData = (String) msg.getData();
				lblStatus.setText("Closed");
				log(stringData);
			break;
			
			case Message.SERVER_IP:
				stringData = (String) msg.getData();
				lblIP.setText(stringData);
			break;
			case Message.SERVER_HOST_NAME:
				stringData = (String) msg.getData();
				lblHost.setText(stringData);
			break;
			case Message.NEW_CLIENT_HAS_BEEN_CONNECTED:
				Connections.add( chnl );
				log("New Client has been joined, socketID = " + chnl.getSocketID());
			break;
			case Message.REGESTER_INFO:
				
				RegisterInfo r = (RegisterInfo)msg.getData();
				
				//before you try to link with exsisted Student
				Student st = findStudentOrAddNewOne(r.ID);

				//student is already added before.
				if(st.isConnected()){
					Message<String> m;
					m = new Message<>(Message.YOU_ARE_ALREADY_CONNECTED);
					chnl.sendMessage( m );
					Connections.remove(chnl);			
					break;
				}
				
				st.setServerLinker(chnl);
				st.setId( r.ID );
				st.setName( r.Name );
				
				if(!isValidStudent(st)){
					st.setStatus(Student.REJECTED);
					st.getServerLinker().sendMessage(
						new Message<>(Message.YOU_ARE_REJECTED));
					break;
				}
				
				if (st.isFINISHED()){
					st.getServerLinker().sendMessage( 
							new Message<>(Message.YOU_HAVE_ALREADY_FINISHED) );
					break;
				}

				//student is valid, connected, and not finished
				if(examIsRunning){
					sendExam(st); //resume or late
				}else{
					//new or reconnect before starting exam.
					st.setStatus(Student.READY);  
				}
				
				
			break;			
			case Message.STUDENT_CUTOFF:
				student = findStudent(chnl);
				if(student == null)
					break;
				student.cutOffNow();
				log("CuttOff happend with "+ student.getName() );
			break;			
			case  Message.PLAIN_TEXT_FROM_CLIENT:
				stringData = (String) msg.getData();
				student = findStudent(chnl);
				log(  student.getName() + ": " + stringData );
			break;
			case  Message.EXAM_OBJECT_RECIVED_SUCCESSFYLLY:
				chnl.sendMessage(new Message<Map<String, byte[]>>(Message.IMAGES_LIST, imageList ));
			break;
			case  Message.IMAGE_LIST_RECIVED_SUCCESSFYLLY:
				student = findStudent(chnl);
				student.runExam(examConfig.examTime);
				Message<Duration> m = new Message<>(Message.TIME_LEFT, student.getTimeLeft() );
				chnl.sendMessage(m);
			break;
			case  Message.BACKUP_COPY_OF_EXAM_WITH_ANSWERS:
				synchronized(backupLock)
				{
					examData = (Exam) msg.getData();
					student = findStudent(chnl);
					student.setExamSheet(examData);
					student.setLastUpdate(new Date());
					//TODO: remove this
					//System.out.println("copy-" + student.getId());
					log("copy-" + student.getId()); 
					backupLock.notify();
				}
			break;
			case  Message.FINAL_COPY_OF_EXAM_WITH_ANSWERS:
				examData = (Exam) msg.getData();
				student = findStudent(chnl);
				student.setExamSheet(examData);
				log("Final Copy has been taken from "+ student.getName() );
				chnl.sendMessage(new Message<>(Message.FINAL_COPY_IS_RECIEVED));

				//TODO: remove this
//				synchronized (Students)
//				{
//					//if All student has finised
//					if( Students.stream()
//								.allMatch( s -> !s.isREADY() && !s.isRESUMED() ) )
//						Students.notify();						
//				}
			break;
			case  Message.I_HAVE_FINISHED:
				student = findStudent(chnl);
				student.setStatus( Student.FINISHED );
				log("Finished -> "+ student.getName() );
			break;
			
			//*************************
			default:
				log( "##" + msg + "##");
			break;
		}
		examTable.refresh();
	}

	private void log(String str)
	{
		String fDate = Util.MY_DATE_FORMAT.format(new Date());
		txtLog.appendText(  fDate + ": " + str + "\n");	
	}

	private void sendExam(Student student)
	{
		Exam newSheet = new Cloner().deepClone(examConfig);
		Exam.shuffleExam(newSheet);
		
		
		Exam sheet = student.getOptionalExamSheet().orElse(newSheet);
		Message<Exam> m = new Message<Exam>(Message.EXAM_OBJECT, sheet );
		student.getServerLinker().sendMessage( m );
	}

	private boolean isValidStudent(Student st)
	{
		return examConfig.isValidStudent(st.getId());
	}
	
	private Student findStudentOrAddNewOne(final String id)
	{
		return
		Students.stream()
				.filter( st -> st.getId().equals(id))
				.findFirst()
				.orElseGet( () -> {
					Student s = new Student(id);
					Students.add(s);
					return s;
				});
	}
	
	private Student findStudent(final Channel ch)
	{
		return Students.stream()
					   .filter(st -> (st.getServerLinker() == ch) )
					   .findFirst()
					   .get();
	}
	
	public void mntmRemoveRejected_click(){
		Students.stream()
				.filter(st -> st.isREJECTED() )
				.forEach(st -> {
					st.getServerLinker().interrupt();
					Students.remove(st);
					Connections.remove(st.getServerLinker());
				});
	}

	public void setImageList(Map<String,byte[]> imgLst){
		this.imageList = imgLst;
	}
	
	public void setExamConfig(Exam examConfig) 
	{
		this.examConfig = examConfig;
		//FileInputStream fin;
		try(FileInputStream fin = new FileInputStream(examConfig.SourceFile);)
		{
			wrkBook = WorkbookFactory.create( fin );
		}
		catch (EncryptedDocumentException | InvalidFormatException | IOException e)
		{
			Util.showError(e, e.getMessage());
		}
	}
	
	public void btnChangePort_click()
	{
		TextInputDialog dialog = new TextInputDialog("10000");
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Util.getResource("images/servericon.png").toString() ));
		ImageView iv = new ImageView ( Util.getResource("images/port1.png").toString());
		iv.setFitWidth(70); iv.setFitHeight(70);
		dialog.setGraphic( iv );		
		dialog.setTitle("Change port");		
		dialog.setHeaderText(null);
		dialog.setContentText("Please enter a new Port number:");

		Optional<String> userInput = dialog.showAndWait();
		userInput.ifPresent(port -> 
		{
			try{
				if(listenter.isAlive())
					listenter.interrupt();
				
				lblPort.setText(port);
				startLisening();
			}
			catch (Exception e){ Util.showError(e, e.getMessage()); }
		});
		
	}
	
	public void mntmRunExam_click(){
		btnRunExam_click();
	}
	public void mntmBackup_click()
	{
		try{
			StartTakingBackupForAll();
		}
		catch (InterruptedException | ExecutionException e)
		{ Util.showError(e, e.getMessage()); }
	}
	public void mntmFinish_click(){
		btnFinish_click();
	}
	
	public void mntmRecordOnExcel_click(){
		btnRecordOnExcel_click();
	}

	public void mntmDelete_click(){		
		int i = examTable.getSelectionModel().getSelectedIndex();
		if(Students.get(i).isConnected())
			Students.get(i).getServerLinker().interrupt();
		Students.remove(i);
	}
				
	public void mntmRefresh_click(){
		refreshHeap();
		examTable.refresh();
	}

	private void refreshHeap()
	{
		lblTotalHeap.setText(  String.format("%s / %s", 
				Heap.ReadableByte( Heap.getUsedSize())  ,
				Heap.ReadableByte( Heap.getTotalSize()) ));
		
		double prog = 	Double.parseDouble(Heap.getUsedSize()+"") / 
						Double.parseDouble(Heap.getTotalSize()+""); 
		prgHeap.setProgress( prog );
		
	}

	public void btnGC_click()
	{
		System.gc();
	}
}
