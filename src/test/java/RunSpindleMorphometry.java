import de.embl.cba.spindle3d.Spindle3DCommand;
import net.imagej.ImageJ;
import net.imglib2.type.numeric.RealType;

import java.io.File;

public class RunSpindleMorphometry
{
	public static < R extends RealType< R > > void main( String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final Spindle3DCommand< R > command = new Spindle3DCommand<>();
		command.opService = ij.op();

		command.inputImageFile = new File("/Users/tischer/Downloads/20200605_HeLa_MCB309_024-1.tif");

		command.outputDirectory = new File( "/Users/tischer/Desktop/kletter" );
		command.spindleChannelIndexOneBased = 1;
		command.dnaChannelIndexOneBased = 2;
		command.showIntermediateResults = false;
		command.run();
	}
}
