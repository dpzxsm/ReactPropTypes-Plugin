package com.suming.plugin.utils;

import com.intellij.lang.ecmascript6.psi.ES6Class;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.impl.JSVarStatementBase;
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

    public static PsiElement getRealFirstChild (ES6Class es6Class){
        PsiElement[] children = es6Class.getChildren();
        if(children.length>2){
           if((children[2] instanceof JSFunction)|| children[2] instanceof JSVarStatementBase){
               return children[2];
           }
        }
        if(children.length>3){
            if((children[3] instanceof JSFunction)|| children[3] instanceof JSVarStatementBase){
                return children[3];
            }
        }
        return null;
    }
}
