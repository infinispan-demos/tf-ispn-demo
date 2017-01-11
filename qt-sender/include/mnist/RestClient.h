#ifndef RESTCLIENT_H
#define RESTCLIENT_H

#include <QtCore>
#include <QString>
#include <QNetworkAccessManager>
#include <QNetworkReply>
#include <QSettings>
#include <QMap>

class RestClient : public QObject {
    Q_OBJECT

public:
    static const QString ORG_NAME;
    static const QString APP_NAME;
    static const QString KEY_REST_URL;

    RestClient();
    RestClient(QString cacheName);
    ~RestClient();

    void put(QString key, QByteArray value);

private:
    static const QString DEFAULT_REST_URL;

    QNetworkAccessManager *manager;
    QSettings *settings;
    QString cacheUrl;
    QByteArray imageContent;

    void init(QString cacheName);

public slots:
    void onReply(QNetworkReply *reply);
    void onError(QNetworkReply::NetworkError error);
};

#endif // RESTCLIENT_H
