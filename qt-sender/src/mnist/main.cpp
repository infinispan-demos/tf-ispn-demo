#include <QCoreApplication>
#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQuickView>

#include <iostream>

#include "include/mnist/MnistReader.h"
#include "include/mnist/MnistSender.h"
#include "include/mnist/RestClient.h"

/**
 * Main app, which runs MNIST data sample browser and send the data to Infinispan upon click.
 *
 */
int main(int argc, char *argv[])
{
    QCoreApplication::setOrganizationName(RestClient::ORG_NAME);
    QCoreApplication::setApplicationName(RestClient::APP_NAME);
    QSettings settings;
    settings.setValue(RestClient::KEY_REST_URL, "http://localhost:8080/rest"); //URL with Infinispan REST API
    settings.setValue(MnistReader::KEY_FILE_PATH, "/tmp/mnist/data/t10k-images-idx3-ubyte"); //path to the test MNIST data sample

    QGuiApplication app(argc, argv);
    //registers MnistSender class to be available from QML and we can create MnistSender instance there
    qmlRegisterType<MnistSender>("demo", 1, 0, "MnistSender");
    QQmlApplicationEngine engine;
    //TODO avoid creating another MnistReader instance
    engine.addImageProvider("images", new MnistReader()); //adds MnistReader and images provider for QML (via image://images URL)
    //loads QML app
    engine.load(QUrl("qrc:/main.qml"));

    return app.exec();
}
