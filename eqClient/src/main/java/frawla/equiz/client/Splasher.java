package frawla.equiz.client;

import java.awt.*;

public class Splasher
{
	private static SplashScreen splash = SplashScreen.getSplashScreen();
	public static void splash() 
	{
    	
        if (splash == null) {
            System.out.println("SplashScreen.getSplashScreen() returned null");
            return;
        }
        Graphics2D g = splash.createGraphics();
        if (g == null) {
            System.out.println("Graphics object g is null");
            return;
        }
        
        renderSplashFrame(g);
        splash.update(); 
	}

    private static void renderSplashFrame(Graphics2D g) 
    {
        
    }
    
    public static void close() 
    {
    	if(splash != null)
    		splash.close();
    	
    	splash = null;
    }
    

}//end class
