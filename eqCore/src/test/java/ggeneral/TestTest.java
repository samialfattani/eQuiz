package ggeneral;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTest
{
	@BeforeEach
	public void before()
	{
		System.out.println("bef");
	}

	@Test
	public void test()
	{
		for (int i = 1; i <=3; i++)
		{
			System.out.println(i);
		}
	}

	@Test
	public void test10()
	{
		for (int i = 1; i <=3; i++)
		{
			System.out.println(i*10);
		}
	}

	@AfterEach
	public void after()
	{
		System.out.println("after");
	}

}
