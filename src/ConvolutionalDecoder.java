import java.util.ArrayList;
import java.util.Collections;

public class ConvolutionalDecoder {
    private ArrayList<Integer> inputVector = new ArrayList<Integer>();
    private ArrayList<Integer> outputVector = new ArrayList<Integer>();
    private ArrayList<Integer> tempBitsTop = new ArrayList<Integer>(Collections.nCopies(6, 0));
    private ArrayList<Integer> tempBitsBot = new ArrayList<Integer>(Collections.nCopies(6, 0));

    public void setInputVector(ArrayList<Integer> inputVector) {
        this.inputVector = inputVector;
    }

    public ArrayList<Integer> getOutputVector() {
        return outputVector;
    }

    public void decode(){       //decode the inputVector and store it in outputVector
        outputVector.clear();
        int syndromeBit;
        int outputBit;
        int MDE;

        for(int i = 0; i < inputVector.size(); i += 2){
            syndromeBit = inputVector.get(i + 1);
            syndromeBit += inputVector.get(i);
            syndromeBit += tempBitsTop.get(1) + tempBitsTop.get(4) + tempBitsTop.get(5);
            syndromeBit %= 2;

            MDE = 0;
            MDE += syndromeBit + tempBitsBot.get(0) + tempBitsBot.get(3) + tempBitsBot.get(5);

            if(MDE >= 3)
                MDE = 1;
            else
                MDE = 0;

            outputBit = MDE + tempBitsTop.get(5);
            outputBit %= 2;

            if(i >= 12) // skip first 6 iterations because 6 '0' bits were added, so no actual vector information is passed in these bits
                outputVector.add(outputBit);

            tempBitsTop.add(0, inputVector.get(i)); // add to tempBitsTop

            tempBitsBot.set(3, (tempBitsBot.get(3) + MDE) % 2); // feedback decoder here (update tempBitsBot with MDE)
            tempBitsBot.set(0, (tempBitsBot.get(0) + MDE) % 2);

            tempBitsBot.add(0, (syndromeBit + MDE) % 2);

            if(tempBitsBot.size() > 7){      // remove everything past 6, this saves a lot of time with bigger jpg's
                tempBitsBot.remove(6);
            }
            if(tempBitsTop.size() > 7){
                tempBitsTop.remove(6);
            }
        }
    }
}