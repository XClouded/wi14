package sapphire.kernel.common;

import sapphire.kernel.server.KernelServerImpl;
import sapphire.stats.StatsManager;

/**
 * Variables visible in the entire address space. They are set at
 * the node (nodeServer) creation:
 * 
 * nodeServer - reference to the Server of the node
 * 
 * The stubs use them as its easier and more transparent than passing
 * them as parameters.
 * 
 * @author aaasz
 *
 */

public class GlobalKernelReferences {
	public static KernelServerImpl nodeServer;
	public static StatsManager stats;
}