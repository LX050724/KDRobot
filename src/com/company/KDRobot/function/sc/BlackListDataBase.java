package com.company.KDRobot.function.sc;

import com.company.KDRobot.function.Get;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;

import static org.w3c.dom.Node.ELEMENT_NODE;

public class BlackListDataBase {
    private final String XmlPath;
    private ArrayList<Long> BlackList;

    public BlackListDataBase(String DataBasePath) {
        XmlPath = DataBasePath + "/BlackList.xml";
        BlackList = new ArrayList<>();

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

            NodeList blackList = root.getElementsByTagName("BlackList").item(0).getChildNodes();

            for (int i = 0; i < blackList.getLength(); i++) {
                if (blackList.item(i).getNodeType() != ELEMENT_NODE)
                    continue;
                Element bl = (Element) blackList.item(i);
                BlackList.add(Long.parseLong(bl.getAttribute("ID")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Save() {
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert builder != null;
        Document doc = builder.newDocument();
        Element root = doc.createElement("KDRobotDataBase");
        Element BlackListElement = doc.createElement("BlackList");
        doc.appendChild(root);
        root.appendChild(BlackListElement);

        for (Long ID : BlackList) {
            Element bl = doc.createElement("bl");
            bl.setAttribute("ID", ID.toString());
            BlackListElement.appendChild(bl);
        }

        try {
            FileOutputStream fos = new FileOutputStream(XmlPath);
            OutputStreamWriter outwriter = new OutputStreamWriter(fos);
            Source source = new DOMSource(doc);
            Result result = new StreamResult(outwriter);

            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
            outwriter.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String RemoveBlackList(String ID) {
        Long Id = Get.At2Long(ID);

        if (Id == null || !BlackList.contains(Id))
            return null;

        BlackList.remove(Id);
        Save();
        return Id.toString();
    }

    public String AddBlackList(String ID) {
        Long Id = Get.At2Long(ID);

        if (Id == null || BlackList.contains(Id))
            return null;

        BlackList.add(Id);
        Save();
        return Id.toString();
    }

    public String ListBlackList() {
        StringBuilder str = new StringBuilder();
        str.append("总计").append(BlackList.size()).append('\n');
        for (Long ID : BlackList) {
            str.append(ID).append("\n");
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    public boolean Check(String ID) {
        return BlackList.contains(Long.parseLong(ID));
    }
}
