package org.smartregister.compression;


public interface ICompression {

    byte[] compress(String rawString);

    String decompress(byte[] compressedBytes);

    void compress(String inputFilePath, String compressedOutputFilepath);

    void decompress(String compressedInputFilePath, String decompressedOutputFilePath);
}
