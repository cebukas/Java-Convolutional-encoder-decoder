import java.math.BigInteger;
import java.util.ArrayList;

public class TestRunner {
    Helper helper = new Helper();
    Channel channel = new Channel();
    ConvolutionalDecoder decoder = new ConvolutionalDecoder();
    ConvolutionalEncoder encoder = new ConvolutionalEncoder();

   public void runTextTests(float probability){ // outputs each text encoded, sent through channel and decoded with the given probability
       channel.setProbability(probability);
       String[] texts = new String[11];
       texts[0] = "labas, ";
       texts[1] = "labas, as";
       texts[2] = "labas, as esu";
       texts[3] = "labas, as esu vardu";
       texts[4] = "labas, as esu vardu Karolis";
       texts[5] = "labas, as esu vardu Karolis ir";
       texts[6] = "labas, as esu vardu Karolis ir man";
       texts[7] = "labas, as esu vardu Karolis ir man labai";
       texts[8] = "labas, as esu vardu Karolis ir man labai patinka";
       texts[9] = "labas, as esu vardu Karolis ir man labai patinka kodavimo";
       texts[10] = "labas, as esu vardu Karolis ir man labai patinka kodavimo teorija";

       for(int i = 0; i < texts.length; i++){
           ArrayList<Integer> vector = new ArrayList<Integer>();
           String binary = new BigInteger(texts[i].getBytes()).toString(2);
           vector = helper.stringToArrayList(binary);
           encoder.setInputVector(vector);
           encoder.encode();
           channel.setInputVector(encoder.getOutputVector());
           channel.sendThroughChannel();
           decoder.setInputVector(channel.getOutputVector());
           decoder.decode();
           StringBuilder sb = new StringBuilder();
           for (int s : decoder.getOutputVector())
           {
               sb.append(s);
           }
           String outputText = new String(new BigInteger(sb.toString(), 2).toByteArray());

          // System.out.println(channel.getErrorPositions().size());
           System.out.println(outputText);

       }
   }
       public void runVectorTests(float probability){ // outputs the decoding success rate
           channel.setProbability(probability);
           ArrayList<Integer> vector = new ArrayList<Integer>();

           for( int i = 0; i < 100; i++){
               if (Math.random() <= 0.5)
                  vector.add(1);
               else
                   vector.add(0);
           }

            float successfulDecodeCount = 0;
           for(int i = 0; i < 1000; i++){
               encoder.setInputVector(vector);
               encoder.encode();
               channel.setInputVector(encoder.getOutputVector());
               channel.sendThroughChannel();
               decoder.setInputVector(channel.getOutputVector());
               decoder.decode();

               if(helper.compareVectors(decoder.getOutputVector(), vector) == 0)
                   successfulDecodeCount++;

              //  System.out.println(channel.getErrorPositions().size());

           }
           System.out.println("decoding success rate: " + successfulDecodeCount / 10);

    }
}