package sapphire.appexamples.jeffsdocs.cloud;

import sapphire.app.AppEntryPoint;
import sapphire.app.AppObjectNotCreatedException;
import sapphire.appexamples.jeffsdocs.app.UserManager;
import static sapphire.runtime.Sapphire.*;
import sapphire.common.AppObjectStub;

public class DocsStart implements AppEntryPoint {

	@Override
	public AppObjectStub start() throws AppObjectNotCreatedException {
		AppObjectStub tl = null;
		tl = (AppObjectStub) new_(UserManager.class, null);
		return tl;
	}
}
