package frawla.equiz.util;

@FunctionalInterface
public interface Receivable
{
	public void MessageReleased(Message<?> msg, Channel myChannel);

}
