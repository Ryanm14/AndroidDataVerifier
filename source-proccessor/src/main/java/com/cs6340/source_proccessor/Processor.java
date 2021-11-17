package com.cs6340.source_proccessor;

import com.cs6340.source_annotation.Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Processor extends AbstractProcessor {

    File sourceSinksFile = null;
    OutputStreamWriter writer;
    FileOutputStream fileOutputStream;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        try {
            sourceSinksFile = createSourceSinksFile();
            openStreams();
            writeXmlHeader();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void openStreams() throws FileNotFoundException {
        fileOutputStream = new FileOutputStream(sourceSinksFile, true);
        writer = new OutputStreamWriter(fileOutputStream);
    }

    private void writeXmlHeader() throws IOException {
        writer.append("<sinkSources>\n").append("\t<category id=\"NO_CATEGORY\">\n");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(Source.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (sourceSinksFile == null) {
            return false;
        }

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Source.class)) {
            String paramTypesAndReturnType = element.asType().toString();
            String methodName = element.getSimpleName().toString();
            String classPath = element.getEnclosingElement().toString();

            writeSourceToFile(classPath, methodName, paramTypesAndReturnType);
        }

        writeClosing();

        return false;
    }


    private void writeClosing() {
        try {
            writer.append("\t</category>\n")
                    .append("</sinkSources>");
            writer.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeSourceToFile(String classPath, String methodName, String paramTypesAndReturnType) {
        int paramLastIndex = paramTypesAndReturnType.indexOf(")") + 1;
        String params = paramTypesAndReturnType.substring(0, paramLastIndex);
        String returnType = paramTypesAndReturnType.substring(paramLastIndex);

        String signature = classPath + ": " + returnType + " " + methodName + params;
        String[] paramsList = new String[0];

        if (params.length() > 2) {
            String paramsNoParentheses = params.substring(1, params.length() - 1);
            paramsList = paramsNoParentheses.split(",");
        }

        try {
            writer.append("\t\t<method signature=\"&lt;" + signature + "&gt;\">\n");


            for (int i = 0; i < paramsList.length; i++) {
                String type = paramsList[i];

                writer.append("\t\t\t<param index=\"" + i + "\" type=\"" + type + "\">\n")
                        .append("\t\t\t\t<accessPath isSource=\"false\" isSink=\"false\" />\n")
                        .append("\t\t\t</param>\n");
            }

            writer.append("\t\t\t<return type=\"" + returnType + "\">\n")
                    .append("\t\t\t\t<accessPath isSource=\"true\" isSink=\"false\"/>\n")
                    .append("\t\t\t</return>\n")
                    .append("\t\t</method>\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createSourceSinksFile() throws IOException {
        File file = new File(fetchSourcePath());
        File generatedFolder = file.getParentFile().getParentFile().getParentFile().getParentFile();
        File flowDroidFolder = new File(generatedFolder.getAbsolutePath() + "/flowdroid/");
        File outputFile = new File(flowDroidFolder.getAbsolutePath() + "/source-sinks.xml");

        if (!flowDroidFolder.exists()) {
            flowDroidFolder.mkdir();
        }

        if (outputFile.exists()) {
            outputFile.delete();
        }

        outputFile.createNewFile();

        return outputFile;
    }

    String fetchSourcePath() {
        try {
            JavaFileObject generationForPath = processingEnv.getFiler().createSourceFile("GetPathFor" + getClass().getSimpleName());
            Writer writer = generationForPath.openWriter();
            String sourcePath = generationForPath.toUri().getPath();
            writer.close();
            generationForPath.delete();

            return sourcePath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}