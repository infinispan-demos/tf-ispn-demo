package cz.jurankovi.demo.ispn.tf;

import java.nio.ByteBuffer;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCustomEvent;
import org.infinispan.commons.io.UnsignedNumeric;

@ClientListener(converterFactoryName = "string-byte-array-converter-factory")
public class MnistListener {

	private final MnistTwoHiddenLayersClassifier classifier;
	private final RemoteCache<String, String> resultCache;
	
	public MnistListener(String modelPath, String checkpointPath, RemoteCache<String, String> resultCache) {
		this.classifier = new MnistTwoHiddenLayersClassifier(modelPath, checkpointPath);
		this.resultCache = resultCache;
	}
	
	@ClientCacheEntryCreated
	@ClientCacheEntryModified
	public void onCacheEvent(ClientCacheEntryCustomEvent<byte[]> e) {
		ByteBuffer buffer = ByteBuffer.wrap(e.getEventData());
		int keyLength = UnsignedNumeric.readUnsignedInt(buffer);
		byte[] keyBytes = new byte[keyLength];
		buffer.get(keyBytes);
        String key = new String(keyBytes);
        int valueLength = UnsignedNumeric.readUnsignedInt(buffer);
        byte valueBytes[] = new byte[valueLength]; 
        buffer.get(valueBytes);
        
		String result = classifier.processEvent(key, valueBytes);
		resultCache.put(key, result);
		System.out.printf("On the image with ID %s could be number %s\n", key, result);
	}
}
