package com.xk.annotation_compile;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.xk.annotation_lib.BindView;
import com.xk.annotation_lib.BindView1;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by xuekai on 2017/11/20.
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private Types typeUtils;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //可处理的注解的集合
        HashSet<String> annotations = new HashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        annotations.add(BindView1.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.err.println("process");
        //key为一个类（typeElement），value为这个类里被BindView注解的view的信息
        Map<TypeElement, List<BindViewInfo>> bindViewMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(BindView.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                error("注解必须要在field上", element);
                return false;
            }

            //注解上的viewId
            int viewId = element.getAnnotation(BindView.class).value();
            VariableElement viewElement = (VariableElement) element;
            //该注解所属的类
            TypeElement typeElement = ((TypeElement) viewElement.getEnclosingElement());


            if (!bindViewMap.containsKey(typeElement)) {
                bindViewMap.put(typeElement, new ArrayList<>());
            }

            List<BindViewInfo> bindViewInfos = bindViewMap.get(typeElement);
            bindViewInfos.add(new BindViewInfo(viewId, viewElement.getSimpleName().toString(), viewElement.asType()));
        }
        bindViewMap.forEach((typeElement, bindViewInfos) -> {
            System.err.println("↓↓↓↓↓↓↓↓" + typeElement);
            for (BindViewInfo bindViewInfo : bindViewInfos) {
                System.err.println("bindViewInfo" + bindViewInfo);
            }
        });

//        generateCodeByStringBuffer(bindViewMap);
        generateCodeByJavaPoet(bindViewMap);
        return false;
    }

    private void generateCodeByStringBuffer(Map<TypeElement, List<BindViewInfo>> bindViewMap) {
        bindViewMap.forEach((typeElement, bindViewInfos) -> {
            generateJavaClassBySb(typeElement, bindViewInfos);
        });
    }

    private void generateJavaClassBySb(TypeElement typeElement, List<BindViewInfo> bindViewInfos) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("package ");
            sb.append(elementUtils.getPackageOf(typeElement).getQualifiedName() + ";\n");
            sb.append("import com.xk.butterknifelib.ViewBinder;\n");
            sb.append("public class " + typeElement.getSimpleName() + "$$ViewBinder<T extends " + typeElement.getSimpleName() + "> implements ViewBinder<T> {\n");
            sb.append("@Override\n");
            sb.append("public void bind(T activity) {\n");

            for (BindViewInfo bindViewInfo : bindViewInfos) {
                sb.append("activity." + bindViewInfo.name + "=activity.findViewById(" + bindViewInfo.id + ");\n");

            }
            sb.append("}\n}");


            JavaFileObject sourceFile = filer.createSourceFile(typeElement.getQualifiedName().toString() + "$$ViewBinder");
            Writer writer = sourceFile.openWriter();
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateCodeByJavaPoet(Map<TypeElement, List<BindViewInfo>> bindViewMap) {
        bindViewMap.forEach((typeElement, bindViewInfos) -> {
            generateJavaClassByJavaPoet(typeElement, bindViewInfos);
        });
    }


//    public class AutoSampleCode<T extends MainActivity> implements ViewBinder<T>{
//        @Override
//        public void bind(T activity) {
//            activity.textView=activity.findViewById(R.id.textView);
//        }
//    }

    /**
     * @param typeElement   类的节点（MainActivity那个节点）
     * @param bindViewInfos
     */
    private void generateJavaClassByJavaPoet(TypeElement typeElement, List<BindViewInfo> bindViewInfos) {
        String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();

        ClassName t = ClassName.bestGuess("T");
        ClassName viewBinder = ClassName.bestGuess("com.xk.butterknifelib.ViewBinder");
//        ParameterSpec.builder(Typen)
        //方法
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(t, "activity");
        MethodSpec methodSpec;
        for (BindViewInfo bindViewInfo : bindViewInfos) {
            methodSpecBuilder.addStatement("activity.$L=activity.findViewById($L)", bindViewInfo.name,bindViewInfo.id);
        }
        methodSpec = methodSpecBuilder.build();
        //类
        TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName() + "$$ViewBinder")//设置类名
                .addModifiers(Modifier.PUBLIC)//添加修饰符
                .addTypeVariable(TypeVariableName.get("T", TypeName.get(typeElement.asType())))//添加泛型声明
                .addMethod(methodSpec)//添加方法
                .addSuperinterface(ParameterizedTypeName.get(viewBinder, t))//添加实现接口
                .build();


        //通过包名和TypeSpec（类）生成一个java文件
        JavaFile build = JavaFile.builder(packageName, typeSpec).build();
        try {
            //写入到filer中
            build.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void error(String msg, Element e) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg),
                e);
    }

    /**
     * 一个被bindview注解的view字段的信息
     */
    class BindViewInfo {
        int id;
        String name;
        TypeMirror typeMirror;

        public BindViewInfo(int id, String name, TypeMirror typeMirror) {
            this.id = id;
            this.name = name;
            this.typeMirror = typeMirror;
        }

        @Override
        public String toString() {
            return "BindViewInfo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", typeMirror=" + typeMirror +
                    '}';
        }
    }
}
