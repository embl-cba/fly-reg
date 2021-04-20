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

import de.embl.cba.abberation.RefractiveIndexMismatchCorrectionSettings;
import de.embl.cba.abberation.RefractiveIndexMismatchCorrections;
import de.embl.cba.morphometry.Logger;
import de.embl.cba.morphometry.Projection;
import de.embl.cba.morphometry.Utils;
import de.embl.cba.transforms.utils.Transforms;
import ij.ImagePlus;
import ij.io.FileSaver;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.File;
import java.util.ArrayList;

import static de.embl.cba.morphometry.Constants.Z;
import static de.embl.cba.morphometry.Utils.openWithBioFormats;

@Plugin(type = Command.class, menuPath = "Plugins>Registration>Fly>Embryo Nerve Cord Registration..." )
public class FlyEmbryoNerveCordRegistrationCommand< T extends RealType< T > & NativeType< T > > implements Command
{
	FlyEmbryoRegistrationSettings settings = new FlyEmbryoRegistrationSettings();

	@Parameter
	public UIService uiService;

	@Parameter
	public DatasetService datasetService;

	@Parameter
	public LogService logService;

	@Parameter
	public OpService opService;

	@Parameter
	public StatusService statusService;

	@Parameter( label = "Images to be registered")
	public File[] files;

	@Parameter( style = "directory" )
	public File outputDirectory;

	@Parameter
	public String fileNameEndsWith = ".czi,.lsm";

	@Parameter
	public boolean showIntermediateResults = settings.showIntermediateResults;

	@Parameter ( label = "Nerve cord channel index (one-based)")
	public int alignmentChannelIndexOneBased = settings.alignmentChannelIndexOneBased;

	@Parameter
	public double registrationResolution = settings.registrationResolution;

	@Parameter
	public double outputResolution = settings.outputResolution;

	@Parameter
	public double refractiveIndexIntensityCorrectionDecayLength = settings.refractiveIndexIntensityCorrectionDecayLength;

	public String rollAngleAlignmentMethod = FlyEmbryoRegistrationSettings.INTENSITY;

	public void run()
	{
		setSettingsFromUI();

		final FlyEmbryoNerveCordRegistration registration = new FlyEmbryoNerveCordRegistration( settings, opService );

		for( File file : files )
		{
			if ( acceptFile( fileNameEndsWith, file.toString() ) )
			{
				final String outputFilePathStump = outputDirectory + File.separator + file.getName();

				Utils.setNewLogFilePath( outputFilePathStump + ".log.txt" );

				/**
				 * Open images
				 */

				final String inputPath = file.getAbsolutePath();
				Logger.log( " " );
				Logger.log( "Reading: " + inputPath + "..." );
				final ImagePlus inputImagePlus = openWithBioFormats( inputPath );

				if ( inputImagePlus == null )
				{
					logService.error( "Error opening file: " + inputPath );
					continue;
				}

				/**
				 * Register
				 */

				RandomAccessibleInterval< T > registeredImages =
						createAlignedImages( inputImagePlus, registration );

				if ( registeredImages == null )
				{
					Logger.log( "ERROR: Could not find central embryo" );
					continue;
				}

				/**
				 * Save registered images
				 */

				saveResults( outputFilePathStump, registeredImages );

//					RandomAccessibleInterval< T > watershed = (RandomAccessibleInterval) registration.getWatershedLabelImg();
//					new FileSaver( ImageJFunctions.wrap( watershed, "" ) ).saveAsTiff( outputFilePathStump + "-watershed.tif" );
//
//					Utils.log( "Creating projections..." );
//					final ArrayList< ImagePlus > projections = createProjections( registeredImages );
//
//					Utils.log( "Saving projections..." );
//					saveImages( outputFilePathStump, projections );
//
//					// Save ch1 non-registered projection
//					RandomAccessibleInterval< T > channel1Image = getChannelImage( getChannelImages( inputImagePlus ) );
//					RandomAccessibleInterval shavenbabyMaximum = new Projection( channel1Image, Z ).maximum();
//					new FileSaver( ImageJFunctions.wrap( shavenbabyMaximum, "" ) ).saveAsTiff( outputFilePathStump + "-projection-ch1-raw.tif" );
//
//					// Save ch2 non-registered projection
//					RandomAccessibleInterval< T > channel2Image = getChannel2Image( getChannelImages( inputImagePlus ) );
//					RandomAccessibleInterval ch2Maximum = new Projection( channel2Image, Z ).maximum();
//					new FileSaver( ImageJFunctions.wrap( ch2Maximum, "" ) ).saveAsTiff( outputFilePathStump + "-projection-ch2-raw.tif" );
			}
		}

		Logger.log( "Done!" );
	}

	public void saveResults( String outputFilePathStump, RandomAccessibleInterval< T > registeredImages )
	{
		// 3D image stack
		//
		final RandomAccessibleInterval< T > registeredWithImagePlusDimensionOrder =
				Utils.copyAsArrayImg( Views.permute( registeredImages, 2, 3 ) );
		final ImagePlus registered = ImageJFunctions.wrap( registeredWithImagePlusDimensionOrder, "transformed" );
		registered.getCalibration().setUnit( "micrometer" );
		registered.getCalibration().pixelWidth = settings.outputResolution;
		registered.getCalibration().pixelHeight = settings.outputResolution;
		registered.getCalibration().pixelDepth = settings.outputResolution;

		final String outputPath = outputFilePathStump + "-registered.tif";
		Logger.log( "Saving registered image: " + outputPath );
		new FileSaver( registered ).saveAsTiff( outputPath );
	}

	public boolean acceptFile( String fileNameEndsWith, String file )
	{
		final String[] fileNameEndsWithList = fileNameEndsWith.split( "," );

		for ( String endsWith : fileNameEndsWithList )
		{
			if ( file.endsWith( endsWith.trim() ) )
			{
				return true;
			}
		}

		return false;
	}


	public void saveImages( String outputPath, ArrayList< ImagePlus > imps )
	{
		for ( ImagePlus imp : imps )
		{
			final String outputPath2 = outputPath + "-" + imp.getTitle() + ".tif";
			FileSaver fileSaver = new FileSaver( imp );
			fileSaver.saveAsTiff( outputPath2 );
		}
	}

	public ArrayList< ImagePlus > createProjections( RandomAccessibleInterval< T > images )
	{
		int Z = 2;

		ArrayList< ImagePlus > projections = new ArrayList<>(  );

		for ( int channelId = 0; channelId < images.dimension( 3 ); ++channelId )
		{
			RandomAccessibleInterval channel = Utils.getChannelImage( images, channelId );

			// top
			long rangeMin = (long) ( settings.finalProjectionMinDistanceToCenter / settings.outputResolution );
			long rangeMax = images.max( Z );
			Projection projection = new Projection( channel, Z, rangeMin, rangeMax );
			RandomAccessibleInterval maximum = projection.maximum();
			ImagePlus wrap = ImageJFunctions.wrap( maximum, "top-projection-ch" + ( channelId + 1 ) );
			projections.add( wrap );

			// bottom
			rangeMin = images.min( Z );
			rangeMax = - (long) ( settings.finalProjectionMinDistanceToCenter / settings.outputResolution );
			projection = new Projection( channel, Z, rangeMin, rangeMax );
			maximum = projection.maximum();
			wrap = ImageJFunctions.wrap( maximum, "bottom-projection-ch" + ( channelId + 1 ) );
			projections.add( wrap );

			// full
			rangeMin = images.min( Z );
			rangeMax = images.max( Z );
			projection = new Projection( channel, Z, rangeMin, rangeMax );
			maximum = projection.maximum();
			wrap = ImageJFunctions.wrap( maximum, "projection-ch" + ( channelId + 1 ) );
			projections.add( wrap );
		}

		return projections;
	}

	public RandomAccessibleInterval< T > createAlignedImages( ImagePlus imagePlus, FlyEmbryoNerveCordRegistration registration )
	{
		final double[] inputCalibration = Utils.getCalibration( imagePlus );
		RandomAccessibleInterval< T > images = Utils.getChannelImages( imagePlus );
		RandomAccessibleInterval< T > image = Utils.getChannelImage( images, alignmentChannelIndexOneBased - 1  );

		/**
		 * Compute registration
		 */
		Logger.log( "Computing registration...." );
		registration.run( image, inputCalibration );

		/**
		 * Apply intensity correction
		 */
		Logger.log( "Applying intensity correction to all channels...." );
		final RandomAccessibleInterval< T > intensityCorrectedImages =
				createIntensityCorrectedImages(
						images,
						registration.getCorrectedCalibration()[ Z ],
						registration.getCoverslipPosition()  );


		/**
		 * Create transformation for desired output resolution
		 */
		final AffineTransform3D registrationTransform =
				registration.getRegistrationTransform(
						registration.getCorrectedCalibration(), settings.outputResolution );
		if ( registrationTransform == null ) return null;

		/**
		 * Apply transformation
		 */
		Logger.log( "Creating registered and masked images (can take some time)..." );
		ArrayList< RandomAccessibleInterval< T > > registeredImages =
				Transforms.transformAllChannels(
						intensityCorrectedImages,
						registrationTransform,
						settings.getOutputImageInterval()
				);

		/**
		 * Apply masking ( in order to remove other, potentially touching, embryos )
		 */
		final RandomAccessibleInterval< BitType > alignedMaskAtOutputResolution
				= registration.getAlignedMask( settings.outputResolution, settings.getOutputImageInterval() );
		registeredImages = Utils.maskAllChannels( registeredImages, alignedMaskAtOutputResolution, settings.showIntermediateResults );

		return Views.stack( registeredImages );
	}

	public RandomAccessibleInterval< T > createIntensityCorrectedImages( RandomAccessibleInterval< T > images,
																		 double axialCalibration,
																		 double coverslipPosition )
	{

		final RefractiveIndexMismatchCorrectionSettings correctionSettings = new RefractiveIndexMismatchCorrectionSettings();
		correctionSettings.pixelCalibrationMicrometer = axialCalibration;
		correctionSettings.coverslipPositionMicrometer = coverslipPosition;
		correctionSettings.intensityDecayLengthMicrometer = settings.refractiveIndexIntensityCorrectionDecayLength;

		return RefractiveIndexMismatchCorrections.createIntensityCorrectedImages( images, correctionSettings  );
	}

	public void setSettingsFromUI()
	{
		settings.showIntermediateResults = showIntermediateResults;
		settings.registrationResolution = registrationResolution;
		settings.outputResolution = outputResolution;
		settings.refractiveIndexIntensityCorrectionDecayLength = refractiveIndexIntensityCorrectionDecayLength;
		settings.thresholdModality = "";
		settings.rollAngleComputationMethod = rollAngleAlignmentMethod;
		settings.alignmentChannelIndexOneBased = alignmentChannelIndexOneBased;
	}

}
