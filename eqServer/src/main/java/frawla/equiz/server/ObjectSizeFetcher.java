package frawla.equiz.server;

import java.lang.instrument.Instrumentation;

public class ObjectSizeFetcher 
{
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) 
    {
    	System.out.println("JavaAgent is working....");
        instrumentation = inst;
    }

    public static long getObjectSize(Object o) {
    	System.out.println(o.toString());
        return instrumentation.getObjectSize(o);
    }
}