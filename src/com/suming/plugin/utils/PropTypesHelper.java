package com.suming.plugin.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropTypesHelper {
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static boolean isBool(String str){
        Pattern pattern = Pattern.compile("true|false");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

    public static  boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    
    public static String getPropTypeByValue(String value){
        String type = "any";
        if(value.startsWith("\"") && value.endsWith("\"")){
            type = "string";
        }else if(value.startsWith("\'") && value.endsWith("\'")){
            type = "string";
        }else if(value.startsWith("{") && value.endsWith("}")){
            type = "object";
        }else if(value.startsWith("[") && value.endsWith("]")){
            type = "array";
        }else if (PropTypesHelper.isBool(value)){
            type = "bool";
        }else if (PropTypesHelper.isNumeric(value)){
            type = "number";
        }
        return type;
    }
}
