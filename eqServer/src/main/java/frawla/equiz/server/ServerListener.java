package frawla.equiz.server;

//network
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import frawla.equiz.util.Channel;
import frawla.equiz.util.Message;
import frawla.equiz.util.Receivable;
import frawla.equiz.util.Util;

public class ServerListener  extends Thread
{

	private Socket mySocket;
	private ServerSocket myServerSocket ;
	private Receivable MsgRoutine;
	private int socketID = 1;
	private boolean running = true;
	
	Executor pool = Executors.newCachedThreadPool(); 
	
	
	ServerListener() {
	}

	public ServerListener(String threadName, int port) throws IOException
	{		
		super(threadName); // Initialize thread.
		
		//connect to the available port
	    myServerSocket = CreateServer(port);
	    if(myServerSocket == null)
	    	throw new IOException("no free port found");
		
	}


	//wait until a client connects then accept.
	@Override
	public void run() 
	{
		try
		{	    	
			MsgRoutine.MessageReleased(new Message<String>(Message.SERVER_IS_INITIALIZED ,  InetAddress.getLocalHost().getHostAddress() + ":" + myServerSocket.getLocalPort() ), null);
			MsgRoutine.MessageReleased(new Message<String>(Message.SERVER_HOST_NAME , InetAddress.getLocalHost().getHostName()), null);

			while(running)
			{
				mySocket = myServerSocket.accept();

				/*for each client, a new Channel object will be created and to be run
				 * in a seperate thread. then the object will be included in the Student.
				 */				
				Channel clientLinker = new Channel("svrPlug-" + socketID, mySocket);
				clientLinker.setOnNewMessage( (msg, ch) -> 
								MsgRoutine.MessageReleased(msg, ch) );
				clientLinker.setSocketID(socketID);

				//start run() in a new thread
				pool.execute(clientLinker);
				clientLinker.sendMessage(new Message<String>(Message.WELCOME_FROM_SERVER , "Hello"));
				MsgRoutine.MessageReleased(new Message<String>(Message.NEW_CLIENT_HAS_BEEN_CONNECTED), clientLinker);				
				socketID++;
				
			}//end while
		}catch(IOException e)
		{
			Util.showError(e, e.getMessage());
		}
	
	}//run
	
	@Override
	public void interrupt() 
	{
		try 
		{
			running =false;
			if(!myServerSocket.isClosed())
				myServerSocket.close();

			if(mySocket != null)
				if(!mySocket.isClosed())
					mySocket.close();
			
			MsgRoutine.MessageReleased(new Message<String>(Message.SERVER_CLOSED , "Server is Closed by inturrupt"), null);
		}catch (IOException e){ }
		finally{
			super.interrupt();
		}
	}	

	public void setOnNewMessage(Receivable nml){
		MsgRoutine = nml;
	}

	public int getLocalPort()
	{		
		return myServerSocket.getLocalPort();
	}

	public String getIP()  
	{
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "Unknown Host";
		}
	}

	//CREATE server with available port.
	private ServerSocket CreateServer(int port) 
	{
		ServerSocket s= null;
		for (int p=port; p< port+100; p++) 
	    {
	        try{
	        	s = new ServerSocket(p);
	        	break;
	        }catch (IOException ex){
	            continue; // try next port
	        }
	    }
		return s;
	}

}