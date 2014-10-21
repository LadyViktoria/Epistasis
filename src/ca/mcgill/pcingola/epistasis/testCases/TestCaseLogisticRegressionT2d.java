package ca.mcgill.pcingola.epistasis.testCases;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.pcingola.epistasis.LikelihoodAnalysis;

/**
 * Test cases for logistic regression using phenotypes and VCF data
 *
 * @author pcingola
 */
public class TestCaseLogisticRegressionT2d extends TestCase {

	public static final String LL_INFO_FIELD = "LL";

	public static boolean debug = false;
	public static boolean verbose = false || debug;

	/**
	 * Max Abs difference between each position in two vectors
	 */
	double maxAbsDiff(double d1[], double d2[]) {
		double max = 0;

		for (int i = 0; i < d1.length; i++)
			max = Math.max(max, Math.abs(d1[i] - d2[i]));

		return max;

	}

	/**
	 * Model: Genotype is phenotype (so it should match perfect)
	 */
	public void test_02() {
		Gpr.debug("Test");

		String args[] = { "test/pheno.covariates.T2D_13K.txt", "test/t2d_13K.test_02.vcf" };
		LikelihoodAnalysis la = new LikelihoodAnalysis(args);
		la.setWriteToFile(debug);
		la.setDebug(debug);
		la.setLogLikInfoField(LL_INFO_FIELD);

		List<VcfEntry> list = la.run(true);

		//---
		// Check ALT model
		//---
		double betaAlt[] = la.getLrAlt().getTheta();
		Gpr.debug("beta [Alt] : " + Gpr.toString(betaAlt));

		// Check that coefficients are ~ [20, 0, 0, 0, ..., 0, 0, -10 ]
		for (int i = 1; i < betaAlt.length - 1; i++)
			Assert.assertTrue(Math.abs(betaAlt[i]) < 1E-5);
		Assert.assertTrue(betaAlt[0] > 20);
		Assert.assertTrue(betaAlt[betaAlt.length - 1] < -10);

		//---
		// Check null model
		//---
		double betaNull[] = la.getLrNull().getTheta();
		Gpr.debug("beta [Null]: " + Gpr.toString(betaNull));
		double betaNullExpected[] = { 0.3090173, 4.075187, 1.490912, -3.58731, 5.213386, -3.410404, 0.1404156, 1.266376, 1.617885, 6.241032, -0.1195287, -0.00546481, 0.0192881 };
		Assert.assertTrue(maxAbsDiff(betaNullExpected, betaNull) < 0.0001);

		// Check result (only on line)
		System.out.println(list.get(0).getInfo(LL_INFO_FIELD));
	}

}