package General;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.Test;

import frawla.equiz.client.ClientChannel;
import frawla.equiz.util.Message;
import frawla.equiz.util.Util;

public class simulationTest {

	@Test 
	public void test() throws UnknownHostException, IOException, InterruptedException {
			
			String[][] csvFile = LosdSCVFile(); 
			ArrayList<ClientChannel> examiners = new ArrayList<ClientChannel>();
			int stCount = csvFile.length ;
			for (int i=0; i < stCount; i++) 
//			for (int i=0; i < 1; i++)
			{
				Socket mySocket = new Socket("localhost", 10000);
		    	final ClientChannel c = new ClientChannel("myClient"+csvFile[i][0], mySocket);
		    	c.studentID = csvFile[i][0];
		    	c.studentName = csvFile[i][1]; 

		    	System.out.println(c.studentID); 

				c.setOnNewMessage((msg, ch) -> {							
					try {
						c.handleTheMsg(msg, ch);
						simulateStudent(msg, c);
					}catch(Exception e){e.printStackTrace();}			
				});
							
				examiners.add(c);
				c.start();
				//try{Thread.sleep(1 * 1000);} catch (InterruptedException e) {e.printStackTrace();}
			}
			
			//wait 5 min
			String lastlog = "";
			while( examiners.stream().anyMatch(c -> c.isConnected() && c.isAlive()) ) {
				long cc = examiners.stream().filter(c -> c.isConnected()).count();
				String newlog = cc + " is connected, waiting for 5 sec...";
				if (!newlog.equals(lastlog)) {
					System.out.println(newlog);
					lastlog = newlog;
				}
				try{Thread.sleep(5 * 1000);} catch (InterruptedException e) {e.printStackTrace();}
			}

	}


	private void simulateStudent(Message<?> msg, ClientChannel myChannel) 
	{
		String code = msg.getCode();
		
		switch(code)
		{
			case Message.WELCOME_FROM_SERVER:
				System.out.println("WELCOME_FROM_SERVER");
			break;
			case Message.YOU_ARE_ALREADY_CONNECTED:
				System.out.println("YOU_ARE_ALREADY_CONNECTED");
				
			case Message.YOU_ARE_REJECTED:
				System.out.println("YOU_ARE_REJECTED");
			break;
			case Message.STUDENT_CUTOFF:
				System.out.println("STUDENT_CUTOFF");
			break;			
			case Message.YOU_HAVE_ALREADY_FINISHED:
				System.out.println("YOU_HAVE_ALREADY_FINISHED");
			break; 
			case Message.EXAM_OBJECT: 
				System.out.println("EXAM_OBJECT");
			break;
			case Message.IMAGES_LIST: 
				System.out.println("IMAGES_LIST");
			break;
			case Message.TIME_LEFT:
				//runExam
				myChannel.mySheet.getQuestionList()
					.forEach(q -> q.setStudentAnswer("A") );
			break;
			
			case Message.FINAL_COPY_IS_RECIEVED:
				System.out.println("FINAL_COPY_IS_RECIEVED");
			break;

		}	
	}//handleTheMsg 


	private String[][] LosdSCVFile() throws IOException 
	{
		
		File f = Util.getResourceAsFile("student-list.csv");
		int count = 0;		
		Scanner scanner = new Scanner(f);
	    while (scanner.hasNextLine()) {
	    	scanner.nextLine();
	    	count += 1;
	    }
	    scanner.close();
	
		String[][] csvFile = new String[count][]; 	
		
		scanner = new Scanner(f);
	    for (int i=0; i < count; i++) 
	    {
	    	csvFile[i] = scanner.nextLine().split(",");
	    	
	    }
	    scanner.close();
	    
		return csvFile;
	}

}
