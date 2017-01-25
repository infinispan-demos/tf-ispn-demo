#include "include/mnist/RestClient.h"
#include <iostream>
#include <QByteArray>

const QString RestClient::ORG_NAME = "Infinispan";
const QString RestClient::APP_NAME = "RestClient";

const QString RestClient::KEY_REST_URL = "rest/url";
const QString RestClient::DEFAULT_REST_URL = "http://localhost:8080/rest";
const QString RestClient::DEFAULT_CACHE_NAME = "mnistRawImgs";


RestClient::RestClient() {
    init(DEFAULT_CACHE_NAME);
}

RestClient::RestClient(QString cacheName) {
    init(cacheName);
}

RestClient::~RestClient() {
    delete manager;
    delete settings;
}

void RestClient::init(QString cacheName) {
    settings = new QSettings(RestClient::ORG_NAME, RestClient::APP_NAME);
    cacheUrl = settings->value(RestClient::KEY_REST_URL, RestClient::DEFAULT_REST_URL).toString() + "/" + cacheName;
    manager = new QNetworkAccessManager(this);
}

void RestClient::put(QString key, QByteArray value) {
    QString targetUrl(cacheUrl + "/" + key);
    QNetworkRequest req = QNetworkRequest(QUrl(targetUrl));
    req.setHeader(QNetworkRequest::ContentTypeHeader, "application/octet-stream");
    QNetworkReply *reply = manager->put(req, value);
    connect(reply, SIGNAL(error(QNetworkReply::NetworkError)), this, SLOT(onError(QNetworkReply::NetworkError)));
}

void RestClient::onError(QNetworkReply::NetworkError error) {
    std::cout << "Error: " << std::endl;
}
