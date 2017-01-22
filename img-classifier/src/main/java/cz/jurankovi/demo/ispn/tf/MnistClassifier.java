package cz.jurankovi.demo.ispn.tf;

import java.util.concurrent.TimeUnit;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

public class MnistClassifier {
	public static final String MODEL_PATH = "/tmp/my-model/nn2h.pb";
	public static final String CHECKPOINT_PATH = "/tmp/my-model/nn2h.ckpt";
	public static final int DURATION = 15; //how long client will be listening, in minutes
	
	public static final String ISPN_IP = "127.0.0.1";
	public static final String IMAGE_CACHE_NAME = "mnistRawImgs";
	public static final String PNG_CACHE_NAME = "mnistJpgImgs";
	public static final String RESULT_CACHE_NAME = "mnistResults";
	
	public static void main(String[] args) throws Exception {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer().host(ISPN_IP).port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
		RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
		RemoteCache<String, byte[]> cacheImg = cacheManager.getCache(IMAGE_CACHE_NAME);
		RemoteCache<String, String> pngCache = cacheManager.getCache(PNG_CACHE_NAME);
		RemoteCache<String, String> resultCache = cacheManager.getCache(RESULT_CACHE_NAME);
		cacheImg.addClientListener(new MnistListener(MODEL_PATH, CHECKPOINT_PATH, pngCache, resultCache));

		System.out.printf("Client will be listening for %d minutes\n", DURATION);
		Thread.sleep(TimeUnit.MINUTES.toMillis(DURATION));

		cacheManager.stop();
	}

}
