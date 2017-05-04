package uk.co.claytapp.taggerbath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Sam on 08/02/2017.
 */

public class BiHashMap<K1, K2, V> {

    private final Map<K1, Map<K2, V>> mMap;

    public BiHashMap() {
        mMap = new HashMap<K1, Map<K2, V>>();
    }


    public V put(K1 key1, K2 key2, V value) {
        Map<K2, V> map;
        if (mMap.containsKey(key1)) {
            map = mMap.get(key1);
        } else {
            map = new HashMap<K2, V>();
            mMap.put(key1, map);
        }

        return map.put(key2, value);
    }


    public V get(K1 key1, K2 key2) {
        if (mMap.containsKey(key1)) {
            return mMap.get(key1).get(key2);
        } else {
            return null;
        }
    }


    public boolean containsKeys(K1 key1, K2 key2) {
        return mMap.containsKey(key1) && mMap.get(key1).containsKey(key2);
    }

    public void clear() {
        mMap.clear();
    }

    public Iterator<Map.Entry<K1, Map<K2, V>>> getIterator(){
        return mMap.entrySet().iterator();

    }

    public Iterators<V> getIterators(){
        ArrayList<Iterator<V>> al = new ArrayList<Iterator<V>>();
        for (Iterator<Map<K2, V>> it = mMap.values().iterator() ; it.hasNext(); ) {
            al.add(it.next().values().iterator());
        }
        return new Iterators<V>(al);
    }

    public void remove(K1 key1, K2 key2){
        if (mMap.containsKey(key1)) {
            mMap.get(key1).remove(key2);

            if (mMap.get(key1).isEmpty()){
                mMap.remove(key1);
            }
        }

    }

    public int size(){
        int count = 0;

        for (Iterator<Map<K2, V>> it = mMap.values().iterator(); it.hasNext(); ){
            count += it.next().size();
        }

        return count;
    }



}
