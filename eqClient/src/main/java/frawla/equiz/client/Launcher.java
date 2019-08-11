package frawla.equiz.client;

import com.martiansoftware.jsap.JSAPException;

//-splash:splash-client.png

public class Launcher 
{
    public static void main(String[] args) throws JSAPException 
    {
    	//System.out.println(new File(".").getAbsolutePath());
        Splasher.splash();      
    	try {Thread.sleep(10 * 1000);}catch(InterruptedException e) {}
        
    	Main.main(args);
    }
    
}//end class
