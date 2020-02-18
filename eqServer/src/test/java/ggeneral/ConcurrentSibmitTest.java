package ggeneral;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import frawla.equiz.server.ServerListener;
import frawla.equiz.util.Channel;
import frawla.equiz.util.Message;

public class ConcurrentSibmitTest
{
	private List<byte[]> lst = new ArrayList<>();
	private volatile Integer count =0;
	private final int THREAD_COUNT = 5;
	
	@Test
	public void Sibmi120MessagesConcurrentlyTest() throws IOException, InterruptedException
	{
		ServerListener serverListener = new ServerListener("serverTest", 9999);
		
        serverListener.setOnNewMessage((msg, myChannel) ->
		{
			synchronized (count)
			{
				if(!msg.getCode().equals(Message.EXAM_OBJECT))
					return;
				
				byte[] recData  = (byte[]) msg.getData();
				byte[] b = Arrays.copyOfRange(recData, 0, 4);
				int id = ByteBuffer.wrap(b).getInt();
				
				//System.out.println(id);
				assertEquals(true, Arrays.equals(recData, lst.get(id)) );
				count++;
				myChannel.sendMessage(new Message<String>(Message.TIME_LEFT, ""));
				
				
				if(count.compareTo(THREAD_COUNT) >= 0)
					serverListener.interrupt();
				
			}
		});
        
        //Listener.start();

        //===================================================
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.execute(serverListener);
        //===================================================
        
		Channel[] clients = new Channel[THREAD_COUNT];
        for (int i=0; i<clients.length; i++)
		{
        	clients[i]  = new Channel ("client"+i, new Socket("localhost", 9999));
        	lst.add( getNewData(i) );
		}
        
        Thread[] T = new Thread[clients.length];
        for (int i=0; i<clients.length; i++)
		{
        	final int fi = i;
        	T[i] = new Thread (() -> {
            	Message<byte[]> msg = new Message<>(Message.EXAM_OBJECT , lst.get(fi) );
            	clients[fi].sendMessage(msg);
        	});
		}
        
        
        for (int i=0; i<clients.length; i++)
		{
        	pool.execute(T[i]);
		}

        pool.shutdown();
        pool.awaitTermination(15, TimeUnit.SECONDS);
        
        assertEquals(THREAD_COUNT, count.intValue());	
        
	}


	public byte[] getNewData(int id)
	{
		Random rnd = new Random();
		byte[] data = new byte[100000 + rnd.nextInt(100000)]; //100KB ~ 200KB
		//byte[] data = new byte[1000 + rnd.nextInt(1000)]; //10KB ~ 20KB
		
		byte[] IDbytes = ByteBuffer.allocate(4).putInt(id).array();
		System.arraycopy(IDbytes, 0, data , 0, 4);
		
		//name[0]  = (byte) id;
		for(int j=4; j< data.length ; j++){
			data[j] = (byte) rnd.nextInt(256);
		}
		
		return data;
	}
	
	@Test
	public void intToByteArray()
	{
		int i = 51981981;
		byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
		
		//System.out.println(bytes.length+ " " + Arrays.toString(bytes));
		
		int result = ByteBuffer.wrap(bytes).getInt();
		assertEquals(i, result, 0.0);
		
	}
	
}


