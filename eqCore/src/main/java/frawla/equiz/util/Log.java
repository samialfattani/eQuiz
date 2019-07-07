package frawla.equiz.util;

public class Log {
	/**
	 * 
	 */
	private EQDate time;
	private String text;
	
	public Log(EQDate time, String text) {
		super();
		this.time = time;
		this.text = text;
	}

	public EQDate getTime() {return time;}
	public void setTime(EQDate time) {this.time = time;}
	public String getText() {return text;}
	public void setText(String text) {this.text = text;}

	@Override
	public String toString() 
	{
		String fDate = Util.MY_DATE_FORMAT.format(getTime());
		return  fDate + ": " + getText()  ;	
	}
	
		

}
