#ifndef MNISTSENDER_H
#define MNISTSENDER_H

#include <QtCore>

#include "MnistReader.h"
#include "RestClient.h"

class MnistSender : public QObject {
    Q_OBJECT

public:
    MnistSender();
    MnistSender(MnistReader* reader, RestClient* client);
    ~MnistSender();

    Q_INVOKABLE void sendImage(QString key);

private:
    MnistReader* reader;
    RestClient* client;

    void init(MnistReader* reader, RestClient* client);
};

#endif // MNISTSENDER_H
