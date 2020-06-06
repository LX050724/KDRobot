package com.company.KDRobot.function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BaiKe {
    private static String GetHTML(String keyWord) throws IOException {
        String key = new String(keyWord.getBytes(StandardCharsets.UTF_8), System.getProperty("file.encoding"));
        URL url = new URL("https://baike.baidu.com/item/" + key);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("GET");

        InputStreamReader reader = new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8);
        StringBuilder results = new StringBuilder();
        int tmp;
        while ((tmp = reader.read()) != -1) {
            results.append((char) tmp);
        }

        if (httpURLConnection.getURL().toString().equals("https://baike.baidu.com/error.html")) {
            System.err.println("跳转到错误页面https://baike.baidu.com/error.html");
            return null;
        }

        return results.toString();
    }

    private static Element FindDevByClass(Element parent, String Class) {
        if (parent == null)
            return null;

        for (Node i : parent.childNodes()) {
            for (Attribute attribute : i.attributes()) {
                if (attribute.getKey().equals("class")) {
                    String[] val = attribute.getValue().split("\\s+");
                    for (String v : val) {
                        if (v.equals(Class))
                            return (Element) i;
                    }
                }
            }
        }
        return null;
    }

    private static String GetText(Node node) {
        StringBuilder text = new StringBuilder();
        for (Node child : node.childNodes()) {
            for (Attribute attribute : child.attributes()) {
                switch (attribute.getKey()) {
                    case "#text":
                        text.append(attribute.getValue());
                        break;
                    case "href":
                        text.append(GetText(child));
                        break;
                }
            }
        }
        return text.toString();
    }

    public static String GetBaiKe(String keyWord) {
        String HTML = null;
        try {
            HTML = GetHTML(keyWord);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (HTML == null) {
            System.err.println("HTML错误");
            return "错误";
        }

        Document doc = Jsoup.parse(HTML);

        Element body = doc.body();

        Element BodyWrapper = FindDevByClass(body, "body-wrapper");
        Element ContentWrapper = FindDevByClass(BodyWrapper, "content-wrapper");
        Element Content = FindDevByClass(ContentWrapper, "content");
        Element MainContent = FindDevByClass(Content, "main-content");
        Element LemmaSummary = FindDevByClass(MainContent, "lemma-summary");

        if (LemmaSummary == null) {
            System.err.println("LemmaSummary错误");
            return "错误";
        }

        StringBuilder text = new StringBuilder();

        for (Node child : LemmaSummary.childNodes()) {
            for (Attribute attribute : child.attributes()) {
                if (attribute.getKey().equals("class") && attribute.getValue().equals("para")) {
                    text.append(GetText(child));
                }
            }
        }
        return text.toString();
    }
}
