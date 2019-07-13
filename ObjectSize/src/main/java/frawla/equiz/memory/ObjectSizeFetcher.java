package frawla.equiz.memory;

import java.lang.instrument.Instrumentation;

public class ObjectSizeFetcher 
{
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) 
    {
        instrumentation = inst;
        System.out.println("Agent is running.... SAMI");
    }

    public static long getObjectSize(Object o) {
        return instrumentation.getObjectSize(o);
    }
}