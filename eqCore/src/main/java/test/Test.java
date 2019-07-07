package test;

import java.text.DecimalFormat;

public class Test {

	public static void main(String[] args) 
	{
		DecimalFormat formatter = new DecimalFormat("0.##");
		System.out.println(formatter.format(1.23456789));		
		System.out.format(formatter.format(10.2));
		

	}

}
