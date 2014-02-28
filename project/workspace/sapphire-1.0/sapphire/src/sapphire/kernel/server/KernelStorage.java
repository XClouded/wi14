package sapphire.kernel.server;

import java.net.InetSocketAddress;

import voldemort.client.ClientConfig;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.versioning.Versioned;

/**
 * Connection to the storage server for the kernel server.
 * @author iyzhang
 *
 */


public class KernelStorage {

	StoreClient<String, Object> client;

	public KernelStorage(InetSocketAddress storageHost, String storeName) {
		String url = "tcp://"+storageHost.getHostName()+":"+storageHost.getPort();
		ClientConfig cc = new ClientConfig();
		cc.setBootstrapUrls(url);
		StoreClientFactory factory = new SocketStoreClientFactory(cc);
		client = factory.getStoreClient(storeName);
		System.out.println("Connected to Storage server: " + url);
	}

	public void put(String key, Object value) {
		client.put(key, value);
	}

	public Object get(String key) {
		Versioned<Object> version = client.get(key);
		return version.getValue();
	}
}
