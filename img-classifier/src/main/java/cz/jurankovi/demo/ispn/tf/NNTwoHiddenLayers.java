package cz.jurankovi.demo.ispn.tf;

import static org.bytedeco.javacpp.tensorflow.DT_FLOAT;
import static org.bytedeco.javacpp.tensorflow.DT_STRING;
import static org.bytedeco.javacpp.tensorflow.InitMain;
import static org.bytedeco.javacpp.tensorflow.ReadBinaryProto;

import java.nio.FloatBuffer;

import org.bytedeco.javacpp.tensorflow.Env;
import org.bytedeco.javacpp.tensorflow.GraphDef;
import org.bytedeco.javacpp.tensorflow.Session;
import org.bytedeco.javacpp.tensorflow.SessionOptions;
import org.bytedeco.javacpp.tensorflow.Status;
import org.bytedeco.javacpp.tensorflow.StringTensorPairVector;
import org.bytedeco.javacpp.tensorflow.StringVector;
import org.bytedeco.javacpp.tensorflow.Tensor;
import org.bytedeco.javacpp.tensorflow.TensorShape;
import org.bytedeco.javacpp.tensorflow.TensorVector;
import org.bytedeco.javacpp.helper.tensorflow.StringArray;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;

//@ClientListener(converterFactoryName = "___eager-key-value-version-converter", useRawData = true)
@ClientListener
public class NNTwoHiddenLayers {

	public static final int BATCH_SIZE = 100;
	protected final String modelPath;
	protected final String checkpointPath;
	private RemoteCache<Integer, byte[]> cacheImg;
	private RemoteCache<String, String> cacheNodeJS;

	protected final Session session;

	public NNTwoHiddenLayers(String modelPath, String checkpointPath, RemoteCache<Integer, byte[]> cacheImg, RemoteCache<String, String> cacheNodeJS) {
		this.modelPath = modelPath;
		this.checkpointPath = checkpointPath;
		this.cacheImg = cacheImg;
		this.cacheNodeJS = cacheNodeJS;

		// load graph
		InitMain("tf", (int[]) null, null);
		GraphDef graph = new GraphDef();
		ReadBinaryProto(Env.Default(), modelPath, graph);
		SessionOptions options = new SessionOptions();
		this.session = new Session(options);
		Status status = session.Create(graph);
		checkStatus(status);

		// load checkpoint
		Tensor cpPath = new Tensor(DT_STRING, new TensorShape(1));
		StringArray strArray = cpPath.createStringArray();
		strArray.position(0).put(checkpointPath);
		status = session.Run(new StringTensorPairVector(new String[] { "save/Const:0" }, new Tensor[] { cpPath }),
				new StringVector(), new StringVector("save/restore_all"), new TensorVector());
		checkStatus(status);

	}

	@ClientCacheEntryCreated
	public void handleCreatedEvent(ClientCacheEntryCreatedEvent<String> e) {
		processEvent(e.getKey());
	}

	@ClientCacheEntryModified
	public void handleModifiedEvent(ClientCacheEntryModifiedEvent<String> e) {
		processEvent(e.getKey());
	}

	private void processEvent(String key) {
		byte[] raw = cacheImg.get(key);
		float[] img = new float[raw.length];
		for (int i = 0; i < raw.length; i++) {
			img[i] = (float) raw[i] < 0 ? ((float) raw[i] + 256) / 255 : (float) raw[i] / 255;
		}
		predictImage(key, img);
	}

	private void predictImage(String key, float[] image) {
		Tensor img = new Tensor(DT_FLOAT, new TensorShape(1, image.length));
		FloatBuffer imgBuff = img.createBuffer();
		imgBuff.limit(image.length);
		imgBuff.put(image);

		TensorVector outputs = new TensorVector();
		Status status = session.Run(new StringTensorPairVector(new String[] { "images" }, new Tensor[] { img }),
				new StringVector("softmax_linear/logits"), new StringVector("softmax_linear/logits"), outputs);
		checkStatus(status);

		FloatBuffer output = outputs.get(0).createBuffer();
		int maxPos = 0;
		float maxY = Float.MIN_VALUE;
		for (int i = 0; i < output.limit(); i++) {
			float yi = output.get(i);
			if (yi > maxY) {
				maxY = yi;
				maxPos = i;
			}
		}
		cacheNodeJS.put(key, Integer.toString(maxPos));
		System.out.printf("Image could be %d\n", maxPos);
	}

	protected void checkStatus(Status status) {
		if (!status.ok()) {
			throw new RuntimeException(status.error_message().getString());
		}
	}

}
