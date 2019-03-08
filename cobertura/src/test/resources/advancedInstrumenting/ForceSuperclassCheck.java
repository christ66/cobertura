
public class ForceSuperclassCheck {

	public void test() {
		ExceptionInInitializer eii = new ExceptionInInitializer3();
		for(int i=0;i<100;i++) {
			if(i%2 == 0) {
				eii = new ExceptionInInitializer2();				
			}
			blob(eii);
		}
	}
	
	public void blob(ExceptionInInitializer i) {
		i.doStuff();
	}
}
