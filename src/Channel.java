import java.util.ArrayList;
import java.util.Collections;

public class Channel {

    private float probability;
    private boolean isZero;

    private ArrayList<Integer> inputVector = new ArrayList<Integer>();
    private ArrayList<Integer> outputVector = new ArrayList<Integer>();
    private ArrayList<Integer> errorPositions = new ArrayList<Integer>();

    private ArrayList<Byte> safeBytesArray = new ArrayList<>();
    private ArrayList<Integer> safeBytesPositionInArray = new ArrayList<>();

    public void setProbability(float probability) {
        this.probability = probability;
    }

    public void setInputVector(ArrayList<Integer> inputVector) {
        this.inputVector = inputVector;
    }
    public ArrayList<Integer> getOutputVector() {
        return outputVector;
    }

    public ArrayList<Integer> getErrorPositions() {
        return errorPositions;
    }

    public ArrayList<Integer> getDisplayErrorPositions(){           // sorts the given ArrayList of errors and prepares it for display (starts from 1 and not from 0).
        ArrayList<Integer> errorPositionsTemp = new ArrayList<>();
        for (int i : errorPositions){
            errorPositionsTemp.add(i + 1);
        }
        Collections.sort(errorPositionsTemp);
        return errorPositionsTemp;
    }

    public void sendThroughChannel(){           // makes errors in inputVector with the given probability and returns it in outputVector
        outputVector.clear();
        errorPositions.clear();
        for (int i = 0; i < inputVector.size(); i++) {
            if (Math.random() <= probability)
            {
                if (inputVector.get(i) == 1)
                    outputVector.add(0);
                else
                    outputVector.add(1);
                errorPositions.add(i);
            }
            else
                outputVector.add(inputVector.get(i));
        }
    }

    public void changeBit(int position){        // changes a bit at the given position in the outputVector. Updates errorPositions array as well.
        if (position < outputVector.size() && position >= 0){
            if(outputVector.get(position) == 0)
                outputVector.set(position, 1);
            else
                outputVector.set(position, 0);

            if(errorPositions.contains(position)){  // change errorPositions depending on whether we are adding an error or fixing one
                errorPositions.remove((Object) position);
            }
            else
                errorPositions.add(position);
        }
    }

    public void sendRawPictureThroughChannel(){ // makes errors in inputVector with the given probability and returns it in outputVector.
        outputVector.clear();
        errorPositions.clear();
        int amountOfSafeBits = 16; // amountOfSafeBits is needed to not change the required JPG Marker data
        int ones = 0;
        for (int i = 0; i < inputVector.size(); i += 8) { // jump every 8 bits

            if(amountOfSafeBits <= 0){
                if (!(inputVector.size() - i < 16)) {
                    amountOfSafeBits = byteAmountToIgnore(i) * 8;
                } else
                    amountOfSafeBits = 16;
            }

            if(amountOfSafeBits == 0) {
                ones = 0;
                for(int j =0; j < 8; j++){
                    if (Math.random() <= probability)
                    {
                        outputVector.add((inputVector.get(i + j) + 1) % 2);
                        errorPositions.add(i + j);
                        if(((inputVector.get(i + j) + 1) % 2) == 1)
                            ones++;
                    }
                    else{
                        outputVector.add(inputVector.get(i + j));
                        if (inputVector.get(i + j) == 1)
                            ones++;
                    }
                    if(isZero){
                        outputVector.set(i+j, 0);
                    }
                }
                isZero = false;
                if(ones > 7){ // ff 00 situation
                    isZero = true;
                }

            }
            if(amountOfSafeBits >= 8){
                for(int j =0; j < 8; j++){
                    outputVector.add(inputVector.get(i + j));
                }
                amountOfSafeBits -= 8;
            }
        }
    }
    private int getSafeBytes(int position, byte[] bytesArray){         // calculates and returns the byte size of a JPG Marker.
        ArrayList<Integer> bitArrayList = new ArrayList<>();
        Helper helper = new Helper();
        for(int j = 0; j < 32; j++){ //The first 16 bits in inputVector contain the name of the Marker, the next 16 bits contain the size of it
            bitArrayList.add(j, inputVector.get(position));
            position++;
        }
        bytesArray =  helper.toByteArray(bitArrayList);
        bytesArray[0] = 0;
        bytesArray[1] = 0;
        return helper.convertByteArrayToInt(bytesArray) + 2;
    }

    private int byteAmountToIgnore(int position){   //finds and identifies the JPG Marker and returns the amount of bytes that have to be unchanged
        int rememberPosition = position;

        ArrayList<Integer> bitArrayList = new ArrayList<Integer>();
        for(int i = 0; i < 16; i++){
            bitArrayList.add(i, inputVector.get(position));
            position++;
        }
        Helper helper = new Helper();
        byte[] bytesArray =  helper.toByteArray(bitArrayList);

        if(bytesArray[0] == (byte) 255 && bytesArray[1] == (byte) 216){ // SOI Marker found
            return 2;
        }
        if(bytesArray[0] == (byte) 255 && bytesArray[1] == (byte) 224){ // APP0 Marker found
            return 18;
        }
        if(bytesArray[0] == (byte) 255 && bytesArray[1] == (byte) 219){ // DQT Marker found
            return getSafeBytes(rememberPosition, bytesArray);
        }
        if(bytesArray[0] == (byte) 255 && bytesArray[1] == (byte) 196){ // DHT Marker found
            return getSafeBytes(rememberPosition, bytesArray);
        }
        if(bytesArray[0] == (byte) 255 && bytesArray[1] == (byte) 192){ // SOF0 Marker found
            return getSafeBytes(rememberPosition, bytesArray);
        }
        if(bytesArray[0] == (byte) 255 && bytesArray[1] == (byte) 218){ // SOS Marker found
            return getSafeBytes(rememberPosition, bytesArray);
        }
        if(bytesArray[0] == (byte) 255 && bytesArray[1] == (byte) 217){ // EOI found
            return 2;
        }
        return 0;
    }
    public byte[] replaceWithSafeMarkerBytes(byte[] outputBytes){   //Places the JPG Marker data in the given byte array

        //first make sure all non segment ff's are followed by 00
        for(int i = 0; i < outputBytes.length; i++){
            if (outputBytes[i] == (byte) 255){
                outputBytes[i+1] = (byte) 0;
            }
        }
        //then make sure to include the marker bytes that were saved earlier
        for(int i = 0; i < safeBytesPositionInArray.size(); i++){
            if(safeBytesPositionInArray.get(i) == i){
                outputBytes[i] = safeBytesArray.get(i);
            }
        }
        return outputBytes;
    }

    public void extractMarkerBytes(byte[] bytesArray){  //finds the required JPG marker data and stores it in safeBytesArray and safeBytesPositionInArray
        safeBytesArray.clear();
        safeBytesPositionInArray.clear();
        Helper helper = new Helper();
        for(int i = 0; i < bytesArray.length;){
            if(bytesArray[i] == (byte)255 && bytesArray[i+1] != (byte) 0){
                if(bytesArray[i+1] == (byte) 216){ // SOI Marker found
                    safeBytesPositionInArray.add(i);
                    safeBytesPositionInArray.add(i + 1);
                    safeBytesArray.add(bytesArray[i]);
                    safeBytesArray.add(bytesArray[i + 1]);
                    i+=2;
                }
                else if(bytesArray[i+1] == (byte) 224){ // APP0 Marker found
                    for(int j = i; j < i + 18; j++){
                        safeBytesArray.add(bytesArray[j]);
                        safeBytesPositionInArray.add(j);
                    }
                    i+=18;
                }
                else if(bytesArray[i+1] == (byte) 219){ // DQT Marker found
                    byte[] tempArray = new byte[4];
                    tempArray[0] = 0;
                    tempArray[1] = 0;
                    tempArray[2] = bytesArray[i+2];
                    tempArray[3] = bytesArray[i+3];
                    int byteAmount = helper.convertByteArrayToInt(tempArray) + 2;

                    for(int j = i; j < i + byteAmount; j++){
                        safeBytesArray.add(bytesArray[j]);
                        safeBytesPositionInArray.add(j);
                    }
                    i+=byteAmount;
                }
                else if(bytesArray[i+1] == (byte) 196){ // DHT Marker found
                    byte[] tempArray = new byte[4];
                    tempArray[0] = 0;
                    tempArray[1] = 0;
                    tempArray[2] = bytesArray[i+2];
                    tempArray[3] = bytesArray[i+3];
                    int byteAmount = helper.convertByteArrayToInt(tempArray) + 2;

                    for(int j = i; j < i + byteAmount; j++){
                        safeBytesArray.add(bytesArray[j]);
                        safeBytesPositionInArray.add(j);
                    }
                    i+=byteAmount;
                }
                else if(bytesArray[i+1] == (byte) 192){ // SOF0 Marker found
                    byte[] tempArray = new byte[4];
                    tempArray[0] = 0;
                    tempArray[1] = 0;
                    tempArray[2] = bytesArray[i+2];
                    tempArray[3] = bytesArray[i+3];
                    int byteAmount = helper.convertByteArrayToInt(tempArray) + 2;

                    for(int j = i; j < i + byteAmount; j++){
                        safeBytesArray.add(bytesArray[j]);
                        safeBytesPositionInArray.add(j);
                    }
                    i+=byteAmount;
                }
                else if(bytesArray[i+1] == (byte) 218){ // SOS Marker found
                    byte[] tempArray = new byte[4];
                    tempArray[0] = 0;
                    tempArray[1] = 0;
                    tempArray[2] = bytesArray[i+2];
                    tempArray[3] = bytesArray[i+3];
                    int byteAmount = helper.convertByteArrayToInt(tempArray) + 2;

                    for(int j = i; j < i + byteAmount; j++){
                        safeBytesArray.add(bytesArray[j]);
                        safeBytesPositionInArray.add(j);
                    }
                    i+=byteAmount;
                }
                else if(bytesArray[i+1] == (byte) 217){ // EOI found
                    safeBytesPositionInArray.add(i);
                    safeBytesPositionInArray.add(i+1);
                    safeBytesArray.add(bytesArray[i]);
                    safeBytesArray.add(bytesArray[i + 1]);
                    i+=2;
                }
            }
            else
                i++;
        }
    }
}