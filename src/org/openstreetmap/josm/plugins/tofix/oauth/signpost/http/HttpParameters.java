package org.openstreetmap.josm.plugins.tofix.oauth.signpost.http;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openstreetmap.josm.plugins.tofix.oauth.signpost.OAuth;

@SuppressWarnings("serial")
public class HttpParameters implements Map<String, SortedSet<String>>, Serializable {

    private TreeMap<String, SortedSet<String>> wrappedMap = new TreeMap<>();

    @Override
    public SortedSet<String> put(String key, SortedSet<String> value) {
        return wrappedMap.put(key, value);
    }

    public SortedSet<String> put(String key, SortedSet<String> values, boolean percentEncode) {
        if (percentEncode) {
            remove(key);
            for (String v : values) {
                put(key, v, true);
            }
            return get(key);
        } else {
            return wrappedMap.put(key, values);
        }
    }

    /**
     * Convenience method to add a single value for the parameter specified by
     * 'key'.
     *
     * @param key
     *        the parameter name
     * @param value
     *        the parameter value
     * @return the value
     */
    public String put(String key, String value) {
        return put(key, value, false);
    }

    /**
     * Convenience method to add a single value for the parameter specified by
     * 'key'.
     *
     * @param key
     *        the parameter name
     * @param value
     *        the parameter value
     * @param percentEncode
     *        whether key and value should be percent encoded before being
     *        inserted into the map
     * @return the value
     */
    public String put(String key, String value, boolean percentEncode) {
         // fix contributed by Bjorn Roche - key should be encoded before wrappedMap.get
         key = percentEncode ? OAuth.percentEncode(key) : key;
         SortedSet<String> values = wrappedMap.get(key);
         if (values == null) {
             values = new TreeSet<>();
             wrappedMap.put( key, values);
         }
         if (value != null) {
             value = percentEncode ? OAuth.percentEncode(value) : value;
             values.add(value);
         }

         return value;
     }

    /**
     * Convenience method to allow for storing null values. {@link #put} doesn't
     * allow null values, because that would be ambiguous.
     *
     * @param key
     *        the parameter name
     * @param nullString
     *        can be anything, but probably... null?
     * @return null
     */
    public String putNull(String key, String nullString) {
        return put(key, nullString);
    }

    @Override
    public void putAll(Map<? extends String, ? extends SortedSet<String>> m) {
        wrappedMap.putAll(m);
    }

    public void putAll(Map<? extends String, ? extends SortedSet<String>> m, boolean percentEncode) {
        if (percentEncode) {
            for (String key : m.keySet()) {
                put(key, m.get(key), true);
            }
        } else {
            wrappedMap.putAll(m);
        }
    }

    public void putAll(String[] keyValuePairs, boolean percentEncode) {
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            this.put(keyValuePairs[i], keyValuePairs[i + 1], percentEncode);
        }
    }

    public void putMap(Map<String, List<String>> m) {
        for (String key : m.keySet()) {
            SortedSet<String> vals = get(key);
            if (vals == null) {
                vals = new TreeSet<>();
                put(key, vals);
            }
            vals.addAll(m.get(key));
        }
    }

    @Override
    public SortedSet<String> get(Object key) {
        return wrappedMap.get(key);
    }

    public String getFirst(Object key) {
        return getFirst(key, false);
    }

    
    public String getFirst(Object key, boolean percentDecode) {
        SortedSet<String> values = wrappedMap.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        String value = values.first();
        return percentDecode ? OAuth.percentDecode(value) : value;
    }

    public String getAsQueryString(Object key) {
    	return getAsQueryString(key, true);
    }

     public String getAsQueryString(Object key, boolean percentEncode) {
        // fix contributed by Stjepan Rajko - we need the percentEncode parameter
        // because some places (like SignatureBaseString.normalizeRequestParameters)
        // need to supply the parameter percent encoded

        StringBuilder sb = new StringBuilder();
        if(percentEncode)
        	key = OAuth.percentEncode((String) key);
        Set<String> values = wrappedMap.get(key);
        if (values == null) {
            return key + "=";
        }
        Iterator<String> iter = values.iterator();
        while (iter.hasNext()) {
            sb.append(key + "=" + iter.next());
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    public String getAsHeaderElement(String key) {
        String value = getFirst(key);
        if (value == null) {
            return null;
        }
        return key + "=\"" + value + "\"";
    }

    @Override
    public boolean containsKey(Object key) {
        return wrappedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Set<String> values : wrappedMap.values()) {
            if (values.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        int count = 0;
        for (String key : wrappedMap.keySet()) {
            count += wrappedMap.get(key).size();
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return wrappedMap.isEmpty();
    }

    @Override
    public void clear() {
        wrappedMap.clear();
    }

    @Override
    public SortedSet<String> remove(Object key) {
        return wrappedMap.remove(key);
    }

    @Override
    public Set<String> keySet() {
        return wrappedMap.keySet();
    }

    @Override
    public Collection<SortedSet<String>> values() {
        return wrappedMap.values();
    }

    @Override
    public Set<Entry<String, SortedSet<String>>> entrySet() {
        return wrappedMap.entrySet();
    }

    public HttpParameters getOAuthParameters() {
        HttpParameters oauthParams = new HttpParameters();

        for (Entry<String, SortedSet<String>> param : this.entrySet()) {
            String key = param.getKey();
            if (key.startsWith("oauth_") || key.startsWith("x_oauth_")) {
                oauthParams.put(key, param.getValue());
            }
        }

        return oauthParams;
    }
}
