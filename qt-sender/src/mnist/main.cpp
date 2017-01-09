#include <QCoreApplication>
#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQuickView>

#include <iostream>

#include "include/mnist/MnistReader.h"
#include "include/mnist/RestClient.h"

int main(int argc, char *argv[])
{
    QCoreApplication::setOrganizationName(RestClient::ORG_NAME);
    QCoreApplication::setApplicationName(RestClient::APP_NAME);
    //QCoreApplication app(argc, argv);
    QSettings settings;
    settings.setValue(RestClient::KEY_REST_URL, "http://localhost:8080/rest");

    MnistReader* reader = new MnistReader(std::string("/tmp/mnist/data/t10k-images-idx3-ubyte"));
    RestClient* client = new RestClient("default");

    QByteArray data = reader->imgByteArray(10);
    //client->put("10", data);
    //reader->saveImg(10);

    //return app.exec();

    QGuiApplication app(argc, argv);
    QQmlApplicationEngine engine;
    engine.addImageProvider("images", reader);
    engine.load(QUrl("qrc:/main.qml"));

    return app.exec();
}
