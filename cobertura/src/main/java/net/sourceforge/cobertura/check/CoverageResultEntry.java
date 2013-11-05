package net.sourceforge.cobertura.check;

/**
 * Contains coverage information.
 */
public class CoverageResultEntry {

	private CoverageLevel coverageLevel;
	private CoverageType coverageType;
	private String name;
	private double currentCoverage;
	private double expectedCoverage;

	public CoverageResultEntry(CoverageLevel coverageLevel,
			CoverageType coverageType, String name, double currentCoverage,
			double expectedCoverage) {
		this.coverageLevel = coverageLevel;
		this.coverageType = coverageType;
		this.name = name;
		this.currentCoverage = currentCoverage;
		this.expectedCoverage = expectedCoverage;
	}

	public CoverageLevel getCoverageLevel() {
		return coverageLevel;
	}

	public CoverageType getCoverageType() {
		return coverageType;
	}

	public String getName() {
		return name;
	}

	public double getCurrentCoverage() {
		return currentCoverage;
	}

	public double getExpectedCoverage() {
		return expectedCoverage;
	}

	public boolean isBelowExpectedCoverage() {
		return getCurrentCoverage() < getExpectedCoverage();
	}

	enum CoverageLevel {
		CLASS, PACKAGE, PROJECT;
	}

	enum CoverageType {
		BRANCH, LINE;
	}
}
