/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

/**
 * This class is used by the JUnit test for instrumenting
 * a class.  This is just a basic class where we can check
 * that the number of lines and branches is correct.
 */
public class HelloWorld
{

	private final static String inEnglish = "Hello, world!";
	private final static String enEspanol = "¡Hola, mundo!";
	private String useThisOne;
	private int iterations;

	public HelloWorld(boolean useSpanish, int iterations)
	{
		if (useSpanish)
			useThisOne = enEspanol;
		else
			useThisOne = inEnglish;

		try
		{
			this.iterations = iterations;
		}
		catch (Exception e)
		{
			iterations = 1;
		}
	}

	public void sayHello()
	{
		for (int i = 0; i < iterations; i++)
		{
			System.out.println(useThisOne);
		}

		switch (iterations)
		{
			case 0:
				System.out
						.println("Why don't you want to greet the world?!?");
			default:
				System.out.println("Hello to you too, bub.");
		}
	}

	public static void main(String[] args)
	{
		HelloWorld helloWorld = new HelloWorld(false, 3);
		helloWorld.sayHello();
	}
}