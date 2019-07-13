package frawla.equiz.memory;

public class Main 
{
    private int x=0;
    private int y=0;

    public static void main(String [] args) 
    {
        Main obj = new Main();
        System.out.println("-------------------------");
        System.out.println(obj.x + " , " + obj.y);
    	System.out.println(ObjectSizeFetcher.getObjectSize(obj) + " Bytes");
    	System.out.println("-------------------------");
    }
    
}
