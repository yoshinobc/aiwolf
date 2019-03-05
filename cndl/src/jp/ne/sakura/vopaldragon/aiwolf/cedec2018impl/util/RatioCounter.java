package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RatioCounter {

    public static class Ratio {

        public String name;
        public double total = 0;
        public double trueCount = 0;

        public double ratio() {
            return trueCount / total;
        }

        @Override
        public String toString() {
            return Utils.join(name, ratio(), total);
        }

    }
    
    public Ratio get(String key){
        return map.get(key);
    }
    
    public Set<String> keySet(){
        return map.keySet();
    }

    private Map<String, Ratio> map = new LinkedHashMap<>();

    public void count(String key, boolean result) {
        Ratio p = map.get(key);
        if (p == null) {
            p = new Ratio();
            p.name = key;
            map.put(key, p);
        }
        p.total+=1;
//        System.out.println(key+"\t"+p.total);
        if (result) p.trueCount+=1;
    }

    public void print() {
        List<String> arrayList = new ArrayList<>(map.keySet());
        Collections.sort(arrayList);

        for (String key : arrayList) {
            System.out.println(map.get(key));
        }
    }

}
