#include "include/mnist/MnistReader.h"
#include <iostream>
#include <fstream>
#include <QImage>

const QString MnistReader::ORG_NAME = "Infinispan";
const QString MnistReader::APP_NAME = "RestClient";

const QString MnistReader::KEY_FILE_PATH = "mnist/data";
const QString MnistReader::DEFAULT_FILE_PATH = "/tmp/mnist/data/t10k-images-idx3-ubyte";

const int MnistReader::MAGIC = 2051;

MnistReader::MnistReader() : QQuickImageProvider(QQuickImageProvider::Image) {
    settings = new QSettings(MnistReader::ORG_NAME, MnistReader::APP_NAME);
    string filePath = settings->value(MnistReader::KEY_FILE_PATH, MnistReader::DEFAULT_FILE_PATH).toString().toUtf8().constData();
    init(filePath);
}

MnistReader::MnistReader(string filePath)  : QQuickImageProvider(QQuickImageProvider::Image) {
    init(filePath);
}

MnistReader::~MnistReader() {
    delete[] data;
}

void MnistReader::init(string filePath) {
    fstream imgFile(filePath, ios_base::in);

    imgFile.read((char*)&magic, sizeof(magic));
    imgFile.read((char*)&imgCnt, sizeof(imgCnt));
    imgFile.read((char*)&rowCnt, sizeof(rowCnt));
    imgFile.read((char*)&colCnt, sizeof(colCnt));

    //stored with big endianness, while I have Intel
    magic =  __builtin_bswap32(magic);
    imgCnt = __builtin_bswap32(imgCnt);
    rowCnt = __builtin_bswap32(rowCnt);
    colCnt = __builtin_bswap32(colCnt);

    cout << "Img count: " << imgCnt << endl;
    cout << "Row count: " << rowCnt << endl;
    cout << "Col count: " << colCnt << endl;

    if (MAGIC != magic) {
        cerr << "Magic number doesn't match, exiting" << endl;
        //TODO exit
    }

    int pixelCnt =  imgCnt * rowCnt * colCnt;
    data = new char[pixelCnt];

    for (int i = 0; i < pixelCnt; i++) {
        unsigned char pixel = 0;
        imgFile.read((char*)&pixel, sizeof(pixel));
        data[i] = pixel;
    }

    imgFile.close();
}

int MnistReader::imgSize() {
    return rowCnt * colCnt;
}

const char* MnistReader::imgBytes(int imgNum) {
    //TODO check imgNum > 0
    int imgSize = rowCnt * colCnt;
    char* img = new char[imgSize];
    //TODO better way
    for (int i = 0; i < imgSize; i++) {
        img[i] = data[imgNum * imgSize + i];
    }
    return img;
}

QByteArray MnistReader::imgByteArray(int imgNum) {
    return QByteArray::fromRawData(imgBytes(imgNum), imgSize());
}

void MnistReader::saveImg(int numImg) {
    const char* raw = imgBytes(numImg);
    uchar* data = new uchar[784];
    for (int i = 0; i < 784; i++) {
        data[i] = static_cast<unsigned char>(raw[i]);
    }
    QImage img(data, 28, 28, QImage::Format_Grayscale8);
    img.save("/tmp/testimg.png", "PNG");
}

QImage MnistReader::requestImage(const QString &id, QSize *size, const QSize& requestedSize) {
    const char* raw = imgBytes(id.toInt());
    uchar* data = new uchar[784];
    for (int i = 0; i < 784; i++) {
        data[i] = static_cast<unsigned char>(raw[i]);
    }
    QImage img(data, 28, 28, QImage::Format_Grayscale8);
    return img;
}
