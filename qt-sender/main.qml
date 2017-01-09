import QtQuick 2.7
import QtQuick.Window 2.2
import QtQuick.Controls 2.0

Window {
    visible: true
    width: 280 + 20
    height: 280 + 30

    Grid {
        columns: 10
        spacing: 2

        Repeater {
            model: 300

            Image {
                id: image
                source: "image://images/" + index
                width: 28
                height: 28
                smooth: true

                MouseArea {
                    anchors.fill: parent
                    onClicked: { mnistSender.test(index) }
                }
            }
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
            id: score
            anchors { left: parent.left; verticalCenter: parent.verticalCenter }
            text: "Image ID:"
        }
    }

}
