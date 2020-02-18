package ggeneral;

import static org.junit.jupiter.api.Assertions.*;
import java.time.*;

import org.junit.jupiter.api.Test;

class TimeClock{

	@Test
	void test() 
	{
		Instant.now(Clock.fixed( 
				  Instant.parse("2018-08-22T10:00:00Z"),
				  ZoneOffset.UTC));
		
		//assertEquals("Thu Aug 15 15:04:21 AST 2019", new Date().toString() );
		
		String instantExpected = "2014-12-22T10:15:30Z";
	    Clock clock = Clock.fixed(
	    				Instant.parse(instantExpected), 
	    				ZoneOffset.UTC );
	 
	    Instant instant = Instant.now(clock);
	 
	    assertEquals(instant.toString(), instantExpected);

	    System.out.println( Instant.now() );
	    
	}

}
