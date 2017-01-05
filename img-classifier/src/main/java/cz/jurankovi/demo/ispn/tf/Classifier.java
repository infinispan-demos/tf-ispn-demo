package cz.jurankovi.demo.ispn.tf;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

public class Classifier {

	public static final String ISPN_IP = "127.0.0.1";
	public static final int IMG_SIZE = 28;
	public static final int IMG_PIXELS = IMG_SIZE * IMG_SIZE;

	public static void main(String[] args) throws Exception {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer().host(ISPN_IP).port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
		RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
		RemoteCache<Integer, byte[]> cacheImg = cacheManager.getCache();
		RemoteCache<String, String> cacheNodeJS = cacheManager.getCache("nodejs");
		cacheImg.addClientListener(new NNTwoHiddenLayers("/tmp/my-model/nn2h.pb", "/tmp/my-model/nn2h.ckpt", cacheImg, cacheNodeJS));

		System.out.println("Client will be listening to avg. temperature updates for 5 minutes");
		Thread.sleep(5 * 60 * 1000);

		cacheManager.stop();
	}

}
