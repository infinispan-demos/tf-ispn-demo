package cz.jurankovi.demo.ispn.tf;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCustomEvent;
import org.infinispan.commons.io.UnsignedNumeric;

@ClientListener(converterFactoryName = "string-byte-array-converter-factory")
public class MnistListener {

	private static final int IMG_SIZE = 28;
	private static final int[] BAND_MASKS = { 0xFF0000, 0xFF00, 0xFF, 0xFF000000 };
	
	private final MnistTwoHiddenLayersClassifier classifier;
	private final RemoteCache<String, String> jpgCache;
	private final RemoteCache<String, String> resultCache;
	
	public MnistListener(String modelPath, String checkpointPath, RemoteCache<String, String> jpgCache, RemoteCache<String, String> resultCache) {
		this.classifier = new MnistTwoHiddenLayersClassifier(modelPath, checkpointPath);
		this.jpgCache = jpgCache;
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
        jpgCache.put(key, bufferAsJpgString(valueBytes));
        
		String result = classifier.processEvent(key, valueBytes);
		resultCache.put(key, result);
		System.out.printf("On the image with ID %s could be number %s\n", key, result);
	}
	
	private String bufferAsJpgString(byte[] rawImg) {
		int[] pixels = new int[rawImg.length];
		for (int i = 0; i < rawImg.length; i++) {
			pixels[i] = (int)rawImg[i];
		}
		DataBufferInt buffer = new DataBufferInt(pixels, pixels.length);
		WritableRaster raster = Raster.createPackedRaster(buffer, IMG_SIZE, IMG_SIZE, IMG_SIZE, BAND_MASKS, null);
		ColorModel cm = ColorModel.getRGBdefault();
		BufferedImage image = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
		
		byte[] imgBytes = null;
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "JPG", baos);
			baos.flush();
			imgBytes = baos.toByteArray();
		} catch (IOException e) {
			//TODO log exception
		}

		byte[] encoded = Base64.getEncoder().encode(imgBytes);
		return new String(encoded);
	}
}
