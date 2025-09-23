package com.execlib;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown图片替换
 */
public class MarkDownGeneTest {

    private static final String FILE_PATH = "D:\\GitHub\\lwaytogo.github.io\\post\\710_趣学设计模式";

    private static final String IMAGE_PATH = "images";

    private static final Pattern IMG_ALT_PATTERN = Pattern.compile("<img\\s+src=\"([^\"]+)\"\\s+alt=\"([^\"]+)\"[^>]*>");

    private static final Pattern IMG_PATTERN = Pattern.compile("<img\\s+src=\"([^\"]*)\"[^>]*>");

    private static final Pattern LANG_JAVA_PATTERN = Pattern.compile("<pre\\s+class=\"[^\"]*lang-java[^\"]*\"[^>]*>.*?</pre>", Pattern.DOTALL);


    /** 1.解析Markdown文件 */
    @Test
    public void parseMarkdownFileTest() throws IOException {
        File dirFile = new File(FILE_PATH);
        List<File> files = FileUtil.loopFiles(dirFile, f -> f.getName().endsWith(".md"));
        for (File file : files) {
            replaceImage(file);
        }
    }

    /** 2.将markdown中的代码判断解析，原来显示的有问题 */
    @Test
    public void parseMarkDown4CodeTest() throws IOException {
        File dirFile = new File(FILE_PATH);
        List<File> files = FileUtil.loopFiles(dirFile, f -> f.getName().endsWith(".md"));

        for (File file : files) {
            String context = FileUtils.readFileToString(file, "utf-8");
            Matcher matcher = LANG_JAVA_PATTERN.matcher(context);
            while (matcher.find()) {
                String codeClass = matcher.group(0);  // 提取语言类型: lang-java
                System.out.println("code 处理前: " + codeClass);
                String codeJava = "<code data-language=\"java\">";
                String tag = codeClass.contains(codeJava) ? "java" : "";
                String codeClassAfter = "\n```" + tag + "\n" + cleanHtmlTags(codeClass) + "\n```\n";
                System.out.println("code 处理后: " + codeClassAfter);
                context = context.replace(codeClass, codeClassAfter);
            }
            FileUtils.writeStringToFile(file, context, "utf-8");
            System.out.println("parseMarkDown4CodeTest success file:{}" + file.getName());
        }
    }

    /**
     * 去除HTML标签，只保留纯代码内容
     * @param htmlContent 包含HTML标签的内容
     * @return 清理后的纯代码
     */
    private static String cleanHtmlTags(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }

        // 移除<code>和</code>标签
        String result = htmlContent.replaceAll("<code[^>]*>", "").replaceAll("</code>", "");

        // 移除<span>标签及其属性，但保留标签内的内容
        result = result.replaceAll("</?span[^>]*>", "");

        // 移除其他HTML标签
        result = result.replaceAll("<[^>]+>", "");

        // 处理HTML实体
        result = result.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&nbsp;", " ")
                .replace("&#39;", "'");

        return result;
    }

    /**
     * 解析，并且替换图片
     * @param file 文件.md
     */
    public static void replaceImage(File file) throws IOException {
        System.out.println("file:" + file.getName());
        List<String> lines = FileUtils.readLines(file, "utf-8");
        List<String> newLines = new ArrayList<>();
        for (String line : lines) {
            System.out.println("line 转换前:" + line);
            Matcher imgMatcher = IMG_PATTERN.matcher(line);
            if (imgMatcher.find()) {
                getMarkDownNewLines(imgMatcher, newLines);
                continue;
            }
            Matcher imgAltMatcher = IMG_ALT_PATTERN.matcher(line);
            if (imgMatcher.find()) {
                getMarkDownNewLines(imgAltMatcher, newLines);
            }
            newLines.add(line);
        }
        file.delete();
        FileUtil.writeLines(newLines, file, "utf-8");
    }

    /**
     * 正则匹配，获取图片链接，替换为markdown图片
     * @param matcher 图片正则
     * @param newLines 替换图片链接之后的line
     */
    private static void getMarkDownNewLines(Matcher matcher, List<String> newLines) {
        String src = matcher.group(1);  // 提取src属性值
        String alt = "";
        if (matcher.groupCount() >= 2) {
            alt = matcher.group(2);  // 提取alt属性值
        }
        System.out.println("图片链接: " + src);
        System.out.println("替代文本: " + alt);
        if (alt.contains("金句")) {
            newLines.add("\n");
            return;
        }
        String mkImage = getMkImage(alt, getImagePath(src));
        newLines.add("\n");
        newLines.add(mkImage);
        System.out.println("line 转换后:" + mkImage);
        newLines.add("\n");
    }

    private static String getImagePath(String httpUrl) {
        HttpUtil.downloadFileFromUrl(httpUrl, FILE_PATH + "\\" + IMAGE_PATH);
        String fileName = StringUtils.getFilename(httpUrl);
        return fileName;
    }

    /**
     * 获取mk图片路径
     * @return
     */
    private static String getMkImage(String alt, String imgName) {
        return "![" + alt + "](./" + IMAGE_PATH + "/" + imgName + ")";
    }


}
