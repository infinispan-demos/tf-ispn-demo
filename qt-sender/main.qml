import QtQuick 2.7
import QtQuick.Window 2.2
import QtQuick.Controls 2.0
import QtQuick.Layouts 1.1
import demo 1.0

Window {
    id: main
    visible: true
    width: (28 + 2) * imgGrid.columns + 10
    height: (2/3) * width + zoomArea.height + toolBar.height

    MnistSender {
        id: sender
    }

    Flickable {
        anchors.fill: parent
        contentHeight: imgGrid.height

        GridLayout {
            id: imgGrid
            columns: 20
            columnSpacing: 2

            Repeater {
                model: 900

                Image {
                    id: image
                    source: "image://images/" + index
                    width: 28
                    height: 28
                    smooth: true

                    MouseArea {
                        anchors.fill: parent
                        hoverEnabled: true
                        onClicked: { sender.sendImage(index) }
                        onEntered: {
                            imgInfo.text = "Image ID: " + index
                            imageZoom.source = "image://images/" + index
                        }
                        onExited:  {
                            imgInfo.text = "Image ID: "
                            imageZoom.source = "image://images/" + 0
                        }
                    }
                }
            }
        }

        ScrollBar.vertical: ScrollBar {
            id: scrollBar
        }
    }

    Rectangle {
        id: zoomArea
        width: parent.width
        height: 180
        anchors.bottom: parent.bottom

        Image {
            id: imageZoom
            source: "image://images/" + 0
            anchors.centerIn: parent
            width: 140
            height: 140
            smooth: true
        }
    }

    Rectangle {
        id: toolBar
        width: parent.width
        height: 30
        anchors.bottom: parent.bottom

        Button {
            height: 30
            anchors { right: parent.right; verticalCenter: parent.verticalCenter }
            text: "Quit"
            onClicked: {
                Qt.quit();
            }
        }

        Text {
            id: imgInfo
            anchors { left: parent.left; verticalCenter: parent.verticalCenter }
            text: "Image ID:"
        }
    }

}
