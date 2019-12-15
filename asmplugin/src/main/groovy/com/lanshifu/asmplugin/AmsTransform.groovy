package com.lanshifu.asmplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.lanshifu.asmplugin.MethodLogAdapter
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class AmsTransform extends Transform {


    private Project project

    public AmsTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "MethodLog"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        println("lxb-getInputTypes")
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        println("lxb-getScopes")
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        println("lxb-transform")
        for (TransformInput input : inputs) {
            for (DirectoryInput dirInput : input.directoryInputs) {//目录中的class文件
                println "lxb-dirInput.name=" + dirInput.name
                readClassWithPath(dirInput.file)
                File dest = outputProvider.getContentLocation(dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(dirInput.file, dest)
            }
            for (JarInput jarInput : input.jarInputs) {//jar（第三方库，module）
                if (jarInput.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS)) {//module library
                    //从module中获取注解信息
//                    readClassWithJar(jarInput)
                }
                copyFile(jarInput, outputProvider)
            }
        }
    }


    //从编译class文件目录找到注解
    void readClassWithPath(File dir) {
        def root = dir.absolutePath
        dir.eachFileRecurse { File file ->
            def filePath = file.absolutePath
            if (!filePath.endsWith(".class")) return
            def className = getClassName(root, filePath)
            if (isSystemClass(className)) return
            hookClass(filePath, className)
        }
    }

    void hookClass(String filePath, String className) {
        ClassReader reader = new ClassReader(new FileInputStream(new File(filePath)))
        ClassWriter cw = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
        MethodLogAdapter adapter = new MethodLogAdapter(cw)
        reader.accept(adapter, ClassReader.EXPAND_FRAMES)
        System.out.println(adapter.changed)
        if (adapter.changed) {
            byte[] bytes = cw.toByteArray()
            FileOutputStream fos = new FileOutputStream(new File(filePath))
            fos.write(bytes)
        }

    }


    //默认排除
    static final DEFAULT_EXCLUDE = [
            '^android\\..*',
            '^androidx\\..*',
            '.*\\.R$',
            '.*\\.R\\$.*$',
            '.*\\.BuildConfig$',
    ]

    //获取类名
    String getClassName(String root, String classPath) {
        return classPath.substring(root.length() + 1, classPath.length() - 6)
                .replaceAll("/", ".")       // unix/linux
                .replaceAll("\\\\", ".")    //windows
    }

    boolean isSystemClass(String fileName) {
        for (def exclude : DEFAULT_EXCLUDE) {
            if (fileName.matches(exclude)) return true
        }
        return false
    }


    void copyFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def dest = getDestFile(jarInput, outputProvider)
        FileUtils.copyFile(jarInput.file, dest)
    }

    static File getDestFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def destName = jarInput.name
        // 重名名输出文件,因为可能同名,会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        // 获得输出文件
        File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        return dest
    }
}