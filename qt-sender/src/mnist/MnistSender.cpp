#include "include/mnist/MnistSender.h"
#include <iostream>
#include <QByteArray>

MnistSender::MnistSender() {
    init(new MnistReader(), new RestClient());
}

MnistSender::MnistSender(MnistReader* reader, RestClient* client) {
    init(reader, client);
}

MnistSender::~MnistSender() {
    delete reader;
    delete client;
}

void MnistSender::init(MnistReader* reader, RestClient* client) {
    this->reader = reader;
    this->client = client;
}

void MnistSender::sendImage(QString key) {
    client->put(key, reader->imgByteArray(key.toInt()));
}
