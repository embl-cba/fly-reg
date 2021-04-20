/*-
 * #%L
 * Fiji plugin for automated 3d spindle morphometry
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package test;

import de.embl.cba.flyreg.FlyEmbryoRegistrationSettings;
import de.embl.cba.flyreg.FlyEmbryoNerveCordRegistration;
import de.embl.cba.morphometry.Logger;
import de.embl.cba.morphometry.Utils;
import ij.ImagePlus;
import loci.common.DebugTools;
import net.imagej.ImageJ;
import net.imagej.ops.DefaultOpService;
import net.imagej.ops.OpService;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestFlyEmbryoRegistration
{
	public static void main( String[] args )
	{
		final TestFlyEmbryoRegistration testFlyEmbryoRegistration = new TestFlyEmbryoRegistration();
		testFlyEmbryoRegistration.test0();
		testFlyEmbryoRegistration.test1();
	}

	//@Test
	public void test0()
	{
		runRegistrationTest( "src/test/resources/test-data/low_res_x60_y55_z41_yaw-22.zip", new double[]{ 60.0, 55.0, 41.0 }, -22 );
	}

	//@Test
	public void test1()
	{
		runRegistrationTest( "src/test/resources/test-data/low_res_x58_y69_z38_yaw-54.zip", new double[]{ 58.0, 69.0, 38.0 }, -54 );
	}

	public static < T extends RealType< T > & NativeType< T > > void runRegistrationTest( String filePath, double[] actualCentre, int actualAngle )
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

		final FlyEmbryoNerveCordRegistration registration = new FlyEmbryoNerveCordRegistration( settings, opService );

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

	public static void logAngle( double angle )
	{
		Logger.log( "Angle: " + angle );
	}

	public static void logCentre( double[] centre )
	{
		Logger.log( "Centre: " + centre[ 0 ] + ", " + centre[ 1 ] + ", " + centre[ 2 ] );
	}

}
