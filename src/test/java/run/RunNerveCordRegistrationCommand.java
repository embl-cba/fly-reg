package run;

import de.embl.cba.flyreg.FlyEmbryoNerveCordRegistrationCommand;
import net.imagej.ImageJ;

public class RunNerveCordRegistrationCommand
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		imageJ.command().run( FlyEmbryoNerveCordRegistrationCommand.class, true );
	}
}
