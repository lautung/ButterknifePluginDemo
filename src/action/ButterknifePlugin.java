package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;
import entity.Element;
import org.apache.http.util.TextUtils;
import utils.Util;
import view.FindViewByIdDialog;

import java.util.ArrayList;
import java.util.List;

public class ButterknifePlugin extends AnAction {

    private FindViewByIdDialog mDialog;
    private String xmlFilename;

    @Override
    public void actionPerformed(AnActionEvent e) {
        //1.获取用户选择的layout文件的名字
        Project project=e.getProject();
        //得到编辑区对象
        Editor mEditor=e.getData(PlatformDataKeys.EDITOR);
        if(null==mEditor){
            return;
        }
        //获取用户选择的字符
        SelectionModel model = mEditor.getSelectionModel();
        xmlFilename=model.getSelectedText();
        //如果用户没有选择正确，就弹一个对话框出来做提示
        //如果用户没有选择正确定，就弹一个对话框出来
        if(TextUtils.isEmpty(xmlFilename)){
            //获取光标所在的位置对应的布局文件
            xmlFilename=getCurrentLayout(mEditor);
            if(TextUtils.isEmpty(xmlFilename)){
                //弹一个对话框让用户自己输入
                xmlFilename=Messages.showInputDialog(project,"请输入layout名","未输入",Messages.getInformationIcon());
                if(TextUtils.isEmpty(xmlFilename)){
                    Util.showPopupBalloon(mEditor,"用户没有输入layout",5);
                    return;
                }
            }
        }

        //如果程序能运行到这个位置，就表示已经得到了layout名字了
        //2.找到对应的XML文件，并把XML文件中所有的ID都获取出来并记录下来（保存到一个集合中）
        PsiFile[] psiFiles=FilenameIndex.getFilesByName(project,xmlFilename+".xml",GlobalSearchScope.allScope(project));
        if(psiFiles.length==0){
            Util.showPopupBalloon(mEditor,"未找到选中的布局文件"+xmlFilename,5);
            return;
        }

        //如果找到了XML文件,就去得到这零点文件对应的PSI对象
        XmlFile xmlFile=(XmlFile)psiFiles[0];
        //开始解析XML JDOM  DOM4J  pull
        List<Element> elements=new ArrayList();
        //把得到的ID,包装起来存放到elements
        Util.getIDsFromLayout(xmlFile,elements);

        //3.生成UI,并根据用户的选择生成代码
        if(elements.size()!=0){
            //得到一个psiFile
            PsiFile psiFile=PsiUtilBase.getPsiFileInEditor(mEditor,project);
            PsiClass psiClass = Util.getTargetClass(mEditor, psiFile);
            //生成UI
            mDialog=new FindViewByIdDialog(mEditor,project,psiFile,psiClass,elements,xmlFilename);
            mDialog.showDialog();
        }



    }
    private String getCurrentLayout(Editor editor){
        Document document=editor.getDocument();
        //获取光标对象
        CaretModel caretModel=editor.getCaretModel();
        //得到光标的位置
        int caretOffset=caretModel.getOffset();
        //得到一行开始和结束的位置
        int lineNum=document.getLineNumber(caretOffset);
        int lineStartOffset=document.getLineStartOffset(lineNum);
        int lineEndOffset=document.getLineEndOffset(lineNum);
        //得到一行的所有字符串
        String lineContent=document.getText(new TextRange(lineStartOffset,lineEndOffset));
        String layoutMatching="R.layout.";

        if(!TextUtils.isEmpty(lineContent) && lineContent.contains(layoutMatching)){
            //获取layout文件的字符串
            int startPosition=lineContent.indexOf(layoutMatching)+layoutMatching.length();
            int endPosition=lineContent.indexOf(")",startPosition);
            String layoutStr=lineContent.substring(startPosition,endPosition);
            //return "activity_main"
            return layoutStr;
        }
        return null;

    }

}









