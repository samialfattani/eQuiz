package general;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

import frawla.equiz.util.EQDate;
import frawla.equiz.util.Log;
import frawla.equiz.util.Util;

public class LogTest {

	@Test
	public void test() throws ParseException {
		
		String sdate = "12-08-1906 12:00:00";
		EQDate date = new EQDate( Util.MY_DATE_FORMAT, sdate );
		
		Log log = new Log(date, "this is just text");
		assertEquals("this is just text", log.getText());
		assertEquals("12-08-1906 12:00:00",  log.getTime().format() );
	}
}

