#ifndef RESTCLIENT_H
#define RESTCLIENT_H

#include <QtCore>
#include <QString>
#include <QNetworkAccessManager>
#include <QNetworkReply>
#include <QSettings>
#include <QMap>

/**
 * @brief The REST client for sending the data into Infinispan via REST API.
 */
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
    static const QString DEFAULT_CACHE_NAME;

    QNetworkAccessManager *manager;
    QSettings *settings;
    QString cacheUrl;
    QByteArray imageContent;

    void init(QString cacheName);

public slots:
    void onError(QNetworkReply::NetworkError error);
};

#endif // RESTCLIENT_H
