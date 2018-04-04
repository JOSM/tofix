package org.openstreetmap.josm.plugins.tofix.util;

import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author ridixcr
 */
public class Crypto {
    public static String encodeBASE64(String data){    
        return new String(encodeBinaryBASE64(data.getBytes()));
    }
    public static byte[] encodeBinaryBASE64(byte[] data){
        return DatatypeConverter.printBase64Binary(data).getBytes();
    }
    public static String decodeBASE64(String data){
        return new String(decodeBASE64Binary(data.getBytes()));
    }
    public static byte[] decodeBASE64Binary(byte[] data){
        return DatatypeConverter.parseBase64Binary(new String(data));
    }
}
