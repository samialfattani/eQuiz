package frawla.equiz.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import frawla.equiz.util.Channel;
import frawla.equiz.util.EQuizException;
import frawla.equiz.util.Message;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.RegisterInfo;

public class ClientChannel extends Channel {
	
	public String studentID = "";   
	public String studentName = "";
	public ExamSheet mySheet;
	public Map<String, byte[]> myImageList;
	
	public ClientChannel(String threadName, Socket sock) throws IOException {
		super(threadName, sock);
	}

	public void handleTheMsg(Message<?> msg, Channel ch) throws Exception 
	{
		String code = msg.getCode();
		switch(code)
		{
			case Message.WELCOME_FROM_SERVER:	
				RegisterInfo r = new RegisterInfo(studentID, studentName);
				this.sendMessage(new Message<RegisterInfo>(Message.REGESTER_INFO , r));
			break;
			case Message.YOU_ARE_ALREADY_CONNECTED:
				throw new EQuizException(
						"You Are Aready Connected\\nYou can't connect twice at the same time.", 
						Message.YOU_ARE_ALREADY_CONNECTED );
				
			case Message.YOU_ARE_REJECTED:
				throw new EQuizException(
						"You Are Rejected\\nYou don't have right to enter this exam. Please inform your exam Administrator.", 
						Message.YOU_ARE_REJECTED );
				
			case Message.STUDENT_CUTOFF:
				throw new EQuizException(
						"You just have diconnected!", Message.STUDENT_CUTOFF ); 
				
			case Message.YOU_HAVE_ALREADY_FINISHED:
				throw new EQuizException(
						"You have Finished the exam, you can't connect again", 
						Message.YOU_HAVE_ALREADY_FINISHED );
				 
			case Message.EXAM_OBJECT: 
				mySheet = (ExamSheet) msg.getData();
				this.sendMessage(new Message<String>(Message.EXAM_OBJECT_RECIVED_SUCCESSFYLLY));								
			break;
			case Message.IMAGES_LIST: 
				myImageList = (Map<String, byte[]>) msg.getData();
				this.sendMessage(new Message<String>(Message.IMAGE_LIST_RECIVED_SUCCESSFYLLY));								
			break;
			case Message.TIME_LEFT: 
				//runExam
			break;
			case Message.GIVE_ME_A_BACKUP_NOW:
		    	this.sendMessage( 
		    			new Message<>( Message.BACKUP_COPY_OF_EXAM_WITH_ANSWERS, mySheet));
		    break;
			case Message.KHALAS_TIMES_UP:
				this.sendFinalCopy();
			break;				
			case Message.FINAL_COPY_IS_RECIEVED:
				this.getSocket().close();
			break;
		}	 
	}//handleTheMsg

	@Override
	public String toString() {
		String s = String.format("%s - %s", studentID, studentName);
		return s;
	}

	public void sendFinalCopy() {
    	this.sendMessage( 
    			new Message<>( Message.FINAL_COPY_OF_EXAM_WITH_ANSWERS, mySheet));				
	}


}//end class
