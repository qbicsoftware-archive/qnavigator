package qbic.utils;

import java.io.Serializable;
import java.util.Set;

import org.json.JSONArray;

public class JsonHelper implements Serializable{
  private static final long serialVersionUID = 1562589447592073408L;

  public static JSONArray fromSet(Set<String> set){
    JSONArray ret = new JSONArray();
    if(set == null || set.isEmpty()) return ret;
    for(String str: set){
      ret.put(str);
    }
    return ret;
  }
}
