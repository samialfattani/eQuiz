package frawla.equiz.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EQDate extends Date 
{
	private static final long serialVersionUID = 1L;

	public EQDate() {super();}
	public EQDate(long date) {super(date);}
	
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
	

}
