package cz.jurankovi.demo.ispn.tf;

import org.infinispan.filter.NamedFactory;
import org.infinispan.notifications.cachelistener.filter.CacheEventConverter;
import org.infinispan.notifications.cachelistener.filter.CacheEventConverterFactory;

@NamedFactory(name = "string-byte-array-converter-factory")
public class StringByteArrayConverterFactory implements CacheEventConverterFactory {
	
    public StringByteArrayConverterFactory() {
    }

    @Override
    public <K, V, C> CacheEventConverter<K, V, C> getConverter(Object[] params) {
       return (CacheEventConverter<K, V, C>) new StringByteArrayConverter();
    }
 }