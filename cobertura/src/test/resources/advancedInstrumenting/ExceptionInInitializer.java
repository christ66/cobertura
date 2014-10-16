
public class ExceptionInInitializer {

	static {
		/*
		 * This causes the JVM to crash, so
		 * one might argue cobertura does not 
		 * need to handle it. 
		 * 
		 * However, if bug #151 is fixed (https://github.com/cobertura/cobertura/issues/151)
		 * This should work as well. 
		 */
		if(true) {
			throw new IllegalStateException();
		}
	}
	
	public void doStuff() {
		System.out.println("stuff");
	}
}
