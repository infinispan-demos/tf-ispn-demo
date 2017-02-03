package cz.jurankovi.demo.ispn.tf;

import java.io.Serializable;

import org.infinispan.commons.io.UnsignedNumeric;
import org.infinispan.metadata.Metadata;
import org.infinispan.notifications.cachelistener.filter.CacheEventConverter;
import org.infinispan.notifications.cachelistener.filter.EventType;

/**
 * Convert Infinispan event into custom event which contains string key and byte
 * array value. Resulting byte array contains also sizes of key and value so
 * that we can decode it later on.
 * 
 * @author vjuranek
 *
 */
public class StringByteArrayConverter implements CacheEventConverter<String, byte[], byte[]>, Serializable {

	public StringByteArrayConverter() {
	}

	@Override
	public byte[] convert(String key, byte[] oldValue, Metadata oldMetadata, byte[] newValue, Metadata newMetadata,
			EventType eventType) {
		byte[] keyBytes = key.getBytes();
		int keyOffset = UnsignedNumeric.sizeUnsignedInt(keyBytes.length);
		int valueOffset = UnsignedNumeric.sizeUnsignedInt(newValue.length);

		byte[] out = new byte[keyOffset + keyBytes.length + valueOffset + newValue.length];
		UnsignedNumeric.writeUnsignedInt(out, 0, keyBytes.length);
		System.arraycopy(keyBytes, 0, out, keyOffset, keyBytes.length);
		UnsignedNumeric.writeUnsignedInt(out, keyOffset + keyBytes.length, newValue.length);
		System.arraycopy(newValue, 0, out, keyOffset + keyBytes.length + valueOffset, newValue.length);

		return out;
	}
}