# Copyright 2015 The TensorFlow Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================


#
# This is slightly modified TF example for MNIST with NN with 2 hidden layer
# See
# https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/tutorials/mnist/fully_connected_feed.py
# for original file with detail comments
#

import math
from six.moves import xrange
import tensorflow as tf
from tensorflow.examples.tutorials.mnist import input_data

# The MNIST dataset has 10 classes, representing the digits 0 through 9.
NUM_CLASSES = 10

# The MNIST images are always 28x28 pixels.
IMAGE_SIZE = 28
IMAGE_PIXELS = IMAGE_SIZE * IMAGE_SIZE

# Basic model parameters as external flags.
flags = tf.app.flags
FLAGS = flags.FLAGS
flags.DEFINE_float('learning_rate', 0.01, 'Initial learning rate.')
flags.DEFINE_integer('max_steps', 6000, 'Number of steps to run trainer.')
flags.DEFINE_integer('hidden1', 128, 'Number of units in hidden layer 1.')
flags.DEFINE_integer('hidden2', 32, 'Number of units in hidden layer 2.')
flags.DEFINE_integer('batch_size', 100, 'Batch size.  '
                     'Must divide evenly into the dataset sizes.')
flags.DEFINE_string('train_dir', 'data', 'Directory to put the training data.')
flags.DEFINE_boolean('fake_data', False, 'If true, uses fake data '
                     'for unit testing.')

def fill_feed_dict(data_set, images_pl, labels_pl):
  images_feed, labels_feed = data_set.next_batch(FLAGS.batch_size,
                                                 FLAGS.fake_data)
  feed_dict = {
      images_pl: images_feed,
      labels_pl: labels_feed,
  }
  return feed_dict


def inference(images, hidden1_units, hidden2_units):
  # Hidden 1
  with tf.name_scope('hidden1'):
    weights = tf.Variable(
        tf.truncated_normal([IMAGE_PIXELS, hidden1_units],
                            stddev=1.0 / math.sqrt(float(IMAGE_PIXELS))),
        name='weights')
    biases = tf.Variable(tf.zeros([hidden1_units]),
                         name='biases')
    hidden1 = tf.nn.relu(tf.matmul(images, weights) + biases)
  # Hidden 2
  with tf.name_scope('hidden2'):
    weights = tf.Variable(
        tf.truncated_normal([hidden1_units, hidden2_units],
                            stddev=1.0 / math.sqrt(float(hidden1_units))),
        name='weights')
    biases = tf.Variable(tf.zeros([hidden2_units]),
                         name='biases')
    hidden2 = tf.nn.relu(tf.matmul(hidden1, weights) + biases)
  # Linear
  with tf.name_scope('softmax_linear'):
    weights = tf.Variable(
        tf.truncated_normal([hidden2_units, NUM_CLASSES],
                            stddev=1.0 / math.sqrt(float(hidden2_units))),
        name='weights')
    biases = tf.Variable(tf.zeros([NUM_CLASSES]),
                         name='biases')
    logits = tf.add(tf.matmul(hidden2, weights), biases, name = "logits")
  return logits


def compute_loss(logits, labels):
  labels = tf.to_int64(labels)
  cross_entropy = tf.nn.sparse_softmax_cross_entropy_with_logits(
      logits, labels, name='xentropy')
  loss = tf.reduce_mean(cross_entropy, name='xentropy_mean')
  return loss


def training(loss, learning_rate):
  tf.scalar_summary(loss.op.name, loss)
  optimizer = tf.train.GradientDescentOptimizer(learning_rate)
  global_step = tf.Variable(0, name='global_step', trainable=False)
  train_op = optimizer.minimize(loss, global_step=global_step, name = "train")
  return train_op


def evaluation(logits, labels):
  correct = tf.nn.in_top_k(logits, labels, 1)
  rs = tf.reduce_sum(tf.cast(correct, tf.int32), name = "eval_correct")
  return rs

def do_eval(sess,
            eval_correct,
            images_placeholder,
            labels_placeholder,
            data_set):
  true_count = 0  # Counts the number of correct predictions.
  steps_per_epoch = data_set.num_examples // FLAGS.batch_size
  num_examples = steps_per_epoch * FLAGS.batch_size
  for step in xrange(steps_per_epoch):
    feed_dict = fill_feed_dict(data_set,
                               images_placeholder,
                               labels_placeholder)
    true_count += sess.run(eval_correct, feed_dict=feed_dict)
  precision = true_count / num_examples
  print('  Num examples: %d  Num correct: %d  Precision @ 1: %0.04f' %
        (num_examples, true_count, precision))

def run_training():
  with tf.Graph().as_default():
    # Generate placeholders for the images and labels.
    images_placeholder = tf.placeholder(tf.float32, [None, IMAGE_PIXELS], name = "images")
    labels_placeholder = tf.placeholder(tf.int32, [None], name = "labels")

    # Build a Graph that computes predictions from the inference model.
    logits = inference(images_placeholder, FLAGS.hidden1, FLAGS.hidden2)

    # Add to the Graph the Ops for loss calculation.
    loss = compute_loss(logits, labels_placeholder)

    # Add to the Graph the Ops that calculate and apply gradients.
    train_op = training(loss, FLAGS.learning_rate)

    # Add the Op to compare the logits to the labels during evaluation.
    eval_correct = evaluation(logits, labels_placeholder)

    # Add the variable initializer Op.
    init = tf.initialize_all_variables()
    
    # Create a session for running Ops on the Graph.
    saver = tf.train.Saver()
    sess = tf.Session()
    # Store the graph into binary ProtoBuf file
    tf.train.write_graph(sess.graph_def, '/tmp/my-model', 'nn2h.pb', as_text=False)
    
    data_sets = input_data.read_data_sets(FLAGS.train_dir)

    # Train the NN
    sess.run(init)
    for step in xrange(FLAGS.max_steps):
      feed_dict = fill_feed_dict(data_sets.train,
                                 images_placeholder,
                                 labels_placeholder)
      _, loss_value = sess.run([train_op, loss],
                               feed_dict=feed_dict)
    # Store trained NN into checkpoint file
    save_path = saver.save(sess, "/tmp/my-model/nn2h.ckpt")
    print("Model saved in file: %s" % save_path)

    # Test trained model
    print('Test Data Eval:')
    do_eval(sess,
            eval_correct,
            images_placeholder,
            labels_placeholder,
            data_sets.test)
      
def main(_):
  run_training()


if __name__ == '__main__':
  tf.app.run()
