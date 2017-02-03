#ifndef MNIST_READER_H
#define MNIST_READER_H

#include <iostream>

#include <QByteArray>
#include <QQuickImageProvider>
#include <QSettings>

using namespace std;

/**
 * @brief Loads the specified MNIST data sample.
 */
class MnistReader : public QQuickImageProvider {

public:
    static const QString ORG_NAME;
    static const QString APP_NAME;
    static const QString KEY_FILE_PATH;

    static const int MAGIC;

    MnistReader();
    MnistReader(string filePath);
    ~MnistReader();

    int imgSize();
    const char* imgBytes(int imgNum);
    QByteArray imgByteArray(int imgNum);
    void saveImg(int numImg);

    QImage requestImage(const QString &id, QSize *size, const QSize& requestedSize);

private:
    static const QString DEFAULT_FILE_PATH;

    QSettings *settings;
    int magic = 0;
    int imgCnt = 0;
    int rowCnt = 0;
    int colCnt = 0;
    char* data;

    void init(string filePath);
};


#endif // MNIST_READER_H
