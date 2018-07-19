package mypackage;

public class ParentChildStaticField {

	public static void main(String[] args) {
		System.out.println("main: new Child()");
		new Child();
	}
}

abstract class Parent {

	public static Parent child = new Child();

	public static Parent getChild() {
		System.out.println("Parent.getChild()");
		return child;
	}

	public Parent() {
		System.out.println("Parent()");
	}

	static {
		System.out.println("Parent.static");
	}
}

class Child extends Parent {

	public static String chieldField;

	public Child() {
		System.out.println("Child()");
	}
	static {
		System.out.println("Child.static");
	}
}

