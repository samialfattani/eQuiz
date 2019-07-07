package general;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import frawla.equiz.util.Channel;
import frawla.equiz.util.Message;
import frawla.equiz.util.ServerListener;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.RegisterInfo;

public class SibmitTest
{
	private Map<String, byte[]> images;
	private Map<String, byte[]> msgMap;
	private Object lock = new Object();

	@Before
	public void before() throws FileNotFoundException, IOException{
		File imgFile ;
        byte[] imgData;
        images = new HashMap<>();
        
		imgFile =  new File(Util.getResourceAsURI("sami.jpg")); //new File("data/sami.jpg");
        imgData = fileToByteArray(imgFile);
        images.put(imgFile.getName(), imgData);
        
		imgFile = new File(Util.getResourceAsURI("sami2.jpg")); //new File("data/sami2.jpg");
        imgData = fileToByteArray(imgFile);
        images.put(imgFile.getName(), imgData);
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void CreateMapOfImagesAndSibmiTest() throws IOException, InterruptedException
	{
		ServerListener Listener = new ServerListener("serverTest", 10000);
		
        Listener.setOnNewMessage( (msg, myChannel) ->
        {
			if(!msg.getCode().equals(Message.IMAGES_LIST))
				return;
			
			synchronized (lock)
			{
				msgMap = (Map<String, byte[]>)msg.getData();
				lock.notify();
			}
		});
        
        Listener.start();

        Socket c =  new Socket("localhost", 10000);
        Channel client = new Channel ("client", c);
        
        Message<Map<String, byte[]>> msg = new Message<>(Message.IMAGES_LIST, images);

		sendAndWaitUntilRecieved(msg, client);
        assertEquals(18577, msgMap.get("sami2.jpg").length );
		assertEquals(95176, msgMap.get("sami.jpg").length );
		
		client.interrupt();
	}

	private byte[] fileToByteArray(File file) throws FileNotFoundException, IOException
	{
		FileInputStream fin = new FileInputStream(file );
        
        int fileSize = (int) file.length();
        byte[] fileData = new byte[fileSize];

		fin.read(fileData, 0, fileSize); //fill the imgData        
        fin.close();
		return fileData;
	}

	// ==================================================================
	
	private RegisterInfo r;
	private String Str="";
	@Test
	public void SendObjectMultipleTimesTest() throws IOException, InterruptedException
	{
		ServerListener Listener = new ServerListener("serverTest", 9999);
		
        Listener.setOnNewMessage((msg, myChannel)->
		{
			synchronized(lock)
			{
				if(msg.getCode().equals(Message.REGESTER_INFO)){
					r = (RegisterInfo)msg.getData();
					lock.notify();
				}
				
				if(msg.getCode().equals(Message.PLAIN_TEXT_FROM_CLIENT)){
					Str = (String)msg.getData();
					lock.notify();
				}
			}
		});
        
        Listener.start();

        Socket c =  new Socket("localhost", 9999);
        Channel client = new Channel ("client", c);
        
        Message<RegisterInfo> msg;
        Message<String> msgStr;
        RegisterInfo msgReg ;
        
        //1.send base-object Test
        msgStr = new Message<>(Message.PLAIN_TEXT_FROM_CLIENT, "car");
        sendAndWaitUntilRecieved(msgStr, client);
        assertEquals("car", Str);
        sendAndWaitUntilRecieved(msgStr, client);//again
        assertEquals("car", Str);
        
        msgStr.setData("book"); //same reference but different data.
        sendAndWaitUntilRecieved(msgStr, client);
        assertEquals("book", Str);
        
        
        //2. send deep-object (object with sub-object) Test
        msgReg = new RegisterInfo("13", "Sami Ali");
        msg = new Message<>(Message.REGESTER_INFO, msgReg);
        sendAndWaitUntilRecieved(msg, client);
        assertEquals(msgReg, r);
        
        //-----------------------
        msgReg.ID = "44"; msgReg.Name = "koko"; //same reference but different data.
        msg = new Message<>(Message.REGESTER_INFO, msgReg);
        sendAndWaitUntilRecieved(msg, client);
        assertEquals(msgReg, r);
        
        client.interrupt();
	
	}
	
	private void sendAndWaitUntilRecieved(Message<?> msg, Channel client) throws InterruptedException
	{
		synchronized(lock)
		{
	        client.sendMessage(msg);
	        lock.wait();
		}
	}
}
