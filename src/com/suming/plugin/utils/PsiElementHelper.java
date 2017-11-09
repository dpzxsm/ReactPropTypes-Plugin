package com.suming.plugin.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;

public class PsiElementHelper {
    public static PsiElement getRealPreElement (PsiElement p){
        PsiElement p1 = p.getPrevSibling();
        if(p1 !=null){
            if(!(p1 instanceof PsiWhiteSpace)){
                return  p1;
            }
            PsiElement p2 = p1.getPrevSibling();
            if(p2 !=null && !( p2 instanceof PsiWhiteSpace)){
                return  p2;
            }
        }
        return null;
    }

    public static PsiElement getRealNextElement (PsiElement p){
        PsiElement p1 = p.getNextSibling();
        if(p1 !=null){
            if(!(p1 instanceof PsiWhiteSpace)){
                return  p1;
            }
            PsiElement p2 = p1.getNextSibling();
            if(p2 !=null && !( p2 instanceof PsiWhiteSpace)){
                return  p2;
            }
        }
        return null;
    }
}
