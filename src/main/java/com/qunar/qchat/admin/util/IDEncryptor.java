package com.qunar.qchat.admin.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 通用加密解密工具，支持Integer加密解密。生成10位加密结果
 *
 * @since 1.0.0
 * @author dujia-tts
 * @author kris.zhang
 */
public class IDEncryptor {

	private final static Logger logger = LoggerFactory.getLogger(IDEncryptor.class);
	
	//TODO use object pool to reuse IDEncryptor
	//Take care! IDEncryptor is not thread safe
	//so to resue IDEncryptor, we have to set up
	//a pool, maybe we should consider apache common pool
	private static IDEncryptor getInstance(){
		return new IDEncryptor(DEFAULT_KEY);
	}
	
    //////////////////
    // Private Data //
    //////////////////

    /** Non-zero default key, from www.random.org */
    private final static int DEFAULT_KEY = 0x6CFB18E2;
    private static long LARGE_VAL = 1l<<32;
    private final static int LOW_16_MASK = 0xFFFF;
    private final static int HALF_SHIFT = 16;
    private final static int NUM_ROUNDS = 4;

    /** Permutation key */
    private int mKey;

    /** Round key schedule */
    private int[] mRoundKeys = new int[NUM_ROUNDS];

    //////////////////
    // Constructors //
    //////////////////
    private IDEncryptor() { this(DEFAULT_KEY); }

    private IDEncryptor(int key) { setKey(key); }

    ////////////////////
    // Public Methods //
    ////////////////////
    /** Sets a new value for the key and key schedule. */
    private void setKey(int newKey) {
        assert (NUM_ROUNDS == 4) : "NUM_ROUNDS is not 4";
        mKey = newKey;

        mRoundKeys[0] = mKey & LOW_16_MASK;
        mRoundKeys[1] = ~(mKey & LOW_16_MASK);
        mRoundKeys[2] = mKey >>> HALF_SHIFT;
        mRoundKeys[3] = ~(mKey >>> HALF_SHIFT);
    } // end setKey()

    /** Returns the current value of the key. */
    private int getKey() { return mKey; }

    /**
     * Calculates the enciphered (i.e. permuted) value of the given integer
     * under the current key.
     *
     * @param plain the integer to encipher.
     *
     * @return the enciphered (permuted) value.
     */
    private long encipher(int plain) {
        // 1 Split into two halves.
        int rhs = plain & LOW_16_MASK;
        int lhs = plain >>> HALF_SHIFT;  // >>>表示无符号右移位

        // 2 Do NUM_ROUNDS simple Feistel rounds.
        for (int i = 0; i < NUM_ROUNDS; ++i) {
            if (i > 0) {
                // Swap lhs <-> rhs
                final int temp = lhs;
                lhs = rhs;
                rhs = temp;
            } // end if
            // Apply Feistel round function F().
            rhs ^= F(lhs, i);
        } // end for

        // 3 Recombine the two halves and return.
        long v= (lhs << HALF_SHIFT) + (rhs & LOW_16_MASK);
        //把负数变成正数
        if(v<0){
        	v+=LARGE_VAL;
        }
        return v;
    } // end encipher()


    /**
     * Calculates the deciphered (i.e. inverse permuted) value of the given
     * integer under the current key.
     * @return the deciphered (inverse permuted) value.
     */
    private int decipher(long cypherlong) {
    	int cypher=0;
    	if(cypherlong>Integer.MAX_VALUE){
    		cypher=(int)(cypherlong-LARGE_VAL);
    	}else{
    		cypher=(int)cypherlong;
    	}
        // 1 Split into two halves.
        int rhs = cypher & LOW_16_MASK;
        int lhs = cypher >>> HALF_SHIFT;

        // 2 Do NUM_ROUNDS simple Feistel rounds.
        for (int i = 0; i < NUM_ROUNDS; ++i) {
            if (i > 0) {
                // Swap lhs <-> rhs
                final int temp = lhs;
                lhs = rhs;
                rhs = temp;
            } // end if
            // Apply Feistel round function F().
            rhs ^= F(lhs, NUM_ROUNDS - 1 - i);
        } // end for

        // 4 Recombine the two halves and return.
        return (lhs << HALF_SHIFT) + (rhs & LOW_16_MASK);
    } // end decipher()

    /////////////////////
    // Private Methods //
    /////////////////////

    // The F function for the Feistel rounds.
    private int F(int num, int round) {
        // XOR with round key.
        num ^= mRoundKeys[round];
        // Square, then XOR the high and low parts.
        num *= num;
        return (num >>> HALF_SHIFT) ^ (num & LOW_16_MASK);
    } // end F()

    private String encryptWithoutException(int id) {
    	String str = null;
    	try {
    		str = this.encrypt(id);
    	} catch(Exception e) {
    		logger.warn("error in encrypt. id:" + id);
    		// e.printStackTrace();
    	}
    	return str;
    }

    private int decryptWithoutException(String str) {
    	int id = -1;
    	try {
   	 		id = this.decipher(Long.valueOf(str));
    	} catch(Exception e) {
    		logger.warn("error in decrypt. str:" + str);
    		// e.printStackTrace();
    	}
    	return id;
    }

    /**
     * 产生异常的解密方式
     *
     * @param str
     * @return
     * @throws Exception
     */
    public static int decrypt(String str) throws Exception{
    	return getInstance().decipher(Long.valueOf(str));
    }

    /**
     * 进行批量的解密
     *
     * @param ids
     * @return
     * @exception Exception 其中有一个id出错就抛出异常
     */
    public static List<Integer> decrypt(List<String> ids) throws Exception {
        List<Integer> list = Lists.newArrayList();
        for (String id : ids) {
            list.add(decrypt(id));
        }
        return list;
    }

    /**
     * 批量解密id，这些id使用separator进行分割。
     *
     * @param ids
     * @param separator
     * @return
     * @throws Exception
     */
    public static List<Integer> decrypt(String ids, String separator) throws Exception {
        List<Integer> list = Lists.newArrayList();
        for (String id : Splitter.on(separator).trimResults().omitEmptyStrings().split(ids)) {
            list.add(decrypt(id));
        }
        return list;
    }

    /**
     * 产生异常的加密方式
     *
     * @param id
     * @return
     * @throws Exception
     */
    public static String encrypt(int id) throws Exception{
        return "" + getInstance().encipher(id);
    }

    /**
     * 进行批量的加密
     *
     * @param ids
     * @return
     */
    public static List<String> encrypt(List<Integer> ids) throws Exception {
        List<String> list = Lists.newArrayList();
        for (Integer id : ids) {
            list.add(encrypt(id));
        }
        return list;
    }

    /**
     * 进行批量的加密，将加密结果拼接成一个字符串，使用separator指定
     *
     * @param ids
     * @param separator
     * @return
     */
    public static String encrypt(List<Integer> ids, String separator) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< ids.size(); i++) {
            if (i != 0 ) {
                sb.append(separator);
            }
            sb.append(encrypt(ids.get(i)));
        }
        return sb.toString();
    }

    /**
     * 使用源有方式，代码太长，很不好看，因此这里提供了encode，用来代替
     * IDEncryptor.getInstance().encryptWithoutException()
     * 现在只需
     * import static com.qunar.vacation.b2c.util.IDEncryptor.*;
     * 直接使用encode和decode即可
     *
     * {@link #encryptWithoutException(int)}
     * @param id
     * @return 如果错误的id，那么将会返回null
     */
    public static String encode(int id) {
        return getInstance().encryptWithoutException(id);
    }

    /**
     * 加密id的便捷方法
     *
     * @param id
     *
     * {@link #encryptWithoutException(int)}
     * @return   如果id==null那么返回null
     *           如果产生异常那么返回null
     */
    public static String encode(Integer id) {
        if (id == null) return null;
        return getInstance().encryptWithoutException(id);
    }

    /**
     * 进行批量的加密
     *
     * @param ids
     * @return
     */
    public static List<String> encode(List<Integer> ids) {
        List<String> list = Lists.newArrayList();
        for (Integer id : ids) {
            list.add(encode(id));
        }
        return list;
    }

    /**
     * 进行批量的加密，将加密结果拼接成一个字符串，使用separator指定
     *
     * @param ids
     * @param separator
     * @return
     */
    public static String encode(List<Integer> ids, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< ids.size(); i++) {
            if (i != 0 ) {
                sb.append(separator);
            }
            //tolerate the null
            String result = encode(ids.get(i));
            if (!Strings.isNullOrEmpty(result)) {
                sb.append(result);
            }
        }
        return sb.toString();
    }

    /**
     * 解密id的便捷方法
     *
     * {@link #decryptWithoutException(String)}
     * @param id
     * @return 如果id==null，那么返回null
     *         如果id发生错误那么返回-1
     */
    public static Integer decode(String id) {
        if (Strings.isNullOrEmpty(id)) return null;
        return getInstance().decryptWithoutException(id);
    }

    /**
     * 进行批量的解密
     *
     * @param ids
     * @return
     */
    public static List<Integer> decode(List<String> ids) {
        List<Integer> list = Lists.newArrayList();
        for (String id : ids) {
            list.add(decode(id));
        }
        return list;
    }

    /**
     * 批量解密id，这些id使用separator进行分割。
     *
     * @param ids
     * @param separator
     * @return
     */
    public static List<Integer> decode(String ids, String separator) {
        List<Integer> list = Lists.newArrayList();
        for (String id : Splitter.on(separator).trimResults().omitEmptyStrings().split(ids)) {
            Integer result = decode(id);
            //tolerate the illegal result
            if (result != null && !result.equals(-1)) {
                list.add(result);
            }
        }
        return list;
    }

}
