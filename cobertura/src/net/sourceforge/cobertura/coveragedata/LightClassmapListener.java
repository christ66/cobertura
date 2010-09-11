package net.sourceforge.cobertura.coveragedata;

public interface LightClassmapListener {
	public void setClazz(Class<?> clazz);
	
	public void setSource(String source);
	
	public void putLineTouchPoint(int classLine,int counterId, String methodName, String methodDescription);
	
	public void putJumpTouchPoint(int classLine,int trueCounterId,int falseCounterId);
	
	public void putSwitchTouchPoint(int classLine, int... counterIds);
}
