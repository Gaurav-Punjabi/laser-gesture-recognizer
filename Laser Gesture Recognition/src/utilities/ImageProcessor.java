/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
/**
 *
 * @author Gaurav-Punjabi
 */
public class ImageProcessor {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /**
    * @param : mat : matrix which needs to be converted into buffereImage.
    * @returns : bufferedImage of the given matrix.
    * 
    **/
    public static BufferedImage toBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if(mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        byte[] buffer = new byte[(int)(mat.total() * mat.elemSize())];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, (int)(mat.total() * mat.elemSize()));
        return image;
    }
}
