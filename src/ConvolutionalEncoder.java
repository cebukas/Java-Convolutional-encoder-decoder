import java.util.*;

public class ConvolutionalEncoder {
    private ArrayList<Integer> inputVector = new ArrayList<Integer>();
    private ArrayList<Integer> outputVector = new ArrayList<Integer>();

    public void setInputVector(ArrayList<Integer> inputVector) {
        this.inputVector = inputVector;
    }

    public ArrayList<Integer> getOutputVector() {
        return outputVector;
    }

    public void encode(){   //encode the inputVector and store it in outputVector
        ArrayList<Integer> tempBits = new ArrayList<Integer>(Collections.nCopies(6, 0));
        int outputBit;
        outputVector.clear();
        for(int i = 0; i < 6; i++){
            inputVector.add(0);
        }

        for(int i = 0; i < (inputVector.size()); i++){
            outputVector.add(inputVector.get(i));

            outputBit = inputVector.get(i);
            outputBit += tempBits.get(1) + tempBits.get(4) + tempBits.get(5);
            outputBit %= 2;

            outputVector.add(outputBit);

            tempBits.add(0, inputVector.get(i));

            if(tempBits.size() > 7){     // remove everything past 6, this saves a lot of time with bigger jpg's
                tempBits.remove(6);
            }
        }
        for(int i = 0; i < 6; i++){ // remove the added 0's so inputVector does not get changed
            inputVector.remove(inputVector.size() - 1);
        }
    }

}