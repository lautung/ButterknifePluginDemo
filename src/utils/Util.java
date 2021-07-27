package utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import entity.Element;
import org.apache.http.util.TextUtils;

import java.awt.*;
import java.util.Locale;

public class Util {
    // 通过strings.xml获取的值
    private static String stringValue;

    /**
     * 显示dialog
     *
     * @param editor
     * @param result 内容
     * @param time   显示时间，单位秒
     */
    public static void showPopupBalloon(final Editor editor, final String result, final int time) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(116, 214, 238), new Color(76, 112, 117)), null)
                        .setFadeoutTime(time * 1000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }

    /**
     * 驼峰  app_text转换成appText
     *
     * @param fieldName
     * @return
     */
    public static String getFieldName(String fieldName) {
        if (!TextUtils.isEmpty(fieldName)) {
            String[] names = fieldName.split("_");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < names.length; i++) {
                sb.append(firstToUpperCase(names[i]));
            }
            fieldName = sb.toString();
        }
        return fieldName;
    }

    /**
     * 第一个字母大写
     *
     * @param key
     * @return
     */
    public static String firstToUpperCase(String key) {
        return key.substring(0, 1).toUpperCase(Locale.CHINA) + key.substring(1);
    }


    /**
     * 获取所有id
     * @param file
     * @param elements
     * @return
     */
    public static java.util.List<Element> getIDsFromLayout(final PsiFile file, final java.util.List<Element> elements) {
        //遍历一个文件的所有元素(Xml递归元素访问程序)
        file.accept(new XmlRecursiveElementVisitor(){
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                //element就是一个XML文件中的所有节点
                if(element instanceof XmlTag){
                    XmlTag tag=(XmlTag)element;
                    //获取Tag的名字(TextView)或自定义的
                    String name=tag.getName();
                    //如果有include
//                    if(name.equalsIgnoreCase("include")){
//                        //获取布局  LinearLayout
//                        XmlAttribute layout=tag.getAttribute("layout",null);
//                        Project project=file.getProject();
//                        //布局文件
//                        XmlFile include=null;
//                        PsiFile[] psiFiles=FilenameIndex.getFilesByName(project,getLayoutName(layout.getValue())+".xml",GlobalSearchScope.allScope(project));
//                        if(psiFiles.length>0){
//                            include=(XmlFile)psiFiles[0];
//                        }
//                        if(include!=null){
//                            //开始递归
//                            getIDsFromLayout(include,elements);
//                            return;
//                        }
//                    }
                    //获取id属性  android:id="@+id/tvText2"
                    XmlAttribute id=tag.getAttribute("android:id",null);
                    if(id==null){
                        return;
                    }
                    //获取id的值   @+id/tvText2
                    String idValue=id.getValue();
                    if(idValue==null){
                        return;
                    }
                    //获取节点对应的类  比如 TextView  Button
                    XmlAttribute aClass=tag.getAttribute("class",null);
                    if(aClass!=null){
                        //得到类名    "包名.TextView"
                        name=aClass.getValue();
                    }
                    //添加到list中
                    Element e=new Element(name,idValue,tag);
                    elements.add(e);
                }

            }
        });

        return elements;
    }

    /**
     * layout.getValue()返回的值为@layout/layout_view
     * 该方法返回layout_view
     * @param layout
     * @return
     */
    public static String getLayoutName(String layout) {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null;
        }
        // @layout layout_view
        String[] parts = layout.split("/");
        if (parts.length != 2) {
            return null;
        }
        // layout_view
        return parts[1];
    }

    /**
     * 根据当前文件获取对应的psiClass文件
     * @param editor
     * @param file
     * @return
     */
    public static PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }



}
