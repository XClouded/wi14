package sapphire.appexamples.microbenchmark.cloud;
import sapphire.app.AppEntryPoint;
import sapphire.app.AppObjectNotCreatedException;
import sapphire.appexamples.microbenchmark.app.TestObject;
import static sapphire.runtime.Sapphire.*;
import sapphire.common.AppObjectStub;

public class MicrobenchmarkStart implements AppEntryPoint {
	@Override
	public AppObjectStub start() throws AppObjectNotCreatedException {
			return (AppObjectStub) new_(TestObject.class);
	}
}