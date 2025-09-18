package com.execlib;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import org.apache.commons.io.FileUtils;
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

    private static final Pattern imgPattern = Pattern.compile("<img\\s+src=\"([^\"]+)\"\\s+alt=\"([^\"]+)\"[^>]*>");

    public static void main(String[] args) throws IOException {
        File dirFile = new File(FILE_PATH);
        List<File> files = FileUtil.loopFiles(dirFile, f -> f.getName().endsWith(".md"));
        for (File file : files) {
            replaceImage(file);
        }
    }

    /**
     * 解析，并且替换图片
     * @param file 文件.md
     */
    public static void replaceImage(File file) throws IOException {
        List<String> lines = FileUtils.readLines(file, "utf-8");
        List<String> newLines = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = imgPattern.matcher(line);
            if (matcher.find()) {
                String src = matcher.group(1);  // 提取src属性值
                String alt = matcher.group(2);  // 提取alt属性值
                System.out.println("图片链接: " + src);
                System.out.println("替代文本: " + alt);
                if (alt.contains("金句")) {
                    newLines.add("\n");
                    continue;
                }
                String mkImage = getMkImage(alt, getImagePath(src));
                newLines.add("\n");
                newLines.add(mkImage);
                newLines.add("\n");
                continue;
            }
            newLines.add(line);
        }
        file.delete();
        FileUtil.writeLines(newLines, file, "utf-8");
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
