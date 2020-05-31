package com.company.KDRobot.function.MessageBord;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import static org.w3c.dom.Node.TEXT_NODE;

public class MessageBordDataBase {
    public static class Message {
        public Long msgID;
        public Long userID;
        public Long time;
        public String title;
        public String body;

        @Override
        public String toString() {
            return "Message{" +
                    "msgID=" + msgID +
                    ", userID=" + userID +
                    ", time=" + time +
                    ", title='" + title + '\'' +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    private String XmlPath;

    private HashMap<Long, Message> Data;

    public MessageBordDataBase(String DataBasePath) {
        XmlPath = DataBasePath + "/Message.xml";
        Data = new HashMap<>();

        File f = new File(XmlPath);
        if (!f.exists()) return;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document d = builder.parse(XmlPath);

            /* 获取根元素 */
            Element root = d.getDocumentElement();

            if (!root.getTagName().equals("KDRobotDataBase")) {
                System.err.println(DataBasePath + " 可能不是bot数据库");
                System.exit(0);
            }

            NodeList MessageList = root.getElementsByTagName("Message");

            for (int i = 0; i < MessageList.getLength(); i++) {
                Message msg = new Message();
                Element MessageElement = (Element) MessageList.item(i);
                msg.msgID = Long.parseLong(MessageElement.getAttribute("ID"));
                msg.time = Long.parseLong(MessageElement.getAttribute("time"));
                msg.userID = Long.parseLong(MessageElement.getAttribute("User"));
                msg.title = MessageElement.getAttribute("title");

                Node bodyNode = MessageElement.getFirstChild();
                if (bodyNode.getNodeType() == TEXT_NODE) {
                    msg.body = bodyNode.getNodeValue();
                }
                Data.put(msg.msgID, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Save() {
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert builder != null;
        Document doc = builder.newDocument();
        Element root = doc.createElement("KDRobotDataBase");
        doc.appendChild(root);

        Data.forEach((ID, msg) -> {
            Element MessageElement = doc.createElement("Message");
            MessageElement.setAttribute("ID", msg.msgID.toString());
            MessageElement.setAttribute("time", msg.time.toString());
            MessageElement.setAttribute("User", msg.userID.toString());
            MessageElement.setAttribute("title", msg.title);
            MessageElement.appendChild(doc.createTextNode(msg.body));
            root.appendChild(MessageElement);
        });

        try {
            FileOutputStream fos = new FileOutputStream(XmlPath);
            OutputStreamWriter outwriter = new OutputStreamWriter(fos);
            Source source = new DOMSource(doc);
            Result result = new StreamResult(outwriter);

            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.ENCODING, "GBK");
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
            outwriter.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Vector<Message> ListMsg() {
        Vector<Message> vector = new Vector<>();
        Vector<Long> delete = new Vector<>();

        Long nowTime = new Date().getTime();
        Data.forEach((ID, msg) -> {
            if (nowTime - msg.time > 3600000 * 24 * 3) {
                delete.add(ID);
            } else {
                vector.add(msg);
            }
        });
        delete.forEach((ID) -> {
            Data.remove(ID);
        });
        Save();
        return vector;
    }

    public Long pushMsg(Message msg) {
        Random r = new Random();
        msg.time = new Date().getTime();
        do {
            msg.msgID = (long) r.nextInt(10000);
        } while (find(msg.msgID) != null);
        Data.put(msg.msgID, msg);
        Save();
        return msg.msgID;
    }

    public void deleteMsg(Long MsgID) {
        Data.remove(MsgID);
        Save();
    }

    public Message find(Long MsgID) {
        return Data.get(MsgID);
    }
}
