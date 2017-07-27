package raytracing.photon;

import raytracing.Camera;
import raytracing.Ray;
import raytracing.Vec3d;
import raytracing.load.BasicLoader;
import raytracing.load.CameraLoader;
import raytracing.load.ConfLoader;
import raytracing.load.ModelLoader;
import raytracing.log.LogFactory;
import raytracing.model.Primitive;
import raytracing.trace.CameraTrace;
import utils.DirectoryChecker;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by THU73 on 17/7/25.
 */
public class PhotonTest {

    private static PhotonTracing photonTracing = new PhotonTracing();

    public static void main(String[] args) throws IOException {
    	LogFactory.setIgnore(true);
    	
        ModelLoader ml = new ModelLoader("tmp.mods", null, BasicLoader.ENV.NATIVE);
        ml.parse(photonTracing.getScene());
        ml.close();

        CameraLoader cl = new CameraLoader("tmp.camera", null, BasicLoader.ENV.NATIVE);
        Camera origCamera = new Camera(new Vec3d(), new Vec3d(), new Vec3d(), 60.0, 480, 640);
        ArrayList<CameraTrace> cats = new ArrayList<CameraTrace>();
        cl.parse(origCamera, cats);
        cl.close();

        HashMap<String, String> opts = new HashMap<String, String>();
        ConfLoader confLoader = new ConfLoader("tmp.conf", null, BasicLoader.ENV.NATIVE);
        confLoader.parse(opts);
        confLoader.close();

        photonTracing.setMaxRayDepth(opts.getOrDefault("MAX_RAY_DEPTH", "5"));

        photonTracing.emitPhoton(200000);

        photonTracing.printFile("tmp.photon");
    }
}
