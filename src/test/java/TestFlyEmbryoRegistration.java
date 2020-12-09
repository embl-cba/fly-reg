import de.embl.cba.flyreg.FlyEmbryoRegistrationSettings;
import de.embl.cba.flyreg.FlyEmbryoSingleChannelRegistration;
import de.embl.cba.morphometry.Logger;
import de.embl.cba.morphometry.Utils;
import ij.ImagePlus;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestFlyEmbryoRegistration< T extends RealType< T > & NativeType< T > >
{
	@Test
	public void test0()
	{
		runRegistrationTest( "src/test/resources/test-data/low_res_x60_y55_z41_yaw-22.zip", new double[]{ 60.0, 55.0, 41.0 }, -22 );

	}

	@Test
	public void test1()
	{
		runRegistrationTest( "src/test/resources/test-data/low_res_x58_y69_z38_yaw-54.zip", new double[]{ 58.0, 69.0, 38.0 }, -54 );
	}

	public void runRegistrationTest( String filePath, double[] actualCentre, int actualAngle )
	{
		final ImageJ ij = new ImageJ();
		final OpService opService = ij.op();

		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final ImagePlus imagePlus = Utils.openWithBioFormats( filePath );
		final double[] calibration = Utils.getCalibration( imagePlus );
		RandomAccessibleInterval< T > images = Utils.getChannelImages( imagePlus );
		RandomAccessibleInterval< T > image = Utils.getChannelImage( images, 0  );

		final FlyEmbryoRegistrationSettings settings = new FlyEmbryoRegistrationSettings();

		settings.onlyComputeEllipsoidParameters = true;

		final FlyEmbryoSingleChannelRegistration registration = new FlyEmbryoSingleChannelRegistration( settings, opService );

		registration.run( image, calibration );

		final double[] centre = registration.getElliposidCentreInInputImagePixelUnits();
		final double[] angles = registration.getElliposidEulerAnglesInDegrees();

		logCentre( actualCentre );
		logCentre( centre );
		logAngle( actualAngle );
		logAngle( angles[ 0 ] );

		assertArrayEquals( centre, actualCentre, 5.0 );
		assertEquals( angles[0], actualAngle, 5.0 );
	}

	public void logAngle( double angle )
	{
		Logger.log( "Angle: " + angle );
	}

	public void logCentre( double[] centre )
	{
		Logger.log( "Centre: " + centre[ 0 ] + ", " + centre[ 1 ] + ", " + centre[ 2 ] );
	}

}
