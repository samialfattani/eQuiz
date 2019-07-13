package frawla.equiz.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//network
import java.net.Socket;
import java.net.SocketException;


public class Channel  extends Thread 
{
	
	private Socket mySocket;
	private Receivable myRoutine;
	private ObjectOutputStream  myWriter; 
	private int socketID = 0;
	private boolean running = true;
	
	//Constructor
	public Channel(String threadName, Socket sock) throws IOException 
	{
		super(threadName); // Initialize thread.

		mySocket = sock ;

		//IMPORTATNT: ObjectOutputStream MUST be initialized only once until the close of connection.
		myWriter = new ObjectOutputStream(mySocket.getOutputStream());

	}
	
	//wait until a message came then print it.
	@Override
	public void run() 
	{
		try(ObjectInputStream objInptStrm = new ObjectInputStream(mySocket.getInputStream());){
			

			while(running)
			{
				Message<?>  msg; // = new Message<>("");
				//readUnshared(): to read the last updated object
				//msg = (Message<?>) objInptStrm.readUnshared();
				//mySocket.getInputStream().reset();
				msg = (Message<?>) objInptStrm.readObject();

				if(msg == null)
					break;
				
//				if(msg.getCode().equals(Message.STOP_RECIEVING_MESSAGES))
//					int
//					break;
				
				//System.out.println(msg);
				myRoutine.MessageReleased(msg, this);
			}
			
		}catch(SocketException | EOFException  e){
			//if the socket is suddenly closed. it will send to both Server and Client 
			if(myRoutine != null)
				myRoutine.MessageReleased(new Message<String>(Message.STUDENT_CUTOFF), this);
		}catch(ClassNotFoundException | IOException e ){
			Util.showError(e, e.toString());
		}	
	} 

	@Override
	public void interrupt() 
	{
		try 
		{
			running = false;
			mySocket.close();
			
		}catch (IOException e){ e.printStackTrace();}
		finally{
			super.interrupt();
		}
	}	
	
	public void sendMessage(Message<?> msg) 
	{
		if (mySocket.isClosed())
			return;
		
//	      synchronized(myWriter)
//	        {
//	        }		
		/**
		 * reset(): to garentee that last updated object to be sent. Otherwise, 
		 * the Outputstream will send the (previously written object)!.
		 * 
		 * writeUnshared() and readUnshared() also can be used to solve problem, but they
		 * will send the last updated base-object. i.e if the object contains another sub-objects,
		 * then they will not get updated.
		 */
		try {
			myWriter.reset();
			myWriter.writeObject((Object) msg);
			myWriter.flush();
		}catch (IOException e) 
		{ e.printStackTrace(); }
		
	}

	public int getSocketID(){return socketID;}
	public void setSocketID(int socketID){this.socketID = socketID;}

	public Socket getSocket(){
		return mySocket;
	}

	
	public void setOnNewMessage(Receivable rc){
		myRoutine = rc;
	}
	
	public boolean isConnected(){
		return (mySocket != null && mySocket.isBound() && isAlive() );
	}

}