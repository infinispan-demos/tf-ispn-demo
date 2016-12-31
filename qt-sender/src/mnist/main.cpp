#include <QCoreApplication>
#include <iostream>

#include "include/mnist/MnistReader.h"
#include "include/mnist/RestClient.h"

int main(int argc, char *argv[])
{
    QCoreApplication::setOrganizationName(RestClient::ORG_NAME);
    QCoreApplication::setApplicationName(RestClient::APP_NAME);
    QCoreApplication app(argc, argv);
    QSettings settings;
    settings.setValue(RestClient::KEY_REST_URL, "http://localhost:8080/rest");

    MnistReader* reader = new MnistReader(std::string("/tmp/mnist/data/t10k-images-idx3-ubyte"));
    RestClient* client = new RestClient("default");

    QByteArray data = reader->imgByteArray(1);
    client->put("1", data);

    return app.exec();
}
