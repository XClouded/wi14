package sapphire.appexamples.linpack.cloud;

import sapphire.app.AppEntryPoint;
import sapphire.app.AppObjectNotCreatedException;
import sapphire.appexamples.linpack.app.Linpack;
import static sapphire.runtime.Sapphire.*;
import sapphire.common.AppObjectStub;

public class LinpackStart implements AppEntryPoint {

	@Override
	public AppObjectStub start() throws AppObjectNotCreatedException {
		return (AppObjectStub) new_(Linpack.class);
	}
}
