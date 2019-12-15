package com.lanshifu.asmplugin;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Set;

//1.实现 Plugin 接口，自定义插件

//2.每个 Transform 其实都是一个 Gradle task，他们链式组合，
// 前一个的输出作为下一个的输入，而我们自定义的 Transform 是作为第一个 task 最先执行的。

public class AsmPlugin  implements Plugin<Project> {



    //Plugin 接口的实现方法，打印一下
    @Override
    public void apply(Project project) {
        System.out.println("----------- 蓝师傅Plugin 开始注册 >>>>> -----------");

//        project.extensions.create("traceMan", TraceManConfig)
//
//        def android = project.extensions.getByType(AppExtension)
//        android.registerTransform(new TraceManTransform(project))

    }
}
