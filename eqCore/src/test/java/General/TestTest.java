package general;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestTest
{
	@Before
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

	@After
	public void after()
	{
		System.out.println("after");
	}

}
