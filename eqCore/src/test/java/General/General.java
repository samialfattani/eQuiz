package general;



import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


import frawla.equiz.util.Util;
import frawla.equiz.util.exam.BlankField;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.Randomizable;
import frawla.equiz.util.exam.Student;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class General
{
	@BeforeAll
	public static void before(){
		new JFXPanel();
	}

	
	@Test
	public void userHomePathTest() throws IOException 
	{
		File b; 
		b = new File("" );
		assertEquals(b.getAbsolutePath(), System.getProperty("user.dir") );
		assertEquals("C:\\Users\\sami", System.getProperty("user.home") );
	}
	
	

	@Test
	public void relativePathTest() throws IOException 
	{
		File b; 
		b = new File("D:\\", "./some/relative/path");
		assertEquals("D:\\some\\relative\\path", b.getCanonicalPath()); 

		b = new File("D:\\koko\\", "../some/relative/path");
		assertEquals("D:\\some\\relative\\path", b.getCanonicalPath()); 

		b = new File("D:\\", "../some/relative/path");
		assertEquals("D:\\some\\relative\\path", b.getCanonicalPath());

		Path basePath  = Paths.get("D:\\Dropbox\\Sami-Programming\\JAVA\\eQuiz\\eQuiz-Server\\src\\main\\resources\\template.xlsx");
		// use getParent() if basePath is a file (not a directory)
		Path resolvedPath = basePath.getParent().resolve("..\\..\\..\\..\\..\\..\\..\\IMG_20150723_054905.jpg");  
		Path abolutePath = resolvedPath .normalize();
		assertEquals("D:\\Dropbox\\IMG_20150723_054905.jpg", abolutePath.toString());		
	}
	
	@Test
	public void dateTest() throws ParseException
	{
		SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		Date d = myFormat.parse("31-08-2015 10:20:56");
		String df = myFormat.format(d);
		assertEquals("31-08-2015 10:20:56", df);
	}

	@Test 
	public void instanceofTest(){

		MultipleChoice mc = new MultipleChoice();
		assertEquals(true, mc instanceof Randomizable);
		// mc = (Question)qmc; //ERROR 
		
		Question qmc = new MultipleChoice();		
		assertEquals(true, qmc instanceof Randomizable);
		
		
		Question qbf = new BlankField();
		assertEquals(true, qbf instanceof Question);
		assertEquals(false, qbf instanceof MultipleChoice);
		assertEquals(true, qbf instanceof BlankField);
		
		assertEquals("I'm MultipleChoice", whoRU((MultipleChoice) qmc));
	}

	private String whoRU(MultipleChoice q){
		return "I'm MultipleChoice";		
	}

	@Test
	public void regexTest(){
		String s = "  Hello I'm    \n   \t  here\n";
		s = s.toLowerCase().trim().replaceAll("\\s+", " ");

		assertEquals("hello i'm here", s);
	}


	@Test
	public void RandomizeTest(){

		String[] A = {"1", "2","3", "4", "5", "", "", ""};

		List<String> l = Arrays.asList(A)
				.stream()
				.filter(choice -> choice != "" )						
				.collect(Collectors.toList() );

		Collections.shuffle( l);

		l.toArray(A);
	}

	@Test
	public void CharTest(){
		char choiceID = 'A';
		String actual =  (char)(choiceID+1) + "ffef" ;
		assertEquals("Bffef", actual); 
	}
	
	@Test
	public void ImageToByteArrayTest() throws IOException
	{
		byte[] imgData;
		File imgFile = new File(Util.getResourceAsURI("sami.jpg"));//new File( "data/sami.jpg");
		FileInputStream fis = new FileInputStream( imgFile  );
		int imgSize = (int)imgFile.length();
		imgData = new byte[imgSize];
		fis.read(imgData, 0, imgSize);        
        fis.close();
        
        assertEquals(true, imgFile.exists());
        assertEquals(imgFile.length(), imgData.length);
        
        imgFile = Util.getTempFile(); //new File("D:\\koko.jpg");
        FileOutputStream fos = new FileOutputStream(imgFile);
        fos.write(imgData, 0, imgData.length);
        fos.flush();
        fos.close();

        assertEquals(true, imgFile.exists());
        assertEquals(imgFile.length(), imgData.length);

	}

	@Test
	public void TryCatchTest(){
		int res = 0;
		String s = "";
		try{
			res = 10/0;
		}
		catch(Exception e)
		{
			s = e.getClass().getName() + ",";
		}
		finally 
		{
			s += "Finally,";
		}
		res = res + 0;
		s += "After try";
		assertEquals( "java.lang.ArithmeticException,Finally,After try", s);
		
	}
	
	@Test
	public void ConcurrecnyTest() throws InterruptedException
	{
		
		class Person{
			private String name;
			private int age;
			private int total;

			public Person(String name, int age, int total){
				super();
				this.name = name;
				this.age = age;
				this.total = total;
			}

			@Override
			public String toString(){
				return "Person [name=" + name + ", age=" + age + ", total=" + total + "]";
			}
		}//end Person
		
		List<Person> lst = new ArrayList<>();
		
		lst.add(new Person("Sami", 35, 0));
		lst.add(new Person("Ahmed", 30, 0));
		lst.add(new Person("talal", 29, 0));
		final int MY_TOTAL = 5000000; //5M
		
		Thread t1 = new Thread( () -> {
			for (int i = 0; i < MY_TOTAL; i++)
			{
				lst.set(0, new Person("Sami", 35, lst.get(0).total+1));
			}
		});
		Thread t2 = new Thread( () -> {
			for (int i = 0; i < MY_TOTAL; i++)
			{
				lst.set(1, new Person("Ahmed", 30, lst.get(1).total+1));
			}
		});
		Thread t3 = new Thread( () -> {
			for (int i = 0; i < MY_TOTAL; i++)
			{
				lst.set(2, new Person("talal", 29, lst.get(2).total+1));
			}
		});
		t1.start(); t1.join(); 
		t2.start(); t2.join();  
		t3.start(); t3.join();
		
		assertEquals(MY_TOTAL, lst.get(0).total); 
		assertEquals(MY_TOTAL, lst.get(1).total);
		assertEquals(MY_TOTAL, lst.get(2).total);
	}
	
	@Test
	public void CounterTest() 
	{
		int c = 1;
		assertEquals(c++, 1);
		assertEquals(++c, 3);
		
	}

	@Test
	public void OptionalTest() 
	{
		List<Student> lst = new ArrayList<>();
		lst.stream()
		   .filter(st -> st.getStatus()== Student.STARTED)
		   .findFirst()
		   .ifPresent(st -> assertEquals("", st.getName()) );
	}

	@Test
	public void TimerTest() throws InterruptedException 
	{
		new JFXPanel(); // initializes JavaFX environment
		StringBuilder sb = new StringBuilder("");
		Timeline mytimer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				sb.append("d");
			}
		}));
		
		mytimer.setCycleCount(1);
		mytimer.play();
		mytimer.play();
		mytimer.play();
		Thread.sleep(1500);
		assertEquals("d", sb.toString());
		
		mytimer.play();
		mytimer.stop();
		Thread.sleep(1500);
		assertEquals("d", sb.toString());
		
	}

	@Test
	public void DurationTest() 
	{
        new JFXPanel(); // initializes JavaFX environment
        Duration d;
		d = Duration.millis(3*1000);
		assertEquals( "00:03", Util.formatTime(d) ); 

		d = Duration.millis( 3*60*1000 );
		assertEquals( "03:00", Util.formatTime(d) );
		
		d = Duration.hours(2).add( Duration.minutes(30) );
		assertEquals( "2:30:00", Util.formatTime(d) );
	}

	@Test
	public void isNumericTest() {
		
		assertEquals(true, Util.isNumeric("12"));
		assertEquals(true, Util.isNumeric("12.0"));
		assertEquals(true, Util.isNumeric("12.01"));
		assertEquals(true, Util.isNumeric("12.10"));
		assertEquals(true, Util.isNumeric(".5"));
		assertEquals(true, Util.isNumeric("-.5"));
		assertEquals(true, Util.isNumeric("-0.5"));
		assertEquals(true, Util.isNumeric("-0000.5"));
		assertEquals(true, Util.isNumeric("-.500"));
		assertEquals(false, Util.isNumeric("+."));
		assertEquals(true, Util.isNumeric("+.0"));
		assertEquals(true, Util.isNumeric("+0"));
		assertEquals(true, Util.isNumeric("-0"));
		assertEquals(true, Util.isNumeric("+0.0"));
		assertEquals(true, Util.isNumeric("-0.0"));
		
	}

	@Test
	public void stringFormatTest() {
		
		assertEquals("32.10", String.format("%.2f", 32.1));
		assertEquals("32.00", String.format("%.2f", 32.0));
		
		DecimalFormat formatter = new DecimalFormat("0.##");
		assertEquals("32.5", formatter.format( 32.5));
		assertEquals("32.55", formatter.format( 32.5499));
		assertEquals("0.5", formatter.format( 0.5));

		
	}
	
	
	@Test
	public void stringJoinTest(){
		
		String[] A = {"not included", "" , "Hello", "//", null, "c"};
		
		A = Arrays.copyOfRange(A, 2, A.length);
		String res = Arrays.asList(A)
			  .stream()
			  .map( x -> {return (x==null)?"":x ;}  )
			  .collect( Collectors.joining(""));
		assertEquals("Hello//c", res);
		
	}	
}//end class
