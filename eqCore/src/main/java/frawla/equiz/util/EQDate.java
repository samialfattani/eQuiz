package frawla.equiz.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;

import javafx.util.Duration;

public class EQDate extends Date 
{
	private static final long serialVersionUID = 1L;

	public EQDate() {super();}
	public EQDate(long date) {super(date);}
	public EQDate(Clock  clock) { super( clock.millis() ); }
	
	public EQDate(SimpleDateFormat f, String sdate) throws ParseException
	{
		super ( f.parse(sdate).getTime()  );
	}
	
	public String format() {
		return Util.MY_DATE_FORMAT.format(this);
	}
	public String format(SimpleDateFormat f) {
		return f.format(this);
	}
	public String format(String pattern) {
		SimpleDateFormat f = new SimpleDateFormat(pattern);		
		return f.format(this);
	}
	public EQDate minus(Duration dur) 
	{
		long durMillis = Double.valueOf( dur.toMillis() ).longValue();
		EQDate res = new EQDate (this.getTime() - durMillis);
		return res;
	}
	public EQDate plus(Duration dur) {
		long durMillis = Double.valueOf( dur.toMillis() ).longValue();
		EQDate res = new EQDate (this.getTime() + durMillis);
		return res;
	}
	

}
