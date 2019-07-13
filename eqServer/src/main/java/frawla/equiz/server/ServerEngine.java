package frawla.equiz.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import frawla.equiz.util.Channel;
import frawla.equiz.util.EQDate;
import frawla.equiz.util.EQuizException;
import frawla.equiz.util.Log;
import frawla.equiz.util.Message;
import frawla.equiz.util.Receivable;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.RegisterInfo;
import frawla.equiz.util.exam.Student;
import javafx.collections.ObservableList;

public class ServerEngine extends ServerListener 
{
	public ObservableList<Student> Students; //= FXCollections.observableArrayList();
	public ExamConfig examConfig;
	public Map<String, byte[]> imageList = new HashMap<>();
	public List<Log> Logs = new ArrayList<>();
	
	private Object backupLock = new Object();
	private Routine OnAllBackupsAreRecieved;

	public ServerEngine(String threadName, int port) throws IOException 
	{
		super(threadName, port);
	}

	@Override
	public void setOnNewMessage(Receivable nml) 
	{
		super.setOnNewMessage((msg, ch) -> 
		{
			try {
				handelTheMessage(msg, ch);
				nml.MessageReleased(msg, ch);
			}catch(EQuizException e) {
				e.printStackTrace();
			}
		});
	}

	private void handelTheMessage(final Message<?> msg, final Channel chnl) throws EQuizException  
	{
		Student student;
		String code = msg.getCode();
		ExamSheet examData = null;

		switch(code)		
		{
			case Message.SERVER_IS_INITIALIZED:
			case Message.SERVER_CLOSED:
			case Message.SERVER_HOST_NAME:
			case Message.NEW_CLIENT_HAS_BEEN_CONNECTED:
			break;
			case Message.STOP_RECIEVING_MESSAGES:
				chnl.interrupt();
			break;
	 		case Message.REGESTER_INFO:
				RegisterInfo r = (RegisterInfo)msg.getData();

				//before you try to link with existed Student
				Student st = findStudentOrAddNewOne(r.ID);

				//student is already connected before.
				if(st.isConnected()){
					chnl.sendMessage( 
							new Message<>(Message.YOU_ARE_ALREADY_CONNECTED) );
					break;
				}

				st.setServerLinker(chnl);
				st.setId( r.ID );
				st.setName( r.Name );
				
				if(  not(isValidStudent(st))  )
					st.setStatus(Student.REJECTED);
				
				
				takeActionAfterConnect(st);

				break;			
			case Message.STUDENT_CUTOFF:
				student = findStudent(chnl);
				if(student == null)
					break;
				
				student.cutOffNow();
				break;			
			case  Message.PLAIN_TEXT_FROM_CLIENT:
			break;
			case  Message.EXAM_OBJECT_RECIVED_SUCCESSFYLLY:
				chnl.sendMessage(
						new Message<Map<String, byte[]>>(Message.IMAGES_LIST, imageList ));
				break;
			case Message.IMAGE_LIST_RECIVED_SUCCESSFYLLY:
				student = findStudent(chnl);
				student.runExam(examConfig.examTime);
				chnl.sendMessage(
						new Message<>(Message.TIME_LEFT, student.getLeftTime() ));
				break;
			case Message.BACKUP_COPY_OF_EXAM_WITH_ANSWERS:
				synchronized(backupLock)
				{
					examData = (ExamSheet) msg.getData(  );
					student = findStudent(chnl);        
					student.setExamSheet(examData);
					student.setLastUpdate(new EQDate());  
					backupLock.notify();                
				}
				break;
			case  Message.FINAL_COPY_OF_EXAM_WITH_ANSWERS:
				examData = (ExamSheet) msg.getData();
				student = findStudent(chnl);
				student.setExamSheet(examData);
				
				student.getServerLinker().sendMessage(
						new Message<>(Message.FINAL_COPY_IS_RECIEVED));

				student = findStudent(chnl);
				student.finishNow();
				break;

				//*************************
			default:
				System.err.println("Unknown Message");
		}
		
	}//end handler

	private void takeActionAfterConnect(Student st) 
	{
		switch(st.getStatus()) 
		{
		case Student.NONE:
		case Student.DISCONNECTED:
			st.setStatus(Student.READY);
		break;
		case Student.REJECTED:
			st.getServerLinker().sendMessage(
					new Message<>(Message.YOU_ARE_REJECTED));
		break;
		//new, reconnect or late
		case Student.READY:
		case Student.RESUMED:
			st.getServerLinker().sendMessage( 
					new Message<>(Message.YOU_ARE_ALREADY_CONNECTED) );			
		break;
		case Student.CUTOFF:
			sendLastUpdatedExamOrNewSheet(st); //resume
		break;
		
		case Student.FINISHED:
		case Student.GRADED:
			st.getServerLinker().sendMessage( 
					new Message<>(Message.YOU_HAVE_ALREADY_FINISHED) );
		break;
		}
		
	}

	private boolean isValidStudent(Student st)
	{
		return ExamLoader.getInstance().isValidStudent(st.getId());
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

	private void sendLastUpdatedExamOrNewSheet(Student student)
	{
		ExamSheet newSheet = ExamLoader.getInstance().generateNewSheet();
		
		ExamSheet sheet = student.getOptionalExamSheet().orElse(newSheet);
		Message<ExamSheet> m = new Message<ExamSheet>(Message.EXAM_OBJECT, sheet );
		student.getServerLinker().sendMessage( m );
	}

	public Student findStudent(final Channel ch)
	{
		return Students.stream()
				.filter(st -> (st.getServerLinker() == ch) )
				.findFirst()
				.get();
	}

	
	public void setExamData() throws FileNotFoundException, IOException 
	{
		Students = ExamLoader.getInstance().getStudentList();
		examConfig = ExamLoader.getInstance().getExamConfig();

		for(File f : ExamLoader.getInstance().getImageFiles()) {
			imageList.put(f.getName(), Util.fileToByteArray(f));	
		}
		Logs = ExamLoader.getInstance().getLog();
	}

	private boolean not(boolean b) {
		return !b;
	}

	public void sendExamToAll() 
	{
		Students
		.parallelStream()
		.filter(st -> st.isConnected())
		.filter(st -> st.isREADY() || st.isRESUMED() )
		.forEach(st -> { 
			sendLastUpdatedExamOrNewSheet(st );
		});
	}

	public void backupAll() 
	{
		Runnable task = () -> 
		{
			synchronized(backupLock)
			{
				for(Student st: Students)
				{
					if(st.isSTARTED() || st.isRESUMED()){
						Message<ExamConfig> msg = new Message<>(Message.GIVE_ME_A_BACKUP_NOW);
						st.getServerLinker().sendMessage(msg);
						
						try{ backupLock.wait(3000); } catch(InterruptedException e){ Util.showError(e, e.getMessage()); }
					}
				}
				OnAllBackupsAreRecieved.execute();
			}//synchronized
		};

		ExecutorService  exec = Executors.newSingleThreadExecutor();
		exec.execute(task);
	}

	public void setOnAllBackupsAreRecieved(Routine myAction) {
		this.OnAllBackupsAreRecieved = myAction;
	}

	public void finishAllStudents() 
	{
		Students
		.stream()
		.filter( st -> st.isSTARTED() || st.isRESUMED())
		.forEach(st ->{
			st.getServerLinker()
			.sendMessage( new Message<>(Message.KHALAS_TIMES_UP) );
		});
	}

}//end class
