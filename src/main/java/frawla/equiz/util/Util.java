package frawla.equiz.util;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import frawla.equiz.util.exam.Student;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Util
{

	public static SimpleDateFormat MY_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
	public static final Image CONNECTED_IMAGE = new Image(getResource("images/connect.jpg").toString());
	public static final Image DISCONNECTED_IMAGE = new Image(getResource("images/disconnect.jpg").toString());

	static{
		new JFXPanel();
	}
	public Util()
	{
		
	}

  public static void RunApplication(File f) 
    {
        try {
        	//String appPath = f.getAbsolutePath();
            //Runtime rt = Runtime.getRuntime();
            //Process myProcess = rt.exec(appPath);
            
            Desktop.getDesktop().open(f);
            // or 
            //rt.exec("rundll32 url.dll,FileProtocolHandler \"" + appPath + "\"");
            
            //wait until program closed.
            //myProcess.waitFor();  
            //System.out.println(myProcess.exitValue());
        }catch (IOException e) {
            Util.showError(e, e.getMessage());
        }
    }
  
	public static URI getResource(String s)
	{
		URI uri =null;
		try{
			uri = Util.class.getClassLoader().getResource(s).toURI();
		}catch (URISyntaxException e){
			Util.showError(e , e.getMessage());
		}
		return uri ;
	}

	public static InputStream getResourceAsStream(String s)
	{		
		return Util.class.getClassLoader().getResourceAsStream(s);
	}

	public  static void cleanDirectory(File dir) 
	{
		if(!dir.exists())
			return;

		for (File file: dir.listFiles()) 
		{
			if (file.isDirectory()) 
				cleanDirectory(file);
			file.delete();
		}
	}	

	/** Converts a string into something you can safely insert into a URL. */
	public static String encodeURI(String s)
	{
		StringBuilder o = new StringBuilder();
		for (char ch : s.toCharArray()) {
			if (isUnsafe(ch)) {
				o.append('%');
				o.append(toHex(ch / 16));
				o.append(toHex(ch % 16));
			}
			else 
				o.append(ch);
		}
		return o.toString();
	}
	private static char toHex(int ch){
		return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
	}
	private static boolean isUnsafe(char ch){
		if (ch < 0 || ch > 128) //not between 0~128
			return true;	    
		return " %$&+,/:;=?@<>#%\r\n\t".indexOf(ch) >= 0;
	}

	public static String getTempDir(){
		return System.getProperty("java.io.tmpdir");
	}
	public static File getTempFile() throws IOException{
		return File.createTempFile( "Samitemp", null, new File(System.getProperty("java.io.tmpdir")) );		
	}

	public static String getCurrentDir()
	{		
		return new File("").getAbsolutePath();
	}

	public static Optional<File> getFileChooserForOpen()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Open File");
		fc.getExtensionFilters().addAll(
				new ExtensionFilter("MS-Excel Files", "*.xlsx"),
				new ExtensionFilter("MS-Excel(97-2003) Files", "*.xls"),
				new ExtensionFilter("All Files", "*.*"));
		fc.setTitle("Open MS-Excel files");
		fc.setInitialDirectory(new File("."));
		File selectedFile = fc.showOpenDialog( null );
		return Optional.ofNullable(selectedFile);
	}

	public static Optional<File> getFileChooserForSaveExcel()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Save File");
		fc.getExtensionFilters().addAll(
				new ExtensionFilter("MS-Excel Files", "*.xlsx"),
				new ExtensionFilter("MS-Excel(97-2003) Files", "*.xls"),
				new ExtensionFilter("All Files", "*.*"));
		fc.setTitle("Save MS-Excel files");
		fc.setInitialDirectory(new File("."));
		File selectedFile = fc.showSaveDialog( null );
		return Optional.ofNullable(selectedFile);
	}

	public static Optional<File> getFileChooserForSavePDF()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Save File");
		fc.getExtensionFilters().addAll(
				new ExtensionFilter("PDF Files", "*.pdf"),
				new ExtensionFilter("All Files", "*.*"));
		fc.setTitle("Save PDF files");
		fc.setInitialDirectory(new File("."));
		File selectedFile = fc.showSaveDialog( null );
		return Optional.ofNullable(selectedFile);
	}

	public static String formatTime(Duration d)
	{
		if (d.lessThan(Duration.ZERO)) {
			return "Unknown";
		}

		int seconds = (int) Math.floor(d.toSeconds());
		int h = seconds / 3600;
		int m = (seconds - h * 3600) / 60;
		int s = seconds - h * 3600 - m * 60;
		//int mi = ((int)(d.toMillis()) - seconds*1000)/100;

		if (h <= 0)
			return String.format("%02d:%02d", m, s );
		else
			return String.format("%d:%02d:%02d", h, m, s);
	}
	
	public static void copyFile(File source, File dest) 
	{
		try
		{
			Files.copy(source.toPath(), dest.toPath());
		}catch (FileAlreadyExistsException  e){
			Util.showError("File is already exists. Choose another file name.");
		}
		catch (IOException e){
			Util.showError(e, e.getMessage());
		}
	}
	

	public static void sleep(long mills)
	{
		try
		{
			Thread.sleep(mills);
		}
		catch (InterruptedException e)
		{
			showError(e, e.getMessage());
		}

	}


	/**
	 * @param tFile
	 *            : a text file
	 * @return: the file content as a String.
	 * @throws IOException
	 */
	public static String readFileAsString(final File tFile)
	{
		return readFileAsString(tFile, System.lineSeparator());
	}

	public static String readFileAsString(final File f, final String LineSeperator)
	{
		//final close all open objects
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));)
		{
			//read the extraxcted file normally.
			String line = null;
			StringBuilder str = new StringBuilder();

			if(!f.exists()){
				JOptionPane.showMessageDialog(null, "Subtitle File is not found !!", "File Not Found", JOptionPane.ERROR_MESSAGE);
				return "";
			}

			//first line only
			if ((line = in.readLine()) != null)
			{
				line = line.replace("\n", "").replace("\r", "");
				str.append(line);
			}

			while ((line = in.readLine()) != null)
			{
				str.append(LineSeperator + line);
			}
			return str.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return "";
	}


	public static void Save(Object obj, File f){
		try
		{
			FileOutputStream fileOut = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(obj);
			out.close();
			fileOut.close();
		}catch(IOException i){
			i.printStackTrace();
		}    	
	}

	public static Object readFileAsObject(File f)
	{
		Object obj = null;
		try
		{
			FileInputStream fileIn = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			obj = in.readObject();
			in.close();
			fileIn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return obj;
	}

	public static byte[] fileToByteArray(File file) throws FileNotFoundException, IOException
	{
		FileInputStream fin = new FileInputStream(file );
        
        int fileSize = (int) file.length();
        byte[] fileData = new byte[fileSize];

		fin.read(fileData, 0, fileSize); //fill the imgData        
        fin.close();
		return fileData;
	}

	public static void showError(String string){
		Platform.runLater( ()->{
			Alert alert = new Alert(AlertType.ERROR);
			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.getIcons().add(new Image(getResource("images/error.png").toString()));
			alert.setTitle("Error - " + System.getProperty("program.name"));
			alert.setHeaderText(null);
			alert.setContentText(string);
			alert.showAndWait();
		});    	
	}
	public static void showError(Exception ex, String string){
		Platform.runLater( ()->{
			showErrorInAppThread(ex, string);
		});    	
	}
	
	private static void showErrorInAppThread(Exception ex, String string)
	{
		new JFXPanel(); //initialize graphics
		Alert alert = new Alert(AlertType.ERROR);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getResource("images/error.png").toString()));
		alert.setTitle("Exception - " + System.getProperty("program.name"));
		alert.setHeaderText("Suddenly a problem has been occured, Please Contact the customer support");
		alert.setContentText(string);

		if(ex != null)
		{
			// Create expandable Exception.
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			String exceptionText = sw.toString();

			Label label = new Label("The exception stacktrace was:");

			TextArea textArea = new TextArea(exceptionText);
			textArea.setEditable(false);
			textArea.setWrapText(false);
			textArea.setFont(new Font("Consolas", 16));
			textArea.setStyle("-fx-font-weight: bold");

			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);

			GridPane expContent = new GridPane();
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.add(label, 0, 0);
			expContent.add(textArea, 0, 1);

			// Set expandable Exception into the dialog pane.
			alert.getDialogPane().setExpandableContent(expContent);

			ex.printStackTrace();
		}

		alert.showAndWait();    	

	}//showErrorInAppThread


	public static boolean isNumeric(String s) 
	{  
		
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  


	public static void jaxbStudentToXML(Student st, File f) {

        try {
            JAXBContext context = JAXBContext.newInstance(Student.class);
            Marshaller m = context.createMarshaller();
            //for pretty-print XML in JAXB
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Write to System.out for debugging
            // m.marshal(emp, System.out);

            // Write to File
            m.marshal(st, f);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

	public static Student jaxbXMLToStudent(File f) {
        try {
            JAXBContext context = JAXBContext.newInstance(Student.class);
            Unmarshaller un = context.createUnmarshaller();
            Student st = (Student) un.unmarshal(f);
            return st;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

}
