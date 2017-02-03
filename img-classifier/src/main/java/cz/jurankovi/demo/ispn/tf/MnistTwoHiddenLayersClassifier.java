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

/**
 * TensorFlow classifier which classifies MNIST data samples. Expects model and checkpoint paths with trained NN for
 * MNIST sample.
 * 
 * @author vjuranek
 *
 */
public class MnistTwoHiddenLayersClassifier {

    protected final String modelPath;
    protected final String checkpointPath;

    protected final Session session;

    public MnistTwoHiddenLayersClassifier(String modelPath, String checkpointPath) {
        this.modelPath = modelPath;
        this.checkpointPath = checkpointPath;

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

    /**
     * Converts byte array into float array and values from Java range <-128,127> into <0,255> which was used for
     * training NN in Python. After that passes float array into classificator.
     * 
     */
    public String processEvent(byte[] raw) {
        float[] img = new float[raw.length];
        for (int i = 0; i < raw.length; i++) {
            img[i] = (float) raw[i] < 0 ? ((float) raw[i] + 256) / 255 : (float) raw[i] / 255;
        }
        return predictImage(img);
    }

    /**
     * Does actual classification by finding the most probable number to be on the image.
     * 
     * @param image
     *            byte array of the image to be classified
     * @return most probable number to be in the image
     */
    private String predictImage(float[] image) {
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

        return Integer.toString(maxPos);
    }

    /**
     * Convenient method for checking if something went wrong during TF call.
     * 
     */
    private void checkStatus(Status status) {
        if (!status.ok()) {
            throw new RuntimeException(status.error_message().getString());
        }
    }

}
