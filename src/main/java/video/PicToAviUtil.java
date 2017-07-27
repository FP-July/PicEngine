package video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.jim2mov.core.DefaultMovieInfoProvider;
import org.jim2mov.core.ImageProvider;
import org.jim2mov.core.Jim2Mov;
import org.jim2mov.core.MovieInfoProvider;
import org.jim2mov.core.MovieSaveException;
import org.jim2mov.utils.MovieUtils;

/**
 * Created by THU73 on 17/7/23.
 */
public class PicToAviUtil {

    /*  Example Code
        String jpgDirPath = "/Users/THU73/desktop/pic"; // jpg dir path
        String aviFileName = "test.avi"; // generated file's name
        int fps = 3; // frame per second
        int mWidth = 1280; // width of the video
        int mHeight = 800; // height of the video
        PicToAviUtil.convertPicToAvi(jpgDirPath, aviFileName, fps, mWidth, mHeight);
     */

    public static void convertPicToAvi(BufferedImage[] image, String aviFileName, int fps, int mWidth, int mHeight) {

        final BufferedImage[] innerImage = image;

        DefaultMovieInfoProvider dmip = new DefaultMovieInfoProvider(aviFileName);
        // frame per second
        dmip.setFPS(fps > 0 ? fps : 3); // 如果未设置，默认为3
        // total frame count
        dmip.setNumberOfFrames(image.length);
        // set movie parameters
        dmip.setMWidth(mWidth > 0 ? mWidth : 1280); // default 1440
        dmip.setMHeight(mHeight > 0 ? mHeight : 800); // default 800

        try {
            new Jim2Mov(new ImageProvider() {
                public byte[] getImage(int frame) {
                    try {
                        // quality: compression ratio
                        return MovieUtils.bufferedImageToJPEG(innerImage[frame], 1.0f);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                    return null;
                }
            }, dmip, null).saveMovie(MovieInfoProvider.TYPE_AVI_MJPEG);
        } catch (MovieSaveException e) {
            System.err.println(e);
        }

        System.out.println("create avi success.");
    }

}

