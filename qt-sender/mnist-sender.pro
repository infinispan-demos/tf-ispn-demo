QT += core network gui
#QT -= gui

CONFIG += c++11

TARGET = mnist-sender
CONFIG += console
CONFIG -= app_bundle

TEMPLATE = app

SOURCES += src/mnist/main.cpp \
    src/mnist/RestClient.cpp \
    src/mnist/MnistReader.cpp

HEADERS += \
    include/mnist/RestClient.h \
    include/mnist/MnistReader.h
