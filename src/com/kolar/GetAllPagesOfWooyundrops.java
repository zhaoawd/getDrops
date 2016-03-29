package com.kolar;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoa on 2016/1/29 0029.
 */
public class GetAllPagesOfWooyundrops {

    private static String wooyunCategoryUrl = "http://drops.wooyun.org/category/";

    private static String wooyunUrl = "http://drops.wooyun.org/";
    //乌云知识库的分类词
    private static String[] category = {"papers","tips","tools","news","%e8%bf%90%e7%bb%b4%e5%ae%89%e5%85%a8","web",
            "pentesting","mobile","wireless","database","binary"};


    //获取某个分类下的总页数
    public static int getTheCategoryPageNum(String category){
        //每个分类都有数据,不满一页的没有分页条目,默认返回1
        int totalPageNum = 1;
        try {
            URL targetUrl = new URL(wooyunCategoryUrl + category);
            HttpURLConnection urlConnection = (HttpURLConnection) targetUrl.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
                String line = "";
                StringBuffer content = new StringBuffer();
                while ((line=reader.readLine())!=null){
                    content.append(line);
                }
                reader.close();

                //士大夫撒

                String moreThanFivePageRegx = "([1-9][0-9]*)\' class=\'last\'>.*?</a>";
                Pattern moreThanFivePagePattern = Pattern.compile(moreThanFivePageRegx);
                Matcher moreThanFivePageMatcher = moreThanFivePagePattern.matcher(content.toString());
                while (moreThanFivePageMatcher.find()) {
                    totalPageNum = Integer.valueOf(moreThanFivePageMatcher.group(1));
                }

                if (totalPageNum == 1) {
                    String notMoreThanFivePageFirstRegx = "class=\'page larger\'>([1-5])";
                    Pattern notMoreThanFivePageFirstPattern = Pattern.compile(notMoreThanFivePageFirstRegx);
                    Matcher notMoreThanFivePageFirstMatcher = notMoreThanFivePageFirstPattern.matcher(content.toString());
                    while (notMoreThanFivePageFirstMatcher.find()) {
                        totalPageNum = Integer.valueOf(notMoreThanFivePageFirstMatcher.group(1));
                    }
                }

                return totalPageNum;
            }
            else {
                return -1;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return 1;
    }

    //获取每一页的文章url和文章名
    public static ArrayList<Map<String,String>> getArticalUrlsOfEachPage (URL url,String category,String pageNum) {
        ArrayList<Map<String,String>> articalIds = new ArrayList<Map<String,String>>();
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if ( 200 == urlConnection.getResponseCode()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
                String line = "";
                StringBuffer content = new StringBuffer();
                while ((line=reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();

                String articalInfoLineRegx = "<h3 class=\"entry-title\"><a href=\"(http://drops.wooyun.org/.{3,50})\" rel=\"bookmark\"" +
                        " title=\".{2,100}>(.{2,100})</a></h3>";
                Pattern articalInfoLinePattern = Pattern.compile(articalInfoLineRegx);
                Matcher articalInfoLineMatcher = articalInfoLinePattern.matcher(content.toString());
                while (articalInfoLineMatcher.find()) {
                    System.out.println(articalInfoLineMatcher.group(1));
                    System.out.println(articalInfoLineMatcher.group(2));

                    Map<String,String> argus = new HashMap<String, String>();
                    argus.put("articalUrl",articalInfoLineMatcher.group(1));
                    //argus.put("path","/Users/Y/Documents/wooyun");
                    argus.put("path","C:\\wooyundrops");
                    argus.put("catalog",category);
                    argus.put("pageNum",pageNum);
                    argus.put("articalName",articalInfoLineMatcher.group(2));

                    articalIds.add(argus);
                }
                return articalIds;

            }
            else {
                return null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    //根据文章id生成html文件
    public static void makeArticalHtml (Map<String,String> argus) {

        String articalUrl = argus.get("articalUrl");
        String path = argus.get("path");
        String catalog = argus.get("catalog");
        String page = argus.get("pageNum");
        String articalName = argus.get("articalName");
        try {
            URL targetUrl = new URL(articalUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) targetUrl.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
                String line = "";
                StringBuffer content = new StringBuffer();
                while ((line=reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();

                File targetCatalog = new File(path + "/" + catalog);
                File targetCatalogPage = new File(path + "/" + catalog + "/" + page);
                File articalHtml = new File(path + "/" + catalog + "/" + page + "/" + articalName+".html");
                if (!targetCatalog.exists()) {
                    targetCatalog.mkdir();
                }
                if (!targetCatalogPage.exists()) {
                    targetCatalogPage.mkdir();
                }
                if (!articalHtml.exists()) {
                    articalHtml.createNewFile();
                    OutputStream writer = new FileOutputStream(articalHtml);
                    writer.write(content.toString().getBytes());
                    writer.close();
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    //通过jsoup生成html
    public static void getHtmlByJsoup (Map<String,String> argus) {

        String articalUrl = argus.get("articalUrl");
        String path = "c:\\wooyundrop";
        String catalog = argus.get("catalog");
        String page = argus.get("pageNum");
        String articalName = argus.get("articalName").replaceAll(":|<|>|\\*|\\?|/"," ");

//        String articalUrl = "http://drops.wooyun.org/papers/5460";
//        String path = "C:\\wooyundrops";
//        String catalog = "papers";
//        String page = "19";
//        String articalName = "Exploiting CVE-2015-0311, Part II: Bypassing Control Flow Guard on Windows 8.1".replaceAll(":|<|>|\\*|\\?|/"," ");

        File targetCatalog = new File(path + "\\" + catalog);
        File targetCatalogPage = new File(path + "\\" + catalog + "\\" + page);
        File targetCatalogPageImg = new File(path + "\\" + catalog + "\\" + page + "\\" + "img");
        File articalHtml = new File(path + "\\" + catalog + "\\" + page + "\\" + articalName + ".html");

        if (!targetCatalog.exists()) {
            targetCatalog.mkdir();
        }
        if (!targetCatalogPage.exists()) {
            targetCatalogPage.mkdir();
        }
        if (!targetCatalogPageImg.exists()) {
            targetCatalogPageImg.mkdir();
        }

        try{
            Document doc = Jsoup.connect(articalUrl).get();
            String title = doc.title();

            //去掉不需要的部分
            Elements navvarmobile = doc.getElementsByClass("navbar-wooyun");
            Element newcomment = doc.getElementById("new_comment");
            navvarmobile.html("");
            newcomment.html("");

            //获取页面的img集合，下载下来，修改img的src属性为相对位置
            Elements imgs = doc.select("img");
            String imgReg = "http://static.wooyun.org/{1,2}drops/*";
            Pattern imgPattern = Pattern.compile(imgReg);

            for (Element img : imgs) {
                String imgUrl = img.attr("src");
                Matcher m = imgPattern.matcher(imgUrl);
                if (m.find()){
                    String fileName = getUrlFileName(imgUrl);
                    boolean downFile = downFile(imgUrl,targetCatalogPageImg.getPath());
                    if (downFile) {
                        img.attr("src","./"+fileName);
                    }

                }
            }
            String html = doc.html();
            articalHtml.createNewFile();
            OutputStream writer = new FileOutputStream(articalHtml);
            writer.write(html.getBytes());
            writer.close();

        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println(articalHtml.getName() + " failed");
        }

    }

    //获取url里面的文件名
    public static String getUrlFileName(String url) {
        return url.split("/")[url.split("/").length - 1];
    }

    //通过url下载文件
    public static boolean downFile(String url,String filePath) {
        String fileName = getUrlFileName(url).replaceAll(":|<|>|\\*|\\?|/"," ");
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) targetUrl.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if ( 200 == responseCode) {
                InputStream jpg = new BufferedInputStream((urlConnection.getInputStream()));
                int len = 0;
                File file = new File(filePath + "\\" + fileName);
                System.out.println(file.getPath());
                file.createNewFile();
                OutputStream writer = new FileOutputStream(file);
                while ( (len = jpg.read()) != -1){
                    writer.write(len);
                }

                writer.close();
                return true;
            }
            else {
                System.out.println("没有成功get到图片" + url);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(fileName + "  failed");
        }
        return false;
    }

    public static void main(String[] args) {

        ArrayList<String> pageOfEachCategory = new ArrayList<String>();
        ArrayList<ArrayList<Map<String,String>>> articalUrlsOfEveryCategory = new ArrayList<ArrayList<Map<String,String>>>();

        for (int i = 0; i < category.length; i++) {
            int totalPage = getTheCategoryPageNum(category[i]);
            pageOfEachCategory.add(String.valueOf(totalPage));
        }

        for (int i = 0; i <pageOfEachCategory.size() ; i++) {
            for (int j = 1; j <= Integer.valueOf(pageOfEachCategory.get(i)); j++) {
                ArrayList<Map<String,String>> articalIdsOfEachPage = new ArrayList<Map<String,String>>();
                try {
                    URL url = new URL( wooyunCategoryUrl +category[i] + "/page/" + j );
                    articalIdsOfEachPage = getArticalUrlsOfEachPage(url,category[i],String.valueOf(j));
                    articalUrlsOfEveryCategory.add(articalIdsOfEachPage);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            for (int k = 0; k < articalUrlsOfEveryCategory.size(); k++) {
                ArrayList<Map<String,String>> articalArgumentsOfEachPage = new ArrayList<Map<String, String>>();
                articalArgumentsOfEachPage = articalUrlsOfEveryCategory.get(k);
                for (int m = 0; m < articalArgumentsOfEachPage.size(); m++) {
                    getHtmlByJsoup(articalArgumentsOfEachPage.get(m));
                }
            }
        }

        System.out.println("done");
    }
}
