
package servidor;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Criptografias{

    public Criptografias() {
    }
       
        
        static String IV = "AAAAAAAAAAAAAAAA";
        private String chaveencriptacao = "0123456789abcdef";
         
         
                   
    
    //SHA-1 E MD5
   public static String stringHexa(byte[] bytes) {
   StringBuilder s = new StringBuilder();
   for (int i = 0; i < bytes.length; i++) {
       int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
       int parteBaixa = bytes[i] & 0xf;
       if (parteAlta == 0) s.append('0');
       s.append(Integer.toHexString(parteAlta | parteBaixa));
   }
   return s.toString();
}
    
    public static byte[] gerarHash(String frase, String algoritmo) {
  try {
    MessageDigest md = MessageDigest.getInstance(algoritmo);
    md.update(frase.getBytes());
    return md.digest();
  } catch (NoSuchAlgorithmException e) {
    return null;
  }
}

    
    
    
    
    
    //AES
    public String getChaveencriptacao() {
        return chaveencriptacao;
    }
   
    public String encrypt(String textopuro, String chaveencriptacao) throws Exception {
               Cipher encripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
               SecretKeySpec key = new SecretKeySpec(chaveencriptacao.getBytes(), "AES");
               encripta.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes()));
               return new BASE64Encoder().encode(encripta.doFinal(textopuro.getBytes()));
         }
    
    public String decrypt(String textoencriptado, String chaveencriptacao) throws Exception{
               byte[] byteTexto = new BASE64Decoder().decodeBuffer(textoencriptado);
               Cipher decripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
               SecretKeySpec key = new SecretKeySpec(chaveencriptacao.getBytes(), "AES");
               decripta.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes()));
               return new String(decripta.doFinal(byteTexto));
         }

}
