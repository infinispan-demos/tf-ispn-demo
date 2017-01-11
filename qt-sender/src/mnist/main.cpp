#include <QCoreApplication>
#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQuickView>

#include <iostream>

#include "include/mnist/MnistReader.h"
#include "include/mnist/MnistSender.h"
#include "include/mnist/RestClient.h"

int main(int argc, char *argv[])
{
    QCoreApplication::setOrganizationName(RestClient::ORG_NAME);
    QCoreApplication::setApplicationName(RestClient::APP_NAME);
    QSettings settings;
    settings.setValue(RestClient::KEY_REST_URL, "http://localhost:8080/rest");
    settings.setValue(MnistReader::KEY_FILE_PATH, "/tmp/mnist/data/t10k-images-idx3-ubyte");

    QGuiApplication app(argc, argv);
    qmlRegisterType<MnistSender>("demo", 1, 0, "MnistSender");
    QQmlApplicationEngine engine;
    engine.addImageProvider("images", new MnistReader());
    engine.load(QUrl("qrc:/main.qml"));

    return app.exec();
}
