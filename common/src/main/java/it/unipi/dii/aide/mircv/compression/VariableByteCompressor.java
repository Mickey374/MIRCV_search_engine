package it.unipi.dii.aide.mircv.compression;

import java.util.ArrayList;
import static java.lang.Math.log;

public class VariableByteCompressor {
    /**
     * Method for compressing a single integer
     * @param toBeCompressed the integer to be compressed
     * @return the compressed representation of the input number
     */
    public static byte[] integerCompression(int toBeCompressed) {
        // case of number 0
        if (toBeCompressed == 0) {
            return new byte[]{0};
        }

        // compute the number of bytes needed
        int numBytes = (int) Math.ceil(log(toBeCompressed) / log(128)) + 1;

        // create the byte array
        byte[] output = new byte[numBytes];

        // fill the byte array starting from the least significant byte
        // and moving to the most significant byte
        for (int i = numBytes - 1; i >= 0; i--) {
            output[i] = (byte) (toBeCompressed % 128);
            toBeCompressed /= 128;
        }

        // set the most significant bit of the last byte to 1
        output[numBytes - 1] += 128;

        return output;
    }

    /**
     * Method to compress an array of integers into an array of bytes using Unary compression algorithm
     * @param toBeCompressed: array of integers to be compressed
     * @return an array containing the compressed bytes
     */
    public static byte[] integerArrayCompression(int[] toBeCompressed) {
        ArrayList<Byte> compressedArray = new ArrayList<>();

        // compress each integer in the array
        for (int i : toBeCompressed) {
            // compress the integer and append the compressed bytes to the compressedArray
            for (byte elem : integerCompression(i))
                compressedArray.add(elem);
        }

        // convert the ArrayList to an array of bytes
        byte[] output = new byte[compressedArray.size()];
        for (int i = 0; i < compressedArray.size(); i++)
            output[i] = compressedArray.get(i);

        return output;
    }

    /**
     * Method to decompress an array of bytes int an array of totNums integers using Unary compression algorithm
     * @param toBeDecompressed: array of bytes to be decompressed
     * @param totNums: total number of integers to be decompressed
     * @return an array containing the decompressed integers
     */
    public static int[] integerArrayDecompression(byte[] toBeDecompressed, int totNums) {
        int[] decompressedArray = new int[totNums];

        // Counter for processed data
        int decompressedNumber = 0;

        // Index of already processed data
        int alreadyDecompressed = 0;

        // decompress each integer in the array
        for(byte elem: toBeDecompressed){
            // if the most significant bit is 1, the current byte is the last byte of the compressed integer
            if((elem & 0xff) < 128)
                // if the most significant bit is 0, the current byte is not the last byte of the compressed integer
                // so we need to append the byte to the previous byte
                decompressedNumber = 128 * decompressedNumber + elem;
            else{
                // termination byte, remove the 1 at the MSB and then append the byte to the number
                decompressedNumber = 128 * decompressedNumber + ((elem - 128) & 0xff);

                // save the number in the output array
                decompressedArray[alreadyDecompressed] = decompressedNumber;

                // increase the number of processed numbers
                alreadyDecompressed ++;

                //reset the variable for the next number to decompress
                decompressedNumber = 0;
            }
        }
        return decompressedArray;
    }
}