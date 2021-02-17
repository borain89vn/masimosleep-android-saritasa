package com.masimo.android.airlib;

import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;

class PinGenerator {
    //    private static final TaggedLog LOG = com.masimo.common.logging.Log.tag("KeyGen");

    public static final int PIN_MAX_LENGTH          = 6;
    public static final int SN_MAX_LENGTH_INCLUSIVE = 10;
    public static final int SN_MIN_LENGTH_INCLUSIVE = 0;

    public static byte[] GeneratePinFromMac(String macAddress) {
        byte[] buffer = convertMacToBytes(macAddress);
        //Generate key from given data
        return generatePin(buffer);
    }

    /**
     * This only works correctly if serialNumber is in UTF_8
     * @param serialNumber
     * @return
     * @throws IllegalFormatException
     * @Deprecated Not assuming serialNumber can be handled properly by String
     */
    @Deprecated
    public static byte[] GeneratePinFromSN(String serialNumber) {
        byte[] buffer = convertSNToBytes(serialNumber);
        //Generate key from given data
        return generatePin(buffer);
    }

    public static byte[] GeneratePinFromSN(byte[] serialNumber) {
        if (serialNumber.length < SN_MIN_LENGTH_INCLUSIVE || serialNumber.length > SN_MAX_LENGTH_INCLUSIVE) {
            throw new IllegalArgumentException(String.format("The argument {%s} is not in the correct format.", serialNumber));
        }
        return generatePin(serialNumber);
    }

    protected static byte[] convertSNToBytes(String serialNumber) throws IllegalFormatException {

        //prepare data to store keys
        byte[] buffer = serialNumber.getBytes(StandardCharsets.UTF_8);

        //Sanity check, make sure the buffer length is in 0..10
        if (buffer.length < SN_MIN_LENGTH_INCLUSIVE || buffer.length > SN_MAX_LENGTH_INCLUSIVE) {
            throw new IllegalArgumentException(String.format("The argument {%s} is not in the correct format.", serialNumber));
        }
        return buffer;
    }

    protected static byte[] convertMacToBytes(String macAddress) throws IllegalFormatException {
        String[] tokens = macAddress.split(":");

        //prepare data to store keys
        byte[] buffer = new byte[tokens.length];

        //Sanity check, make sure the buffer is 6-byte long
        if (buffer.length != PinGenerator.PIN_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("The argument {%s} is not in the correct format.", macAddress));
        }

        short i = 0;
        for (String str : tokens) {
            Integer hex = Integer.parseInt(str, 16);
            buffer[i++] = hex.byteValue();
        }

        return buffer;
    }

    private static int UniformRand_INT32(int piX) {
        int iXHi, iXALo, iLeftLo, iFHi, k;
        int iA   = 16807;                    /* 7^5      */
        int iB15 = 32768;                    /* 2^15     */
        int iB16 = 65536;                    /* 2^16     */
        int iP   = 2147483647;               /* 2^31 - 1 */

        iXHi    = piX / iB16;
        iXALo   = (piX - iXHi * iB16) * iA;
        iLeftLo = iXALo / iB16;
        iFHi    = iXHi * iA + iLeftLo;
        k       = iFHi / iB15;
        piX     = (((iXALo - iLeftLo * iB16) - iP) + (iFHi - k * iB15) * iB16) + k;
        if (piX < 0) {
            piX = piX + iP;
        }

        return piX;
    }

    /**
     * Converts all  bytes to unsigned ints prior to applying operations
     *
     * @param pawInput_U
     * @return generated PIN
     */
    private static byte[] generatePin(byte[] pawInput_U) {
        byte   wTemp;
        int    wTemp_U;
        short  iIndex, iCnt;
        int    wMask_U = 0, wKey1, wKey2;
        int    wSeed_U = 0xD01CC4AD;
        byte[] awMask  = {'C', 'D'};

        byte[] pawOutput = new byte[PIN_MAX_LENGTH];

        //Prepare the mask
        if (pawInput_U.length == 0) {
            //treat as the even input and using the predefined mask bytes
            wMask_U |= ((awMask[0] << 24) | (awMask[1] << 8));
        } else if (pawInput_U.length == 1) {
            //treat as the odd input and using 1 byte from the predefined mask bytes
            wMask_U |= ((awMask[0] << 16) | pawInput_U[0]);
        } else {
            //Prepare seed to the PRNG engine
            short iShiftIndex = (short) ((ByteToUnsignedInt(pawInput_U[1]) & 0x1) != 0 ? 1 : 0);

            wMask_U = iShiftIndex == 1 ? wMask_U | ByteToUnsignedInt(pawInput_U[0]) << 16 | ByteToUnsignedInt(pawInput_U[1]) :
                      wMask_U | ByteToUnsignedInt(pawInput_U[0]) << 24 | ByteToUnsignedInt(pawInput_U[1]) << 8;

            iIndex  = 2; //continuing with the 3rd bytes
            wSeed_U = 0;// Reset seed to 0 for standard cases
            while (iIndex < pawInput_U.length) {
                //get the max length for each pass is 4 bytes or the rest if it is less than 4 bytes
                int wCalLen = ((pawInput_U.length - iIndex) > 4) ? 4 : (pawInput_U.length - iIndex);

                for (int i = 0, j = 24; i < wCalLen; i++, j -= 8) {
                    wSeed_U |= ByteToUnsignedInt(pawInput_U[iIndex + i]) << j;
                }
                iIndex += wCalLen;
            }

        }

        wSeed_U ^= wMask_U;

        wSeed_U = wKey1 = UniformRand_INT32(wSeed_U);
        wKey2   = UniformRand_INT32(wSeed_U);

        //Get key from the decimal data, the upper bytes of wKey2 are dont-care terms.
        for (iCnt = 0, iIndex = 0; iCnt < 4; iCnt++, iIndex += 8) {
            wTemp   = (byte) ((byte) (wKey1 >> iIndex) & 0x0F);
            wTemp_U = ByteToUnsignedInt(wTemp);
            if (wTemp_U >= 10) {
                wTemp_U -= 10;
            }
            //Convert to ascii key by adding the offset value
            pawOutput[iCnt] = (byte) (wTemp_U + 0x30);
        }
        //Continue to get key from decimal data
        for (iCnt = 4, iIndex = 0; iCnt < PIN_MAX_LENGTH; iCnt++, iIndex += 8) {
            wTemp   = (byte) ((byte) (wKey2 >> iIndex) & 0x0F);
            wTemp_U = ByteToUnsignedInt(wTemp);
            if (wTemp_U >= 10) {
                wTemp_U -= 10;
            }
            //Convert to ascii key by adding the offset value
            pawOutput[iCnt] = (byte) (wTemp_U + 0x30);
        }

        return pawOutput;
    }

    public static int ByteToUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }
}
