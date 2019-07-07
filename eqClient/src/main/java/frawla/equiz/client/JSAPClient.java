package frawla.equiz.client;

import java.io.IOException;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.StringParser;
import com.martiansoftware.jsap.Switch;

import frawla.equiz.util.Util;

public class JSAPClient extends SimpleJSAP
{

	private JSAPResult result;

	/**
	 * -h 10.4.10.100 -p 10000 -i SAM000 -u "Sami Alfattani"
	 * -h 192.168.200.1 -p 10000 -i SAM000 -u "Sami Alfattani"
	 * -h localhost -p 10000 -i SAM000 -u "Sami Alfattani"
	 * */
	public JSAPClient(String[] args) throws JSAPException
	{
		super("kkkkkk");
		try{
			// create a flagged option we'll access using the id "host".
			// it's going to be an integer, with a default value of 'localhost'.
			// its short flag is "h", so a command line containing "-h 10.4.10.191 "
			FlaggedOption opt1 = new FlaggedOption("host")
					.setStringParser(JSAP.STRING_PARSER)
					.setDefault("localhost") 
					.setRequired(false) 
					.setShortFlag('h') 
					.setLongFlag("host");
			opt1.setHelp("the host name that is running the server App. you can specify wither IP adress or computer name. If the host is running on this computer then please write '127.0.0.1' or 'localhost' as a host name.");
			registerParameter(opt1);

			FlaggedOption opt2 = new FlaggedOption("port")
					.setStringParser(JSAP.INTEGER_PARSER)
					.setDefault("10000") 
					.setRequired(false) 
					.setShortFlag('p') 
					.setLongFlag("port");
			registerParameter(opt2);

			FlaggedOption opt3 = new FlaggedOption("user")
					.setStringParser(JSAP.STRING_PARSER)
					.setRequired(false)
					.setShortFlag('u')
					.setLongFlag("user-name");
			opt3.setHelp("Name of the Student who participated this exam.");
			registerParameter(opt3);

			
			FlaggedOption opt4 = new FlaggedOption("id")
					.setStringParser( new StringParser()
					{
						@Override
						public Object parse(String arg) throws ParseException
						{
							return arg.toUpperCase();
						}
					})
					.setRequired(false) 
					.setShortFlag('i')
					.setLongFlag("user-id");
			opt4.setHelp("Student ID is a unique identifier over all exam participants. Using ID, marks will be registered.");
			registerParameter(opt4);

			Switch sw1 = new Switch("verbose")
					.setShortFlag('v')
					.setLongFlag("verbose");
			sw1.setHelp("put this flag if you want to check the assigned parameters.");
			registerParameter(sw1);

			//--- finish -----
			result = parse(args);
			showVerbose();
			if ( messagePrinted() ) 
				System.exit( 1 );
		}
		catch (JSAPException e){ Util.showError(e , e.getMessage()); }

	}//end constructor

	private void showVerbose()
	{
		if( result.getBoolean("verbose")){
			System.out.println("host: " + result.getString("host"));
			System.out.println("port: " + result.getInt("port"));			
			System.out.println("user id: " + result.getString("id"));
			System.out.println("user name: " + result.getString("user"));
		}
			
	}

	public JSAPClient(String resourceName) throws IOException, JSAPException{
		super(resourceName);
	}

	public JSAPResult getResult()
	{
		return result;
	}

}
