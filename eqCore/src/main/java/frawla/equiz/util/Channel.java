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
	private Receivable messageReceiver;
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
			Message<?>  msg = new Message<>("");

			while(running)
			{
				//readUnshared(): to read the last updated object
				//msg = (Message<?>) objInptStrm.readUnshared();
				msg = (Message<?>) objInptStrm.readObject();

				if(msg == null)
					break;
				
				if(msg.getCode().equals(Message.STOP_RECIEVING_MESSAGES))
					break;
				
				//System.out.println(msg);
				messageReceiver.MessageReleased(msg, this);
			}
			
		}catch(SocketException | EOFException e){
			messageReceiver.MessageReleased(new Message<String>(Message.STUDENT_CUTOFF), this);
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
//			if(messageReceiver != null)
//				messageReceiver.MessageReleased(new Message<String>(Message.STUDENT_CUTOFF), this);
			
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
		
		try{	
			/**
			 * reset(): to garentee that last updated object to be sent. Otherwise, 
			 * the Outputstream will send the (previously written object)!.
			 * 
			 * writeUnshared() and readUnshared() also can be used to solve problem, but they
			 * will send the last updated base-object. i.e if the object contains another sub-objects,
			 * then they will not get updated.
			 */
			myWriter.reset();
			myWriter.writeObject((Object) msg);
			//myWriter.writeUnshared((Object) msg);
			myWriter.flush();
		}
		catch (IOException e){
			Util.showError(e, e.toString());		
		}
		
	}

	public int getSocketID(){return socketID;}
	public void setSocketID(int socketID){this.socketID = socketID;}

	public Socket getSocket(){
		return mySocket;
	}

	
	public void setOnNewMessage(Receivable rc){
		messageReceiver = rc;
	}
	
	public boolean isConnected(){
		return (mySocket != null && mySocket.isBound() && isAlive() );
	}

}