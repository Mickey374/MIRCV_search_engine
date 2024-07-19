package it.unipi.dii.aide.mircv.compression;

/**
 * class used to implement the unary compressor used to compress the frequencies in the inverted index
 */
public class UnaryCompressor {
    /**
     * Method to compress an array of integers into an array of bytes using Unary compression algorithm
     * @param toBeCompressed: array of integers to be compressed
     *                      (in this case the frequencies of the terms in the documents)
     * @return an array containing the compressed bytes
     */
    public static byte[] integerArrayCompression(int[] toBeCompressed) {
        int nBits = 0;

        // find the maximum number of bits needed to represent the frequencies
        for(int i=0; i<toBeCompressed.length; i++) {
            // Each integer number will be compressed in a number of bits equal to its value
            nBits += toBeCompressed[i];
        }

        // Compute the total number of bytes needed as ceil of nBits/8
        int nBytes = (nBits/ 8 + (((nBits % 8) != 0) ? 1 : 0));

        System.out.println("nBits: " + nBits + " nBytes: " + nBytes);

        // Initialize array of bytes to store the compressed data
        byte[] compressedArray = new byte[nBytes];

        // Initialize the index of the byte in the compressedArray
        int nextByteToWrite = 0;
        int nextBitToWrite = 0;

        // Compress each integer in the array
        for(int i=0; i<toBeCompressed.length; i++) {
            // Check if the integer is 0
            if(toBeCompressed[i] <= 0){
                System.out.println("Skipped element <=0 in the list of integers to be compressed");
                continue;
            }

            // Compress the integer and append the compressed bytes to the compressedArray
            for(int j=0; j<toBeCompressed[i] -1; j++) {
                // Set the bit at the position nextBitToWrite in the byte nextByteToWrite
                compressedArray[nextByteToWrite] = (byte) (compressedArray[nextByteToWrite] | (1 << 7-nextBitToWrite));

                // Increment the nextBitToWrite
                nextBitToWrite++;

                // If the byte is full, move to the next byte
                if(nextBitToWrite == 8) {
                    nextByteToWrite++;
                    nextBitToWrite = 0;
                }
            }

            // Set the last bit of the byte to 0
            nextBitToWrite++;

            // If the byte is full, move to the next byte
            if(nextBitToWrite == 8) {
                nextByteToWrite++;
                nextBitToWrite = 0;
            }
        }

        return compressedArray;
    }

    /**
     * Method to decompress an array of bytes into an array of integers using Unary compression algorithm
     * @param toBeDecompressed: array of bytes to be decompressed
     * @param totNums: total number of integers to be decompressed
     * @return an array containing the decompressed integers
     */
    public static int[] integerArrayDecompression(byte[] toBeDecompressed, int totNums){
        int[] decompressedArray = new int[totNums];

        int toBeReadedByte = 0;
        int toBeReadedBit = 0;
        int nextInteger = 0;
        int onesCounter = 0;

        // Decompress each integer in the array
        for(int i=0; i < toBeDecompressed.length * 8; i++){
            // Create a byte, b, where only the bit (i%8) is set to 1 or 0
            byte b = 0b00000000;
            b |= (1 << 7-(i%8));

            System.out.println("Integer Binary String: " + Integer.toBinaryString(b & 255 | 256).substring(1));

            // Check if the bit is set to 1 or 0
            if((toBeDecompressed[toBeReadedByte] & b) == 0){

                // Set the next integer in the decompressedArray
                decompressedArray[nextInteger] = onesCounter + 1;

                nextInteger++;
                if(nextInteger == totNums)
                    break;

                onesCounter = 0;
            } else {
                // Increment the counter of 1s
                onesCounter++;
            }
            toBeReadedBit++;

            // If the byte is full, move to the next byte
            if(toBeReadedBit == 8){
                toBeReadedByte++;
                toBeReadedBit = 0;
            }
        }
        return decompressedArray;
    }
}
