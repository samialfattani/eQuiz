package test;

import java.text.DecimalFormat;

import javafx.embed.swing.JFXPanel;

public class Test {

	public static void main(String[] args) 
	{
		new JFXPanel();
		System.out.println("This is CORE application - just for test");
		DecimalFormat formatter = new DecimalFormat("0.##");
		System.out.println(formatter.format(1.23456789));		
		System.out.format(formatter.format(10.2));
		
		System.out.println("--------------------------------bye! from core");

	}

}
