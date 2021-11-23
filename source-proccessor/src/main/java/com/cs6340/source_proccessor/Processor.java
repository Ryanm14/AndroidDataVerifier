package com.cs6340.source_proccessor;

import com.cs6340.source_annotation.Sink;
import com.cs6340.source_annotation.Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

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
            addDefaultSinks();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addDefaultSinks() throws IOException {
        writer.append(DefaultSinkConfig.CONFIG);
    }

    private void openStreams() throws FileNotFoundException {
        fileOutputStream = new FileOutputStream(sourceSinksFile, true);
        writer = new OutputStreamWriter(fileOutputStream);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(Source.class.getCanonicalName());
        annotations.add(Sink.class.getCanonicalName());
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

            writeAnnotationToFile(classPath, methodName, paramTypesAndReturnType, "_SOURCE_");
        }

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Sink.class)) {
            String paramTypesAndReturnType = element.asType().toString();
            String methodName = element.getSimpleName().toString();
            String classPath = element.getEnclosingElement().toString();

            writeAnnotationToFile(classPath, methodName, paramTypesAndReturnType,  "_SINK_");
        }

        writeClosing();

        return false;
    }


    private void writeClosing() {
        try {
            writer.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeAnnotationToFile(String classPath, String methodName, String paramTypesAndReturnType, String stringType) {
        int paramLastIndex = paramTypesAndReturnType.indexOf(")") + 1;
        String params = paramTypesAndReturnType.substring(0, paramLastIndex);
        String returnType = paramTypesAndReturnType.substring(paramLastIndex);

        String signature = classPath + ": " + returnType + " " + methodName + params;

        try {
            writer.append("<").append(signature).append("> -> ").append(stringType).append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createSourceSinksFile() throws IOException {
        File file = new File(fetchSourcePath());
        File generatedFolder = file.getParentFile().getParentFile().getParentFile().getParentFile();
        File flowDroidFolder = new File(generatedFolder.getAbsolutePath() + "/flowdroid/");
        File outputFile = new File(flowDroidFolder.getAbsolutePath() + "/source-sinks.txt");

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