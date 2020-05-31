package com.company.KDRobot.function.Top;

import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.w3c.dom.Node.ELEMENT_NODE;

public class TopDataBase {

    public static class Member {
        public Long today;
        public Long all;
        public int ticket;
        public int Kill;
    }

    private final String XmlPath;
    private HashMap<Long, Member> TopData;
    boolean flash = false;

    String getSaveTime() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document d = builder.parse(XmlPath);

        /* 获取根元素 */
        Element root = d.getDocumentElement();
        Element topData = (Element) root.getElementsByTagName("TopData").item(0);
        return topData.getAttribute("SaveTime");
    }

    public TopDataBase(String DataBasePath) {
        XmlPath = DataBasePath + "/Top.xml";
        TopData = new HashMap<>();

        File f = new File(XmlPath);
        if (!f.exists()) return;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document d = builder.parse(XmlPath);

            /* 获取根元素 */
            Element root = d.getDocumentElement();

            if (!root.getTagName().equals("KDRobotDataBase")) {
                System.err.println(XmlPath + " 可能不是bot数据库");
                System.exit(0);
            }

            NodeList topData = root.getElementsByTagName("TopData").item(0).getChildNodes();

            for (int i = 0; i < topData.getLength(); i++) {
                if (topData.item(i).getNodeType() != ELEMENT_NODE)
                    continue;
                Element top = (Element) topData.item(i);
                Member m = new Member();
                m.today = Long.parseLong(top.getAttribute("ToDay"));
                m.all = Long.parseLong(top.getAttribute("All"));
                m.ticket = Integer.parseInt(top.getAttribute("ticket"));
                m.Kill = Integer.parseInt(top.getAttribute("Kill"));
                TopData.put(Long.parseLong(top.getAttribute("ID")), m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Save() {
        flash = false;
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String SaveTime;
        String NowTime = ft.format(new Date());
        try {
            SaveTime = getSaveTime();
        } catch (Exception e) {
            e.printStackTrace();
            SaveTime = NowTime;
        }

        if (!SaveTime.substring(8, 10).equals(NowTime.substring(8, 10)))
            flash = true;

        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert builder != null;
        Document doc = builder.newDocument();
        Element root = doc.createElement("KDRobotDataBase");
        Element TopDataElement = doc.createElement("TopData");
        doc.appendChild(root);
        root.appendChild(TopDataElement);

        TopDataElement.setAttribute("SaveTime", NowTime);

        TopData.forEach((a, m) -> {
            Element top = doc.createElement("top");
            top.setAttribute("All", m.all.toString());
            top.setAttribute("ToDay", flash ? "0" : m.today.toString());
            top.setAttribute("ID", a.toString());
            top.setAttribute("Kill", Integer.toString(m.Kill));
            top.setAttribute("ticket", Integer.toString(m.ticket));
            TopDataElement.appendChild(top);
            if (flash) {
                m.today = 0L;
                m.ticket = 1;
                m.Kill = m.Kill > 3 ? m.Kill - 3 : 0;
                TopData.put(a, m);
            }
        });

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

    public ArrayList<Pair<Long, Long>> getTop() {
        ArrayList<Pair<Long, Long>> list = new ArrayList<>();

        TopData.forEach((ID, m) -> list.add(new Pair<>(ID, m.all)));

        list.sort((o1, o2) -> (int) (o2.getValue() - o1.getValue()));
        return list;
    }

    public ArrayList<Pair<Long, Long>> getTopTodat() {
        ArrayList<Pair<Long, Long>> list = new ArrayList<>();

        TopData.forEach((ID, m) -> list.add(new Pair<>(ID, m.today)));

        list.sort((o1, o2) -> (int) (o2.getValue() - o1.getValue()));
        return list;
    }

    public void Add(Long ID) {
        Member m;
        if (TopData.containsKey(ID)) {
            m = TopData.get(ID);
            m.all++;
            m.today++;
        } else {
            m = new Member();
            m.today = 1L;
            m.all = 1L;
            m.ticket = 1;
            m.Kill = 0;
        }
        TopData.put(ID, m);
    }

    public Member vote(Long OperatorID, Long UserID) {
        Member Operator = TopData.get(OperatorID);
        Member User = TopData.get(UserID);
        if (User == null || Operator.ticket < 1) return null;
        Operator.ticket = 0;
        User.Kill++;
        TopData.put(OperatorID, Operator);
        TopData.put(UserID, User);
        return User;
    }
}
