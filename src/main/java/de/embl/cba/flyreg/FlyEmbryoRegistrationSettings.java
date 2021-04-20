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
package de.embl.cba.flyreg;

import net.imglib2.FinalInterval;

public class FlyEmbryoRegistrationSettings
{
	public static final String MANUAL_THRESHOLD = "Manual threshold";
	public static final String HUANG_AUTO_THRESHOLD = "Huang auto threshold";
	public static final String CENTROID_SHAPE_BASED_ROLL_TRANSFORM = "Shape - Centroids";
	public static final String PROJECTION_SHAPE_BASED_ROLL_TRANSFORM = "Shape - Projection";
	public static final String MOMENTS = "Moments";
	public static final String INTENSITY =  "Intensity";

	// all spatial values are in micrometer

	public static double drosophilaLength = 420;
	public static double drosophilaWidth = 160;

	public int alignmentChannelIndexOneBased = 2;
	public int secondaryChannelIndexOneBased = 2;

	public boolean showIntermediateResults = false;

	public double refractiveIndexAxialCalibrationCorrectionFactor = 1.6;
	public double refractiveIndexIntensityCorrectionDecayLength = 170; //170;

	public double registrationResolution = 6.0;
	public double outputResolution = 0.7;

	public double rollAngleMinDistanceToAxis = 0;
	public double rollAngleMinDistanceToCenter = drosophilaLength / 2.0 * 0.5;
	public double rollAngleMaxDistanceToCenter = drosophilaLength / 2.0 - 10.0;

	public double watershedSeedsGlobalDistanceThreshold = drosophilaWidth / 3.0;
	public double watershedSeedsLocalMaximaDistanceThreshold = 0.0;

	public String thresholdModality = MANUAL_THRESHOLD;
	public double thresholdInUnitsOfBackgroundPeakHalfWidth = 5.0;

	public static double[] outputImageSize =
			new double[]{ drosophilaLength * 1.5,
					drosophilaWidth * 1.5,
					drosophilaWidth * 1.5};

	public double minimalObjectSize = drosophilaWidth * drosophilaWidth * drosophilaWidth;

	public double projectionXMin = +20.0;
	public double projectionXMax = +80.0;
	public double projectionBlurSigma = 20.0;
	public double finalProjectionMinDistanceToCenter = 60;
	public String rollAngleComputationMethod = INTENSITY;
	public double watershedSeedsLocalMaximaSearchRadius = 2 * registrationResolution;
	public String yawTransformComputationMethod;
	public double centralRegionDistance = drosophilaWidth * 0.5;
	public boolean onlyComputeEllipsoidParameters = false;

	public FinalInterval getOutputImageInterval()
	{
		final long[] min = new long[ 3 ];
		final long[] max = new long[ 3 ];

		for ( int d = 0; d < 3; ++d )
		{
			min[ d ] = - ( long ) ( outputImageSize[ d ] / 2.0 / outputResolution );
			max[ d ] = -1 * min[ d ];
		}

		return new FinalInterval( min, max );
	}
}
