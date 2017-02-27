package frawla.equiz.util;

public class Heap
{

	public static long getUsedSize()
	{
		// Get current size of heap in bytes
		long heapSize = Runtime.getRuntime().totalMemory();
		return heapSize;
	}
	
	public static long getFreeSize()
	{
		 // Get amount of free memory within the heap in bytes. This size will increase
		 // after garbage collection and decrease as new objects are created.
		long heapFreeSize = Runtime.getRuntime().freeMemory();
		return heapFreeSize;
	}
	
	public static long getTotalSize()
	{
		// Get maximum size of heap in bytes. The heap cannot grow beyond this size.
		// Any attempt will result in an OutOfMemoryException.
		long heapMaxSize = Runtime.getRuntime().maxMemory();
		return heapMaxSize;
	}
	
	public static String ReadableByte(long bytes ) 
	{
	    int unit = 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    
	    String pre = ("KMGTPE").charAt(exp-1) +"";
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
