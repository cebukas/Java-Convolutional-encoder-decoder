import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

// pic path for testing: C:/Users/cebukas/A14/pics/code.jpg

public class Main {

    public static void main(String[] args) {

        //TestRunner testRunner = new TestRunner();
        //testRunner.runTextTests(0.01f);
        //testRunner.runVectorTests(0.15f);

        Scanner scanner = new Scanner(System.in);
        Helper helper = new Helper();
        Channel channel = new Channel();
        ConvolutionalDecoder decoder = new ConvolutionalDecoder();
        ConvolutionalEncoder encoder = new ConvolutionalEncoder();

        ArrayList<Integer> vector = new ArrayList<Integer>();
        float probability;

        do {
            System.out.println("Enter the channel error probability (0 - 1). The format is 'x.y' or 'x,y'  ");
            String stringProbability = scanner.next();
            stringProbability = stringProbability.replaceAll(",", ".");
            probability = Float.valueOf(stringProbability);
        } while(!((probability >= 0) && (probability <= 1)));

        channel.setProbability(probability);

        System.out.println("0. Exit");
        System.out.println("1. Vector");
        System.out.println("2. Text");
        System.out.println("3. Picture");
        System.out.println("4. Change the probability");

        boolean quit = false;
        int menuItem;
        do {

            System.out.print("Choose menu item: ");

            menuItem = scanner.nextInt();

            switch (menuItem) {
//------------------------------------------------ VECTOR INPUT CASE -----------------------------------------------------------------
                case 1:
                    vector.clear();

                    String vectorString;

                    System.out.println("Enter binary vector (the length is unlimited and all non binary input is ignored) ");
                    vectorString = scanner.next();

                    vector = helper.stringToArrayList(vectorString);

                    encoder.setInputVector(vector);
                    encoder.encode();
                    System.out.println("Encoded vector is \n" + encoder.getOutputVector());

                    channel.setInputVector(encoder.getOutputVector());
                    channel.sendThroughChannel();
                    System.out.println("The vector that was received from the channel: \n" + channel.getOutputVector());
                    System.out.println("There were " + channel.getErrorPositions().size() + " errors made in these positions: \n" +
                            channel.getDisplayErrorPositions());

                    int positionToChange;
                    do {
                        System.out.println("If you wish to add or remove an error in any position, please enter it. Enter 0 to continue");
                        positionToChange = scanner.nextInt();
                            channel.changeBit(--positionToChange);
                            System.out.println("The vector that was received from the channel: " + channel.getOutputVector());
                            System.out.println("There were " + channel.getErrorPositions().size() + " errors made in these positions: " +
                                    channel.getDisplayErrorPositions());
                    }while(!(positionToChange == -1));

                    decoder.setInputVector(channel.getOutputVector());
                    decoder.decode();

                    System.out.println("the decoded vector is \n" + decoder.getOutputVector());

                    break;
//------------------------------------------------ TEXT INPUT CASE -----------------------------------------------------------------
                case 2:
                    String text;
                    vector.clear();
                    scanner.useDelimiter("\n");

                    System.out.println("Enter any text");
                    text = scanner.next();
                    String binary = new BigInteger(text.getBytes()).toString(2);

                    vector = helper.stringToArrayList(binary);

                    channel.setInputVector(vector);
                    System.out.println("The entered text converted to binary is: \n" + vector);
                    channel.sendThroughChannel();
                    System.out.println("The vector that was received from the channel: \n" + channel.getOutputVector());
                    System.out.println("There were " + channel.getErrorPositions().size() + " errors made in these positions: \n" +
                            channel.getDisplayErrorPositions());

                    StringBuilder sb = new StringBuilder();
                    for (int s : channel.getOutputVector())
                    {
                        sb.append(s);
                    }

                    String outputText = new String(new BigInteger(sb.toString(), 2).toByteArray());
                    System.out.println("The text after sending it through the channel is: \n" + outputText);

                    System.out.println("------------------------------------");

                    encoder.setInputVector(vector);
                    encoder.encode();
                    System.out.println("Encoded vector is \n" + encoder.getOutputVector());
                    channel.setInputVector(encoder.getOutputVector());
                    channel.sendThroughChannel();
                    System.out.println("The vector that was received from the channel: \n" + channel.getOutputVector());
                    System.out.println("There were " + channel.getErrorPositions().size() + " errors made in these positions: \n" +
                            channel.getDisplayErrorPositions());
                    decoder.setInputVector(channel.getOutputVector());
                    decoder.decode();

                    System.out.println("the decoded vector is \n" + decoder.getOutputVector());
                    System.out.println("The entered text converted to binary is: \n" + vector);
                    System.out.println("The amount of different bits between the input binary and after decoding is: " + helper.compareVectors(vector, decoder.getOutputVector()));

                    sb = new StringBuilder();
                    for (int s : decoder.getOutputVector())
                    {
                        sb.append(s);
                    }

                    outputText = new String(new BigInteger(sb.toString(), 2).toByteArray());
                    System.out.println("The text after sending it through the channel and decoding is: \n" + outputText);

                    break;

//------------------------------------------------ PICTURE INPUT CASE -----------------------------------------------------------------
                case 3:
                    System.out.println("Enter the path to the input JPG picture. The format is 'C:/.../picture.jpg'");
                    String imageInputPath = scanner.next();

                    System.out.println("Enter the path to the output (sending through channel only) JPG picture");
                    String imageOutputPath1 = scanner.next();

                    System.out.println("Enter the path to the output (encoding, sending through channel, decoding) JPG picture");
                    String imageOutputPath2 = scanner.next();
                    try{
                        File fnew = new File(imageInputPath);
                        BufferedImage originalImage = ImageIO.read(fnew);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(originalImage, "jpg", baos );
                        byte[] imageInByte = baos.toByteArray();

                       String imageInBits = helper.toBitString(imageInByte);
                       ArrayList<Integer> ImageInArrayList = helper.stringToArrayList(imageInBits);
                        System.out.println("The input image in bytes: ");
                        for(int i = 0; i <imageInByte.length; i++){
                            System.out.print(" " + imageInByte[i]);
                        }
                        System.out.println("");

                       channel.setInputVector(ImageInArrayList);
                       channel.sendRawPictureThroughChannel();

                       byte[] outputImageInBytes = helper.toByteArray(channel.getOutputVector());

                        System.out.println("The output image after sending through the channel in bytes: ");
                        for(int i = 0; i <outputImageInBytes.length; i++){
                            System.out.print(" " + outputImageInBytes[i]);
                        }

                        System.out.println("");
                        System.out.println(helper.compareByteArrays(outputImageInBytes, imageInByte)+ " / " + imageInByte.length + " bytes are different from the original image");

                        File f = new File(imageOutputPath1);
                        BufferedImage imag=ImageIO.read(new ByteArrayInputStream(outputImageInBytes));
                        ImageIO.write(imag, "jpg", f);

                        System.out.println("------------------------------------");
                        encoder.setInputVector(ImageInArrayList);
                        encoder.encode();

                        channel.setInputVector(encoder.getOutputVector());
                        channel.extractMarkerBytes(imageInByte);
                        channel.sendThroughChannel();
                        decoder.setInputVector(channel.getOutputVector());
                        decoder.decode();

                        byte [] decodedBytesWithMarkerErrors = helper.toByteArray(decoder.getOutputVector());
                        byte[] decodedAndSafeBytes = channel.replaceWithSafeMarkerBytes(decodedBytesWithMarkerErrors);

                        System.out.println("The output image after encoding, sending through the channel and decoding in bytes: ");
                        for(int i = 0; i < decodedAndSafeBytes.length; i++){
                            System.out.print(" " + decodedAndSafeBytes[i]);
                        }

                        System.out.println("");
                        System.out.println(helper.compareByteArrays(decodedAndSafeBytes, imageInByte)+ " / " + imageInByte.length + " bytes are different from the original image");

                        File f2 = new File(imageOutputPath2);
                        BufferedImage imag2=ImageIO.read(new ByteArrayInputStream(decodedAndSafeBytes));
                        ImageIO.write(imag2, "jpg", f2);
                        System.out.println("");
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    do {
                        System.out.println("Enter the channel error probability (0 - 1). The format is 'x.y' or 'x,y' ");
                        String stringProbability = scanner.next();
                        stringProbability = stringProbability.replaceAll(",", ".");
                        probability = Float.valueOf(stringProbability);
                        channel.setProbability(probability);
                    } while(!((probability >= 0) && (probability <= 1)));
                    break;
                case 0:
                    quit = true;

                    break;

                default:
                    System.out.println("Invalid choice.");
            }

        } while (!quit);

    }
}