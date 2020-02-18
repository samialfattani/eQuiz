package general;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import frawla.equiz.server.ExamLoader;
import frawla.equiz.server.ServerEngine;
import frawla.equiz.util.Channel;
import frawla.equiz.util.EQuizException;
import frawla.equiz.util.Message;
import frawla.equiz.util.exam.Student;

class SimulationServer 
{
	private ServerEngine serverEngine;
	public static void main(String[] args) throws IOException 
	{
		new SimulationServer();
	}
	
	public SimulationServer() throws IOException
	{
		serverEngine = new ServerEngine("listenter", 10000);
		
		serverEngine.setOnNewMessage( (msg, ch) -> 
		{
			try {
				newMessageHasBeenReleased(msg, ch);
			}catch (InterruptedException | EQuizException e) {e.printStackTrace(); }
		});
		
		serverEngine.setOnAllBackupsAreRecieved( () -> {
			System.out.println("Backup for ALL Done!!");
		});
		
		serverEngine.start();
	
		File f = new File( "../data/MidTerm-1/SE260-MidTerm-1.xlsx" );
		ExamLoader.getInstance(f);
		serverEngine.setExamData();
		
		
		rumSimulator();
	}
	private void rumSimulator() 
	{
		Scanner sn = new Scanner(System.in);
		
		int choice  = -1;
		while( choice != 0 )
		{
			printMenu();
			System.out.println("Choose: ");
			choice = sn.nextInt();
			
			switch (choice) 
			{
			case 1:
				serverEngine.sendExamToAll();
				break;
			case 2:
				serverEngine.finishAllStudents();
				break;

			case 3:
				serverEngine.backupAll();
				break;
			case 4:
				printAllStatus();
				break;
			default: break;
			}
			
		}//end while
		System.out.println("Bye~~~~~~~");
		sn.close();
		
	}

	private void printAllStatus() 
	{
		serverEngine.Students
			.forEach(st -> {
				String s = String.format("%s \t %s", st.getName(), st.getStatus());
				System.out.println( s );
			});
		
	}

	private void printMenu() 
	{
		System.out.println("------------");		
		System.out.println("1. Run Exam");
		System.out.println("2. Finish Exam");
		System.out.println("3. Backup All");
		System.out.println("4. Show All Status");
		System.out.println("0. Exit");
		
	}

	private void newMessageHasBeenReleased(final Message<?> msg, final Channel chnl) throws InterruptedException, EQuizException
	{
		Student student;

		String code = msg.getCode();
		String stringData;

		switch(code)		
		{
			case Message.SERVER_IS_INITIALIZED:
				stringData = (String) msg.getData();
				System.out.println("Server Initialized, " + stringData);
				break;
			case Message.SERVER_CLOSED:
				stringData = (String) msg.getData();
				System.out.println("Closed");
				break;
			case Message.SERVER_HOST_NAME:
				stringData = (String) msg.getData();
				System.out.println(stringData);
				break;
			case Message.NEW_CLIENT_HAS_BEEN_CONNECTED:
				System.out.println("New Client has been joined, socketID = " + chnl.getSocketID());
				break;
			case Message.REGESTER_INFO:
			break;			
			case Message.STUDENT_CUTOFF:
				student = serverEngine.findStudent(chnl);
				if(student.isCUTOFF())
					System.out.println(student.getName() + " is Cut-Off his exam"  );
				break;			
			case  Message.PLAIN_TEXT_FROM_CLIENT:
				stringData = (String) msg.getData();
				student = serverEngine.findStudent(chnl);
				System.out.println(  student.getName() + ": " + stringData );
				break;
			case  Message.EXAM_OBJECT_RECIVED_SUCCESSFYLLY:
			case Message.IMAGE_LIST_RECIVED_SUCCESSFYLLY:
			break;
			case Message.BACKUP_COPY_OF_EXAM_WITH_ANSWERS:
				student = serverEngine.findStudent(chnl);
				System.out.println(student.getId() + " sent a Copy");
				break;
			case  Message.FINAL_COPY_OF_EXAM_WITH_ANSWERS:
				student = serverEngine.findStudent(chnl);
				System.out.println( student.getName() + " -> sent his Final-Copy and Finished");
				break;

				//*************************
			default:
				System.out.println( "##" + msg + "##");
				break;
		}
	}


}//end class
