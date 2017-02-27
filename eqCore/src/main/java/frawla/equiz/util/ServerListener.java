package frawla.equiz.util;

//network
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ServerListener  extends Thread
{

	private Socket mySocket;
	private ServerSocket myServerSocket ;
	private Receivable serverMsgListener;
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
			serverMsgListener.MessageReleased(new Message<String>(Message.SERVER_LOG ,  "Server is Initialized, " + InetAddress.getLocalHost().getHostAddress() + ":" + myServerSocket.getLocalPort() ), null);
			serverMsgListener.MessageReleased(new Message<String>(Message.SERVER_IP , InetAddress.getLocalHost().getHostAddress()), null);
			serverMsgListener.MessageReleased(new Message<String>(Message.SERVER_HOST_NAME , InetAddress.getLocalHost().getHostName()), null);

			while(running)
			{
				serverMsgListener.MessageReleased(new Message<String>(Message.SERVER_LOG , "Server Waiting..."), null);
				mySocket = myServerSocket.accept();


				/*for each client, a new Channel object will be created and to be run
				 * in a seperate thread. then the object will be included in the Student.
				 */				
				Channel clientLinker = new Channel("svrPlug-" + socketID, mySocket);
				clientLinker.setOnNewMessage( (msg, ch) -> 
								serverMsgListener.MessageReleased(msg, ch) );
				clientLinker.setSocketID(socketID);
				//clientLinker.start();

				pool.execute(clientLinker);
				clientLinker.sendMessage(new Message<String>(Message.WELCOME_FROM_SERVER , "Hello"));
				serverMsgListener.MessageReleased(new Message<String>(Message.NEW_CLIENT_HAS_BEEN_CONNECTED), clientLinker);				
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
			
			serverMsgListener.MessageReleased(new Message<String>(Message.SERVER_CLOSED , "Server is Closed by inturrupt"), null);
		}catch (IOException e){ }
		finally{
			super.interrupt();
		}
	}	

	public void setOnNewMessage(Receivable nml){
		serverMsgListener = nml;
	}

	public String getLocalPort()
	{		
		return myServerSocket.getLocalPort() + "";
	}

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