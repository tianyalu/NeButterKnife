package com.sty.ne.butterknife.compiler;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;
import com.sty.ne.butterknife.annotations.BindView;
import com.sty.ne.butterknife.annotations.OnClick;

/**
 * Created by tian on 2019/10/30.
 */

// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，是Google开发的
@AutoService(Processor.class) //相当于Activity在Manifest中注册
// 提供注解的方式赋值
public class ButterKnifeProcessor extends AbstractProcessor {
    private Messager messager; //相当于Android中的Log
    private Filer filer; //文件生成器，用来创建新的源文件，class文件以及辅助文件
    private Elements elementUtils; //Elements中包含用于操作Element的工具方法


    /**
     * 初始化方法，只会执行一次，相当于onCreate
     * 通过该方法的参数ProcessingEnvironment可以获取一系列有用的工具类
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
        //注解处理器中不能写E,因为代表异常、中断
//        messager.printMessage(Diagnostic.Kind.ERROR, "初始化完成");
        messager.printMessage(Diagnostic.Kind.NOTE, "初始化完成");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        //添加支持BindView注解的类型
        types.add(BindView.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        //types.add(...);
        return super.getSupportedAnnotationTypes();
    }

    // 因为是在JDK环境编译，指定编译的版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        //返回此注释 Processor 支持的最新的源版本，该方法可以通过注解@SupportedSourceVersion指定
        return SourceVersion.latestSupported();
    }

    /**
     * 注解处理器的核心方法，处理具体的注解，生成Java文件 ->> MainActivityTest$ViewBinder
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "开始处理注解，生成代码和类文件");

        // 获取MainActivity中所有带BindView注解的属性

        //获取MainActivity中所有带@BindView注解的属性元素集合
        Set<? extends Element> bindViewSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        if(bindViewSet.isEmpty()) {
            return false;
        }
        //保存键值对，key 是com.sty.ne.butterknife.MainActivity value是所有带BindView注解的属性集合
        Map<String, List<VariableElement>> bindViewMap = new HashMap<>();
        //遍历所有带BindView注解的属性
        for (Element element : bindViewSet) {
            //转成原始属性元素（结构体元素）
            VariableElement variableElement = (VariableElement) element;
            //通过属性元素获取它所属的MainActivity类名，如：com.sty.ne.butterknife.MainActivity
            String activityName = getActivityName(variableElement);
            //从缓存集合中获取MainActivity所有带BindView注解的属性集合
            List<VariableElement> list = bindViewMap.get(activityName);
            if(list == null) {
                list = new ArrayList<>();
                //先加入map集合，引用变量list可以动态改变值
                bindViewMap.put(activityName, list);
            }
            //将MainActivity所有带BindView注解的属性加入到list集合
            list.add(variableElement);
            //测试打印：每个属性的名字
            System.out.println("variableElement >>> " + variableElement.getSimpleName().toString());
        }

        System.out.println("所有带OnClick注解的方法---------------------------------> " );
        //获取MainActivity中所有带OnClick注解的方法
        Set<? extends Element> onClickSet = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        //保存键值对，key是com.sty.ne.butterknife.MainActivity value是所有带OnClick注解的方法集合
        Map<String, List<ExecutableElement>> onClickMap = new HashMap<>();
        //遍历所有带OnClick注解的方法
        for (Element element : onClickSet) {
            //转成原始属性元素（结构体元素）
            ExecutableElement executableElement = (ExecutableElement) element;
            //通过属性元素获取它所属的MainActivity类名，如：com.sty.ne.butterknife.MainActivity
            String activityName = getActivityName(executableElement);
            //从缓存集合中获取MainActivity所有带OnClick注解的方法集合
            List<ExecutableElement> list = onClickMap.get(activityName);
            if(list == null) {
                list = new ArrayList<>();
                //先加入map集合，引用变量list可以动态改变值
                onClickMap.put(activityName, list);
            }
            //将MainActivity所有带OnClick注解的方法加入到list集合
            list.add(executableElement);
            //测试打印：每个方法的名字
            System.out.println("executableElement >>> " + executableElement.getSimpleName().toString());
        }


        //---------------------------------造币过程-----------------------------------
        //获取Activity完整的字符串类名（包名 + 类名）
        for (String activityName : bindViewMap.keySet()) {
            //获取“com.sty.ne.butterknife.MainActivity” 中所有控件属性的集合
            List<VariableElement> cacheElements = bindViewMap.get(activityName);
            List<ExecutableElement> clickElements = onClickMap.get(activityName);

            try {
                //创建一个新的源文件（Class）,并返回一个对象以允许写入它
                JavaFileObject javaFileObject = filer.createSourceFile(activityName + "$ViewBinder");
                //通过属性标签获取包名标签（任意一个属性标签的父节点都是同一个包名）
                String packageName = getPackageName(cacheElements.get(0));
                //定义Writer对象，开启造币过程
                Writer writer = javaFileObject.openWriter();

                //类名：MainActivity$ViewBinder,不是com.sty.ne.butterknife.MainActivity$ViewBinder
                //通过属性元素获取它所属的MainActivity类名，再拼接后结果为：MainActivity$ViewBinder
                String activitySimpleName = cacheElements.get(0).getEnclosingElement()
                        .getSimpleName().toString() + "$ViewBinder";
                System.out.println("activityName >>> " + activityName + "\nactivitySimpleName >>> "
                        + activitySimpleName);

                System.out.println("开始造币 --------------------------------------->");
                //第一行生成包
                writer.write("package " + packageName + ";\n");
                //第二行生成要导入的接口类（必须手动导入）
                writer.write("import com.sty.ne.butterknife.library.ViewBinder;\n");
                writer.write("import com.sty.ne.butterknife.library.CustomClickListener;\n");
                writer.write("import android.view.View;\n");

                //第三行生成类
                writer.write("public class " + activitySimpleName + " implements ViewBinder<" +
                        activityName + "> {\n");
                //第四行生成bind方法
                writer.write("public void bind(final " + activityName + " target) {\n");

                System.out.println("每个控件属性 -------------------------------------->");
                //循环生成MainActivity每个控件属性
                for (VariableElement variableElement : cacheElements) {
                    //控件属性名
                    String fieldName = variableElement.getSimpleName().toString();
                    //获取控件的注解
                    BindView bindView = variableElement.getAnnotation(BindView.class);
                    //获取控件注解的id值
                    int id = bindView.value();
                    //生成：target.tv = target.findViewById(xxx);
                    writer.write("target." + fieldName + " = " + "target.findViewById(" + id + ");\n");
                }

                System.out.println("每个点击事件 -------------------------------------->");
                //循环生成MainActivity每个点击事件
                for (ExecutableElement executableElement : clickElements) {
                    //获取方法名
                    String methodName = executableElement.getSimpleName().toString();
                    //获取方法的注释
                    OnClick onClick = executableElement.getAnnotation(OnClick.class);
                    //获取方法注释的id值
                    int id = onClick.value();
                    //获取方法参数
                    List<? extends VariableElement> parameters = executableElement.getParameters();

                    //生成点击事件
                    writer.write("target.findViewById(" + id + ").setOnClickListener(new CustomClickListener() {\n");
                    writer.write("public void doClick(View view) {\n");
                    if(parameters.isEmpty()) {
                        writer.write("target." + methodName + "();\n}\n});\n");
                    }else {
                        writer.write("target." + methodName + "(view);\n}\n});\n");
                    }
                }

                //最后结束标签，造币完成
                writer.write("\n}\n}");
                System.out.println("结束 ----------------------------------------------->");
                writer.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
//        try {
//            //创建一个类文件，放置在：com.sty.ne.butterknife包下面
//            JavaFileObject javaFileObject = filer.createSourceFile(
//                    "com.sty.ne.butterknife.MainActivity$VeiwBinder");
//            Writer writer = javaFileObject.openWriter();
//
//            writer.write("package com.sty.ne.butterknife;\n");
//
//            writer.write("public class MainActivityTest$ViewBinder implements ViewBinder<MainActivity> {\n\n");
//            writer.write("@Override\n");
//            writer.write("public void bind(final MainActivity target) {\n");
//            writer.write("target.tv = target.findViewById(R.id.tv);\n\n");
//            writer.write("target.tv.setOnClickListener(new CustomClickListener() {\n");
//            writer.write("@Override\n");
//            writer.write("public void doClick(View v) {\n");
//            writer.write("target.click();\n");
//            writer.write("}\n");
//            writer.write("});\n");
//            writer.write("}\n");
//            writer.write("}\n");
//
//            writer.close();
//
//            messager.printMessage(Diagnostic.Kind.NOTE, "写完了。。。");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return true;
    }

    private String getActivityName(VariableElement variableElement) {
        //通过书信标签获取类名标签，再通过类名标签获取包名标签
        String packageName = getPackageName(variableElement);
        //通过属性标签获取类名标签
        TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
        //完整字符串拼接：com.sty.ne.butterknife + "." + MainActivity
        return packageName + "." + typeElement.getSimpleName().toString();
    }

    //通过书信标签获取类名标签，再通过类名标签获取包名标签（通过属性节点找到父节点，再找到父节点的父节点）
    private String getPackageName(VariableElement variableElement) {
        //通过属性标签获取类名标签
        TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
        // 通过类名标签获取包名标签
        String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        System.out.println("packageName >>> " + packageName);
        return packageName;
    }

    private String getActivityName(ExecutableElement executableElement) {
        //通过方法标签获取类名标签，再通过类名标签获取包名标签
        String packageName = getPackageName(executableElement);
        //通过方法标签获取类名标签
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        //完整字符串拼接： com.sty.ne.butterknife + "." + MainActivity
        return packageName + "." + typeElement.getSimpleName().toString();
    }

    private String getPackageName(ExecutableElement executableElement) {
        //通过方法标签获取类名标签
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        //通过类名标签获取包名标签
        String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        System.out.println("packageName >>> " + packageName);
        return packageName;
    }
}
