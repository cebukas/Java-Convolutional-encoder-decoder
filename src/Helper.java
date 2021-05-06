import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Helper {
    public ArrayList<Integer> stringToArrayList(String vectorString){   // converts string vector to arraylist ignoring all non binary data. Takes binary string as a parameter, returns binary ArrayList.
        ArrayList<Integer> vector = new ArrayList<Integer>();
        for (int i = 0; i < vectorString.length(); i++)
        {
            char symbolChar = vectorString.charAt(i);
            int symbolInt = Integer.parseInt(String.valueOf(symbolChar));
            if (symbolInt == 1 || symbolInt == 0)
                vector.add(symbolInt);
        }
        return vector;
    }

    public int compareVectors(ArrayList<Integer> vector1, ArrayList<Integer> vector2){ // returns the amount of different integers in two arraylists.
        int differentBits = 0;
        if(vector1.size() == vector2.size()){
            for (int i = 0; i < vector1.size(); i++){
                if (vector1.get(i) != vector2.get(i)){
                    differentBits++;
                }
            }
            return differentBits;
        }
        else
            return -1; // different sized vectors
    }
    public int compareByteArrays(byte[] array1, byte[] array2){ // returns the amount of different bytes in two byte arrays.
        int differentBytes = 0;
        if(array1.length == array2.length){
            for (int i = 0; i < array1.length; i++){
                if (array1[i] != array2[i]){
                    differentBytes++;
                }
            }
            return differentBytes;
        }
        else
            return -1; // different sized arrays
    }
    public int countFF(byte[] array){   // counts the amount of 'ff' bytes in the given byte array. all Markers in JPG format begin with 'ff' bytes.
        int ffCount = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == (byte) 255){
                ffCount++;
            }
        }
        return ffCount;
    }
    public int countFF00(byte[] array){ // counts the amount of 'ff 00' bytes in the given byte array. all 'ff' bytes which are not the start JPG Markers are followed by '00' bytes
        int ff00Count = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == (byte) 255 && array[i + 1] == 0 ){
                ff00Count++;
            }

        }
        return ff00Count;
    }

    public String toBitString(final byte[] b) {        //convert byte to a string of bits
        final char[] bits = new char[8 * b.length];
        for(int i = 0; i < b.length; i++) {
            final byte byteval = b[i];
            int bytei = i << 3;
            int mask = 0x1;
            for(int j = 7; j >= 0; j--) {
                final int bitval = byteval & mask;
                if(bitval == 0) {
                    bits[bytei + j] = '0';
                } else {
                    bits[bytei + j] = '1';
                }
                mask <<= 1;
            }
        }
        return String.valueOf(bits);
    }
    public byte[] toByteArray(ArrayList<Integer> bits) {    //convert ArrayList of bits to a byte array
        byte[] results = new byte[(bits.size() + 7) / 8];
        int byteValue = 0;
        int index;
        for (index = 0; index < bits.size(); index++) {
            byteValue = (byteValue << 1) | bits.get(index);
            if (index % 8 == 7) {
                results[index / 8] = (byte) byteValue;
            }
        }
        if (index % 8 != 0) {
            results[index / 8] = (byte) ((byte) byteValue << (8 - (index % 8)));
        }
        return results;
    }

    public  int convertByteArrayToInt(byte[] bytes) {   // converts byte array to an integer
        return ByteBuffer.wrap(bytes).getInt();
    }
}